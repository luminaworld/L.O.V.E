package com.luminaris.extraction.core.safe

import com.luminaris.extraction.util.ItemStackSerializer
import net.kyori.adventure.text.Component
import org.bukkit.Material
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SafeCaseTest {

    @Test
    fun testSafeCaseDefinition() {
        val title = Component.text("Alpha Secure Pouch (2x2)")
        val def = SafeCaseDefinition(
            id = "alpha_pouch",
            displayName = title,
            material = Material.BUNDLE,
            customModelData = 1001,
            capacity = 4,
            keepsOnDeath = true
        )

        assertEquals("alpha_pouch", def.id)
        assertEquals(title, def.displayName)
        assertEquals(Material.BUNDLE, def.material)
        assertEquals(1001, def.customModelData)
        assertEquals(4, def.capacity)
        assertTrue(def.keepsOnDeath)
    }

    @Test
    fun testNamespacedKeys() {
        assertEquals("luminaris:is_safe_case", SafeCaseManager.KEY_IS_SAFE_CASE.toString())
        assertEquals("luminaris:safe_id", SafeCaseManager.KEY_SAFE_ID.toString())
        assertEquals("luminaris:safe_capacity", SafeCaseManager.KEY_SAFE_CAPACITY.toString())
        assertEquals("luminaris:safe_uuid", SafeCaseManager.KEY_SAFE_UUID.toString())
        assertEquals("luminaris:serialized_contents", SafeCaseManager.KEY_SERIALIZED_CONTENTS.toString())
        assertEquals("luminaris:locked_slot", SafeCaseManager.KEY_LOCKED_SLOT.toString())
    }

    @Test
    fun testMatchingChestSize() {
        // Calculation test: chest sizes must be multiples of 9 between 9 and 54
        fun calculateChestSize(capacity: Int): Int {
            val rows = (capacity + 8) / 9
            val size = rows * 9
            return size.coerceIn(9, 54)
        }

        assertEquals(9, calculateChestSize(1))
        assertEquals(9, calculateChestSize(4))
        assertEquals(9, calculateChestSize(6))
        assertEquals(9, calculateChestSize(9))
        assertEquals(18, calculateChestSize(10))
        assertEquals(18, calculateChestSize(12))
        assertEquals(27, calculateChestSize(19))
        assertEquals(27, calculateChestSize(27))
        assertEquals(54, calculateChestSize(54))
        assertEquals(54, calculateChestSize(60))
    }

    @Test
    fun testEmptyItemStackSerialization() {
        val emptySerialized = ItemStackSerializer.itemStackArrayToBase64(emptyArray())
        assertEquals("", emptySerialized)

        val deserializedFromBlank = ItemStackSerializer.itemStackArrayFromBase64("")
        assertEquals(0, deserializedFromBlank.size)

        val deserializedFromSpaces = ItemStackSerializer.itemStackArrayFromBase64("   ")
        assertEquals(0, deserializedFromSpaces.size)
    }
}
