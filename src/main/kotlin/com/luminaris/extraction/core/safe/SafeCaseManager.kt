package com.luminaris.extraction.core.safe

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.util.ItemStackSerializer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

/**
 * Core manager for Handheld Safe Containers (Pouches) in the L.O.V.E framework.
 * Handles PersistentDataContainer (PDC) lifecycle, Base64 item array serialization,
 * Folia thread-safe regional inventory interactions, and definition loading.
 */
class SafeCaseManager(private val plugin: LuminarisCorePlugin) {

    companion object {
        val KEY_IS_SAFE_CASE = NamespacedKey("luminaris", "is_safe_case")
        val KEY_SAFE_ID = NamespacedKey("luminaris", "safe_id")
        val KEY_SAFE_CAPACITY = NamespacedKey("luminaris", "safe_capacity")
        val KEY_SAFE_UUID = NamespacedKey("luminaris", "safe_uuid")
        val KEY_SERIALIZED_CONTENTS = NamespacedKey("luminaris", "serialized_contents")
        val KEY_LOCKED_SLOT = NamespacedKey("luminaris", "locked_slot")

        /**
         * Serializes an array of [ItemStack] to Base64.
         */
        fun itemStackArrayToBase64(items: Array<out ItemStack?>): String {
            return ItemStackSerializer.itemStackArrayToBase64(items)
        }

        /**
         * Deserializes an array of [ItemStack] from Base64.
         */
        fun itemStackArrayFromBase64(data: String): Array<ItemStack?> {
            return ItemStackSerializer.itemStackArrayFromBase64(data)
        }

        /**
         * Ephemeral registry caching preserved Safe Case items across player deaths until respawn.
         * Maps player [UUID] to a list of preserved [ItemStack] instances.
         */
        val pendingReturns: ConcurrentHashMap<UUID, MutableList<ItemStack>> = ConcurrentHashMap()
    }

    /**
     * Ephemeral registry caching preserved Safe Case items across player deaths until respawn.
     */
    val pendingReturns: ConcurrentHashMap<UUID, MutableList<ItemStack>>
        get() = Companion.pendingReturns

    private val miniMessage = MiniMessage.miniMessage()
    private val definitions = ConcurrentHashMap<String, SafeCaseDefinition>()
    private val lock = ReentrantReadWriteLock()
    private val configFile = File(plugin.dataFolder, "safe_cases.yml")

    /**
     * Serializes an array of [ItemStack] to a Base64 string.
     */
    fun itemStackArrayToBase64(items: Array<out ItemStack?>): String {
        return Companion.itemStackArrayToBase64(items)
    }

    /**
     * Deserializes a Base64 string to an array of [ItemStack].
     */
    fun itemStackArrayFromBase64(data: String): Array<ItemStack?> {
        return Companion.itemStackArrayFromBase64(data)
    }

    /**
     * Loads or reloads all safe case definitions from `safe_cases.yml`.
     */
    fun load() {
        lock.write {
            if (!plugin.dataFolder.exists()) {
                plugin.dataFolder.mkdirs()
            }
            if (!configFile.exists()) {
                if (plugin.getResource("safe_cases.yml") != null) {
                    plugin.saveResource("safe_cases.yml", false)
                } else {
                    configFile.createNewFile()
                }
            }

            val yaml = YamlConfiguration.loadConfiguration(configFile)
            definitions.clear()

            val section = yaml.getConfigurationSection("safe_cases") ?: return
            for (key in section.getKeys(false)) {
                val caseSec = section.getConfigurationSection(key) ?: continue

                val rawDisplay = caseSec.getString("display_name")
                    ?: caseSec.getString("title")
                    ?: "<gold>$key</gold>"
                val displayName = miniMessage.deserialize(rawDisplay)

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

                val def = SafeCaseDefinition(
                    id = key.lowercase(),
                    displayName = displayName,
                    material = material,
                    customModelData = cmd,
                    capacity = capacity,
                    keepsOnDeath = keepsOnDeath
                )
                definitions[def.id] = def
            }
        }
    }

