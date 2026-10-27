package com.luminaris.extraction.web

import org.bukkit.configuration.file.YamlConfiguration
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WebConfigServerTest {

    private fun loadResourceYaml(path: String): YamlConfiguration {
        val stream = javaClass.classLoader.getResourceAsStream(path)
            ?: throw IllegalStateException("Resource not found: $path")
        return InputStreamReader(stream, StandardCharsets.UTF_8).use { reader ->
            YamlConfiguration.loadConfiguration(reader)
        }
    }

    @Test
    fun testConfigYmlSpecifiesWebConfigDefaults() {
        val config = loadResourceYaml("config.yml")
        assertTrue(config.contains("web_config"), "config.yml must contain 'web_config' section")
        assertTrue(config.getBoolean("web_config.enabled"), "web_config.enabled should default to true")
        assertEquals(8080, config.getInt("web_config.port"), "web_config.port should default to 8080")
        assertEquals("0.0.0.0", config.getString("web_config.bind_address"), "web_config.bind_address should default to 0.0.0.0")
        assertEquals("", config.getString("web_config.auth_token"), "web_config.auth_token should default to empty string")
    }

    @Test
    fun testWebStaticAssetsExistInJarResources() {
        val assets = listOf("web/index.html", "web/css/style.css", "web/js/app.js")
        for (asset in assets) {
            val stream = javaClass.classLoader.getResourceAsStream(asset)
            assertNotNull(stream, "Asset $asset must exist in plugin resources")
            val content = stream.bufferedReader(StandardCharsets.UTF_8).readText()
            assertTrue(content.length > 50, "Asset $asset should contain valid content")
        }
    }

    @Test
    fun testIndexHtmlStructureAndMonochromeStyling() {
        val stream = javaClass.classLoader.getResourceAsStream("web/index.html")
        assertNotNull(stream)
        val html = stream.bufferedReader(StandardCharsets.UTF_8).readText()

        assertTrue(html.contains("LUMINARIS"), "index.html should have brand title")
        assertTrue(html.contains("CONFIG CORE"), "index.html should have brand badge")
        assertTrue(html.contains("formViewPanel"), "index.html should contain form view panel")
        assertTrue(html.contains("rawViewPanel"), "index.html should contain raw view panel")
        assertTrue(html.contains("saveBtn"), "index.html should have Save & Apply button")
        assertTrue(html.contains("inGameReloadBtn"), "index.html should have in-game reload button")
        assertTrue(html.contains("globalSearchInput"), "index.html should have search input")
    }

    @Test
    fun testYamlSectionToMapConversion() {
        val yaml = YamlConfiguration()
        yaml.set("web_config.enabled", true)
        yaml.set("web_config.port", 8080)
        yaml.set("web_config.bind_address", "127.0.0.1")
        yaml.set("extraction.default_countdown_seconds", 15)

        val map = WebConfigServer.yamlSectionToMap(yaml)
        assertNotNull(map["web_config"], "web_config section should be mapped")
        val webConfig = map["web_config"] as Map<*, *>
        assertEquals(true, webConfig["enabled"])
        assertEquals(8080, webConfig["port"])
        assertEquals("127.0.0.1", webConfig["bind_address"])

        val extraction = map["extraction"] as Map<*, *>
        assertEquals(15, extraction["default_countdown_seconds"])
    }

    @Test
    fun testYamlSyntaxValidation() {
        val validYaml = """
            lobby:
              world: "world"
              x: 100.5
              y: 65.0
              z: 200.5
        """.trimIndent()

        val invalidYaml = """
            lobby:
              world: "world
              x: [unclosed list
        """.trimIndent()

        val yaml = YamlConfiguration()
        var validLoaded = false
        try {
            yaml.loadFromString(validYaml)
            validLoaded = true
        } catch (_: Exception) {
            validLoaded = false
        }
        assertTrue(validLoaded, "Valid YAML must parse without exception")

        var invalidFailed = false
        try {
            val badYaml = YamlConfiguration()
            badYaml.loadFromString(invalidYaml)
        } catch (_: Exception) {
            invalidFailed = true
        }
        assertTrue(invalidFailed, "Invalid YAML must throw an exception on parse")
    }

    @Test
    fun testItemsAndCategoriesYamlSerialization() {
        val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
        for (file in listOf("items.yml", "categories.yml", "safe_cases.yml", "lang/en.yml", "lang/th.yml")) {
            val yaml = loadResourceYaml(file)
            val map = WebConfigServer.yamlSectionToMap(yaml)
            val json = gson.toJson(map)
            assertNotNull(json)
            assertTrue(json.isNotEmpty())
        }
    }

    @Test
    fun testOptionalSerialization() {
        val gson = com.google.gson.GsonBuilder()
            .registerTypeAdapter(java.util.Optional::class.java, com.google.gson.JsonSerializer<java.util.Optional<*>> { src, _, context ->
                if (src.isPresent) context.serialize(src.get()) else com.google.gson.JsonNull.INSTANCE
            })
            .create()

        val data = mapOf(
            "present" to java.util.Optional.of("Hello Paper"),
            "empty" to java.util.Optional.empty<String>()
        )
        val json = gson.toJson(data)
        assertTrue(json.contains("Hello Paper"))
    }
}
