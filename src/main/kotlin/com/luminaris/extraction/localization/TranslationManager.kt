package com.luminaris.extraction.localization

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * High-performance, thread-safe Multi-Language Localization and Translation Engine.
 * Supports hot-reloading, missing-key fallbacks to English (en.yml), MiniMessage integration,
 * placeholder injection, and automated resource initialization.
 */
class TranslationManager(private val plugin: Plugin? = null) {

    companion object {
        private var _instance: TranslationManager? = null

        /**
         * Singleton accessor to the active TranslationManager instance.
         * Gracefully bootstraps a standalone fallback instance if called in unit tests.
         */
        var instance: TranslationManager
            get() = _instance ?: TranslationManager(null).also {
                it.initialize()
                _instance = it
            }
            set(value) {
                _instance = value
            }

        fun get(key: String, vararg args: Pair<String, Any?>): String =
            instance.format(key, *args)

        fun render(key: String, vararg args: Pair<String, Any?>): Component =
            instance.render(key, *args)

        fun send(audience: Audience, key: String, vararg args: Pair<String, Any?>) =
            instance.send(audience, key, *args)

        fun sendActionBar(audience: Audience, key: String, vararg args: Pair<String, Any?>) =
            instance.sendActionBar(audience, key, *args)

        fun logInfo(key: String, vararg args: Pair<String, Any?>) =
            instance.logInfo(key, *args)

        fun logWarning(key: String, vararg args: Pair<String, Any?>) =
            instance.logWarning(key, *args)

        fun logError(key: String, vararg args: Pair<String, Any?>) =
            instance.logError(key, null, *args)

        fun logError(key: String, throwable: Throwable?, vararg args: Pair<String, Any?>) =
            instance.logError(key, throwable, *args)

        fun setLanguage(lang: String) = instance.setLanguage(lang)

        fun getCurrentLanguage(): String = instance.activeLanguage

        /**
         * Parses any text string into an Adventure [Component], supporting both MiniMessage tags
         * as primary and legacy color formatting codes (& / § / &#RRGGBB / &x&r...).
         */
        fun parseComponent(text: String): Component =
            instance.parseComponent(text)

        /**
         * Converts legacy formatting codes (&, §, &#RRGGBB, &x&r&r&g&g&b&b) to modern MiniMessage tags.
         */
        fun legacyToMiniMessage(input: String): String =
            instance.legacyToMiniMessage(input)
    }

    private val miniMessage = MiniMessage.miniMessage()
    private val lock = ReentrantReadWriteLock()

    var activeLanguage: String = "en"
        private set

    private var activeConfig: YamlConfiguration = YamlConfiguration()
    private var fallbackConfig: YamlConfiguration = YamlConfiguration()

    /**
     * Initializes the localization system, copies default config and language assets,
     * and loads configured language bundles.
     */
    fun initialize() {
        lock.write {
            val p = plugin
            val dataFolder = runCatching { p?.dataFolder }.getOrNull()
            if (dataFolder != null && p != null) {
                if (!dataFolder.exists()) {
                    dataFolder.mkdirs()
                }

                // Ensure config.yml exists
                val configFile = File(dataFolder, "config.yml")
                if (!configFile.exists()) {
                    runCatching { p.saveResource("config.yml", false) }
                }

                // Ensure lang directory and language files exist
                val langDir = File(dataFolder, "lang")
                if (!langDir.exists()) {
                    langDir.mkdirs()
                }

                val enFile = File(langDir, "en.yml")
                if (!enFile.exists()) {
                    runCatching { p.saveResource("lang/en.yml", false) }
                }

                val thFile = File(langDir, "th.yml")
                if (!thFile.exists()) {
                    runCatching { p.saveResource("lang/th.yml", false) }
                }

                // Read selected language from config.yml
                if (configFile.exists()) {
                    val cfg = YamlConfiguration.loadConfiguration(configFile)
                    activeLanguage = cfg.getString("language", "en")?.lowercase()?.trim() ?: "en"
                } else {
                    activeLanguage = "en"
                }
            } else {
                activeLanguage = "en"
            }

            reloadConfigs()
        }
    }

