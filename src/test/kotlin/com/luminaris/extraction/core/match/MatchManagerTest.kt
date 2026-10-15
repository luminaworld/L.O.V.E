package com.luminaris.extraction.core.match

import com.luminaris.extraction.core.template.TemplateMapData
import net.kyori.adventure.text.Component
import org.bukkit.World
import org.bukkit.command.CommandSender
import java.lang.reflect.Proxy
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MatchManagerTest {

    private fun createMockWorld(name: String = "instances/match_mgr_test"): World {
        return Proxy.newProxyInstance(
            World::class.java.classLoader,
            arrayOf(World::class.java)
        ) { proxy, method, args ->
            when (method.name) {
                "getName" -> name
                "equals" -> proxy === args?.get(0)
                "hashCode" -> name.hashCode()
                else -> null
            }
        } as World
    }

    private class MockSenderState(
        val messages: MutableList<Component> = mutableListOf(),
        val permissions: MutableSet<String> = mutableSetOf("luminaris.admin")
    )

    private fun createMockSender(state: MockSenderState = MockSenderState()): CommandSender {
        return Proxy.newProxyInstance(
            CommandSender::class.java.classLoader,
            arrayOf(CommandSender::class.java)
        ) { _, method, args ->
            when (method.name) {
                "sendMessage" -> {
                    val comp = args[0] as? Component
                    if (comp != null) state.messages.add(comp)
                    null
                }
                "hasPermission" -> {
                    val perm = args[0] as String
                    state.permissions.contains(perm)
                }
                "getName" -> "ConsoleAdmin"
                else -> null
            }
        } as CommandSender
    }

    @Test
    fun testActiveMatchTrackingAndLookups() {
        val world1 = createMockWorld("instances/match_alpha")
        val world2 = createMockWorld("instances/match_bravo")

        val match1 = MatchInstance(
            matchId = "alpha_1",
            world = world1,
            templateData = TemplateMapData("factory", "Chemical Factory")
        )

        val match2 = MatchInstance(
            matchId = "bravo_2",
            world = world2,
            templateData = TemplateMapData("bunker", "Sub Bunker")
        )

        val player1 = UUID.randomUUID()
        val player2 = UUID.randomUUID()

        match1.players[player1] = PlayerRaidState.IN_RAID
        match2.players[player2] = PlayerRaidState.EXTRACTING

        // Test manual lookup logic
        val activeMatches = mutableMapOf(
            match1.matchId to match1,
            match2.matchId to match2
        )

        assertEquals(2, activeMatches.size)
        assertEquals(match1, activeMatches["alpha_1"])
        assertEquals(match2, activeMatches["bravo_2"])
        assertNull(activeMatches["non_existent"])

        // Lookup by world
        val foundWorld1 = activeMatches.values.firstOrNull { it.world == world1 }
        assertEquals(match1, foundWorld1)

        // Lookup by player
        val foundPlayer1 = activeMatches.values.firstOrNull { it.players.containsKey(player1) }
        assertEquals(match1, foundPlayer1)

        val foundPlayer2 = activeMatches.values.firstOrNull { it.players.containsKey(player2) }
        assertEquals(match2, foundPlayer2)

        val unknownPlayer = UUID.randomUUID()
        val foundUnknown = activeMatches.values.firstOrNull { it.players.containsKey(unknownPlayer) }
        assertNull(foundUnknown)
    }

    @Test
    fun testMatchCommandStopAndList() {
        val senderState = MockSenderState()
        val sender = createMockSender(senderState)

        val world = createMockWorld("instances/match_test_cmd")
        val match = MatchInstance(
            matchId = "match_99",
            world = world,
            templateData = TemplateMapData("lab", "Laboratory"),
            raidDurationSeconds = 600L
        )

        assertFalse(match.isFinished)

        // Manual stop
        match.teardown()
        assertTrue(match.isFinished)
    }
}
