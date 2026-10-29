package com.luminaris.extraction.core.container

import com.luminaris.extraction.localization.TranslationManager
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Thread-safe registry service for managing container categories and persisting to categories.yml.
 */
class ContainerRegistry(private val plugin: Plugin) {

    private val miniMessage = MiniMessage.miniMessage()
    private val file = File(plugin.dataFolder, "categories.yml")
    private val lock = ReentrantReadWriteLock()
    private val categories = ConcurrentHashMap<String, ContainerCategory>()

    /**
     * Registers a category in memory and optionally writes changes to disk.
     */
    fun register(category: ContainerCategory, saveToDisk: Boolean = true) {
        categories[category.id.lowercase()] = category
        if (saveToDisk) {
            save()
        }
    }

    /**
     * Removes a category from the registry.
     */
    fun unregister(id: String, saveToDisk: Boolean = true): ContainerCategory? {
        val removed = categories.remove(id.lowercase())
        if (removed != null && saveToDisk) {
            save()
        }
        return removed
    }

    /**
     * Retrieves a container category by id.
     */
    fun getCategory(id: String): ContainerCategory? = categories[id.lowercase()]

    /**
     * Checks if a category with the given id is registered.
     */
    fun hasCategory(id: String): Boolean = categories.containsKey(id.lowercase())

    /**
     * Returns an unmodifiable snapshot list of all registered container categories.
     */
    fun getAllCategories(): List<ContainerCategory> = categories.values.toList()

    /**
     * Loads or reloads container categories from categories.yml.
     */
    fun load() {
        lock.write {
            if (!plugin.dataFolder.exists()) {
                plugin.dataFolder.mkdirs()
            }
            if (!file.exists()) {
                if (plugin.getResource("categories.yml") != null) {
                    plugin.saveResource("categories.yml", false)
                } else {
                    file.createNewFile()
                }
            }

            val yaml = YamlConfiguration.loadConfiguration(file)
            categories.clear()
            var needsSave = false

            // Check if embedded default categories has any category missing in disk categories.yml
            plugin.getResource("categories.yml")?.use { stream ->
                InputStreamReader(stream, StandardCharsets.UTF_8).use { reader ->
                    val defaultYaml = YamlConfiguration.loadConfiguration(reader)
                    val defaultSec = defaultYaml.getConfigurationSection("categories")
                    val currentSec = yaml.getConfigurationSection("categories") ?: yaml.createSection("categories")
                    if (defaultSec != null) {
                        for (defKey in defaultSec.getKeys(false)) {
                            if (!currentSec.contains(defKey)) {
                                val defCatSec = defaultSec.getConfigurationSection(defKey)
                                if (defCatSec != null) {
                                    currentSec.set(defKey, defCatSec)
                                    needsSave = true
                                }
                            }
                        }
                    }
                }
            }

            val section = yaml.getConfigurationSection("categories")
            if (section != null) {
                for (key in section.getKeys(false)) {
                    val catSec = section.getConfigurationSection(key) ?: continue

                    // Parse display name, supporting modern display_name or legacy name string
                    val rawDisplay = catSec.getString("display_name")
                        ?: catSec.getString("name")
                        ?: key
                    val displayName = if ((rawDisplay.contains("<") && rawDisplay.contains(">")) || rawDisplay.contains("&") || rawDisplay.contains("§")) {
                        TranslationManager.parseComponent(rawDisplay)
                    } else {
                        val colorHex = catSec.getString("color")
                        if (colorHex != null) {
                            TranslationManager.parseComponent("<color:$colorHex>$rawDisplay</color>")
                        } else {
                            TranslationManager.parseComponent("<yellow>$rawDisplay</yellow>")
                        }
                    }

                    val minRolls = catSec.getInt("min_rolls", 1)
                    val maxRolls = catSec.getInt("max_rolls", maxOf(minRolls, 3))

                    val defaultSearchMin = runCatching { plugin.config.getInt("drop_box.default_search_time_min", 2) }.getOrDefault(2)
                    val defaultSearchMax = runCatching { plugin.config.getInt("drop_box.default_search_time_max", 4) }.getOrDefault(4)
                    val defaultCovered = runCatching { plugin.config.getString("drop_box.default_covered_material", "BLACK_STAINED_GLASS_PANE") }.getOrNull() ?: "BLACK_STAINED_GLASS_PANE"
                    val defaultSearching = runCatching { plugin.config.getString("drop_box.default_searching_material", "STONE") }.getOrNull() ?: "STONE"
                    val defaultSearchSound = runCatching { plugin.config.getString("drop_box.default_search_sound") }.getOrNull()
                    val defaultRevealSound = runCatching { plugin.config.getString("drop_box.default_reveal_sound") }.getOrNull()

                    val searchTimeMin = catSec.getInt("search_time_min", defaultSearchMin)
                    val searchTimeMax = catSec.getInt("search_time_max", maxOf(searchTimeMin, defaultSearchMax))

                    val rawCovered = catSec.getString("covered_material", defaultCovered) ?: defaultCovered
                    val coveredMaterial = Material.matchMaterial(rawCovered) ?: Material.matchMaterial(defaultCovered) ?: Material.BLACK_STAINED_GLASS_PANE

                    val rawSearching = catSec.getString("searching_material", defaultSearching) ?: defaultSearching
                    val searchingMaterial = Material.matchMaterial(rawSearching) ?: Material.matchMaterial(defaultSearching) ?: Material.STONE

                    val searchSound = catSec.getString("search_sound", defaultSearchSound) ?: defaultSearchSound
                    val revealSound = catSec.getString("reveal_sound", defaultRevealSound) ?: defaultRevealSound

                    val isDefaultSafe = key.equals("safe_box", ignoreCase = true) || key.contains("safe", ignoreCase = true)

                    // Auto-fill missing timed unlock keys in disk file
                    if (!catSec.contains("timed_unlock_enabled") && !catSec.contains("timed_unlock")) {
                        catSec.set("timed_unlock_enabled", isDefaultSafe)
                        needsSave = true
                    }
                    if (!catSec.contains("unlock_countdown_seconds") && !catSec.contains("unlock_seconds")) {
                        catSec.set("unlock_countdown_seconds", if (isDefaultSafe) 30 else 0)
                        needsSave = true
                    }
                    if (!catSec.contains("unlock_tick_sound")) {
                        catSec.set("unlock_tick_sound", "BLOCK_NOTE_BLOCK_HAT")
                        needsSave = true
                    }
                    if (!catSec.contains("unlock_finish_sound")) {
                        catSec.set("unlock_finish_sound", "BLOCK_IRON_DOOR_OPEN")
                        needsSave = true
                    }

                    val timedUnlockEnabled = catSec.getBoolean("timed_unlock_enabled", catSec.getBoolean("timed_unlock", isDefaultSafe))
                    val defaultSeconds = if (isDefaultSafe) 30 else 0
                    val unlockCountdownSeconds = catSec.getInt("unlock_countdown_seconds", catSec.getInt("unlock_seconds", defaultSeconds))
                    val unlockTickSound = catSec.getString("unlock_tick_sound", "BLOCK_NOTE_BLOCK_HAT")
                    val unlockFinishSound = catSec.getString("unlock_finish_sound", "BLOCK_IRON_DOOR_OPEN")

                    val rarityWeights = ContainerCategory.defaultRarityWeights(key)
                    val raritySection = catSec.getConfigurationSection("rarity_chances")
                        ?: catSec.getConfigurationSection("rarity_weights")
                    if (raritySection != null) {
                        for (rarity in com.luminaris.extraction.core.item.Rarity.entries) {
                            if (raritySection.contains(rarity.name)) {
                                rarityWeights[rarity] = raritySection.getDouble(rarity.name)
                            }
                        }
                    }

                    val category = ContainerCategory(
                        id = key.lowercase(),
                        displayName = displayName,
                        minRolls = minRolls,
                        maxRolls = maxRolls,
                        rarityWeights = rarityWeights,
                        searchTimeMin = searchTimeMin,
                        searchTimeMax = searchTimeMax,
                        coveredMaterial = coveredMaterial,
                        searchingMaterial = searchingMaterial,
                        searchSound = searchSound,
                        revealSound = revealSound,
                        timedUnlockEnabled = timedUnlockEnabled,
                        unlockCountdownSeconds = unlockCountdownSeconds,
                        unlockTickSound = unlockTickSound,
                        unlockFinishSound = unlockFinishSound
                    )
                    categories[category.id] = category
                }
            }

            if (needsSave) {
                runCatching {
                    yaml.save(file)
                    plugin.logger.info("[LuminaExtractionCore] Automatically updated categories.yml with missing configuration keys.")
                }
            }
        }
    }