    /**
     * Reloads configuration and language bundles from disk or bundled JAR resources.
     */
    fun reload() {
        initialize()
    }

    /**
     * Dynamically switches the active translation language and reloads bundles.
     */
    fun setLanguage(lang: String) {
        lock.write {
            activeLanguage = lang.lowercase().trim()
            reloadConfigs()
        }
    }

    private fun reloadConfigs() {
        // 1. Load fallback English configuration
        fallbackConfig = loadBundle("en")

        // 2. Load active configured language configuration
        activeConfig = if (activeLanguage.equals("en", ignoreCase = true)) {
            fallbackConfig
        } else {
            loadBundle(activeLanguage)
        }
    }

    private fun loadBundle(langCode: String): YamlConfiguration {
        val resourcePath = "lang/$langCode.yml"
        val stream = plugin?.getResource(resourcePath)
            ?: javaClass.classLoader.getResourceAsStream(resourcePath)

        val config = if (stream != null) {
            stream.use { s ->
                InputStreamReader(s, StandardCharsets.UTF_8).use { reader ->
                    YamlConfiguration.loadConfiguration(reader)
                }
            }
        } else {
            YamlConfiguration()
        }

        val dataFolder = runCatching { plugin?.dataFolder }.getOrNull()
        val diskFile = if (dataFolder != null) File(File(dataFolder, "lang"), "$langCode.yml") else null

        if (diskFile != null && diskFile.exists()) {
            runCatching {
                val diskConfig = YamlConfiguration()
                diskConfig.load(diskFile)
                for (key in diskConfig.getKeys(true)) {
                    if (!diskConfig.isConfigurationSection(key)) {
                        config.set(key, diskConfig.get(key))
                    }
                }
            }
        }

        return config
    }

    /**
     * Retrieves the raw string representation for a key with fallback resolution.
     */
    fun getRaw(key: String): String = lock.read {
        val res = activeConfig.getString(key) ?: fallbackConfig.getString(key) ?: key
        return res
    }

    /**
     * Formats a localized string by replacing `{placeholder}` tokens with provided argument values.
     * Kyori [Component] parameters are automatically serialized to MiniMessage format.
     */
    fun format(key: String, vararg args: Pair<String, Any?>): String {
        var text = getRaw(key)
        for ((k, v) in args) {
            val formattedValue = when (v) {
                null -> ""
                is Component -> miniMessage.serialize(v)
                else -> v.toString()
            }
            text = text.replace("{$k}", formattedValue)
        }
        return text
    }

