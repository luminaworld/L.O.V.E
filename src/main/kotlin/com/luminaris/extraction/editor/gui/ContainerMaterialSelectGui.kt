package com.luminaris.extraction.editor.gui

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.core.container.ContainerCategory
import com.luminaris.extraction.editor.tool.ContainerMarkerTool
import com.luminaris.extraction.localization.TranslationManager
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

/**
 * Material Picker GUI allowing administrators to select any of the 38 supported
 * container materials when obtaining marker tools.
 */
class ContainerMaterialSelectGui(
    private val plugin: LuminarisCorePlugin,
    private val category: ContainerCategory
) : EditorGuiHolder {

    private lateinit var inventory: Inventory
    private val slotToMaterial = mutableMapOf<Int, Material>()

    fun open(player: Player) {
        val title = TranslationManager.render("editor.gui.material_select_title", "id" to category.id)
        inventory = Bukkit.createInventory(this, 54, title)

        GuiUtils.fill54Border(inventory, Material.GRAY_STAINED_GLASS_PANE)

        val materials = ContainerMarkerTool.ALL_SUPPORTED_MATERIALS
        // Available slots within the 54-grid inner area
        val validSlots = listOf(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        )

        // For 38 materials, we can use 4 inner rows or custom placement:
        val allContentSlots = mutableListOf<Int>()
        for (r in 1..4) {
            for (c in 1..7) {
                allContentSlots.add(r * 9 + c)
            }
        }
        // allContentSlots has 28 slots (rows 1,2,3,4 cols 1..7).
        // Since we have 38 materials, let's map slots 0..37 or use 5 full rows!
        // Using slots 0..37 directly or rows 0..4:
        val itemSlots = (0..37).toList()

        for (i in materials.indices) {
            if (i >= itemSlots.size) break
            val slot = itemSlots[i]
            val mat = materials[i]
            slotToMaterial[slot] = mat

            val matName = formatMaterialDisplayName(mat)
            val lore = listOf(
                TranslationManager.render("editor.gui.marker_material_lore_cat", "category" to category.id),
                Component.empty(),
                TranslationManager.render("editor.gui.marker_material_lore_click")
            )

            val item = GuiUtils.createItem(
                material = mat,
                name = TranslationManager.parseComponent("<color:#55FFFF><bold>$matName</bold></color>"),
                lore = lore
            )
            inventory.setItem(slot, item)
        }

        // Fill remaining slots in row 4 with border
        for (slot in 38..44) {
            inventory.setItem(slot, GuiUtils.createItem(Material.GRAY_STAINED_GLASS_PANE, Component.empty()))
        }

        // Slot 45: Back Button
        inventory.setItem(45, GuiUtils.createItem(Material.ARROW, TranslationManager.render("editor.gui.btn_back")))

        // Slot 49: Info Item
        val infoLore = listOf(
            TranslationManager.render("editor.gui.material_info_lore_1"),
            TranslationManager.render("editor.gui.material_info_lore_2"),
            TranslationManager.render("editor.gui.material_info_lore_3")
        )
        val infoItem = GuiUtils.createItem(
            Material.KNOWLEDGE_BOOK,
            TranslationManager.render("editor.gui.material_info_title"),
            infoLore
        )
        inventory.setItem(49, infoItem)

        // Fill remaining bottom row slots with gray glass
        for (slot in listOf(46, 47, 48, 50, 51, 52, 53)) {
            inventory.setItem(slot, GuiUtils.createItem(Material.GRAY_STAINED_GLASS_PANE, Component.empty()))
        }

        plugin.scheduler.runAtEntity(player) {
            player.openInventory(inventory)
        }
    }

    override fun handleClick(event: InventoryClickEvent, player: Player) {
        val slot = event.slot
        if (slot == 45) {
            // Back to ContainerEditGui
            GuiUtils.playClickSound(player)
            ContainerEditGui(plugin, category).open(player)
            return
        }

        val mat = slotToMaterial[slot]
        if (mat != null) {
            GuiUtils.playSuccessSound(player)
            val markerTool = ContainerMarkerTool.createContainerMarker(
                categoryId = category.id,
                material = mat,
                categoryDisplayName = null
            )
            val leftover = player.inventory.addItem(markerTool)
            for (item in leftover.values) {
                player.world.dropItemNaturally(player.location, item)
            }
            TranslationManager.send(
                player,
                "editor.gui.marker_tool_given_mat",
                "id" to category.id,
                "mat" to formatMaterialDisplayName(mat)
            )
        }
    }

    override fun getInventory(): Inventory = inventory

    private fun formatMaterialDisplayName(mat: Material): String {
        return when (mat) {
            Material.CHEST -> "หีบไม้ (Chest)"
            Material.TRAPPED_CHEST -> "หีบกับดัก (Trapped Chest)"
            Material.COPPER_CHEST -> "หีบทองแดง (Copper Chest)"
            Material.EXPOSED_COPPER_CHEST -> "หีบทองแดงเริ่มผุกร่อน (Exposed)"
            Material.WEATHERED_COPPER_CHEST -> "หีบทองแดงผุกร่อน (Weathered)"
            Material.OXIDIZED_COPPER_CHEST -> "หีบทองแดงออกซิไดซ์ (Oxidized)"
            Material.WAXED_COPPER_CHEST -> "หีบทองแดงเคลือบแว็กซ์ (Waxed)"
            Material.WAXED_EXPOSED_COPPER_CHEST -> "หีบทองแดงเริ่มผุกร่อนเคลือบแว็กซ์"
            Material.WAXED_WEATHERED_COPPER_CHEST -> "หีบทองแดงผุกร่อนเคลือบแว็กซ์"
            Material.WAXED_OXIDIZED_COPPER_CHEST -> "หีบทองแดงออกซิไดซ์เคลือบแว็กซ์"
            Material.BARREL -> "ถังไม้ บาร์แรล (Barrel)"
            Material.SHULKER_BOX -> "ชัคเกอร์บ็อกซ์ ดั้งเดิม"
            Material.WHITE_SHULKER_BOX -> "ชัคเกอร์บ็อกซ์ สีขาว"
            Material.ORANGE_SHULKER_BOX -> "ชัคเกอร์บ็อกซ์ สีส้ม"
            Material.MAGENTA_SHULKER_BOX -> "ชัคเกอร์บ็อกซ์ สีบานเย็น"
            Material.LIGHT_BLUE_SHULKER_BOX -> "ชัคเกอร์บ็อกซ์ สีฟ้าอ่อน"
            Material.YELLOW_SHULKER_BOX -> "ชัคเกอร์บ็อกซ์ สีเหลือง"
            Material.LIME_SHULKER_BOX -> "ชัคเกอร์บ็อกซ์ สีเขียวมะนาว"
            Material.PINK_SHULKER_BOX -> "ชัคเกอร์บ็อกซ์ สีชมพู"
            Material.GRAY_SHULKER_BOX -> "ชัคเกอร์บ็อกซ์ สีเทา"
            Material.LIGHT_GRAY_SHULKER_BOX -> "ชัคเกอร์บ็อกซ์ สีเทาอ่อน"
            Material.CYAN_SHULKER_BOX -> "ชัคเกอร์บ็อกซ์ สีฟ้าคราม"
            Material.PURPLE_SHULKER_BOX -> "ชัคเกอร์บ็อกซ์ สีม่วง"
            Material.BLUE_SHULKER_BOX -> "ชัคเกอร์บ็อกซ์ สีน้ำเงิน"
            Material.BROWN_SHULKER_BOX -> "ชัคเกอร์บ็อกซ์ สีน้ำตาล"
            Material.GREEN_SHULKER_BOX -> "ชัคเกอร์บ็อกซ์ สีเขียว"
            Material.RED_SHULKER_BOX -> "ชัคเกอร์บ็อกซ์ สีแดง"
            Material.BLACK_SHULKER_BOX -> "ชัคเกอร์บ็อกซ์ สีดำ"
            Material.CRAFTING_TABLE -> "โต๊ะคราฟต์ (Crafting Table)"
            Material.FURNACE -> "เตาเผา (Furnace)"
            Material.BLAST_FURNACE -> "เตาหลอมโลหะ (Blast Furnace)"
            Material.SMOKER -> "เตารมควัน (Smoker)"
            Material.ENCHANTING_TABLE -> "โต๊ะเอนชานท์ (Enchanting Table)"
            Material.DECORATED_POT -> "กระถางตกแต่ง (Decorated Pot)"
            Material.BOOKSHELF -> "ชั้นหนังสือ (Bookshelf)"
            Material.CHISELED_BOOKSHELF -> "ชั้นหนังสือแกะสลัก (Chiseled Bookshelf)"
            Material.ENDER_CHEST -> "หีบเอนเดอร์ (Ender Chest)"
            Material.ARMOR_STAND -> "แท่นวางชุดเกราะ (Armor Stand)"
            else -> mat.name
        }
    }
}
