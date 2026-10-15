package com.luminaris.extraction.editor.dialog

import com.luminaris.extraction.core.container.ContainerCategory
import com.luminaris.extraction.core.container.ContainerRegistry
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
        val idInput = DialogInput.text("category_id", Component.text("Category ID"))
            .initial("crate_")
            .maxLength(64)
            .build()

        val displayNameInput = DialogInput.text("display_name", Component.text("Category Display Name"))
            .initial("<gradient:#FFAA00:#FF5555>Military Supply Crate</gradient>")
            .maxLength(128)
            .build()

        val minRollsInput = DialogInput.numberRange("min_rolls", Component.text("Min Rolls"), 1.0f, 54.0f)
            .step(1.0f)
            .initial(1.0f)
            .build()

        val maxRollsInput = DialogInput.numberRange("max_rolls", Component.text("Max Rolls"), 1.0f, 54.0f)
            .step(1.0f)
            .initial(4.0f)
            .build()

        val dialogTitle = Component.text("Container Category Editor", NamedTextColor.AQUA)
        val descriptionBody = DialogBody.plainMessage(
            miniMessage.deserialize("<gray>Configure loot roll properties and naming for this container category.</gray>")
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
                targetPlayer.sendMessage(miniMessage.deserialize("<red>❌ Category ID cannot be empty!</red>"))
                return@customClick
            }

            val sanitizedId = rawId.lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_-]"), "")
            val rawDisplayName = responseView.getText("display_name")?.trim()
            val displayName = if (!rawDisplayName.isNullOrBlank()) {
                if (rawDisplayName.contains("<") && rawDisplayName.contains(">")) {
                    miniMessage.deserialize(rawDisplayName)
                } else {
                    miniMessage.deserialize("<gold>$rawDisplayName</gold>")
                }
            } else {
                miniMessage.deserialize("<gold>${sanitizedId.replace("_", " ").uppercase()}</gold>")
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

            val successMsg = """
                <dark_gray>----------------------------------------</dark_gray>
                <gradient:#00FFA3:#00B8FF><bold>✔ CATEGORY CREATED SUCCESSFULLY</bold></gradient>
                <gray>ID:           </gray><yellow>$sanitizedId</yellow>
                <gray>Display Name: </gray><white>${rawDisplayName ?: sanitizedId}</white>
                <gray>Min Rolls:    </gray><green>$minRolls</green>
                <gray>Max Rolls:    </gray><green>$maxRolls</green>
                <dark_gray>----------------------------------------</dark_gray>
            """.trimIndent()
            targetPlayer.sendMessage(miniMessage.deserialize(successMsg))
        }, clickOptions)

        val confirmButton = ActionButton.builder(Component.text("✔ Save Category", NamedTextColor.GREEN))
            .tooltip(Component.text("Create and persist container category"))
            .action(confirmAction)
            .build()

        val cancelAction = DialogAction.customClick({ _, audience ->
            (audience as? Player)?.sendMessage(miniMessage.deserialize("<gray>Category creation cancelled.</gray>"))
        }, clickOptions)

        val cancelButton = ActionButton.builder(Component.text("✖ Cancel", NamedTextColor.RED))
            .tooltip(Component.text("Close dialog without saving"))
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
