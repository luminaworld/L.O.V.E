package com.luminaris.extraction.core.world

import org.bukkit.plugin.Plugin
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InstanceWorldServiceTest {

    private fun createMockPlugin(dataFolder: File): Plugin {
        val logger = java.util.logging.Logger.getLogger("InstanceWorldServiceTest")
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
    fun testNioCopyAndPurgeCollisionFiles() {
        val tempRoot = Files.createTempDirectory("luminaris_world_test_").toFile()
        try {
            val templatesFolder = File(tempRoot, "templates_worlds")
            val instancesFolder = File(tempRoot, "instances")
            templatesFolder.mkdirs()
            instancesFolder.mkdirs()

            // 1. Create a dummy world template with collision files
            val templateDir = File(templatesFolder, "factory")
            templateDir.mkdirs()
            File(templateDir, "level.dat").writeText("fake-level-data")
            val regionDir = File(templateDir, "region")
            regionDir.mkdirs()
            File(regionDir, "r.0.0.mca").writeBytes(byteArrayOf(1, 2, 3, 4))
            File(templateDir, "uid.dat").writeBytes(byteArrayOf(9, 9, 9))
            File(templateDir, "session.lock").writeBytes(byteArrayOf(1, 1))

            val dimDir = File(templateDir, "DIM-1")
            dimDir.mkdirs()
            File(dimDir, "uid.dat").writeBytes(byteArrayOf(8, 8))
            File(dimDir, "session.lock").writeBytes(byteArrayOf(2, 2))

            val mockPlugin = createMockPlugin(tempRoot)
            val service = InstanceWorldService(mockPlugin)
            service.templatesWorldsFolder = templatesFolder
            service.instancesFolder = instancesFolder

            val targetDir = service.getInstanceWorldFolder("test_match_101")
            assertFalse(targetDir.exists())

            // 2. Perform copy & purge operations
            val sourceDir = service.resolveTemplateWorldDirectory("factory")
            assertEquals(templateDir.canonicalPath, sourceDir.canonicalPath)

            // Copy files to target
            val copyMethod = InstanceWorldService::class.java.getDeclaredMethod(
                "copyTemplateWorldFolder",
                java.nio.file.Path::class.java,
                java.nio.file.Path::class.java
            )
            copyMethod.isAccessible = true
            copyMethod.invoke(service, sourceDir.toPath(), targetDir.toPath())

            // Purge collision files
            service.purgeWorldCollisionFiles(targetDir.toPath())

            // 3. Assertions
            assertTrue(targetDir.exists())
            assertTrue(File(targetDir, "level.dat").exists())
            assertTrue(File(targetDir, "region/r.0.0.mca").exists())

            // uid.dat and session.lock MUST NOT exist in target!
            assertFalse(File(targetDir, "uid.dat").exists(), "uid.dat must be eliminated from root")
            assertFalse(File(targetDir, "session.lock").exists(), "session.lock must be eliminated from root")
            assertFalse(File(targetDir, "DIM-1/uid.dat").exists(), "uid.dat must be eliminated from subdirectories")
            assertFalse(File(targetDir, "DIM-1/session.lock").exists(), "session.lock must be eliminated from subdirectories")

        } finally {
            tempRoot.deleteRecursively()
        }
    }

    @Test
    fun testTemplatePathResolution() {
        val tempRoot = Files.createTempDirectory("luminaris_path_test_").toFile()
        try {
            val templatesFolder = File(tempRoot, "templates_worlds")
            templatesFolder.mkdirs()

            val customTemplate = File(templatesFolder, "bunker_zone")
            customTemplate.mkdirs()

            val mockPlugin = createMockPlugin(tempRoot)
            val service = InstanceWorldService(mockPlugin)
            service.templatesWorldsFolder = templatesFolder
            service.instancesFolder = File(tempRoot, "instances")

            val resolved = service.resolveTemplateWorldDirectory("bunker_zone")
            assertEquals(customTemplate.canonicalPath, resolved.canonicalPath)

            val instanceTarget = service.getInstanceWorldFolder("match_xyz")
            assertEquals(File(tempRoot, "instances/match_match_xyz").canonicalPath, instanceTarget.canonicalPath)
        } finally {
            tempRoot.deleteRecursively()
        }
    }

    @Test
    fun testActiveInstanceTracking() {
        val tempRoot = Files.createTempDirectory("luminaris_instances_test_").toFile()
        try {
            val mockPlugin = createMockPlugin(tempRoot)
            val service = InstanceWorldService(mockPlugin)
            assertEquals(0, service.getActiveInstanceCount())
            assertTrue(service.getActiveInstances().isEmpty())
        } finally {
            tempRoot.deleteRecursively()
        }
    }

    @Test
    fun testPurgeCollisionFilePatterns() {
        val isCollisionMethod = InstanceWorldService::class.java.getDeclaredMethod(
            "isWorldCollisionFile",
            String::class.java
        ).apply { isAccessible = true }

        val tempRoot = Files.createTempDirectory("luminaris_pattern_test_").toFile()
        try {
            val mockPlugin = createMockPlugin(tempRoot)
            val service = InstanceWorldService(mockPlugin)

            fun check(name: String): Boolean = isCollisionMethod.invoke(service, name) as Boolean

            assertTrue(check("uid.dat"))
            assertTrue(check("UID.DAT"))
            assertTrue(check("session.lock"))
            assertTrue(check("SESSION.LOCK"))
            assertTrue(check("session.lock.tmp"))
            assertFalse(check("level.dat"))
            assertFalse(check("r.0.0.mca"))
            assertFalse(check("map_data.yml"))
        } finally {
            tempRoot.deleteRecursively()
        }
    }

    @Test
    fun testRecursiveDeletion() {
        val tempRoot = Files.createTempDirectory("luminaris_del_test_").toFile()
        try {
            val nested = File(tempRoot, "a/b/c/d")
            nested.mkdirs()
            File(nested, "sample.txt").writeText("Sample content")
            File(tempRoot, "a/root_sample.bin").writeBytes(byteArrayOf(1, 2, 3))

            val mockPlugin = createMockPlugin(tempRoot)
            val service = InstanceWorldService(mockPlugin)

            val delMethod = InstanceWorldService::class.java.getDeclaredMethod(
                "deleteDirectoryRecursively",
                java.nio.file.Path::class.java
            ).apply { isAccessible = true }

            delMethod.invoke(service, File(tempRoot, "a").toPath())
            assertFalse(File(tempRoot, "a").exists())
        } finally {
            tempRoot.deleteRecursively()
        }
    }
}