    /**
     * Registers a [SafeCaseDefinition] programmatically.
     */
    fun registerDefinition(def: SafeCaseDefinition) {
        definitions[def.id.lowercase()] = def
    }

    /**
     * Retrieves a loaded [SafeCaseDefinition] by its case ID.
     */
    fun getDefinition(id: String): SafeCaseDefinition? = definitions[id.lowercase()]

    /**
     * Returns an unmodifiable snapshot of all registered safe case definitions.
     */
    fun getAllDefinitions(): List<SafeCaseDefinition> = definitions.values.toList()

    /**
     * Checks if the given [ItemStack] is a valid Safe Case.
     */
    fun isSafeCase(item: ItemStack?): Boolean {
        if (item == null || item.isEmpty || !item.hasItemMeta()) return false
        val pdc = item.itemMeta.persistentDataContainer
        return pdc.get(KEY_IS_SAFE_CASE, PersistentDataType.BOOLEAN) == true
    }

    /**
     * Checks if the given [ItemStack] is a placeholder locked slot filler item.
     */
    fun isLockedSlot(item: ItemStack?): Boolean {
        if (item == null || item.isEmpty || !item.hasItemMeta()) return false
        val pdc = item.itemMeta.persistentDataContainer
        return pdc.get(KEY_LOCKED_SLOT, PersistentDataType.BOOLEAN) == true
    }

    /**
     * Retrieves the instance UUID of a Safe Case item, or null if not present.
     */
    fun getSafeUuid(item: ItemStack?): UUID? {
        if (item == null || item.isEmpty || !item.hasItemMeta()) return null
        val uuidStr = item.itemMeta.persistentDataContainer.get(KEY_SAFE_UUID, PersistentDataType.STRING) ?: return null
        return runCatching { UUID.fromString(uuidStr) }.getOrNull()
    }

    /**
     * Retrieves the registered profile ID of a Safe Case item, or null if not present.
     */
    fun getSafeId(item: ItemStack?): String? {
        if (item == null || item.isEmpty || !item.hasItemMeta()) return null
        return item.itemMeta.persistentDataContainer.get(KEY_SAFE_ID, PersistentDataType.STRING)
    }

    /**
     * Computes the nearest valid Bukkit Chest inventory size (multiple of 9, between 9 and 54)
     * required to display [capacity] slots.
     */
    fun getMatchingChestSize(capacity: Int): Int {
        val rows = (capacity + 8) / 9
        val size = rows * 9
        return size.coerceIn(9, 54)
    }

    /**
     * Creates a decorative, non-interactable item indicating a locked slot beyond the container's capacity.
     */
    fun createLockedSlotItem(): ItemStack {
        val item = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        item.editMeta { meta ->
            meta.displayName(miniMessage.deserialize("<dark_gray>▪ <gray>Locked Container Slot</gray></dark_gray>"))
            meta.lore(
                listOf(
                    miniMessage.deserialize("<dark_gray>Slot exceeds this container's capacity.</dark_gray>"),
                    miniMessage.deserialize("<dark_gray>Upgrade to a larger Safe Case to unlock.</dark_gray>")
                )
            )
            meta.persistentDataContainer.set(KEY_LOCKED_SLOT, PersistentDataType.BOOLEAN, true)
        }
        return item
    }

