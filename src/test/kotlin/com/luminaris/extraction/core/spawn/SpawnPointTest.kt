package com.luminaris.extraction.core.spawn

import org.bukkit.util.Vector
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SpawnPointTest {

    @Test
    fun testSpawnPointCreationAndProperties() {
        val sp = SpawnPoint(
            id = "spawn_alpha",
            x = 100.5,
            y = 65.0,
            z = -200.5,
            yaw = 180.0f,
            pitch = -15.0f
        )

        assertEquals("spawn_alpha", sp.id)
        assertEquals("spawn_alpha", sp.name)
        assertEquals(100.5, sp.x)
        assertEquals(65.0, sp.y)
        assertEquals(-200.5, sp.z)
        assertEquals(180.0f, sp.yaw)
        assertEquals(-15.0f, sp.pitch)
    }

    @Test
    fun testSecondaryConstructorWithCustomName() {
        val sp = SpawnPoint("sp_2", "Infiltration Delta", 10.0, 70.0, 20.0, 90.0f, 0.0f)
        assertEquals("sp_2", sp.id)
        assertEquals("Infiltration Delta", sp.name)
        assertEquals(10.0, sp.x)
        assertEquals(70.0, sp.y)
        assertEquals(20.0, sp.z)
    }

    @Test
    fun testEuclideanDistanceCalculations() {
        val sp1 = SpawnPoint("p1", 0.0, 0.0, 0.0)
        val sp2 = SpawnPoint("p2", 3.0, 4.0, 0.0)

        // 3D Distance: sqrt(3^2 + 4^2 + 0^2) = 5.0
        assertEquals(25.0, sp1.distanceSquared(sp2), 1e-6)
        assertEquals(5.0, sp1.distance(sp2), 1e-6)
        assertEquals(3.0, sp1.horizontalDistance(sp2), 1e-6)

        val sp3 = SpawnPoint("p3", 1.0, 2.0, 2.0)
        // 3D Distance from origin: sqrt(1^2 + 2^2 + 2^2) = sqrt(9) = 3.0
        assertEquals(9.0, sp1.distanceSquared(sp3), 1e-6)
        assertEquals(3.0, sp1.distance(sp3), 1e-6)
        assertEquals(sqrt(5.0), sp1.horizontalDistance(sp3), 1e-6)
    }

    @Test
    fun testToVectorConversion() {
        val sp = SpawnPoint("v1", 12.3, 45.6, 78.9)
        val vec: Vector = sp.toVector()

        assertEquals(12.3, vec.x, 1e-6)
        assertEquals(45.6, vec.y, 1e-6)
        assertEquals(78.9, vec.z, 1e-6)
    }
}
