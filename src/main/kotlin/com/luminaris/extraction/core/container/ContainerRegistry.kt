package com.luminaris.extraction.core.container

import net.kyori.adventure.text.minimessage.MiniMessage
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

            val section = yaml.getConfigurationSection("categories")
            if (section != null) {
                for (key in section.getKeys(false)) {
                    val catSec = section.getConfigurationSection(key) ?: continue

                    // Parse display name, supporting modern display_name or legacy name string
                    val rawDisplay = catSec.getString("display_name")
                        ?: catSec.getString("name")
                        ?: key
                    val displayName = if (rawDisplay.contains("<") && rawDisplay.contains(">")) {
                        miniMessage.deserialize(rawDisplay)
                    } else {
                        val colorHex = catSec.getString("color")
                        if (colorHex != null) {
                            miniMessage.deserialize("<color:$colorHex>$rawDisplay</color>")
                        } else {
                            miniMessage.deserialize("<yellow>$rawDisplay</yellow>")
                        }
                    }

                    val minRolls = catSec.getInt("min_rolls", 1)
                    val maxRolls = catSec.getInt("max_rolls", maxOf(minRolls, 3))

                    val category = ContainerCategory(
                        id = key.lowercase(),
                        displayName = displayName,
                        minRolls = minRolls,
                        maxRolls = maxRolls
                    )
                    categories[category.id] = category
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
            }

            yaml.save(file)
        }
    }
}
