package com.luminaris.extraction.storage

import com.luminaris.extraction.core.container.PlacedContainer
import com.luminaris.extraction.core.template.ExtractionZoneData
import com.luminaris.extraction.core.template.SpawnPoint
import com.luminaris.extraction.core.template.TemplateMapData
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TemplateStorageTest {

    @Test
    fun testPlacedContainerDataModel() {
        val container = PlacedContainer(
            id = "test_c_1",
            categoryId = "military_crate",
            x = 100,
            y = 65,
            z = -200
        )

        assertEquals("test_c_1", container.id)
        assertEquals("military_crate", container.categoryId)
        assertEquals(100, container.x)
        assertEquals(65, container.y)
        assertEquals(-200, container.z)

        val generatedId = PlacedContainer.generateId(10, 20, 30)
        assertTrue(generatedId.startsWith("c_10_20_30_"))
    }

    @Test
    fun testPlaceholderDataModels() {
        val spawn = SpawnPoint(
            id = "spawn_alpha",
            name = "Infiltration Alpha",
            x = 10.5,
            y = 70.0,
            z = -50.5,
            yaw = 90.0f,
            pitch = 0.0f
        )
        assertEquals("spawn_alpha", spawn.id)
        assertEquals("Infiltration Alpha", spawn.name)
        assertEquals(10.5, spawn.x)
        assertEquals(70.0, spawn.y)
        assertEquals(-50.5, spawn.z)
        assertEquals(90.0f, spawn.yaw)

        val zone = ExtractionZoneData(
            id = "zone_bravo",
            name = "Helipad Bravo",
            minX = 0.0,
            minY = 60.0,
            minZ = 0.0,
            maxX = 15.0,
            maxY = 75.0,
            maxZ = 15.0,
            countdownSeconds = 8
        )
        assertEquals("zone_bravo", zone.id)
        assertEquals("Helipad Bravo", zone.name)
        assertEquals(8, zone.countdownSeconds)
        assertEquals(15.0, zone.maxX)
    }

    @Test
    fun testTemplateYamlPersistenceAndLoading() {
        val tempDir = Files.createTempDirectory("luminaris_test_").toFile()
        try {
            val templateFolder = File(tempDir, "templates/factory")
            templateFolder.mkdirs()
            val mapFile = File(templateFolder, "map_data.yml")

            // Simulate TemplateMapData saving
            val data = TemplateMapData(
                templateId = "factory",
                displayName = "<gradient:#FF0055:#FFAA00>Abandoned Factory</gradient>",
                containers = mutableListOf(
                    PlacedContainer("c_1", "military_crate", 10, 64, 20),
                    PlacedContainer("c_2", "medical_kit", -15, 62, 45)
                ),
                spawns = mutableListOf(
                    SpawnPoint("sp_1", "North Yard", 0.0, 64.0, 0.0, 180.0f, 0.0f)
                ),
                extractions = mutableListOf(
                    ExtractionZoneData("ext_1", "Bunker Gates", 5.0, 60.0, 5.0, 15.0, 70.0, 15.0, 10)
                )
            )

            val yaml = YamlConfiguration()
            yaml.set("template_id", data.templateId)
            yaml.set("display_name", data.displayName)
            yaml.set("containers", data.containers.map {
                mapOf("id" to it.id, "category_id" to it.categoryId, "x" to it.x, "y" to it.y, "z" to it.z)
            })
            yaml.set("spawns", data.spawns.map {
                mapOf("id" to it.id, "name" to it.name, "x" to it.x, "y" to it.y, "z" to it.z, "yaw" to it.yaw, "pitch" to it.pitch)
            })
            yaml.set("extractions", data.extractions.map {
                mapOf(
                    "id" to it.id,
                    "name" to it.name,
                    "min_x" to it.minX,
                    "min_y" to it.minY,
                    "min_z" to it.minZ,
                    "max_x" to it.maxX,
                    "max_y" to it.maxY,
                    "max_z" to it.maxZ,
                    "countdown_seconds" to it.countdownSeconds
                )
            })
            yaml.save(mapFile)

            assertTrue(mapFile.exists())

            // Now read back and verify
            val loadedYaml = YamlConfiguration.loadConfiguration(mapFile)
            assertEquals("factory", loadedYaml.getString("template_id"))
            assertEquals("<gradient:#FF0055:#FFAA00>Abandoned Factory</gradient>", loadedYaml.getString("display_name"))

            val rawContainers = loadedYaml.get("containers") as List<*>
            assertEquals(2, rawContainers.size)

            val firstCat = (rawContainers[0] as Map<*, *>)["category_id"]
            val firstX = ((rawContainers[0] as Map<*, *>)["x"] as Number).toInt()
            assertEquals("military_crate", firstCat)
            assertEquals(10, firstX)

            val rawSpawns = loadedYaml.get("spawns") as List<*>
            assertEquals(1, rawSpawns.size)
            assertEquals("North Yard", (rawSpawns[0] as Map<*, *>)["name"])

            val rawExtractions = loadedYaml.get("extractions") as List<*>
            assertEquals(1, rawExtractions.size)
            assertEquals("Bunker Gates", (rawExtractions[0] as Map<*, *>)["name"])
            assertEquals(10, ((rawExtractions[0] as Map<*, *>)["countdown_seconds"] as Number).toInt())
        } finally {
            tempDir.deleteRecursively()
        }
    }

    private fun createMockPlugin(dataFolder: File): Plugin {
        val logger = java.util.logging.Logger.getLogger("TemplateStorageTest")
        return java.lang.reflect.Proxy.newProxyInstance(
            Plugin::class.java.classLoader,
            arrayOf(Plugin::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "getDataFolder" -> dataFolder
                "getLogger" -> logger
                "getName" -> "LuminaExtractionCore"
                else -> null
            }
        } as Plugin
    }

    @Test
    fun testTemplateStorageCrudAndReload() {
        val tempDir = Files.createTempDirectory("luminaris_storage_").toFile()
        try {
            val mockPlugin = createMockPlugin(tempDir)
            val storage = TemplateStorage(mockPlugin)
            storage.initialize()

            // 1. Add containers
            val c1 = PlacedContainer("c_1", "military_crate", 15, 64, 30)
            val c2 = PlacedContainer("c_2", "medical_kit", -20, 60, 50)
            storage.addContainer("factory", c1)
            storage.addContainer("factory", c2)

            // 2. Query
            assertEquals(2, storage.getContainers("factory").size)
            val fetched1 = storage.getContainerAt("factory", 15, 64, 30)
            assertNotNull(fetched1)
            assertEquals("military_crate", fetched1.categoryId)

            // 3. Verify file on disk
            val mapFile = storage.getMapFile("factory")
            assertTrue(mapFile.exists())

            // 4. Reload from disk using new storage instance
            val storage2 = TemplateStorage(mockPlugin)
            storage2.initialize()
            val reloadedContainers = storage2.getContainers("factory")
            assertEquals(2, reloadedContainers.size)
            assertEquals("military_crate", storage2.getContainerAt("factory", 15, 64, 30)?.categoryId)

            // 5. Remove container
            val removed = storage2.removeContainer("factory", 15, 64, 30)
            assertNotNull(removed)
            assertEquals("c_1", removed.id)
            assertEquals(1, storage2.getContainers("factory").size)
            assertNull(storage2.getContainerAt("factory", 15, 64, 30))

            // 6. Verify removal persisted
            val storage3 = TemplateStorage(mockPlugin)
            storage3.initialize()
            assertEquals(1, storage3.getContainers("factory").size)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testConcurrentStorageAccess() {
        val tempDir = Files.createTempDirectory("luminaris_concurrent_").toFile()
        try {
            val mockPlugin = createMockPlugin(tempDir)
            val storage = TemplateStorage(mockPlugin)
            storage.initialize()

            val threads = (1..8).map { threadIdx ->
                Thread {
                    for (i in 1..20) {
                        val c = PlacedContainer("c_${threadIdx}_$i", "category_$threadIdx", threadIdx * 10, 60, i)
                        storage.addContainer("map_$threadIdx", c)
                        storage.getContainerAt("map_$threadIdx", threadIdx * 10, 60, i)
                    }
                }
            }

            threads.forEach { it.start() }
            threads.forEach { it.join() }

            for (threadIdx in 1..8) {
                assertEquals(20, storage.getContainers("map_$threadIdx").size)
            }
        } finally {
            tempDir.deleteRecursively()
        }
    }

    private fun createMockWorld(name: String): org.bukkit.World {
        return java.lang.reflect.Proxy.newProxyInstance(
            org.bukkit.World::class.java.classLoader,
            arrayOf(org.bukkit.World::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "getName" -> name
                else -> null
            }
        } as org.bukkit.World
    }

    @Test
    fun testWorldTemplateResolution() {
        val tempDir = Files.createTempDirectory("luminaris_res_").toFile()
        try {
            val mockPlugin = createMockPlugin(tempDir)
            val storage = TemplateStorage(mockPlugin)
            storage.initialize()

            // 1. Prefix 'template_'
            val w1 = createMockWorld("template_factory")
            assertEquals("factory", storage.resolveTemplateId(w1))
            assertTrue(storage.isTemplateWorld(w1))

            // 2. Suffix '_template'
            val w2 = createMockWorld("bunker_template")
            assertEquals("bunker", storage.resolveTemplateId(w2))
            assertTrue(storage.isTemplateWorld(w2))

            // 3. Normal survival world (not in templates)
            val w3 = createMockWorld("survival_main")
            assertNull(storage.resolveTemplateId(w3))
            kotlin.test.assertFalse(storage.isTemplateWorld(w3))

            // 4. If directory created for world
            val customFolder = File(storage.templatesFolder, "custom_arena")
            customFolder.mkdirs()
            File(customFolder, "map_data.yml").createNewFile()

            val w4 = createMockWorld("custom_arena")
            assertEquals("custom_arena", storage.resolveTemplateId(w4))
            assertTrue(storage.isTemplateWorld(w4))
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
