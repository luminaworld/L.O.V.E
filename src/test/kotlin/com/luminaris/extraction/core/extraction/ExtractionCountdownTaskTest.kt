package com.luminaris.extraction.core.extraction

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.PlayerInventory
import org.bukkit.plugin.Plugin
import org.bukkit.util.BoundingBox
import java.io.File
import java.lang.reflect.Proxy
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExtractionCountdownTaskTest {

    private class MockPlayerState(
        var location: Location,
        var lastActionBar: Component? = null,
        var teleportedLocation: Location? = null
    )

    private fun createMockPlayer(state: MockPlayerState): Player {
        val uuid = UUID.randomUUID()

        val mockInventory = Proxy.newProxyInstance(
            PlayerInventory::class.java.classLoader,
            arrayOf(PlayerInventory::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "getContents" -> emptyArray<org.bukkit.inventory.ItemStack?>()
                "getSize" -> 36
                else -> null
            }
        } as PlayerInventory

        return Proxy.newProxyInstance(
            Player::class.java.classLoader,
            arrayOf(Player::class.java)
        ) { _, method, args ->
            when (method.name) {
                "getUniqueId" -> uuid
                "getName" -> "Operative"
                "isOnline" -> true
                "isValid" -> true
                "getLocation" -> state.location
                "getInventory" -> mockInventory
                "sendActionBar" -> {
                    state.lastActionBar = args[0] as? Component
                    null
                }
                "teleportAsync" -> {
                    val target = args[0] as Location
                    state.teleportedLocation = target
                    CompletableFuture.completedFuture(true)
                }
                else -> null
            }
        } as Player
    }

    private fun createMockPlugin(): Plugin {
        val tempFolder = File(System.getProperty("java.io.tmpdir"), "luminaris_task_test")
        tempFolder.mkdirs()
        return Proxy.newProxyInstance(
            Plugin::class.java.classLoader,
            arrayOf(Plugin::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "getDataFolder" -> tempFolder
                "getName" -> "LuminaExtractionCore"
                "getLogger" -> java.util.logging.Logger.getLogger("CountdownTaskTest")
                else -> null
            }
        } as Plugin
    }

    @Test
    fun testCountdownProgressionAndActionbar() {
        val plugin = createMockPlugin()
        val manager = ExtractionManager(plugin)
        val mm = MiniMessage.miniMessage()

        val zone = ExtractionZone(
            id = "ext_roof",
            displayName = Component.text("Helipad Evac"),
            boundingBox = BoundingBox(0.0, 60.0, 0.0, 10.0, 70.0, 10.0),
            type = ExtractionType.ALWAYS_OPEN,
            countdownSeconds = 3
        )
        manager.registerZone(zone)

        val playerState = MockPlayerState(location = Location(null, 5.0, 64.0, 5.0))
        val player = createMockPlayer(playerState)

        val task = ExtractionCountdownTask(
            plugin = plugin,
            player = player,
            manager = manager,
            matchElapsedProvider = { 100L },
            teamIdProvider = { "squad_alpha" },
            zonesProvider = { listOf(zone) }
        )

        // Tick 1: Player enters zone -> countdown starts at 1, remaining = 3 - 1 = 2s
        task.tick()
        assertEquals(1, task.countdownTimer)
        assertEquals(zone, task.activeZone)
        assertEquals(ExtractionState.EXTRACTING, manager.getPlayerState(player))
        val bar1 = mm.serialize(playerState.lastActionBar!!)
        assertTrue(bar1.contains("Extracting in") && bar1.contains("2") && bar1.contains("Stay in the zone!"))

        // Tick 2: Player stays in zone -> timer increments to 2, remaining = 3 - 2 = 1s
        task.tick()
        assertEquals(2, task.countdownTimer)
        val bar2 = mm.serialize(playerState.lastActionBar!!)
        assertTrue(bar2.contains("Extracting in") && bar2.contains("1") && bar2.contains("Stay in the zone!"))

        // Set safe lobby
        val lobbyLoc = Location(null, 1000.0, 64.0, 1000.0)
        manager.lobbyLocation = lobbyLoc

        // Tick 3: Timer increments to 3, remaining = 0s -> Triggers extraction!
        task.tick()
        assertEquals(0, task.countdownTimer)
        assertNull(task.activeZone)
        assertEquals(ExtractionState.SURVIVED, manager.getPlayerState(player))
        assertEquals(lobbyLoc, playerState.teleportedLocation)
    }

    @Test
    fun testMovingOutsideBoundingBoxResetsCountdown() {
        val plugin = createMockPlugin()
        val manager = ExtractionManager(plugin)
        val mm = MiniMessage.miniMessage()

        val zone = ExtractionZone(
            id = "ext_zone",
            displayName = Component.text("Evac Alpha"),
            boundingBox = BoundingBox(0.0, 60.0, 0.0, 10.0, 70.0, 10.0),
            type = ExtractionType.ALWAYS_OPEN,
            countdownSeconds = 5
        )
        manager.registerZone(zone)

        val playerState = MockPlayerState(location = Location(null, 5.0, 64.0, 5.0))
        val player = createMockPlayer(playerState)

        val task = ExtractionCountdownTask(
            plugin = plugin,
            player = player,
            manager = manager,
            matchElapsedProvider = { 100L },
            teamIdProvider = { "squad_alpha" },
            zonesProvider = { listOf(zone) }
        )

        // Tick 1: Inside zone -> active
        task.tick()
        assertEquals(1, task.countdownTimer)
        assertNotNull(task.activeZone)

        // Player moves outside the bounding box
        playerState.location = Location(null, 25.0, 64.0, 25.0)

        // Tick 2: Outside zone -> resets countdown and alerts player
        task.tick()
        assertEquals(0, task.countdownTimer)
        assertNull(task.activeZone)
        assertEquals(ExtractionState.IN_MATCH, manager.getPlayerState(player))

        val alertText = mm.serialize(playerState.lastActionBar!!)
        assertTrue(alertText.contains("EXTRACTION CANCELLED") && alertText.contains("outside"), "Actual alert: $alertText")
    }

    @Test
    fun testDamageInterruptsCountdown() {
        val plugin = createMockPlugin()
        val manager = ExtractionManager(plugin)
        val mm = MiniMessage.miniMessage()

        val zone = ExtractionZone(
            id = "ext_zone",
            displayName = Component.text("Evac Alpha"),
            boundingBox = BoundingBox(0.0, 60.0, 0.0, 10.0, 70.0, 10.0),
            type = ExtractionType.ALWAYS_OPEN,
            countdownSeconds = 10
        )
        manager.registerZone(zone)

        val playerState = MockPlayerState(location = Location(null, 5.0, 64.0, 5.0))
        val player = createMockPlayer(playerState)

        val task = ExtractionCountdownTask(
            plugin = plugin,
            player = player,
            manager = manager,
            matchElapsedProvider = { 100L },
            teamIdProvider = { "squad_alpha" },
            zonesProvider = { listOf(zone) }
        )

        // Tick 1 & 2: Building countdown
        task.tick()
        task.tick()
        assertEquals(2, task.countdownTimer)

        // Player takes damage!
        task.onDamage()

        // Verifies reset and interruption alert
        assertEquals(0, task.countdownTimer)
        assertNull(task.activeZone)
        assertEquals(ExtractionState.IN_MATCH, manager.getPlayerState(player))

        val alertText = mm.serialize(playerState.lastActionBar!!)
        assertTrue(alertText.contains("EXTRACTION INTERRUPTED") && alertText.contains("damage"), "Actual alert: $alertText")
    }
}
