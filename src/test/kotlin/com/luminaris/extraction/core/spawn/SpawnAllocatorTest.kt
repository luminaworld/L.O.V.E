package com.luminaris.extraction.core.spawn

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.nio.file.Files
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SpawnAllocatorTest {

    @Test
    fun testAntiClumpingSeparatesOpposingTeams() {
        val allocator = SpawnAllocator()

        // Create 2 clusters of spawns separated by 200 blocks.
        // Cluster A (South-West): around (0, 64, 0)
        val spA1 = SpawnPoint("sp_a1", 0.0, 64.0, 0.0)
        val spA2 = SpawnPoint("sp_a2", 1.0, 64.0, 1.0) // Clumped with A1 (distance ~ 1.41)
        val spA3 = SpawnPoint("sp_a3", 2.0, 64.0, 0.0) // Clumped with A1

        // Cluster B (North-East): around (200, 64, 200)
        val spB1 = SpawnPoint("sp_b1", 200.0, 64.0, 200.0)
        val spB2 = SpawnPoint("sp_b2", 201.0, 64.0, 201.0) // Clumped with B1
        val spB3 = SpawnPoint("sp_b3", 200.0, 64.0, 202.0) // Clumped with B1

        val candidates = listOf(spA1, spA2, spA3, spB1, spB2, spB3)
        val teams = listOf("squad_alpha", "squad_bravo")

        // Allocate spawns
        val allocation = allocator.allocateSpawns(teams, candidates, Random(42))

        assertEquals(2, allocation.size)
        val spawnAlpha = allocation["squad_alpha"]
        val spawnBravo = allocation["squad_bravo"]

        assertNotNull(spawnAlpha)
        assertNotNull(spawnBravo)

        // The Euclidean distance between assigned spawns MUST maximize distance (i.e. cross-cluster >= 200 blocks)
        val distance = spawnAlpha.distance(spawnBravo)
        assertTrue(
            distance >= 250.0,
            "Anti-clumping must separate opposing teams across clusters! Actual distance was: $distance"
        )
    }

    @Test
    fun testAntiClumpingFourCornersDispersion() {
        val allocator = SpawnAllocator()

        // 4 corners of a 500x500 map + 2 center clumped spawns
        val cornerNW = SpawnPoint("nw", -250.0, 64.0, -250.0)
        val cornerNE = SpawnPoint("ne", 250.0, 64.0, -250.0)
        val cornerSE = SpawnPoint("se", 250.0, 64.0, 250.0)
        val cornerSW = SpawnPoint("sw", -250.0, 64.0, 250.0)

        val center1 = SpawnPoint("c1", 0.0, 64.0, 0.0)
        val center2 = SpawnPoint("c2", 5.0, 64.0, 5.0)

        val candidates = listOf(cornerNW, cornerNE, cornerSE, cornerSW, center1, center2)
        val squads = listOf("team_red", "team_blue", "team_green", "team_yellow")

        val allocation = allocator.allocateSpawns(squads, candidates, Random(100))

        assertEquals(4, allocation.size)
        val assignedSpawns = allocation.values.toList()

        // Every pair among the 4 chosen teams should be separated by at least 500 blocks
        for (i in 0 until assignedSpawns.size) {
            for (j in i + 1 until assignedSpawns.size) {
                val dist = assignedSpawns[i].distance(assignedSpawns[j])
                assertTrue(
                    dist >= 490.0,
                    "Expected high dispersion (>= 490), but was $dist between ${assignedSpawns[i].id} and ${assignedSpawns[j].id}"
                )
            }
        }
    }

    @Test
    fun testAllocationEdgeCases() {
        val allocator = SpawnAllocator()

        // 1. Empty squads
        assertTrue(allocator.allocateSpawns(emptyList(), listOf(SpawnPoint("s1", 0.0, 0.0, 0.0))).isEmpty())

        // 2. Empty candidates
        assertTrue(allocator.allocateSpawns(listOf("team_1"), emptyList()).isEmpty())

        // 3. Single squad
        val singleSpawn = SpawnPoint("single", 10.0, 64.0, 10.0)
        val singleRes = allocator.allocateSpawns(listOf("lone_wolf"), listOf(singleSpawn))
        assertEquals(1, singleRes.size)
        assertEquals("single", singleRes["lone_wolf"]?.id)

        // 4. More squads than candidates: must throw IllegalStateException
        val sp1 = SpawnPoint("s1", 0.0, 64.0, 0.0)
        val sp2 = SpawnPoint("s2", 100.0, 64.0, 100.0)
        val threeTeams = listOf("t1", "t2", "t3")
        kotlin.test.assertFailsWith<IllegalStateException> {
            allocator.allocateSpawns(threeTeams, listOf(sp1, sp2))
        }
    }

    @Test
    fun testLoadSpawnsFromMapDataYaml() {
        val allocator = SpawnAllocator()
        val tempDir = Files.createTempDirectory("luminaris_spawn_test_").toFile()

        try {
            val mapFile = File(tempDir, "map_data.yml")
            mapFile.writeText(
                """
                template_id: "factory"
                display_name: "Abandoned Factory"
                spawns:
                  - id: "spawn_gate"
                    name: "Main Gates"
                    x: 10.5
                    y: 65.0
                    z: -50.2
                    yaw: 180.0
                    pitch: 0.0
                  - id: "spawn_roof"
                    name: "Rooftop"
                    x: -25.0
                    y: 90.0
                    z: 110.0
                    yaw: 90.0
                    pitch: -10.0
                """.trimIndent()
            )

            val spawns = allocator.loadSpawnsFromMapData(mapFile)
            assertEquals(2, spawns.size)

            val s1 = spawns.first { it.id == "spawn_gate" }
            assertEquals(10.5, s1.x)
            assertEquals(65.0, s1.y)
            assertEquals(-50.2, s1.z)
            assertEquals(180.0f, s1.yaw)
            assertEquals("Main Gates", s1.name)

            val s2 = spawns.first { it.id == "spawn_roof" }
            assertEquals(-25.0, s2.x)
            assertEquals(90.0, s2.y)
            assertEquals(110.0, s2.z)
            assertEquals(90.0f, s2.yaw)
            assertEquals("Rooftop", s2.name)

        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testLoadSpawnsFromConfigurationSectionMapFormat() {
        val allocator = SpawnAllocator()
        val yaml = YamlConfiguration()
        val sec = yaml.createSection("spawns")

        val s1 = sec.createSection("sp_alpha")
        s1.set("name", "Alpha Site")
        s1.set("x", 50.0)
        s1.set("y", 64.0)
        s1.set("z", -30.0)
        s1.set("yaw", 45.0)
        s1.set("pitch", 0.0)

        val s2 = sec.createSection("sp_bravo")
        s2.set("name", "Bravo Bunker")
        s2.set("x", -80.0)
        s2.set("y", 58.0)
        s2.set("z", 120.0)

        val spawns = allocator.loadSpawnsFromYaml(yaml)
        assertEquals(2, spawns.size)

        val alpha = spawns.first { it.id == "sp_alpha" }
        assertEquals(50.0, alpha.x)
        assertEquals(64.0, alpha.y)
        assertEquals(-30.0, alpha.z)
        assertEquals(45.0f, alpha.yaw)
        assertEquals("Alpha Site", alpha.name)

        val bravo = spawns.first { it.id == "sp_bravo" }
        assertEquals(-80.0, bravo.x)
        assertEquals(58.0, bravo.y)
        assertEquals(120.0, bravo.z)
    }
}
