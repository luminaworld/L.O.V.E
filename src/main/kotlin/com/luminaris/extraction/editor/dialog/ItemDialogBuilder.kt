package com.luminaris.extraction.editor.dialog

import com.luminaris.extraction.core.item.ExtractionItem
import com.luminaris.extraction.core.item.ItemRegistry
import com.luminaris.extraction.core.item.Rarity
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.dialog.DialogResponseView
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import java.time.Duration

/**
 * Dialog builder for registering extraction loot items directly from player's main hand
 * using the native Paper Dialog API.
 */
object ItemDialogBuilder {

    private val miniMessage = MiniMessage.miniMessage()

    /**
     * Constructs and displays the item registration dialog to the player.
     *
     * @param player The player holding the item to register.
     * @param category The container category to associate with this item.
     * @param itemRegistry The item registry service for saving the item.
     */
    fun open(player: Player, category: String, itemRegistry: ItemRegistry) {
        val heldItem = player.inventory.itemInMainHand
        if (heldItem.isEmpty || heldItem.type.isAir) {
            player.sendMessage(miniMessage.deserialize("<red>❌ Your main hand is empty! Please hold an item to register.</red>"))
            return
        }

        // Clone item to capture its exact state at dialog launch
        val capturedStack = heldItem.clone()

        // Resolve default display name for input field
        val initialDisplayName = if (capturedStack.hasItemMeta() && capturedStack.itemMeta.hasDisplayName()) {
            miniMessage.serialize(capturedStack.itemMeta.displayName()!!)
        } else {
            capturedStack.type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
        }

        // Build Dialog Inputs as specified
        val itemIdInput = DialogInput.text("item_id", Component.text("Unique Item ID"))
            .initial(capturedStack.type.name.lowercase())
            .maxLength(64)
            .build()

        val displayNameInput = DialogInput.text("display_name", Component.text("Custom Display Name"))
            .initial(initialDisplayName)
            .maxLength(128)
            .build()

        val priceInput = DialogInput.numberRange("price", Component.text("Sell Price"), 0.0f, 100000.0f)
            .step(10.0f)
            .initial(100.0f)
            .build()

        val weightInput = DialogInput.numberRange("weight", Component.text("Drop Weight"), 1.0f, 1000.0f)
            .step(5.0f)
            .initial(20.0f)
            .build()

        // Build DialogBase
        val dialogTitle = Component.text("Item Registry Editor", NamedTextColor.GOLD)
        val descriptionBody = DialogBody.plainMessage(
            miniMessage.deserialize(
                "<gray>Configuring item: </gray><yellow>${capturedStack.type.name}</yellow><gray> for category: </gray><aqua>$category</aqua>"
            )
        )

        val dialogBase = DialogBase.builder(dialogTitle)
            .body(listOf(descriptionBody))
            .inputs(listOf(itemIdInput, displayNameInput, priceInput, weightInput))
            .canCloseWithEscape(true)
            .pause(false)
            .build()

        // Click options with 5 minute lifetime
        val clickOptions = ClickCallback.Options.builder()
            .lifetime(Duration.ofMinutes(5))
            .build()

        // Custom action for confirming item registration
        val confirmAction = DialogAction.customClick({ responseView: DialogResponseView, audience: Audience ->
            val targetPlayer = audience as? Player ?: player

            val rawId = responseView.getText("item_id")?.trim()
            if (rawId.isNullOrBlank()) {
                targetPlayer.sendMessage(miniMessage.deserialize("<red>❌ Item ID cannot be empty!</red>"))
                return@customClick
            }

            val sanitizedId = rawId.lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_-]"), "")
            val customDisplayName = responseView.getText("display_name")?.trim()
            val price = (responseView.getFloat("price") ?: 0.0f).toDouble()
            val weight = (responseView.getFloat("weight") ?: 1.0f).toDouble()

            // Handheld Item Validation:
            // Safely use the captured snapshot taken at invocation time to prevent exploits where
            // the player switches hotbar slots or drops the item before clicking Confirm.
            val currentHeld = targetPlayer.inventory.itemInMainHand
            val finalStack = if (!currentHeld.isEmpty && currentHeld.isSimilar(capturedStack)) {
                currentHeld.clone()
            } else {
                capturedStack.clone()
            }

            if (!customDisplayName.isNullOrBlank()) {
                finalStack.editMeta { meta ->
                    meta.displayName(miniMessage.deserialize(customDisplayName))
                }
            }

            val rarity = Rarity.fromPrice(price)
            val extractionItem = ExtractionItem(
                id = sanitizedId,
                itemStack = finalStack,
                price = price,
                rarity = rarity,
                weight = weight,
                allowedContainers = mutableSetOf(category.lowercase())
            )

            itemRegistry.register(extractionItem, saveToDisk = true)

            val successMsg = """
                <dark_gray>----------------------------------------</dark_gray>
                <gradient:#00FFA3:#00B8FF><bold>✔ ITEM REGISTERED SUCCESSFULLY</bold></gradient>
                <gray>ID:           </gray><yellow>$sanitizedId</yellow>
                <gray>Display Name: </gray>${customDisplayName ?: finalStack.type.name}
                <gray>Category:     </gray><aqua>$category</aqua>
                <gray>Sell Price:   </gray><gold>$$price</gold>
                <gray>Drop Weight:  </gray><yellow>$weight</yellow>
                <gray>Rarity:       </gray>${rarity.miniMessageTag}${rarity.name}</color>
                <dark_gray>----------------------------------------</dark_gray>
            """.trimIndent()
            targetPlayer.sendMessage(miniMessage.deserialize(successMsg))
        }, clickOptions)

        val confirmButton = ActionButton.builder(Component.text("✔ Register Item", NamedTextColor.GREEN))
            .tooltip(Component.text("Save item to extraction registry"))
            .action(confirmAction)
            .build()

        val cancelAction = DialogAction.customClick({ _, audience ->
            (audience as? Player)?.sendMessage(miniMessage.deserialize("<gray>Item registration cancelled.</gray>"))
        }, clickOptions)

        val cancelButton = ActionButton.builder(Component.text("✖ Cancel", NamedTextColor.RED))
            .tooltip(Component.text("Close dialog without saving"))
            .action(cancelAction)
            .build()

        val confirmationType = DialogType.confirmation(confirmButton, cancelButton)

        // Create Dialog via Paper Dialog API
        val dialog = Dialog.create { factory ->
            val builder = factory.empty()
            builder.base(dialogBase)
            builder.type(confirmationType)
        }

        player.showDialog(dialog)
    }
}