    /**
     * Persists all registered container categories to categories.yml.
     */
    fun save() {
        lock.write {
            val yaml = YamlConfiguration()
            val categoriesSec = yaml.createSection("categories")

            for ((id, category) in categories) {
                val catSec = categoriesSec.createSection(id)
                catSec.set("display_name", miniMessage.serialize(category.displayName))
                catSec.set("min_rolls", category.minRolls)
                catSec.set("max_rolls", category.maxRolls)
                catSec.set("search_time_min", category.searchTimeMin)
                catSec.set("search_time_max", category.searchTimeMax)
                catSec.set("covered_material", category.coveredMaterial.name)
                catSec.set("searching_material", category.searchingMaterial.name)
                if (category.searchSound != null) {
                    catSec.set("search_sound", category.searchSound)
                }
                if (category.revealSound != null) {
                    catSec.set("reveal_sound", category.revealSound)
                }
                catSec.set("timed_unlock_enabled", category.timedUnlockEnabled)
                catSec.set("unlock_countdown_seconds", category.unlockCountdownSeconds)
                if (category.unlockTickSound != null) {
                    catSec.set("unlock_tick_sound", category.unlockTickSound)
                }
                if (category.unlockFinishSound != null) {
                    catSec.set("unlock_finish_sound", category.unlockFinishSound)
                }
                val raritySec = catSec.createSection("rarity_chances")
                for ((rarity, weight) in category.rarityWeights) {
                    raritySec.set(rarity.name, weight)
                }
            }

            yaml.save(file)
        }
    }
}
