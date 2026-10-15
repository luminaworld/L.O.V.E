package com.luminaris.extraction.util

import com.luminaris.extraction.LuminarisCorePlugin
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64

/**
 * Robust utility for serializing and deserializing Bukkit [ItemStack] arrays to and from Base64 strings.
 * Preserves all Bukkit metadata, PersistentDataContainer tags, custom model data, and enchantments.
 */
@Suppress("DEPRECATION")
object ItemStackSerializer {

    /**
     * Serializes an array of [ItemStack] (including null elements) into a Base64-encoded string.
     * Returns an empty string if the array is empty or if serialization fails.
     */
    fun itemStackArrayToBase64(items: Array<out ItemStack?>): String {
        if (items.isEmpty()) return ""
        return runCatching {
            ByteArrayOutputStream().use { byteOut ->
                BukkitObjectOutputStream(byteOut).use { dataOut ->
                    dataOut.writeInt(items.size)
                    for (item in items) {
                        dataOut.writeObject(item)
                    }
                }
                Base64.getEncoder().encodeToString(byteOut.toByteArray())
            }
        }.getOrElse { ex ->
            LuminarisCorePlugin.instance.logger.severe("Failed to serialize ItemStack array to Base64: ${ex.message}")
            ""
        }
    }

    /**
     * Deserializes a Base64-encoded string into an array of nullable [ItemStack].
     * Returns an empty array if the string is blank or if deserialization fails.
     */
    fun itemStackArrayFromBase64(data: String): Array<ItemStack?> {
        if (data.isBlank()) return emptyArray()
        return runCatching {
            val bytes = Base64.getDecoder().decode(data)
            ByteArrayInputStream(bytes).use { byteIn ->
                BukkitObjectInputStream(byteIn).use { dataIn ->
                    val size = dataIn.readInt()
                    val items = arrayOfNulls<ItemStack>(size)
                    for (i in 0 until size) {
                        items[i] = dataIn.readObject() as? ItemStack
                    }
                    items
                }
            }
        }.getOrElse { ex ->
            LuminarisCorePlugin.instance.logger.severe("Failed to deserialize ItemStack array from Base64: ${ex.message}")
            emptyArray()
        }
    }

    /**
     * Serializes a single [ItemStack] into a Base64 string.
     */
    fun itemStackToBase64(item: ItemStack?): String {
        if (item == null || item.isEmpty) return ""
        return itemStackArrayToBase64(arrayOf(item))
    }

    /**
     * Deserializes a single [ItemStack] from a Base64 string.
     */
    fun itemStackFromBase64(data: String): ItemStack? {
        if (data.isBlank()) return null
        val items = itemStackArrayFromBase64(data)
        return items.firstOrNull()
    }
}
