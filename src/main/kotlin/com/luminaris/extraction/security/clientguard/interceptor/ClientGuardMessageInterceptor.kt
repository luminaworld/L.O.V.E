package com.luminaris.extraction.security.clientguard.interceptor

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.security.clientguard.ClientGuardManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import java.nio.charset.StandardCharsets

/**
 * Deep Packet / Channel Interceptor registering with Bukkit's Messenger
 * to capture incoming client payloads, brand announcements, and mod handshakes.
 */
class ClientGuardMessageInterceptor(
    private val plugin: LuminarisCorePlugin,
    private val manager: ClientGuardManager
) : PluginMessageListener {

    companion object {
        val MONITORED_CHANNELS = listOf(
            "minecraft:brand",
            "MC|Brand",
            "lunarclient:pm",
            "lunar:apollo",
            "lunarclient:apollo",
            "fabric:registry/sync",
            "fabric-networking-api-v1:c2s",
            "fabric-screen-handler-api-v1:c2s",
            "fml:handshake",
            "baritone:channel",
            "baritone:command",
            "meteor:chat",
            "wurst:check"
        )
    }

    /**
     * Registers all monitored incoming plugin channels with Bukkit Messenger.
     */
    fun register() {
        val messenger = Bukkit.getMessenger()
        for (channel in MONITORED_CHANNELS) {
            runCatching {
                messenger.registerIncomingPluginChannel(plugin, channel, this)
            }
        }
    }

    /**
     * Unregisters all monitored channels upon shutdown.
     */
    fun unregister() {
        val messenger = Bukkit.getMessenger()
        for (channel in MONITORED_CHANNELS) {
            runCatching {
                messenger.unregisterIncomingPluginChannel(plugin, channel, this)
            }
        }
    }

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        if (!manager.configAdapter.isGuardEnabled) return
        if (manager.shouldBypass(player)) return

        val profile = manager.getOrCreateProfile(player)
        if (profile.isPunished) return

        manager.debug("Plugin message received from ${player.name} on channel '$channel' (${message.size} bytes)")

        // 1. Process Client Brand Announcement
        if (channel.equals("minecraft:brand", ignoreCase = true) || channel.equals("MC|Brand", ignoreCase = true)) {
            val brand = parseBrandString(message)
            if (brand.isNotEmpty()) {
                profile.brand = brand
                manager.inspectPlayer(player, "MESSENGER_BRAND", specificBrand = brand)
            }
            return
        }

        // 2. Track Received Channel Payload
        profile.registeredChannels.add(channel)

        // 3. Inspect for immediate violation
        manager.inspectPlayer(player, "INCOMING_PAYLOAD", specificChannel = channel)
    }

    private fun parseBrandString(bytes: ByteArray): String {
        return runCatching {
            var str = String(bytes, StandardCharsets.UTF_8).trim()
            // Drop any leading VarInt length byte or non-printable ASCII
            str = str.dropWhile { it.code in 0..31 }
            str.filter { it in ' '..'~' || it.isLetterOrDigit() }.trim()
        }.getOrDefault("")
    }
}

