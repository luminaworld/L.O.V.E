package com.luminaris.extraction.core.item

import com.luminaris.extraction.localization.TranslationManager
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

/**
 * Thread-safe registry service for extraction loot items.
 * Handles serialization/deserialization to items.yml while preserving 100% of
 * CustomModelData, Lore, DisplayName, and Minecraft Data Components.
 */
class ItemRegistry(private val plugin: Plugin) {

    private val miniMessage = MiniMessage.miniMessage()
    private val file = File(plugin.dataFolder, "items.yml")
    private val lock = ReentrantReadWriteLock()
    private val items = ConcurrentHashMap<String, ExtractionItem>()

    /**
     * Registers an extraction item in memory and optionally persists to disk.
     */
    fun register(item: ExtractionItem, saveToDisk: Boolean = true) {
        items[item.id.lowercase()] = item
        if (saveToDisk) {
            save()
        }
    }

    /**
     * Registers an extraction item directly from a player's main hand item.
     * Preserves all CustomModelData, Lore, DisplayName, and Data Components.
     *
     * @throws IllegalArgumentException if the held item is air or empty.
     */
    fun registerFromHand(
        player: Player,
        id: String,
        price: Double,
        rarity: Rarity,
        weight: Double,
        allowedContainers: MutableSet<String>,
        customDisplayName: String? = null,
        searchTime: Int = ExtractionItem.defaultSearchTime(rarity),
        revealSound: String? = null,
        saveToDisk: Boolean = true
    ): ExtractionItem {
        val held = player.inventory.itemInMainHand
        require(!held.isEmpty && !held.type.isAir) { "Cannot register empty hand item" }

        val cloned = held.clone()
        if (!customDisplayName.isNullOrBlank()) {
            cloned.editMeta { meta ->
                meta.displayName(TranslationManager.parseComponent(customDisplayName))
            }
        }

        val item = ExtractionItem(
            id = id.lowercase(),
            itemStack = cloned,
            price = price,
            rarity = rarity,
            weight = weight,
            allowedContainers = allowedContainers,
            searchTime = searchTime,
            revealSound = revealSound
        )

        register(item, saveToDisk)
        return item
    }

    /**
     * Unregisters an extraction item by id.
     */
    fun unregister(id: String, saveToDisk: Boolean = true): ExtractionItem? {
        val removed = items.remove(id.lowercase())
        if (removed != null && saveToDisk) {
            save()
        }
        return removed
    }

    /**
     * Gets an extraction item by unique id.
     */
    fun getItem(id: String): ExtractionItem? = items[id.lowercase()]

    /**
     * Checks if an item is registered.
     */
    fun hasItem(id: String): Boolean = items.containsKey(id.lowercase())

    /**
     * Returns an unmodifiable snapshot of all registered extraction items.
     */
    fun getAllItems(): List<ExtractionItem> = items.values.toList()

    /**
     * Retrieves all items permitted inside a given container category.
     */
    fun getItemsForCategory(categoryId: String): List<ExtractionItem> {
        val catLower = categoryId.lowercase()
        return items.values.filter { it.allowedContainers.isEmpty() || it.allowedContainers.contains(catLower) }
    }

    /**
     * Loads or reloads extraction items from items.yml.
     */
    fun load() {
        lock.write {
            if (!plugin.dataFolder.exists()) {
                plugin.dataFolder.mkdirs()
            }
            if (!file.exists()) {
                if (plugin.getResource("items.yml") != null) {
                    plugin.saveResource("items.yml", false)
                } else {
                    file.createNewFile()
                }
            }

            val yaml = YamlConfiguration.loadConfiguration(file)
            items.clear()

            val section = yaml.getConfigurationSection("items") ?: return
            for (key in section.getKeys(false)) {
                val itemSec = section.getConfigurationSection(key) ?: continue

                // 1. Resolve ItemStack: native serialization takes absolute precedence to preserve components
                val loadedStack: ItemStack? = itemSec.getItemStack("item_stack") ?: itemSec.getItemStack("item")
                val itemStack: ItemStack = if (loadedStack != null) {
                    loadedStack
                } else {
                    // Fallback reconstruction for template / manually authored YAML entries
                    val matName = itemSec.getString("material", "IRON_INGOT")!!
                    val mat = Material.matchMaterial(matName) ?: Material.IRON_INGOT
                    val stack = ItemStack(mat)
                    val rawDisplay = itemSec.getString("display_name")
                    val rawLore = itemSec.getStringList("lore")

                    stack.editMeta { meta ->
                        if (!rawDisplay.isNullOrBlank()) {
                            meta.displayName(TranslationManager.parseComponent(rawDisplay))
                        }
                        if (rawLore.isNotEmpty()) {
                            meta.lore(rawLore.map { TranslationManager.parseComponent(it) })
                        }
                    }
                    stack
                }

                // 2. Resolve price / extraction value
                val price = if (itemSec.contains("price")) {
                    itemSec.getDouble("price")
                } else {
                    itemSec.getDouble("value", 10.0)
                }

                // 3. Resolve rarity / tier
                val rawRarity = itemSec.getString("rarity") ?: itemSec.getString("tier")
                val rarity = Rarity.fromString(rawRarity)

                // 4. Resolve drop weight
                val weight = itemSec.getDouble("weight", 10.0)

                // 5. Resolve allowed container categories
                val allowedContainers = itemSec.getStringList("allowed_containers")
                    .map { it.lowercase() }
                    .toMutableSet()

                // 6. Resolve search duration (seconds)
                val searchTime = if (itemSec.contains("search_time")) {
                    itemSec.getInt("search_time")
                } else if (itemSec.contains("search_duration")) {
                    itemSec.getInt("search_duration")
                } else {
                    ExtractionItem.defaultSearchTime(rarity)
                }

                // 7. Resolve reveal sound
                val revealSound = itemSec.getString("reveal_sound") ?: itemSec.getString("sound")

                val item = ExtractionItem(
                    id = key.lowercase(),
                    itemStack = itemStack,
                    price = price,
                    rarity = rarity,
                    weight = weight,
                    allowedContainers = allowedContainers,
                    searchTime = searchTime,
                    revealSound = revealSound
                )

                items[item.id] = item
            }
        }
    }

    /**
     * Persists all registered items to items.yml.
     * Stores native Bukkit ItemStack serialization to preserve all components,
     * while also generating human-readable metadata properties.
     */
    fun save() {
        lock.write {
            val yaml = YamlConfiguration()
            val section = yaml.createSection("items")

            for ((id, item) in items) {
                val itemSec = section.createSection(id)
                // Serializes full ItemStack including Data Components, CustomModelData, Enchantments, Lore, etc.
                itemSec.set("item_stack", item.itemStack.clone())
                itemSec.set("price", item.price)
                itemSec.set("rarity", item.rarity.name)
                itemSec.set("weight", item.weight)
                itemSec.set("allowed_containers", item.allowedContainers.toList())
                itemSec.set("search_time", item.searchTime)
                if (item.revealSound != null) {
                    itemSec.set("reveal_sound", item.revealSound)
                }

                // Readable convenience fields for server administrators viewing items.yml
                itemSec.set("material", item.itemStack.type.name)
                if (item.itemStack.hasItemMeta()) {
                    val meta = item.itemStack.itemMeta
                    if (meta.hasDisplayName()) {
                        itemSec.set("display_name", miniMessage.serialize(meta.displayName()!!))
                    }
                    if (meta.hasLore()) {
                        itemSec.set("lore", meta.lore()?.map { miniMessage.serialize(it) })
                    }
                }
            }

            yaml.save(file)
        }
    }
}
