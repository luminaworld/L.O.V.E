package com.luminaris.extraction.localization

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.configuration.file.YamlConfiguration
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TranslationManagerTest {

    private fun loadResourceYaml(path: String): YamlConfiguration {
        val stream = javaClass.classLoader.getResourceAsStream(path)
            ?: throw IllegalStateException("Resource not found: $path")
        return InputStreamReader(stream, StandardCharsets.UTF_8).use { reader ->
            YamlConfiguration.loadConfiguration(reader)
        }
    }

    private fun getAllLeafKeys(yaml: YamlConfiguration, prefix: String = ""): Set<String> {
        val keys = mutableSetOf<String>()
        val section = if (prefix.isEmpty()) yaml else yaml.getConfigurationSection(prefix)
            ?: return emptySet()

        for (k in section.getKeys(false)) {
            val fullKey = if (prefix.isEmpty()) k else "$prefix.$k"
            if (yaml.isConfigurationSection(fullKey)) {
                keys.addAll(getAllLeafKeys(yaml, fullKey))
            } else {
                keys.add(fullKey)
            }
        }
        return keys
    }

    @Test
    fun testConfigYmlSpecifiesLanguage() {
        val config = loadResourceYaml("config.yml")
        val language = config.getString("language")
        assertNotNull(language, "config.yml must specify 'language'")
        assertEquals("en", language, "Default language in config.yml should be 'en'")
    }

    @Test
    fun testLanguageFilesKeyParity() {
        val enYaml = loadResourceYaml("lang/en.yml")
        val thYaml = loadResourceYaml("lang/th.yml")

        val enKeys = getAllLeafKeys(enYaml)
        val thKeys = getAllLeafKeys(thYaml)

        assertFalse(enKeys.isEmpty(), "en.yml must have translation keys")
        assertFalse(thKeys.isEmpty(), "th.yml must have translation keys")

        val missingInTh = enKeys - thKeys
        val missingInEn = thKeys - enKeys

        assertTrue(
            missingInTh.isEmpty(),
            "The following keys exist in en.yml but are missing in th.yml: $missingInTh"
        )
        assertTrue(
            missingInEn.isEmpty(),
            "The following keys exist in th.yml but are missing in en.yml: $missingInEn"
        )

        assertEquals(enKeys.size, thKeys.size, "Both en.yml and th.yml must have identical key count")
    }

    @Test
    fun testTranslationManagerEnglishRetrievalAndFormatting() {
        TranslationManager.setLanguage("en")
        assertEquals("en", TranslationManager.getCurrentLanguage())

        val prefix = TranslationManager.get("prefix.core")
        assertTrue(prefix.contains("Luminaris"), "Main prefix should contain Luminaris")

        val bossbar = TranslationManager.get("match.bossbar_title", "time" to "15:00")
        assertEquals("<color:#FF5555>RAID TIME: </color><color:#FFFF55>15:00</color>", bossbar)

        val component = TranslationManager.render("match.bossbar_title", "time" to "15:00")
        val plainText = PlainTextComponentSerializer.plainText().serialize(component)
        assertEquals("RAID TIME: 15:00", plainText)
    }

    @Test
    fun testTranslationManagerThaiRetrievalAndFormatting() {
        TranslationManager.setLanguage("th")
        assertEquals("th", TranslationManager.getCurrentLanguage())

        val prefix = TranslationManager.get("prefix.core")
        assertTrue(prefix.contains("Luminaris"), "Main prefix should contain Luminaris")

        val noPerm = TranslationManager.get("general.no_permission")
        assertTrue(noPerm.contains("คุณไม่มีสิทธิ์"), "Thai translation for no_permission expected")

        val playerOnly = TranslationManager.get("general.player_only")
        assertTrue(playerOnly.contains("ผู้เล่นในเกมเท่านั้น"), "Thai translation for player_only expected")

        val commonRarity = TranslationManager.get("rarity.common")
        assertEquals("ทั่วไป", commonRarity)

        val legendaryRarity = TranslationManager.get("rarity.legendary")
        assertEquals("ตำนาน", legendaryRarity)
    }

    @Test
    fun testTranslationManagerFallbackToEnglish() {
        TranslationManager.setLanguage("th")
        val nonExistentKey = "custom.nonexistent.test_key"
        val fallback = TranslationManager.get(nonExistentKey)
        assertEquals(nonExistentKey, fallback, "Missing key should fallback to raw key name")
    }

    @Test
    fun testTerminalLogTranslationKeys() {
        TranslationManager.setLanguage("en")
        val copyError = TranslationManager.get("world.log_copy_error", "template" to "alpha", "error" to "disk full")
        assertTrue(copyError.contains("alpha") && copyError.contains("disk full"))

        TranslationManager.setLanguage("th")
        val copyErrorTh = TranslationManager.get("world.log_copy_error", "template" to "alpha", "error" to "disk full")
        assertTrue(copyErrorTh.contains("alpha") && copyErrorTh.contains("disk full"))
        assertTrue(copyErrorTh.contains("ล้มเหลวในการคัดลอกโฟลเดอร์เทมเพลต"))
    }

    @Test
    fun testLegacyColorConversionAndParsing() {
        // 1. Traditional & codes
        val comp1 = TranslationManager.parseComponent("&aGreen &cRed &6Gold &lBold")
        val plain1 = PlainTextComponentSerializer.plainText().serialize(comp1)
        assertEquals("Green Red Gold Bold", plain1)

        // 2. Section § codes
        val comp2 = TranslationManager.parseComponent("§aGreen §cRed")
        val plain2 = PlainTextComponentSerializer.plainText().serialize(comp2)
        assertEquals("Green Red", plain2)

        // 3. Hex &#RRGGBB format
        val comp3 = TranslationManager.parseComponent("&#00FFA3Cyan Title &#FF5555Red Subtitle")
        val plain3 = PlainTextComponentSerializer.plainText().serialize(comp3)
        assertEquals("Cyan Title Red Subtitle", plain3)

        // 4. Spigot hex &x format
        val comp4 = TranslationManager.parseComponent("&x&0&0&F&F&A&3Hex Spigot")
        val plain4 = PlainTextComponentSerializer.plainText().serialize(comp4)
        assertEquals("Hex Spigot", plain4)

        // 5. Mixed MiniMessage and Legacy codes
        val comp5 = TranslationManager.parseComponent("&6[Prefix] <gradient:#00FFA3:#00B8FF>Gradient Title</gradient> &7(v1.0)")
        val plain5 = PlainTextComponentSerializer.plainText().serialize(comp5)
        assertEquals("[Prefix] Gradient Title (v1.0)", plain5)

        // 6. Non-color ampersands remain untouched
        val comp6 = TranslationManager.parseComponent("Tom & Jerry & 123")
        val plain6 = PlainTextComponentSerializer.plainText().serialize(comp6)
        assertEquals("Tom & Jerry & 123", plain6)

        // 7. Escaped ampersands
        val comp7 = TranslationManager.parseComponent("&&aLiteral Ampersand A")
        val plain7 = PlainTextComponentSerializer.plainText().serialize(comp7)
        assertEquals("&aLiteral Ampersand A", plain7)
    }

    @Test
    fun testLegacyToMiniMessageConversionString() {
        assertEquals("<green>Hello", TranslationManager.legacyToMiniMessage("&aHello"))
        assertEquals("<red>Error: <yellow>Details", TranslationManager.legacyToMiniMessage("&cError: &eDetails"))
        assertEquals("<#00FFA3>Color", TranslationManager.legacyToMiniMessage("&#00FFA3Color"))
        assertEquals("<#112233>Spigot", TranslationManager.legacyToMiniMessage("&x&1&1&2&2&3&3Spigot"))
        assertEquals("<bold><gold>Gold", TranslationManager.legacyToMiniMessage("&l&6Gold"))
        assertEquals("<gradient:#123456:#654321>Gradient</gradient>", TranslationManager.legacyToMiniMessage("<gradient:#123456:#654321>Gradient</gradient>"))
    }

    @Test
    fun testEditorAndDialogTranslationKeys() {
        TranslationManager.setLanguage("en")
        assertEquals("Container Category Editor", TranslationManager.get("dialog.category.title"))
        assertEquals("Container ID", TranslationManager.get("dialog.category.input_id"))
        assertEquals("Drop Items Registry", PlainTextComponentSerializer.plainText().serialize(TranslationManager.render("editor.gui.btn_items")))
        assertTrue(TranslationManager.get("editor.gui.btn_edit_name_id").contains("Edit Name & ID"))
        assertTrue(TranslationManager.get("editor.gui.btn_select_containers").contains("Select Drop Boxes"))
        assertTrue(TranslationManager.get("editor.gui.click_plus_1_pct").contains("+1%"))
        assertTrue(TranslationManager.get("editor.gui.click_plus_5_pct").contains("+5%"))

        TranslationManager.setLanguage("th")
        assertEquals("เครื่องมือแก้ไขหมวดหมู่กล่องดรอป", TranslationManager.get("dialog.category.title"))
        assertEquals("รหัสเฉพาะของกล่อง (Container ID)", TranslationManager.get("dialog.category.input_id"))
        assertEquals("รหัสเฉพาะของไอเทม (Item ID)", TranslationManager.get("dialog.item.input_id"))
        assertEquals("คำอธิบายไอเทม (Lore กด Enter ขึ้นบรรทัดใหม่ได้)", TranslationManager.get("dialog.item.input_lore"))
        assertTrue(TranslationManager.get("editor.gui.btn_items").contains("คลังไอเทมดรอป"))
        assertTrue(TranslationManager.get("editor.gui.btn_edit_name_id").contains("แก้ไขชื่อและรหัส"))
        assertTrue(TranslationManager.get("editor.gui.btn_select_containers").contains("เลือกกล่องที่อนุญาตให้ดรอป"))
        assertTrue(TranslationManager.get("editor.gui.click_plus_1_pct").contains("+1%"))
        assertTrue(TranslationManager.get("editor.gui.click_plus_5_pct").contains("+5%"))

        // Test serializeComponent
        val testComp = TranslationManager.parseComponent("<color:#FFFF55>Test</color>")
        val serialized = TranslationManager.serializeComponent(testComp)
        assertTrue(serialized.contains("Test"))
    }

    @Test
    fun testSendLinesDispatchesEachLineIndependently() {
        val received = mutableListOf<Component>()
        val mockAudience = object : Audience {
            override fun sendMessage(message: Component) {
                received.add(message)
            }
        }

        TranslationManager.sendLines(mockAudience, "banner.shutdown")
        assertTrue(received.size >= 3, "Expected multiple lines dispatched independently, but got ${received.size}")
    }

    @kotlin.test.AfterTest
    fun tearDown() {
        TranslationManager.setLanguage("en")
    }
}