    /**
     * Constructs a physical [ItemStack] representing the provided [SafeCaseDefinition]
     * with all PersistentDataContainer markers, custom model data, and metadata applied.
     */
    fun createSafeItemStack(def: SafeCaseDefinition): ItemStack {
        val item = ItemStack(def.material)
        item.editMeta { meta ->
            meta.displayName(def.displayName)
            if (def.customModelData != null) {
                meta.setCustomModelData(def.customModelData)
            }

            val loreLines = listOf(
                miniMessage.deserialize("<dark_gray>▪ <gray>Capacity: <yellow>${def.capacity} slots</yellow></gray></dark_gray>"),
                miniMessage.deserialize("<dark_gray>▪ <aqua>L.O.V.E Secure Container</aqua></dark_gray>"),
                if (def.keepsOnDeath) {
                    miniMessage.deserialize("<dark_gray>▪ <green>Kept on extraction & death</green></dark_gray>")
                } else {
                    miniMessage.deserialize("<dark_gray>▪ <red>Lost on death</red></dark_gray>")
                },
                miniMessage.deserialize("<dark_gray>------------------------</dark_gray>"),
                miniMessage.deserialize("<yellow>Right-click to access storage</yellow>")
            )
            meta.lore(loreLines)

            val pdc = meta.persistentDataContainer
            pdc.set(KEY_IS_SAFE_CASE, PersistentDataType.BOOLEAN, true)
            pdc.set(KEY_SAFE_ID, PersistentDataType.STRING, def.id)
            pdc.set(KEY_SAFE_CAPACITY, PersistentDataType.INTEGER, def.capacity)
            pdc.set(KEY_SAFE_UUID, PersistentDataType.STRING, UUID.randomUUID().toString())
            pdc.set(KEY_SERIALIZED_CONTENTS, PersistentDataType.STRING, "")
        }
        return item
    }

    /**
     * Constructs a physical [ItemStack] for the safe case with the given ID.
     */
    fun createSafeItemStack(safeId: String): ItemStack? {
        val def = getDefinition(safeId) ?: return null
        return createSafeItemStack(def)
    }

    /**
     * Opens the virtual safe inventory GUI for the specified player.
     * Guaranteed to execute within the Player's entity scheduler context for Folia multi-threading safety.
     *
     * @param player The player opening the safe container.
     * @param safeItem The safe case item stack held by the player.
     */
    fun openSafeInventory(player: Player, safeItem: ItemStack) {
        if (!isSafeCase(safeItem)) return

        // Folia Thread-Safety: Guarantee execution inside Player's entity scheduler context
        plugin.scheduler.runAtEntity(player, runnable = {
            if (!player.isOnline || player.isDead) return@runAtEntity

            val meta = safeItem.itemMeta ?: return@runAtEntity
            val pdc = meta.persistentDataContainer

            // Ensure unique instance UUID exists
            val uuidStr = pdc.get(KEY_SAFE_UUID, PersistentDataType.STRING)
            val safeUuid = if (uuidStr != null) {
                runCatching { UUID.fromString(uuidStr) }.getOrNull() ?: UUID.randomUUID()
            } else {
                val newUuid = UUID.randomUUID()
                pdc.set(KEY_SAFE_UUID, PersistentDataType.STRING, newUuid.toString())
                safeItem.itemMeta = meta
                newUuid
            }

            // Determine capacity & matching chest GUI size
            val capacity = pdc.get(KEY_SAFE_CAPACITY, PersistentDataType.INTEGER) ?: 9
            val chestSize = getMatchingChestSize(capacity)
            val title = meta.displayName() ?: Component.text("Safe Container")

            // Instantiate custom InventoryHolder
            val holder = SafeCaseHolder(safeUuid = safeUuid, sourceItem = safeItem, capacity = capacity)
            val virtualInv = Bukkit.createInventory(holder, chestSize, title)
            holder.bindInventory(virtualInv)

            // Deserialize and populate stored items
            val serializedContents = pdc.get(KEY_SERIALIZED_CONTENTS, PersistentDataType.STRING)
            if (!serializedContents.isNullOrBlank()) {
                val loadedItems = itemStackArrayFromBase64(serializedContents)
                for (i in 0 until minOf(loadedItems.size, capacity)) {
                    val item = loadedItems[i]
                    if (item != null && !item.isEmpty && !item.type.isAir) {
                        virtualInv.setItem(i, item)
                    }
                }
            }

            // Lock unusable slots if capacity is less than the chest multiple of 9
            if (capacity < chestSize) {
                val lockedPane = createLockedSlotItem()
                for (slot in capacity until chestSize) {
                    virtualInv.setItem(slot, lockedPane)
                }
            }

            // Display virtual inventory GUI
            player.openInventory(virtualInv)
        })
    }

