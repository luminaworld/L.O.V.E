package com.luminaris.extraction.core.match

import com.luminaris.extraction.core.template.TemplateMapData
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.World
import java.lang.reflect.Proxy
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MatchInstanceTest {

    private val miniMessage = MiniMessage.miniMessage()

    private fun createMockWorld(name: String = "instances/match_test_1"): World {
        return Proxy.newProxyInstance(
            World::class.java.classLoader,
            arrayOf(World::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "getName" -> name
                else -> null
            }
        } as World
    }

    @Test
    fun testBossBarFormatting() {
        val world = createMockWorld()
        val template = TemplateMapData(templateId = "bunker", displayName = "Subterranean Bunker")
        val match = MatchInstance(
            matchId = "test101",
            world = world,
            templateData = template,
            raidDurationSeconds = 1200L
        )

        // 1200 seconds = 20:00
        val title1200 = match.formatBossBarTitle(1200L)
        val serialized1200 = miniMessage.serialize(title1200)
        assertEquals("<red>RAID TIME: <yellow>20:00", serialized1200)

        // 65 seconds = 01:05
        val title65 = match.formatBossBarTitle(65L)
        val serialized65 = miniMessage.serialize(title65)
        assertEquals("<red>RAID TIME: <yellow>01:05", serialized65)

        // 9 seconds = 00:09
        val title9 = match.formatBossBarTitle(9L)
        val serialized9 = miniMessage.serialize(title9)
        assertEquals("<red>RAID TIME: <yellow>00:09", serialized9)

        // 0 seconds = 00:00
        val title0 = match.formatBossBarTitle(0L)
        val serialized0 = miniMessage.serialize(title0)
        assertEquals("<red>RAID TIME: <yellow>00:00", serialized0)
    }

    @Test
    fun testBossBarProgressUpdate() {
        val world = createMockWorld()
        val template = TemplateMapData(templateId = "factory", displayName = "Chemical Plant")
        val match = MatchInstance(
            matchId = "prog_test",
            world = world,
            templateData = template,
            raidDurationSeconds = 100L
        )

        assertEquals(1.0f, match.bossBar.progress())

        match.elapsedSeconds = 25L
        match.updateBossBar()
        assertEquals(0.75f, match.bossBar.progress(), 0.001f)

        match.elapsedSeconds = 50L
        match.updateBossBar()
        assertEquals(0.50f, match.bossBar.progress(), 0.001f)

        match.elapsedSeconds = 100L
        match.updateBossBar()
        assertEquals(0.0f, match.bossBar.progress(), 0.001f)
    }

    @Test
    fun testPlayerRaidStateTerminalTransitions() {
        assertFalse(PlayerRaidState.IN_RAID.isTerminal)
        assertFalse(PlayerRaidState.EXTRACTING.isTerminal)
        assertTrue(PlayerRaidState.EXTRACTED.isTerminal)
        assertTrue(PlayerRaidState.KILLED_IN_ACTION.isTerminal)
        assertTrue(PlayerRaidState.MISSING_IN_ACTION.isTerminal)
        assertEquals(PlayerRaidState.MIA, PlayerRaidState.MISSING_IN_ACTION)
        assertEquals(PlayerRaidState.KIA, PlayerRaidState.KILLED_IN_ACTION)
    }

    @Test
    fun testMatchTickingAndTimeoutFlaggingMIA() {
        val world = createMockWorld()
        val template = TemplateMapData(templateId = "factory", displayName = "Chemical Plant")

        var completedCallbackFired = false
        val match = MatchInstance(
            matchId = "timeout_test",
            world = world,
            templateData = template,
            raidDurationSeconds = 3L,
            onComplete = { completedCallbackFired = true }
        )

        val player1 = UUID.randomUUID()
        val player2 = UUID.randomUUID()
        match.players[player1] = PlayerRaidState.IN_RAID
        match.players[player2] = PlayerRaidState.EXTRACTING

        assertFalse(match.isFinished)
        assertEquals(0L, match.elapsedSeconds)

        // Tick 1
        match.tick()
        assertEquals(1L, match.elapsedSeconds)
        assertFalse(match.isFinished)

        // Tick 2
        match.tick()
        assertEquals(2L, match.elapsedSeconds)
        assertFalse(match.isFinished)

        // Tick 3: Reaches raidDurationSeconds (3L) -> triggers timeout MIA
        match.tick()
        assertEquals(3L, match.elapsedSeconds)
        assertTrue(match.isFinished)

        // Both non-terminal operatives must be flagged MISSING_IN_ACTION
        assertEquals(PlayerRaidState.MISSING_IN_ACTION, match.players[player1])
        assertEquals(PlayerRaidState.MISSING_IN_ACTION, match.players[player2])
    }

    @Test
    fun testEarlyCompletionWhenAllOperativesFinish() {
        val world = createMockWorld()
        val template = TemplateMapData(templateId = "factory", displayName = "Chemical Plant")

        var completedCallbackFired = false
        val match = MatchInstance(
            matchId = "early_comp",
            world = world,
            templateData = template,
            raidDurationSeconds = 1000L,
            onComplete = { completedCallbackFired = true }
        )

        val p1 = UUID.randomUUID()
        val p2 = UUID.randomUUID()
        match.players[p1] = PlayerRaidState.IN_RAID
        match.players[p2] = PlayerRaidState.IN_RAID

        // Operative 1 extracts
        match.setPlayerState(p1, PlayerRaidState.EXTRACTED)
        match.checkCompletion()
        assertFalse(match.isFinished, "Match must continue while p2 is still in raid")

        // Operative 2 is eliminated
        match.setPlayerState(p2, PlayerRaidState.KILLED_IN_ACTION)
        match.checkCompletion()

        // All operatives have reached terminal states -> early teardown triggered
        assertTrue(match.isFinished, "Match must initiate teardown when all operatives complete")
    }
}
