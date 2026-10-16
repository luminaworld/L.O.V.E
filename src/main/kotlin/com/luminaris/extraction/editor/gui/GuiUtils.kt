package com.luminaris.extraction.editor.gui

import com.luminaris.extraction.localization.TranslationManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

/**
 * Utility functions for creating consistent, aesthetic GUI items and sensory audio feedback.
 */
object GuiUtils {

    /**
     * Builds an [ItemStack] with Adventure component display name, lore, and optional glow effect.
     */
    fun createItem(
        material: Material,
        name: Component? = null,
        lore: List<Component> = emptyList(),
        glowing: Boolean = false,
        amount: Int = 1
    ): ItemStack {
        val stack = ItemStack(material, amount.coerceAtLeast(1))
        stack.editMeta { meta ->
            if (name != null) {
                meta.displayName(name.decoration(TextDecoration.ITALIC, false))
            }
            if (lore.isNotEmpty()) {
                meta.lore(lore.map { it.decoration(TextDecoration.ITALIC, false) })
            }
            if (glowing) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true)
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            }
            meta.addItemFlags(
                ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_DESTROYS,
                ItemFlag.HIDE_PLACED_ON,
                ItemFlag.HIDE_ADDITIONAL_TOOLTIP
            )
        }
        return stack
    }

    /**
     * Builds a decorative background border glass pane.
     */
    fun createBorderPane(material: Material = Material.GRAY_STAINED_GLASS_PANE): ItemStack {
        return createItem(material, Component.empty())
    }

    /**
     * Fills the edges of a 54-slot inventory with border panes.
     */
    fun fill54Border(inventory: org.bukkit.inventory.Inventory, material: Material = Material.GRAY_STAINED_GLASS_PANE) {
        val border = createBorderPane(material)
        // Top and bottom rows
        for (i in 0..8) {
            inventory.setItem(i, border)
            inventory.setItem(45 + i, border)
        }
        // Left and right columns
        for (row in 1..4) {
            inventory.setItem(row * 9, border)
            inventory.setItem(row * 9 + 8, border)
        }
    }

    /**
     * Fills the edges of a 27-slot inventory with border panes.
     */
    fun fill27Border(inventory: org.bukkit.inventory.Inventory, material: Material = Material.GRAY_STAINED_GLASS_PANE) {
        val border = createBorderPane(material)
        for (i in 0..8) {
            inventory.setItem(i, border)
            inventory.setItem(18 + i, border)
        }
        inventory.setItem(9, border)
        inventory.setItem(17, border)
    }

    fun playClickSound(player: Player) {
        player.playSound(player.location, Sound.UI_BUTTON_CLICK, 0.6f, 1.2f)
    }

    fun playSuccessSound(player: Player) {
        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.4f)
    }

    fun playDeleteSound(player: Player) {
        player.playSound(player.location, Sound.BLOCK_LAVA_EXTINGUISH, 0.7f, 1.0f)
    }

    fun playErrorSound(player: Player) {
        player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 0.7f, 1.0f)
    }
}