    /**
     * Serializes stored non-empty items to Base64 and updates the PDC on the physical item
     * located in the player's inventory by its `safe_uuid`.
     * Guaranteed to execute within the Player's entity scheduler context for Folia multi-threading safety.
     *
     * @param player The player whose safe case is being saved.
     * @param holder The SafeCaseHolder associated with the closed inventory session.
     * @param inventory The inventory that is being closed and persisted.
     */
    fun saveSafeInventory(player: Player, holder: SafeCaseHolder, inventory: Inventory) {
        // Capture snapshot preserving exact slot indices across the pouch capacity
        val itemsArray = arrayOfNulls<ItemStack>(holder.capacity)
        for (slot in 0 until holder.capacity) {
            val item = inventory.getItem(slot)
            if (item != null && !item.isEmpty && !item.type.isAir && !isLockedSlot(item)) {
                itemsArray[slot] = item.clone()
            }
        }

        val serializedBase64 = if (itemsArray.any { it != null }) {
            itemStackArrayToBase64(itemsArray)
        } else {
            ""
        }

        val applySave = {
            val uuidStr = holder.safeUuid.toString()
            var updated = false

            // Search player main inventory & hotbar (slots 0..35)
            for (slot in 0 until player.inventory.size) {
                val item = player.inventory.getItem(slot) ?: continue
                if (isSafeCase(item)) {
                    val itemUuid = item.itemMeta?.persistentDataContainer?.get(KEY_SAFE_UUID, PersistentDataType.STRING)
                    if (itemUuid == uuidStr) {
                        applySerializedContents(item, serializedBase64)
                        player.inventory.setItem(slot, item)
                        updated = true
                        break
                    }
                }
            }

            // Search off-hand slot if not yet found in main inventory
            if (!updated) {
                val offhand = player.inventory.itemInOffHand
                if (isSafeCase(offhand)) {
                    val itemUuid = offhand.itemMeta?.persistentDataContainer?.get(KEY_SAFE_UUID, PersistentDataType.STRING)
                    if (itemUuid == uuidStr) {
                        applySerializedContents(offhand, serializedBase64)
                        player.inventory.setItemInOffHand(offhand)
                        updated = true
                    }
                }
            }

            // Fallback: Always update holder's sourceItem snapshot as safety guarantee
            applySerializedContents(holder.sourceItem, serializedBase64)
        }

        // Folia Thread-Safety: On Folia, InventoryCloseEvent is already dispatched synchronously
        // within the Player's EntityScheduler thread context. Execute immediately to prevent
        // abrupt disconnect / player retirement race conditions.
        val isOwned = runCatching { Bukkit.isOwnedByCurrentRegion(player) }.getOrDefault(false)
        if (isOwned) {
            applySave()
        } else {
            plugin.scheduler.runAtEntity(
                entity = player,
                runnable = applySave,
                retired = {
                    // Entity retired during disconnect: persist to holder sourceItem snapshot
                    applySerializedContents(holder.sourceItem, serializedBase64)
                }
            )
        }
    }

    /**
     * Applies the Base64 serialized contents string to the ItemStack's PDC.
     */
    private fun applySerializedContents(item: ItemStack, serialized: String) {
        val meta = item.itemMeta ?: return
        if (serialized.isNotEmpty()) {
            meta.persistentDataContainer.set(KEY_SERIALIZED_CONTENTS, PersistentDataType.STRING, serialized)
        } else {
            meta.persistentDataContainer.remove(KEY_SERIALIZED_CONTENTS)
        }
        item.itemMeta = meta
    }
}
