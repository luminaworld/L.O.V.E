package com.luminaris.extraction.security.clientguard.probe

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.security.clientguard.ClientGuardManager
import com.luminaris.extraction.security.clientguard.model.DetectionRule
import com.luminaris.extraction.security.clientguard.model.InspectionResult
import com.luminaris.extraction.security.clientguard.model.ViolationAction
import com.luminaris.extraction.util.FoliaScheduler
import io.papermc.paper.event.packet.UncheckedSignChangeEvent
import io.papermc.paper.math.BlockPosition
import io.papermc.paper.math.Position
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.block.TileState
import org.bukkit.block.data.BlockData
import org.bukkit.block.sign.Side
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryType
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Advanced Sign Translation & Keybind Probe Engine reverse-engineered from LPSV / MC-265322.
 *
 * Employs raw Packet-level [UncheckedSignChangeEvent] to intercept virtual sign edits,
 * utilizes TileState block updates to guarantee client-side translation resolution,
 * and incorporates %s wrapper bypasses to evade Meteor Client's anti-translation countermeasures.
 */
class SignTranslationProbe(
    private val plugin: LuminarisCorePlugin,
    private val manager: ClientGuardManager,
    private val scheduler: FoliaScheduler
) : Listener {

    private val plainSerializer = PlainTextComponentSerializer.plainText()

    data class ProbeCheck(
        val id: String,
        val name: String,
        val translationKey: String,
        val bypassProtection: Boolean = false,
        val actions: List<ViolationAction>? = null
    )

    data class ProbeSession(
        val loc: Location,
        val realBlockData: BlockData,
        val activeChecks: List<ProbeCheck>,
        val remainingChecks: List<ProbeCheck>,
        val done: AtomicBoolean = AtomicBoolean(false)
    ) {
        fun matchesPosition(pos: BlockPosition): Boolean {
            return pos.blockX() == loc.blockX && pos.blockY() == loc.blockY && pos.blockZ() == loc.blockZ
        }
    }

    companion object {
        /**
         * Default high-priority probes targeting Meteor Client, Wurst, Freecam, and Baritone.
         */
        fun getDefaultChecks(): List<ProbeCheck> {
            return listOf(
                ProbeCheck(
                    id = "meteor",
                    name = "Meteor Client",
                    translationKey = "key.category.meteor-client.meteor-client",
                    bypassProtection = true
                ),
                ProbeCheck(
                    id = "wurst",
                    name = "Wurst Client",
                    translationKey = "key.wurst.zoom",
                    bypassProtection = false
                ),
                ProbeCheck(
                    id = "freecam",
                    name = "Freecam",
                    translationKey = "key.freecam.toggle",
                    bypassProtection = false
                ),
                ProbeCheck(
                    id = "baritone",
                    name = "Baritone",
                    translationKey = "baritone.prefix",
                    bypassProtection = false
                )
            )
        }
    }

    private val activeProbes = ConcurrentHashMap<UUID, ProbeSession>()

    /**
     * Schedules a probe sequence for the player after the configured initial delay.
     */
    fun schedule(player: Player, force: Boolean = false) {
        if (!manager.configAdapter.isGuardEnabled || !manager.configAdapter.isSignProbeEnabled) return
        if (!force && manager.shouldBypass(player)) {
            manager.debug("Sign probe bypassed for ${player.name} (force=$force, isOp=${player.isOp})")
            return
        }

        // Skip Bedrock Edition players connecting through Geyser (UUID MSB == 0)
        if (!force && player.uniqueId.mostSignificantBits == 0L) {
            manager.debug("Skipping sign probe for Bedrock player ${player.name}")
            return
        }

        val profile = manager.getOrCreateProfile(player)
        if (profile.isPunished) return

        val delayTicks = manager.configAdapter.signProbeDelayTicks
        manager.debug("Scheduling sign probe for ${player.name} in $delayTicks ticks (force=$force)")
        scheduler.runAtEntityDelayed(player, delayTicks) {
            tryStart(player, getDefaultChecks(), 0)
        }
    }

    /**
     * Attempts to initiate a probe, waiting if the player is currently in another inventory/dialog.
     */
    private fun tryStart(player: Player, checks: List<ProbeCheck>, tries: Int) {
        if (!player.isOnline || activeProbes.containsKey(player.uniqueId)) return

        val profile = manager.getOrCreateProfile(player)
        if (profile.isPunished) return

        if (!isPlayerReady(player)) {
            if (tries < 60) {
                manager.debug("Player ${player.name} not ready for probe (try $tries/60, inv: ${player.openInventory.type}), retrying in 100 ticks...")
                scheduler.runAtEntityDelayed(player, 100L) {
                    tryStart(player, checks, tries + 1)
                }
            } else {
                manager.debug("Canceled sign probe for ${player.name} (player busy too long)")
            }
            return
        }

        executeProbeBatch(player, checks)
    }

    private fun isPlayerReady(player: Player): Boolean {
        // Player is ready when not actively browsing an external container/menu (chest, furnace, anvil, etc.)
        val type = player.openInventory.type
        return type == InventoryType.CRAFTING || type == InventoryType.CREATIVE || type == InventoryType.PLAYER
    }

    /**
     * Deploys a batch of up to 4 translation checks on a virtual sign.
     */
    private fun executeProbeBatch(player: Player, checks: List<ProbeCheck>) {
        val currentBatch = checks.take(4)
        val remaining = checks.drop(4)

        val baseLoc = player.location
        val targetY = (baseLoc.blockY - 5).coerceAtLeast(baseLoc.world.minHeight)
        val probeLoc = Location(baseLoc.world, baseLoc.blockX.toDouble(), targetY.toDouble(), baseLoc.blockZ.toDouble())

        // Ensure Folia regional ownership before touching blocks
        if (!Bukkit.isOwnedByCurrentRegion(probeLoc)) {
            manager.debug("Probe location ($targetY) not owned by current region for ${player.name}, skipping.")
            return
        }

        val realBlockData = probeLoc.block.blockData
        val signBlockData = Material.OAK_SIGN.createBlockData()
        val blockState = signBlockData.createBlockState()
        if (blockState !is Sign) {
            plugin.logger.warning("[ClientGuard] Failed to create virtual sign BlockState for ${player.name}")
            return
        }

        val signSide = blockState.getSide(Side.FRONT)
        for (i in 0 until 4) {
            if (i < currentBatch.size) {
                val check = currentBatch[i]
                val component = if (check.bypassProtection) {
                    Component.translatable("%s", Component.translatable(check.translationKey))
                } else {
                    Component.translatable(check.translationKey)
                }
                signSide.line(i, component)
            } else {
                signSide.line(i, Component.empty())
            }
        }
        blockState.isWaxed = false

        val probeSession = ProbeSession(
            loc = probeLoc,
            realBlockData = realBlockData,
            activeChecks = currentBatch,
            remainingChecks = remaining
        )
        activeProbes[player.uniqueId] = probeSession

        try {
            // 1. Send fake block and full TileState update to client
            player.sendBlockChange(probeLoc, signBlockData)
            player.sendBlockUpdate(probeLoc, blockState as TileState)
        } catch (ex: Throwable) {
            activeProbes.remove(player.uniqueId, probeSession)
            plugin.logger.warning("[ClientGuard] Could not send virtual sign block to ${player.name}: ${ex.message}")
            return
        }

        manager.debug("Started virtual sign probe for ${player.name} at (${probeLoc.blockX}, ${probeLoc.blockY}, ${probeLoc.blockZ}) with ${currentBatch.size} checks")

        // 2. Open virtual sign editor after 2 ticks
        scheduler.runAtEntityDelayed(player, 2L) {
            if (activeProbes[player.uniqueId] != probeSession) return@runAtEntityDelayed

            val pos = Position.block(probeLoc)
            player.openVirtualSign(pos, Side.FRONT)
            manager.debug("Sent openVirtualSign for ${player.name}")

            // 3. Force-close sign interface 2 ticks later (seamless 0.1s transaction)
            scheduler.runAtEntityDelayed(player, 2L) {
                if (activeProbes[player.uniqueId] == probeSession && !probeSession.done.get()) {
                    player.closeInventory()
                    manager.debug("Sent closeInventory for ${player.name}")
                }
            }
        }

        // 4. Fallback cleanup timeout after 300 ticks (15 seconds) if client fails to respond
        scheduler.runAtEntityDelayed(player, 300L) {
            if (activeProbes.remove(player.uniqueId, probeSession) && !probeSession.done.get()) {
                manager.debug("Probe timeout for ${player.name} (client did not respond with UncheckedSignChangeEvent)")
                restoreBlock(player, probeSession)
            }
        }
    }

    /**
     * Intercepts the raw packet-level [UncheckedSignChangeEvent] fired when the client submits the sign.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    fun onUncheckedSignChange(event: UncheckedSignChangeEvent) {
        val player = event.player
        val probe = activeProbes[player.uniqueId] ?: return

        if (!probe.matchesPosition(event.editedBlockPosition)) {
            return
        }

        // Cancel event to prevent placing text in world
        event.isCancelled = true

        if (!probe.done.compareAndSet(false, true) || !activeProbes.remove(player.uniqueId, probe)) {
            return
        }

        val returnedLines = Array(4) { i ->
            if (i < event.lines().size) {
                plainSerializer.serialize(event.lines()[i]).trim()
            } else ""
        }

        manager.debug("Probe response from ${player.name}: [${returnedLines.joinToString(", ")}]")

        scheduler.runAtEntity(player) {
            restoreBlock(player, probe)

            val profile = manager.getOrCreateProfile(player)
            if (profile.isPunished) return@runAtEntity

            for (i in probe.activeChecks.indices) {
                val check = probe.activeChecks[i]
                val returnedText = returnedLines[i]

                // If text is not empty and NOT equal to the raw translation key, the client resolved it!
                if (returnedText.isNotEmpty() && returnedText != check.translationKey) {
                    manager.debug("MATCH CONFIRMED! ${player.name} resolved key '${check.translationKey}' to '$returnedText'")

                    val rule = manager.configAdapter.getRule(check.id)
                        ?: (if (check.id == "meteor") manager.configAdapter.getRule("meteor_client") else null)
                        ?: DetectionRule(
                            id = check.id,
                            name = check.name,
                            enabled = true,
                            matchBrands = emptyList(),
                            matchChannels = emptyList(),
                            actions = check.actions ?: listOf(
                                ViolationAction.Kick("<red><bold>[Luminaris Security]</bold></red><newline><gray>ตรวจพบการใช้งาน <yellow>${check.name}</yellow> (MC-265322 Signature Verified)</gray>"),
                                ViolationAction.Alert(),
                                ViolationAction.Log
                            )
                        )

                    val result = InspectionResult(
                        isViolated = true,
                        matchedRule = rule,
                        detectionSource = "SIGN_PROBE_${check.id.uppercase()}",
                        matchedBrand = player.clientBrandName ?: profile.brand,
                        matchedChannel = "translated:${check.translationKey} -> $returnedText"
                    )
                    manager.handleViolationDirect(player, profile, result)
                    return@runAtEntity
                }
            }

            // If there are more checks remaining in queue, schedule next batch after 10 ticks
            if (probe.remainingChecks.isNotEmpty()) {
                scheduler.runAtEntityDelayed(player, 10L) {
                    tryStart(player, probe.remainingChecks, 0)
                }
            }
        }
    }

    private fun restoreBlock(player: Player, probe: ProbeSession) {
        if (!player.isOnline) return
        runCatching {
            player.sendBlockChange(probe.loc, probe.realBlockData)
        }
    }

    fun cleanup(playerUuid: UUID) {
        activeProbes.remove(playerUuid)
    }

    fun clearAll() {
        for ((uuid, probe) in activeProbes) {
            val p = Bukkit.getPlayer(uuid) ?: continue
            restoreBlock(p, probe)
        }
        activeProbes.clear()
    }
}
