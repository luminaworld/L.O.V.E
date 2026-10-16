package com.luminaris.extraction.editor.dialog

import com.luminaris.extraction.core.container.ContainerCategory
import com.luminaris.extraction.core.container.ContainerRegistry
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
 * Dialog builder for creating container categories via native Paper Dialog API.
 */
object CategoryDialogBuilder {

    private val miniMessage = MiniMessage.miniMessage()

    /**
     * Constructs and opens the category creator dialog for the player.
     */
    fun open(player: Player, containerRegistry: ContainerRegistry) {
        val idInput = DialogInput.text("category_id", Component.text(TranslationManager.get("dialog.category.input_id")))
            .initial("crate_")
            .maxLength(64)
            .build()

        val displayNameInput = DialogInput.text("display_name", Component.text(TranslationManager.get("dialog.category.input_display_name")))
            .initial(TranslationManager.get("dialog.category.default_display"))
            .maxLength(128)
            .build()

        val minRollsInput = DialogInput.numberRange("min_rolls", Component.text(TranslationManager.get("dialog.category.input_min_rolls")), 1.0f, 54.0f)
            .step(1.0f)
            .initial(1.0f)
            .build()

        val maxRollsInput = DialogInput.numberRange("max_rolls", Component.text(TranslationManager.get("dialog.category.input_max_rolls")), 1.0f, 54.0f)
            .step(1.0f)
            .initial(4.0f)
            .build()

        val dialogTitle = Component.text(TranslationManager.get("dialog.category.title"), NamedTextColor.AQUA)
        val descriptionBody = DialogBody.plainMessage(
            TranslationManager.render("dialog.category.body")
        )

        val dialogBase = DialogBase.builder(dialogTitle)
            .body(listOf(descriptionBody))
            .inputs(listOf(idInput, displayNameInput, minRollsInput, maxRollsInput))
            .canCloseWithEscape(true)
            .pause(false)
            .build()

        val clickOptions = ClickCallback.Options.builder()
            .lifetime(Duration.ofMinutes(5))
            .build()

        val confirmAction = DialogAction.customClick({ responseView: DialogResponseView, audience: Audience ->
            val targetPlayer = audience as? Player ?: player

            val rawId = responseView.getText("category_id")?.trim()
            if (rawId.isNullOrBlank()) {
                TranslationManager.send(targetPlayer, "dialog.category.error_empty_id")
                return@customClick
            }

            val sanitizedId = rawId.lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_-]"), "")
            val rawDisplayName = responseView.getText("display_name")?.trim()
            val displayName = if (!rawDisplayName.isNullOrBlank()) {
                if ((rawDisplayName.contains("<") && rawDisplayName.contains(">")) || rawDisplayName.contains("&") || rawDisplayName.contains("§")) {
                    TranslationManager.parseComponent(rawDisplayName)
                } else {
                    TranslationManager.parseComponent("<gold>$rawDisplayName</gold>")
                }
            } else {
                TranslationManager.parseComponent("<gold>${sanitizedId.replace("_", " ").uppercase()}</gold>")
            }

            val minRolls = (responseView.getFloat("min_rolls") ?: 1.0f).toInt().coerceAtLeast(1)
            val rawMaxRolls = (responseView.getFloat("max_rolls") ?: 3.0f).toInt()
            val maxRolls = maxOf(minRolls, rawMaxRolls)

            val category = ContainerCategory(
                id = sanitizedId,
                displayName = displayName,
                minRolls = minRolls,
                maxRolls = maxRolls
            )

            containerRegistry.register(category, saveToDisk = true)

            TranslationManager.send(
                targetPlayer,
                "dialog.category.success",
                "id" to sanitizedId,
                "display_name" to (rawDisplayName ?: sanitizedId),
                "min_rolls" to minRolls,
                "max_rolls" to maxRolls
            )
        }, clickOptions)

        val confirmButton = ActionButton.builder(Component.text(TranslationManager.get("dialog.category.btn_save"), NamedTextColor.GREEN))
            .tooltip(Component.text(TranslationManager.get("dialog.category.tooltip_save")))
            .action(confirmAction)
            .build()

        val cancelAction = DialogAction.customClick({ _, audience ->
            (audience as? Player)?.let { TranslationManager.send(it, "dialog.category.cancelled") }
        }, clickOptions)

        val cancelButton = ActionButton.builder(Component.text(TranslationManager.get("dialog.category.btn_cancel"), NamedTextColor.RED))
            .tooltip(Component.text(TranslationManager.get("dialog.category.tooltip_cancel")))
            .action(cancelAction)
            .build()

        val confirmationType = DialogType.confirmation(confirmButton, cancelButton)

        val dialog = Dialog.create { factory ->
            val builder = factory.empty()
            builder.base(dialogBase)
            builder.type(confirmationType)
        }

        player.showDialog(dialog)
    }
}
