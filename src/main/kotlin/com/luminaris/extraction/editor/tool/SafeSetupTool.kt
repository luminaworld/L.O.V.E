package com.luminaris.extraction.editor.tool

import com.luminaris.extraction.localization.TranslationManager
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * Specialized editor tool for designating single-block containers as timed safe boxes
 * and mounting their display screens to a specific block face.
 */
object SafeSetupTool {

    val KEY_SAFE_SETUP_TOOL = NamespacedKey("luminaris", "safe_setup_tool")
    val KEY_TARGET_CATEGORY = NamespacedKey("luminaris", "safe_target_category")

    private val miniMessage = MiniMessage.miniMessage()

    /**
     * Creates an ItemStack instance of the Safe Setup Tool.
     *
     * @param categoryId Target category to assign (defaults to "safe_box").
     * @return Prepared tool ItemStack.
     */
    fun createTool(categoryId: String = "safe_box"): ItemStack {
        val stack = ItemStack(Material.TRIPWIRE_HOOK)
        val cleanCatId = categoryId.lowercase().trim()

        stack.editMeta { meta ->
            meta.displayName(
                miniMessage.deserialize("<gradient:#FFAA00:#FF5500><bold>เครื่องมือตั้งค่าตู้เซฟ (Safe Setup Tool)</bold></gradient>")
            )

            val loreLines = listOf(
                miniMessage.deserialize("<gray>หมวดหมู่ที่ใช้: <yellow><bold>$cleanCatId</bold></yellow></gray>"),
                miniMessage.deserialize("<white>คลิกขวาที่ด้านข้างกล่อง (N / S / E / W)</white>"),
                miniMessage.deserialize("<gray>เพื่อกำหนดด้านที่ Hologram จะแสดงแนบหน้ากล่อง</gray>"),
                miniMessage.deserialize("<dark_gray>• รองรับเฉพาะบล็อกเดี่ยว (Single Block) เท่านั้น</dark_gray>"),
                miniMessage.deserialize("<dark_gray>• ไม่รองรับกล่องคู่ (Double Chest) หรือ ArmorStand</dark_gray>")
            )
            meta.lore(loreLines)

            meta.setEnchantmentGlintOverride(true)

            val pdc = meta.persistentDataContainer
            pdc.set(KEY_SAFE_SETUP_TOOL, PersistentDataType.BYTE, 1.toByte())
            pdc.set(KEY_TARGET_CATEGORY, PersistentDataType.STRING, cleanCatId)
        }

        return stack
    }

    /**
     * Checks if the given ItemStack is a valid Safe Setup Tool.
     */
    fun isSafeSetupTool(stack: ItemStack?): Boolean {
        if (stack == null || stack.type.isAir) return false
        val meta = stack.itemMeta ?: return false
        return meta.persistentDataContainer.has(KEY_SAFE_SETUP_TOOL, PersistentDataType.BYTE)
    }

    /**
     * Resolves the target category stored in this tool, or fallback to "safe_box".
     */
    fun getTargetCategory(stack: ItemStack): String {
        val meta = stack.itemMeta ?: return "safe_box"
        return meta.persistentDataContainer.get(KEY_TARGET_CATEGORY, PersistentDataType.STRING) ?: "safe_box"
    }

    /**
     * Validates whether the clicked face is horizontal and can display the side screen.
     */
    fun isSupportedFace(face: BlockFace): Boolean {
        return face == BlockFace.NORTH || face == BlockFace.SOUTH || face == BlockFace.EAST || face == BlockFace.WEST
    }
}

