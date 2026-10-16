package com.luminaris.extraction.editor.dialog

import com.luminaris.extraction.core.item.ExtractionItem
import com.luminaris.extraction.core.item.ItemRegistry
import com.luminaris.extraction.core.item.Rarity
import com.luminaris.extraction.localization.TranslationManager
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
            TranslationManager.send(player, "editor.createitem.hand_empty")
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
        val itemIdInput = DialogInput.text("item_id", Component.text(TranslationManager.get("dialog.item.input_id")))
            .initial(capturedStack.type.name.lowercase())
            .maxLength(64)
            .build()

        val displayNameInput = DialogInput.text("display_name", Component.text(TranslationManager.get("dialog.item.input_display_name")))
            .initial(initialDisplayName)
            .maxLength(128)
            .build()

        val priceInput = DialogInput.numberRange("price", Component.text(TranslationManager.get("dialog.item.input_price")), 0.0f, 100000.0f)
            .step(10.0f)
            .initial(100.0f)
            .build()

        val weightInput = DialogInput.numberRange("weight", Component.text(TranslationManager.get("dialog.item.input_weight")), 1.0f, 1000.0f)
            .step(5.0f)
            .initial(20.0f)
            .build()

        // Build DialogBase
        val dialogTitle = Component.text(TranslationManager.get("dialog.item.title"), NamedTextColor.GOLD)
        val descriptionBody = DialogBody.plainMessage(
            TranslationManager.render(
                "dialog.item.body",
                "material" to capturedStack.type.name,
                "category" to category
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
                TranslationManager.send(targetPlayer, "dialog.item.error_empty_id")
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
                    meta.displayName(TranslationManager.parseComponent(customDisplayName))
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

            TranslationManager.send(
                targetPlayer,
                "dialog.item.success",
                "id" to sanitizedId,
                "display_name" to (customDisplayName ?: finalStack.type.name),
                "category" to category,
                "price" to price,
                "weight" to weight,
                "rarity_tag" to rarity.miniMessageTag,
                "rarity" to rarity.name
            )
        }, clickOptions)

        val confirmButton = ActionButton.builder(Component.text(TranslationManager.get("dialog.item.btn_save"), NamedTextColor.GREEN))
            .tooltip(Component.text(TranslationManager.get("dialog.item.tooltip_save")))
            .action(confirmAction)
            .build()

        val cancelAction = DialogAction.customClick({ _, audience ->
            (audience as? Player)?.let { TranslationManager.send(it, "dialog.item.cancelled") }
        }, clickOptions)

        val cancelButton = ActionButton.builder(Component.text(TranslationManager.get("dialog.item.btn_cancel"), NamedTextColor.RED))
            .tooltip(Component.text(TranslationManager.get("dialog.item.tooltip_cancel")))
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