    /**
     * Converts legacy formatting codes (&, §, &#RRGGBB, &x&r&r&g&g&b&b) to modern MiniMessage tags.
     * Preserves existing MiniMessage tags while ensuring legacy codes are parsed identically.
     *
     * MiniMessage is the primary standard, but traditional & and § color codes (including hex)
     * are seamlessly translated.
     */
    fun legacyToMiniMessage(input: String): String {
        if (input.isEmpty()) return input

        var text = input

        // Handle escaped ampersands (&& -> literal &)
        val hasEscaped = text.contains("&&")
        if (hasEscaped) {
            text = text.replace("&&", "\u0000AMP\u0000")
        }

        // 1. Spigot/Bungee hex format: &x&r&r&g&g&b&b or §x§r§r§g§g§b§b
        if (text.contains("&x", ignoreCase = true) || text.contains("§x", ignoreCase = true)) {
            text = text.replace(Regex("(?i)[&§]x([&§][0-9a-f]){6}")) { match ->
                val hexChars = match.value.filter { it != '&' && it != '§' && it != 'x' && it != 'X' }
                "<#$hexChars>"
            }
        }

        // 2. Standard hex format: &#RRGGBB or §#RRGGBB
        if (text.contains("&#") || text.contains("§#") || text.contains("&#", ignoreCase = true)) {
            text = text.replace(Regex("(?i)[&§]#([0-9a-f]{6})")) { match ->
                "<#${match.groupValues[1]}>"
            }
        }

        // 3. Traditional color & formatting codes: &[0-9a-fk-orA-FK-OR] or §[0-9a-fk-orA-FK-OR]
        if (text.contains('&') || text.contains('§')) {
            text = text.replace(Regex("(?i)[&§]([0-9a-fk-or])")) { match ->
                when (match.groupValues[1].lowercase().first()) {
                    '0' -> "<black>"
                    '1' -> "<dark_blue>"
                    '2' -> "<dark_green>"
                    '3' -> "<dark_aqua>"
                    '4' -> "<dark_red>"
                    '5' -> "<dark_purple>"
                    '6' -> "<gold>"
                    '7' -> "<gray>"
                    '8' -> "<dark_gray>"
                    '9' -> "<blue>"
                    'a' -> "<green>"
                    'b' -> "<aqua>"
                    'c' -> "<red>"
                    'd' -> "<light_purple>"
                    'e' -> "<yellow>"
                    'f' -> "<white>"
                    'k' -> "<obfuscated>"
                    'l' -> "<bold>"
                    'm' -> "<strikethrough>"
                    'n' -> "<underlined>"
                    'o' -> "<italic>"
                    'r' -> "<reset>"
                    else -> match.value
                }
            }
        }

        if (hasEscaped) {
            text = text.replace("\u0000AMP\u0000", "&")
        }

        return text
    }

    /**
     * Parses any text string into an Adventure [Component], supporting both MiniMessage tags
     * as primary and legacy color formatting codes (& / § / &#RRGGBB / &x&r...).
     */
    fun parseComponent(text: String): Component {
        val normalized = legacyToMiniMessage(text)
        return miniMessage.deserialize(normalized)
    }

    /**
     * Resolves a localized template and deserializes it into an Adventure [Component].
     */
    fun render(key: String, vararg args: Pair<String, Any?>): Component {
        val formatted = format(key, *args)
        return parseComponent(formatted)
    }

    /**
     * Sends a localized message component to the target [Audience].
     */
    fun send(audience: Audience, key: String, vararg args: Pair<String, Any?>) {
        audience.sendMessage(render(key, *args))
    }

    /**
     * Sends a localized actionbar component to the target [Audience].
     */
    fun sendActionBar(audience: Audience, key: String, vararg args: Pair<String, Any?>) {
        audience.sendActionBar(render(key, *args))
    }

    /**
     * Sends an informational log message formatted with MiniMessage to the console.
     */
    fun logInfo(key: String, vararg args: Pair<String, Any?>) {
        val component = render(key, *args)
        runCatching {
            Bukkit.getConsoleSender().sendMessage(component)
        }.onFailure {
            println(format(key, *args))
        }
    }

    /**
     * Sends a warning log message formatted with MiniMessage to the console.
     */
    fun logWarning(key: String, vararg args: Pair<String, Any?>) {
        val component = render(key, *args)
        runCatching {
            Bukkit.getConsoleSender().sendMessage(component)
        }.onFailure {
            System.err.println(format(key, *args))
        }
    }

    /**
     * Sends an error log message formatted with MiniMessage to the console and optional Logger.
     */
    fun logError(key: String, vararg args: Pair<String, Any?>) {
        logError(key, null, *args)
    }

    /**
     * Sends an error log message formatted with MiniMessage to the console and optional Logger, including Throwable.
     */
    fun logError(key: String, throwable: Throwable?, vararg args: Pair<String, Any?>) {
        val component = render(key, *args)
        runCatching {
            Bukkit.getConsoleSender().sendMessage(component)
            if (throwable != null && plugin != null) {
                plugin.logger.log(java.util.logging.Level.SEVERE, format(key, *args), throwable)
            }
        }.onFailure {
            System.err.println(format(key, *args))
            throwable?.printStackTrace()
        }
    }
}
