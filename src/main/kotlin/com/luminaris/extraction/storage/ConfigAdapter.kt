package com.luminaris.extraction.storage

import com.luminaris.extraction.core.safe.SafeCaseDefinition
import com.luminaris.extraction.localization.TranslationManager
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Thread-safe abstract YAML configuration adapter for Folia environments.
 * Manages loading, reloading, persisting, and accessing custom YAML files in the plugin data folder.
 */
abstract class ConfigAdapter(
    val plugin: Plugin,
    val fileName: String
) {
    val file: File = File(plugin.dataFolder, fileName)
    private val lock = ReentrantReadWriteLock()
    
    @Volatile
    protected var yamlConfig: YamlConfiguration = YamlConfiguration()

    /**
     * Initializes the configuration: ensures directory and default resource existence, then loads.
     */
    open fun initialize(): ConfigAdapter {
        lock.write {
            if (!plugin.dataFolder.exists()) {
                plugin.dataFolder.mkdirs()
            }
            if (!file.exists()) {
                if (plugin.getResource(fileName) != null) {
                    plugin.saveResource(fileName, false)
                } else {
                    file.createNewFile()
                }
            }
            reloadInternal()
        }
        return this
    }

    /**
     * Reloads configuration from disk and invokes [onLoaded].
     */
    fun reload() {
        lock.write {
            reloadInternal()
        }
    }

    private fun reloadInternal() {
        yamlConfig = YamlConfiguration.loadConfiguration(file)
        
        // Merge defaults from JAR resource if available
        plugin.getResource(fileName)?.use { stream ->
            InputStreamReader(stream, StandardCharsets.UTF_8).use { reader ->
                val defConfig = YamlConfiguration.loadConfiguration(reader)
                yamlConfig.setDefaults(defConfig)
            }
        }

        onLoaded(yamlConfig)
    }

    /**
     * Persists current in-memory configuration to disk.
     */
    fun save() {
        lock.write {
            yamlConfig.save(file)
        }
    }

    /**
     * Lifecycle callback invoked whenever the configuration is loaded or reloaded.
     */
    protected abstract fun onLoaded(config: YamlConfiguration)

    // --- Thread-safe Accessors ---

    fun getString(path: String, def: String? = null): String? = lock.read {
        yamlConfig.getString(path, def)
    }

    fun getInt(path: String, def: Int = 0): Int = lock.read {
        yamlConfig.getInt(path, def)
    }

    fun getLong(path: String, def: Long = 0L): Long = lock.read {
        yamlConfig.getLong(path, def)
    }

    fun getDouble(path: String, def: Double = 0.0): Double = lock.read {
        yamlConfig.getDouble(path, def)
    }

    fun getBoolean(path: String, def: Boolean = false): Boolean = lock.read {
        yamlConfig.getBoolean(path, def)
    }

    fun getStringList(path: String): List<String> = lock.read {
        yamlConfig.getStringList(path)
    }

    fun getSection(path: String): ConfigurationSection? = lock.read {
        yamlConfig.getConfigurationSection(path)
    }

    fun contains(path: String): Boolean = lock.read {
        yamlConfig.contains(path)
    }

    fun set(path: String, value: Any?) {
        lock.write {
            yamlConfig.set(path, value)
        }
    }
}

/**
 * Concrete adapter for items.yml containing extraction item registries and loot definitions.
 */
class ItemsConfigAdapter(plugin: Plugin) : ConfigAdapter(plugin, "items.yml") {
    data class ExtractionItemDefinition(
        val id: String,
        val material: String,
        val displayName: String,
        val tier: String,
        val extractionValue: Double,
        val lore: List<String>
    )

    private val itemCache = mutableMapOf<String, ExtractionItemDefinition>()

    override fun onLoaded(config: YamlConfiguration) {
        itemCache.clear()
        val section = config.getConfigurationSection("items") ?: return
        for (key in section.getKeys(false)) {
            val itemSec = section.getConfigurationSection(key) ?: continue
            val item = ExtractionItemDefinition(
                id = key,
                material = itemSec.getString("material", "IRON_INGOT")!!,
                displayName = itemSec.getString("display_name", key)!!,
                tier = itemSec.getString("tier", "COMMON")!!,
                extractionValue = itemSec.getDouble("value", 10.0),
                lore = itemSec.getStringList("lore")
            )
            itemCache[key] = item
        }
    }

    fun getItem(id: String): ExtractionItemDefinition? = itemCache[id]
    fun getAllItems(): Map<String, ExtractionItemDefinition> = itemCache.toMap()
}

/**
 * Concrete adapter for categories.yml defining extraction tier classifications and rules.
 */
class CategoriesConfigAdapter(plugin: Plugin) : ConfigAdapter(plugin, "categories.yml") {
    data class CategoryDefinition(
        val name: String,
        val colorHex: String,
        val multiplier: Double
    )

    private val categoryCache = mutableMapOf<String, CategoryDefinition>()

    override fun onLoaded(config: YamlConfiguration) {
        categoryCache.clear()
        val section = config.getConfigurationSection("categories") ?: return
        for (key in section.getKeys(false)) {
            val catSec = section.getConfigurationSection(key) ?: continue
            categoryCache[key] = CategoryDefinition(
                name = catSec.getString("name", key)!!,
                colorHex = catSec.getString("color", "#FFFFFF")!!,
                multiplier = catSec.getDouble("multiplier", 1.0)
            )
        }
    }

    fun getCategory(key: String): CategoryDefinition? = categoryCache[key]
    fun getAllCategories(): Map<String, CategoryDefinition> = categoryCache.toMap()
}

/**
 * Concrete adapter for safe_cases.yml managing secure extraction containers and safe containers.
 */
class SafeCasesConfigAdapter(plugin: Plugin) : ConfigAdapter(plugin, "safe_cases.yml") {
    private val miniMessage = MiniMessage.miniMessage()
    private val caseCache = mutableMapOf<String, SafeCaseDefinition>()

    override fun onLoaded(config: YamlConfiguration) {
        caseCache.clear()
        val section = config.getConfigurationSection("safe_cases") ?: return
        for (key in section.getKeys(false)) {
            val caseSec = section.getConfigurationSection(key) ?: continue

            val rawDisplay = caseSec.getString("display_name")
                ?: caseSec.getString("title")
                ?: "<gold>$key</gold>"
            val displayName = TranslationManager.parseComponent(rawDisplay)

            val matName = caseSec.getString("material", "BUNDLE")!!
            val material = Material.matchMaterial(matName) ?: Material.BUNDLE

            val cmd = if (caseSec.contains("custom_model_data")) {
                caseSec.getInt("custom_model_data")
            } else if (caseSec.contains("customModelData")) {
                caseSec.getInt("customModelData")
            } else {
                null
            }

            val capacity = caseSec.getInt("capacity", 9)
            val keepsOnDeath = caseSec.getBoolean("keeps_on_death", true)

            caseCache[key.lowercase()] = SafeCaseDefinition(
                id = key.lowercase(),
                displayName = displayName,
                material = material,
                customModelData = cmd,
                capacity = capacity,
                keepsOnDeath = keepsOnDeath
            )
        }
    }

    fun getCase(id: String): SafeCaseDefinition? = caseCache[id.lowercase()]
    fun getAllCases(): Map<String, SafeCaseDefinition> = caseCache.toMap()
}
