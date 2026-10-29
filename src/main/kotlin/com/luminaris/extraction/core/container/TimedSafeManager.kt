package com.luminaris.extraction.core.container

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.editor.tool.SafeSetupTool
import com.luminaris.extraction.localization.TranslationManager
import com.luminaris.extraction.util.FoliaScheduler
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Service managing extraction timed safe containers, side-mounted TextDisplay countdown screens,
 * ticking sound loops, and persistent unlocked match state.
 */
class TimedSafeManager(
    private val plugin: LuminarisCorePlugin,
    private val scheduler: FoliaScheduler = plugin.scheduler
) {

    private val miniMessage = MiniMessage.miniMessage()

    // Key format: "${worldName}:${x}_${y}_${z}"
    private val activeUnlocks = ConcurrentHashMap<String, SafeUnlockSession>()
    private val unlockedBoxes = ConcurrentHashMap.newKeySet<String>()
    private val displayEntityIds = ConcurrentHashMap<String, UUID>()

    /**
     * Internal state tracking an active safe countdown session.
     */
    data class SafeUnlockSession(
        val key: String,
        val worldName: String,
        val x: Int,
        val y: Int,
        val z: Int,
        val totalSeconds: Int,
        var remainingSeconds: Int,
        val displayFace: BlockFace,
        var displayEntityId: UUID? = null,
        var task: ScheduledTask? = null
    )

    /**
     * Checks if the given container and category require a timed countdown before opening.
     * Evaluates explicit displayFace marker, category configuration, or safe naming convention.
     */
    fun isSafeContainer(container: PlacedContainer?, category: ContainerCategory): Boolean {
        if (!container?.displayFace.isNullOrBlank()) return true
        if (category.timedUnlockEnabled) return true
        if (category.id.contains("safe", ignoreCase = true)) return true
        return false
    }

    /**
     * Checks if the given category requires a timed countdown before opening.
     */
    fun isSafeCategory(category: ContainerCategory): Boolean {
        return isSafeContainer(null, category)
    }

    /**
     * Resolves the configured countdown seconds, falling back to 30s for safe categories if omitted.
     */
    fun getCountdownSeconds(category: ContainerCategory): Int {
        if (category.unlockCountdownSeconds > 0) return category.unlockCountdownSeconds
        return 30
    }

    /**
     * Checks if the container at the given coordinates has finished its countdown and is unlocked.
     */
    fun isUnlocked(world: World, x: Int, y: Int, z: Int): Boolean {
        val key = "${world.name}:${x}_${y}_${z}"
        return unlockedBoxes.contains(key)
    }

    /**
     * Checks if the container at the given coordinates is currently counting down.
     */
    fun isCountingDown(world: World, x: Int, y: Int, z: Int): Boolean {
        val key = "${world.name}:${x}_${y}_${z}"
        return activeUnlocks.containsKey(key)
    }

    /**
     * Retrieves the remaining seconds of an active countdown, or 0 if not active.
     */
    fun getRemainingSeconds(world: World, x: Int, y: Int, z: Int): Int {
        val key = "${world.name}:${x}_${y}_${z}"
        return activeUnlocks[key]?.remainingSeconds ?: 0
    }

    /**
     * Calculates the exact side-mounted Location and Yaw angle for the TextDisplay
     * based on the target block face.
     *
     * Offset is 0.53 blocks from center (just 0.03 blocks off the outer surface).
     */
    fun calculateDisplayLocation(world: World, x: Int, y: Int, z: Int, face: BlockFace): Pair<Location, Float> {
        val centerX = x + 0.5
        val centerY = y + 0.5
        val centerZ = z + 0.5
        val offset = 0.53

        return when (face) {
            BlockFace.NORTH -> Pair(Location(world, centerX, centerY, centerZ - offset), 180.0f)
            BlockFace.SOUTH -> Pair(Location(world, centerX, centerY, centerZ + offset), 0.0f)
            BlockFace.WEST -> Pair(Location(world, centerX - offset, centerY, centerZ), 90.0f)
            BlockFace.EAST -> Pair(Location(world, centerX + offset, centerY, centerZ), 270.0f)
            else -> Pair(Location(world, centerX, centerY, centerZ - offset), 180.0f)
        }
    }

    /**
     * Initiates the unlocking process for a timed safe container in a match.
     */
    fun startUnlock(
        player: Player,
        world: World,
        containerBlock: Block,
        placedContainer: PlacedContainer,
        category: ContainerCategory
    ) {
        val x = containerBlock.x
        val y = containerBlock.y
        val z = containerBlock.z
        val key = "${world.name}:${x}_${y}_${z}"

        if (unlockedBoxes.contains(key)) return
        if (activeUnlocks.containsKey(key)) return

        // 1. Resolve target face for the screen
        val targetFace = placedContainer.displayFace?.let {
            runCatching { BlockFace.valueOf(it.uppercase()) }.getOrNull()
        } ?: run {
            val blockData = containerBlock.blockData
            if (blockData is org.bukkit.block.data.Directional && SafeSetupTool.isSupportedFace(blockData.facing)) {
                blockData.facing
            } else {
                BlockFace.NORTH
            }
        }

        val countdownSec = getCountdownSeconds(category)
        val (displayLoc, fixedYaw) = calculateDisplayLocation(world, x, y, z, targetFace)
        displayLoc.yaw = fixedYaw
        displayLoc.pitch = 0f

        val session = SafeUnlockSession(
            key = key,
            worldName = world.name,
            x = x,
            y = y,
            z = z,
            totalSeconds = countdownSec,
            remainingSeconds = countdownSec,
            displayFace = targetFace
        )
        activeUnlocks[key] = session

        // 2. Play initial tick sound right away at tick 0
        playTickSound(world, displayLoc, category.unlockTickSound)

        // 3. Send action bar confirmation to the initiating operative
        TranslationManager.sendActionBar(
            player,
            "safe.unlock_started",
            "seconds" to countdownSec
        )

        // 4. Folia regional execution to spawn TextDisplay and schedule tick loop
        scheduler.runAtLocation(displayLoc) {
            val display = world.spawn(displayLoc, TextDisplay::class.java) { entity ->
                entity.billboard = Display.Billboard.FIXED
                entity.isSeeThrough = false
                entity.isShadowed = true
                entity.backgroundColor = Color.fromARGB(170, 15, 15, 20)
                entity.textOpacity = 255.toByte()
                entity.alignment = TextDisplay.TextAlignment.CENTER
                entity.transformation = Transformation(
                    Vector3f(0f, 0f, 0f),
                    AxisAngle4f(),
                    Vector3f(0.55f, 0.55f, 0.55f),
                    AxisAngle4f()
                )
                entity.text(renderCountingComponent(session.remainingSeconds, session.totalSeconds))
            }

            session.displayEntityId = display.uniqueId
            displayEntityIds[key] = display.uniqueId

            // 5. Schedule repeating countdown every 20 ticks (1 second)
            var timerTask: ScheduledTask? = null
            timerTask = scheduler.runAtLocationRepeating(displayLoc, 20L, 20L) {
                session.remainingSeconds--

                if (session.remainingSeconds > 0) {
                    // Update display text
                    if (display.isValid) {
                        display.text(renderCountingComponent(session.remainingSeconds, session.totalSeconds))
                    }
                    // Play ticking sound
                    playTickSound(world, displayLoc, category.unlockTickSound)
                } else {
                    // Countdown complete!
                    timerTask?.cancel()
                    session.task?.cancel()
                    session.task = null
                    activeUnlocks.remove(key)
                    unlockedBoxes.add(key)

                    // Update display to permanent UNLOCKED text (stays until end of match)
                    if (display.isValid) {
                        display.text(renderUnlockedComponent())
                    }

                    // Play unlock finish sound
                    playFinishSound(world, displayLoc, category.unlockFinishSound)
                }
            }
            session.task = timerTask
        }
    }

    /**
     * Renders the Adventure Component for the active countdown screen with Progress Bar.
     */
    private fun renderCountingComponent(remaining: Int, total: Int): Component {
        val progressBar = formatProgressBar(remaining, total, 10)
        val raw = "<bold><gradient:#FFAA00:#FF5500>🔒 กำลังปลดล็อค...</gradient></bold>\n" +
                "<white>เวลา: </white><yellow><bold>${remaining}s</bold></yellow>\n" +
                "$progressBar"
        return miniMessage.deserialize(raw)
    }

    /**
     * Renders the Adventure Component for the persistent unlocked screen.
     */
    private fun renderUnlockedComponent(): Component {
        val raw = "<bold><gradient:#55FF55:#00AA00>🔓 ตู้เซฟปลดล็อคแล้ว</gradient></bold>\n" +
                "<white><bold>คลิกเพื่อเปิดหาไอเทม</bold></white>"
        return miniMessage.deserialize(raw)
    }

    /**
     * Formats an ASCII/Unicode progress bar showing safe cracking progression.
     */
    private fun formatProgressBar(remaining: Int, total: Int, length: Int = 10): String {
        if (total <= 0) return "<color:#55FF55>${"■".repeat(length)}</color>"
        val elapsed = (total - remaining).coerceAtLeast(0)
        val progressPct = (elapsed.toDouble() / total.toDouble()).coerceIn(0.0, 1.0)
        val filled = (progressPct * length).toInt().coerceIn(0, length)
        val empty = length - filled
        return "<color:#55FF55>${"■".repeat(filled)}</color><color:#555555>${"□".repeat(empty)}</color>"
    }

    private fun playTickSound(world: World, loc: Location, soundName: String?) {
        val resolved = soundName?.let { runCatching { Sound.valueOf(it.uppercase()) }.getOrNull() }
            ?: Sound.BLOCK_NOTE_BLOCK_HAT
        world.playSound(loc, resolved, 0.9f, 1.0f)
    }

    private fun playFinishSound(world: World, loc: Location, soundName: String?) {
        val resolved = soundName?.let { runCatching { Sound.valueOf(it.uppercase()) }.getOrNull() }
            ?: Sound.BLOCK_IRON_DOOR_OPEN
        world.playSound(loc, resolved, 1.0f, 1.0f)
        world.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f)
    }

    /**
     * Resets a specific container's safe state, cancelling its timer and despawning its TextDisplay.
     * Useful for simulation re-tests in template worlds or manual resets.
     */
    fun resetContainer(world: World, x: Int, y: Int, z: Int): Boolean {
        val key = "${world.name}:${x}_${y}_${z}"
        var changed = false

        val session = activeUnlocks.remove(key)
        if (session != null) {
            session.task?.cancel()
            changed = true
        }

        if (unlockedBoxes.remove(key)) {
            changed = true
        }

        val uuid = displayEntityIds.remove(key)
        if (uuid != null) {
            changed = true
            val entity = Bukkit.getEntity(uuid)
            if (entity != null && entity.isValid) {
                scheduler.runAtEntity(entity) {
                    entity.remove()
                }
            }
        }

        return changed
    }

    /**
     * Cleans up all active countdown tasks and despawns TextDisplay screens
     * for a decommissioned match world.
     */
    fun cleanupMatch(world: World) {
        val prefix = "${world.name}:"

        // Cancel all tasks
        for ((k, session) in activeUnlocks) {
            if (k.startsWith(prefix)) {
                session.task?.cancel()
                activeUnlocks.remove(k)
            }
        }

        // Remove all unlocked cache for this world
        unlockedBoxes.removeIf { it.startsWith(prefix) }

        // Despawn TextDisplay entities safely
        for ((k, uuid) in displayEntityIds) {
            if (k.startsWith(prefix)) {
                displayEntityIds.remove(k)
                val entity = Bukkit.getEntity(uuid)
                if (entity != null && entity.isValid) {
                    scheduler.runAtEntity(entity) {
                        entity.remove()
                    }
                }
            }
        }
    }

    /**
     * Cleans up all active countdown tasks and displays upon plugin shutdown.
     */
    fun shutdown() {
        for (session in activeUnlocks.values) {
            session.task?.cancel()
        }
        activeUnlocks.clear()
        unlockedBoxes.clear()

        for (uuid in displayEntityIds.values) {
            val entity = Bukkit.getEntity(uuid)
            if (entity != null && entity.isValid) {
                entity.remove()
            }
        }
        displayEntityIds.clear()
    }
}
