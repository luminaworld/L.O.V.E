package com.luminaris.extraction.security.clientguard

import com.luminaris.extraction.security.clientguard.model.DetectionRule
import com.luminaris.extraction.security.clientguard.model.GuardMode
import com.luminaris.extraction.security.clientguard.model.ViolationAction
import org.bukkit.configuration.file.YamlConfiguration
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ClientGuardDetectionTest {

    private fun loadClientGuardYaml(): YamlConfiguration {
        val stream = javaClass.classLoader.getResourceAsStream("client_guard.yml")
            ?: throw IllegalStateException("Resource not found: client_guard.yml")
        return InputStreamReader(stream, StandardCharsets.UTF_8).use { reader ->
            YamlConfiguration.loadConfiguration(reader)
        }
    }

    private fun parseRule(yaml: YamlConfiguration, ruleKey: String): DetectionRule {
        val sec = yaml.getConfigurationSection("detections.$ruleKey")
            ?: throw IllegalArgumentException("Missing detection rule section: $ruleKey")

        val name = sec.getString("name", ruleKey)!!
        val enabled = sec.getBoolean("enabled", true)
        val matchBrands = sec.getStringList("match_brands").map { Regex(it) }
        val matchChannels = sec.getStringList("match_channels").map { Regex(it) }

        val actions = mutableListOf<ViolationAction>()
        for (actionMap in sec.getMapList("actions")) {
            when ((actionMap["type"] as? String)?.uppercase()) {
                "KICK" -> actions.add(ViolationAction.Kick(actionMap["message"] as? String))
                "BAN" -> actions.add(ViolationAction.Ban(actionMap["duration"] as? String, actionMap["reason"] as? String))
                "ALERT" -> actions.add(ViolationAction.Alert())
                "COMMAND" -> {
                    val cmds = when (val c = actionMap["commands"] ?: actionMap["command"]) {
                        is List<*> -> c.mapNotNull { it?.toString() }
                        is String -> listOf(c)
                        else -> emptyList()
                    }
                    actions.add(ViolationAction.Command(cmds))
                }
                "LOG" -> actions.add(ViolationAction.Log)
            }
        }

        return DetectionRule(
            id = ruleKey,
            name = name,
            enabled = enabled,
            matchBrands = matchBrands,
            matchChannels = matchChannels,
            actions = actions
        )
    }

    @Test
    fun testClientGuardYamlStructure() {
        val yaml = loadClientGuardYaml()
        assertTrue(yaml.getBoolean("enabled"), "Client guard should be enabled by default")
        assertTrue(yaml.getBoolean("debug"), "Debug should be enabled by default for testing visibility")
        assertTrue(yaml.getBoolean("check_operators"), "check_operators should be enabled by default")
        assertEquals("SMART_HYBRID", yaml.getString("mode"), "Default mode should be SMART_HYBRID")
        assertTrue(yaml.contains("detections.meteor"), "Must contain meteor detection rule")
        assertTrue(yaml.contains("detections.wurst"), "Must contain wurst detection rule")
        assertTrue(yaml.contains("detections.lunar"), "Must contain lunar detection rule")
        assertTrue(yaml.contains("detections.liquidbounce"), "Must contain liquidbounce detection rule")
        assertTrue(yaml.contains("detections.aristois"), "Must contain aristois detection rule")
        assertTrue(yaml.contains("detections.baritone"), "Must contain baritone detection rule")
        assertTrue(yaml.contains("detections.popular_cheats"), "Must contain popular_cheats detection rule")
    }

    @Test
    fun testMeteorDetection() {
        val yaml = loadClientGuardYaml()
        val meteorRule = parseRule(yaml, "meteor")

        assertTrue(meteorRule.enabled)
        assertTrue(meteorRule.matchesBrand("meteor"))
        assertTrue(meteorRule.matchesBrand("Meteor-Client-v0.5.5"))
        assertTrue(meteorRule.matchesBrand("METEOR_DEV"))
        assertTrue(meteorRule.matchesChannel("meteor:chat"))
        assertTrue(meteorRule.matchesChannel("meteor-client:sync"))

        assertFalse(meteorRule.matchesBrand("vanilla"))
        assertFalse(meteorRule.matchesChannel("minecraft:brand"))
    }

    @Test
    fun testWurstDetection() {
        val yaml = loadClientGuardYaml()
        val wurstRule = parseRule(yaml, "wurst")

        assertTrue(wurstRule.enabled)
        assertTrue(wurstRule.matchesBrand("wurst"))
        assertTrue(wurstRule.matchesBrand("Wurst-Client"))
        assertTrue(wurstRule.matchesBrand("wurst7"))
        assertTrue(wurstRule.matchesChannel("wurst:check"))
        assertTrue(wurstRule.matchesChannel("wurstclient:sync"))

        assertFalse(wurstRule.matchesBrand("vanilla"))
    }

    @Test
    fun testLunarClientDetection() {
        val yaml = loadClientGuardYaml()
        val lunarRule = parseRule(yaml, "lunar")

        assertTrue(lunarRule.enabled)
        assertTrue(lunarRule.matchesBrand("lunarclient:v2.18.2"))
        assertTrue(lunarRule.matchesBrand("lunarclient:1.21"))
        assertTrue(lunarRule.matchesBrand("Lunar-Client"))
        assertTrue(lunarRule.matchesChannel("lunarclient:pm"))
        assertTrue(lunarRule.matchesChannel("lunar:apollo"))

        assertFalse(lunarRule.matchesBrand("vanilla"))
        assertFalse(lunarRule.matchesBrand("fabric"))
    }

    @Test
    fun testBaritoneDetection() {
        val yaml = loadClientGuardYaml()
        val baritoneRule = parseRule(yaml, "baritone")

        assertTrue(baritoneRule.enabled)
        assertTrue(baritoneRule.matchesBrand("baritone"))
        assertTrue(baritoneRule.matchesBrand("Baritone-Fabric"))
        assertTrue(baritoneRule.matchesChannel("baritone:channel"))
        assertTrue(baritoneRule.matchesChannel("baritone:control"))
    }

    @Test
    fun testPopularCheatsDetection() {
        val yaml = loadClientGuardYaml()
        val cheatsRule = parseRule(yaml, "popular_cheats")

        assertTrue(cheatsRule.matchesBrand("CheatBreaker"))
        assertTrue(cheatsRule.matchesBrand("ImpactClient"))
        assertTrue(cheatsRule.matchesBrand("Sigma5"))
        assertTrue(cheatsRule.matchesBrand("Jello"))
        assertTrue(cheatsRule.matchesBrand("Huzuni-Reloaded"))
        assertTrue(cheatsRule.matchesBrand("BleachHack"))
        assertTrue(cheatsRule.matchesBrand("Ares-Client"))
        assertTrue(cheatsRule.matchesBrand("RusherHack"))
        assertTrue(cheatsRule.matchesBrand("FutureClient"))
        assertTrue(cheatsRule.matchesBrand("Vape-v4"))
        assertTrue(cheatsRule.matchesBrand("InertiaClient"))

        assertTrue(cheatsRule.matchesChannel("rusherhack:main"))
        assertTrue(cheatsRule.matchesChannel("future:sync"))
        assertTrue(cheatsRule.matchesChannel("bleachhack:comm"))
    }

    @Test
    fun testVanillaClientPassesSmartHybrid() {
        val yaml = loadClientGuardYaml()
        val allRules = yaml.getConfigurationSection("detections")!!.getKeys(false).map {
            parseRule(yaml, it)
        }

        val vanillaBrand = "vanilla"
        val vanillaChannels = setOf("minecraft:brand")

        for (rule in allRules) {
            if (!rule.enabled) continue
            assertFalse(rule.matchesBrand(vanillaBrand), "Rule '${rule.id}' should not match vanilla brand")
            for (ch in vanillaChannels) {
                assertFalse(rule.matchesChannel(ch), "Rule '${rule.id}' should not match channel '$ch'")
            }
        }
    }

    @Test
    fun testFabricPassesSmartHybridWhenModLoadersDisabled() {
        val yaml = loadClientGuardYaml()
        val modLoadersRule = parseRule(yaml, "mod_loaders")

        assertFalse(modLoadersRule.enabled, "mod_loaders rule should be disabled by default in SMART_HYBRID")

        // If enabled manually for Strict mode:
        assertTrue(modLoadersRule.matchesBrand("fabric"))
        assertTrue(modLoadersRule.matchesBrand("forge"))
        assertTrue(modLoadersRule.matchesBrand("neoforge"))
        assertTrue(modLoadersRule.matchesChannel("fabric:registry/sync"))
        assertTrue(modLoadersRule.matchesChannel("fml:handshake"))
    }

    @Test
    fun testGuardModeEnumParsing() {
        assertEquals(GuardMode.SMART_HYBRID, GuardMode.fromString("SMART_HYBRID"))
        assertEquals(GuardMode.STRICT_VANILLA, GuardMode.fromString("strict_vanilla"))
        assertEquals(GuardMode.WHITELIST_ONLY, GuardMode.fromString("WHITELIST_ONLY"))
        assertEquals(GuardMode.SMART_HYBRID, GuardMode.fromString("invalid_mode_defaults_to_hybrid"))
    }

    @Test
    fun testBrandSpoofingAndSignProbeConfiguration() {
        val yaml = loadClientGuardYaml()
        assertTrue(yaml.getBoolean("brand_spoof_detection.enabled"), "Brand spoof detection must be enabled by default")
        assertTrue(yaml.isList("brand_spoof_detection.actions"), "Brand spoof must have actions list")
        assertTrue(yaml.getBoolean("sign_probe.enabled"), "Sign probe must be enabled by default")
        assertEquals(40L, yaml.getLong("sign_probe.delay_ticks"), "Sign probe delay ticks must be 40L")
        assertTrue(yaml.getStringList("mod_namespaces.blacklist").contains("meteor"), "Must blacklist meteor namespace")
        assertTrue(yaml.getStringList("mod_namespaces.blacklist").contains("wurst"), "Must blacklist wurst namespace")
        assertTrue(yaml.getStringList("mod_namespaces.blacklist").contains("baritone"), "Must blacklist baritone namespace")
    }

    @Test
    fun testSignProbeChecksList() {
        val checks = com.luminaris.extraction.security.clientguard.probe.SignTranslationProbe.getDefaultChecks()
        val meteorCheck = checks.find { it.id == "meteor" || it.id == "meteor_client" }
        assertNotNull(meteorCheck)
        assertTrue(meteorCheck.bypassProtection, "Meteor check must use bypassProtection %s wrapper")
        assertEquals("key.category.meteor-client.meteor-client", meteorCheck.translationKey)

        val wurstCheck = checks.find { it.id == "wurst" }
        assertNotNull(wurstCheck)
        assertEquals("key.wurst.zoom", wurstCheck.translationKey)
    }

    @Test
    fun testModRegisterParserExtraction() {
        val parser = com.luminaris.extraction.security.clientguard.parser.ModRegisterParser()
        val raw = "baritone:channel\u0000meteor-client:sync\u0000sodium:main\u0000fabric:registry/sync"
        val result = parser.parseChannels(raw)

        assertTrue(result.hasFabricChannels, "Should detect fabric channels")
        assertTrue(result.detectedBlacklistMods.contains("baritone"), "Should flag baritone")
        assertTrue(result.detectedBlacklistMods.contains("meteor-client"), "Should flag meteor-client")
        assertFalse(result.detectedBlacklistMods.contains("sodium"), "Sodium should not be blacklisted")
        assertTrue(result.allDiscoveredMods.contains("sodium"))
    }

    @Test
    fun testMonitoredPluginChannelsList() {
        val channels = com.luminaris.extraction.security.clientguard.interceptor.ClientGuardMessageInterceptor.MONITORED_CHANNELS
        assertTrue(channels.contains("minecraft:brand"))
        assertTrue(channels.contains("lunarclient:pm"))
        assertTrue(channels.contains("fabric:registry/sync"))
        assertTrue(channels.contains("baritone:channel"))
    }

    @Test
    fun testBypassedPlayersConfiguration() {
        val yaml = loadClientGuardYaml()
        assertTrue(yaml.contains("bypassed_players"), "Must contain bypassed_players configuration key")
        assertTrue(yaml.isList("bypassed_players"), "bypassed_players must be a list")
    }

    @Test
    fun testBypassSetLogic() {
        val bypassed = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()
        val name = "CheatTester"
        val normalized = name.lowercase().trim()

        assertTrue(bypassed.add(normalized))
        assertFalse(bypassed.add(normalized), "Adding duplicate should return false")
        assertTrue(bypassed.contains("cheattester"))
        assertTrue(bypassed.contains(name.lowercase()))

        assertTrue(bypassed.remove("cheattester"))
        assertFalse(bypassed.contains("cheattester"))
    }
}

