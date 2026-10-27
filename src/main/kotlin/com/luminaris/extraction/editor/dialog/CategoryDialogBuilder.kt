package com.luminaris.extraction.editor.dialog

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.core.container.ContainerCategory
import com.luminaris.extraction.core.container.ContainerRegistry
import com.luminaris.extraction.editor.gui.ContainerEditGui
import com.luminaris.extraction.editor.gui.ContainerListGui
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
import org.bukkit.Material
import org.bukkit.entity.Player
import java.time.Duration

/**
 * Dialog builder for configuring container / drop box name and ID via native Paper Dialog API.
 * Other settings such as drop percentages and roll counts are managed directly in the intermediate GUI.
 */
object CategoryDialogBuilder {

    private val miniMessage = MiniMessage.miniMessage()

    /**
     * Constructs and opens the drop box name and ID dialog for the player.
     *
     * @param player Target player viewing the dialog.
     * @param containerRegistry Registry service for containers.
     * @param existingCategory Optional existing category being edited.
     * @param plugin Optional plugin instance for scheduling GUI navigation.
     */
    fun open(
        player: Player,
        containerRegistry: ContainerRegistry,
        existingCategory: ContainerCategory? = null,
        plugin: LuminarisCorePlugin? = null
    ) {
        val defaultId = existingCategory?.id ?: "crate_"
        val defaultDisplay = if (existingCategory != null) {
            miniMessage.serialize(existingCategory.displayName)
        } else {
            TranslationManager.get("dialog.category.default_display")
        }

        val idInput = DialogInput.text("category_id", Component.text(TranslationManager.get("dialog.category.input_id")))
            .initial(defaultId)
            .maxLength(64)
            .width(300)
            .build()

        val displayNameInput = DialogInput.text("display_name", Component.text(TranslationManager.get("dialog.category.input_display_name")))
            .initial(defaultDisplay)
            .maxLength(128)
            .width(300)
            .build()

        val searchMinInput = DialogInput.text("search_time_min", Component.text(TranslationManager.get("dialog.category.input_search_time_min")))
            .initial((existingCategory?.searchTimeMin ?: 2).toString())
            .maxLength(8)
            .width(300)
            .build()

        val searchMaxInput = DialogInput.text("search_time_max", Component.text(TranslationManager.get("dialog.category.input_search_time_max")))
            .initial((existingCategory?.searchTimeMax ?: 4).toString())
            .maxLength(8)
            .width(300)
            .build()

        val coveredMatInput = DialogInput.text("covered_material", Component.text(TranslationManager.get("dialog.category.input_covered_material")))
            .initial(existingCategory?.coveredMaterial?.name ?: "BLACK_STAINED_GLASS_PANE")
            .maxLength(64)
            .width(300)
            .build()

        val searchingMatInput = DialogInput.text("searching_material", Component.text(TranslationManager.get("dialog.category.input_searching_material")))
            .initial(existingCategory?.searchingMaterial?.name ?: "STONE")
            .maxLength(64)
            .width(300)
            .build()

        val dialogTitle = Component.text(TranslationManager.get("dialog.category.title"), NamedTextColor.AQUA)
        val descriptionBody = DialogBody.plainMessage(
            TranslationManager.render("dialog.category.body")
        )

        val dialogBase = DialogBase.builder(dialogTitle)
            .body(listOf(descriptionBody))
            .inputs(listOf(idInput, displayNameInput, searchMinInput, searchMaxInput, coveredMatInput, searchingMatInput))
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
            if (sanitizedId.isEmpty()) {
                TranslationManager.send(targetPlayer, "dialog.category.error_empty_id")
                return@customClick
            }

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

            val rawMin = responseView.getText("search_time_min")?.trim()
            val rawMax = responseView.getText("search_time_max")?.trim()
            val searchTimeMin = rawMin?.toIntOrNull()?.coerceAtLeast(0) ?: (existingCategory?.searchTimeMin ?: 2)
            val searchTimeMax = rawMax?.toIntOrNull()?.coerceAtLeast(searchTimeMin) ?: (existingCategory?.searchTimeMax ?: maxOf(searchTimeMin, 4))

            val rawCovered = responseView.getText("covered_material")?.trim()?.uppercase()
            val coveredMaterial = if (!rawCovered.isNullOrBlank()) {
                Material.matchMaterial(rawCovered) ?: (existingCategory?.coveredMaterial ?: Material.BLACK_STAINED_GLASS_PANE)
            } else {
                existingCategory?.coveredMaterial ?: Material.BLACK_STAINED_GLASS_PANE
            }

            val rawSearching = responseView.getText("searching_material")?.trim()?.uppercase()
            val searchingMaterial = if (!rawSearching.isNullOrBlank()) {
                Material.matchMaterial(rawSearching) ?: (existingCategory?.searchingMaterial ?: Material.STONE)
            } else {
                existingCategory?.searchingMaterial ?: Material.STONE
            }

            // If ID was renamed, unregister the old category ID
            val oldId = existingCategory?.id
            val core = plugin ?: runCatching { LuminarisCorePlugin.instance }.getOrNull()
            if (oldId != null && !oldId.equals(sanitizedId, ignoreCase = true)) {
                containerRegistry.unregister(oldId, saveToDisk = false)
                if (core != null) {
                    for (item in core.itemRegistry.getAllItems()) {
                        if (item.allowedContainers.remove(oldId.lowercase())) {
                            item.allowedContainers.add(sanitizedId.lowercase())
                        }
                    }
                    core.itemRegistry.save()
                }
            }

            val category = if (existingCategory != null) {
                ContainerCategory(
                    id = sanitizedId,
                    displayName = displayName,
                    minRolls = existingCategory.minRolls,
                    maxRolls = existingCategory.maxRolls,
                    rarityWeights = existingCategory.rarityWeights,
                    searchTimeMin = searchTimeMin,
                    searchTimeMax = searchTimeMax,
                    coveredMaterial = coveredMaterial,
                    searchingMaterial = searchingMaterial,
                    searchSound = existingCategory.searchSound,
                    revealSound = existingCategory.revealSound
                )
            } else {
                ContainerCategory(
                    id = sanitizedId,
                    displayName = displayName,
                    minRolls = 1,
                    maxRolls = 4,
                    searchTimeMin = searchTimeMin,
                    searchTimeMax = searchTimeMax,
                    coveredMaterial = coveredMaterial,
                    searchingMaterial = searchingMaterial
                )
            }

            containerRegistry.register(category, saveToDisk = true)

            TranslationManager.send(
                targetPlayer,
                "dialog.category.success",
                "id" to sanitizedId,
                "display_name" to (rawDisplayName ?: sanitizedId)
            )

            // Return to ContainerEditGui seamlessly so administrator can edit other settings
            if (core != null) {
                core.scheduler.runAtEntity(targetPlayer) {
                    ContainerEditGui(core, category).open(targetPlayer)
                }
            }
        }, clickOptions)

        val confirmButton = ActionButton.builder(Component.text(TranslationManager.get("dialog.category.btn_save"), NamedTextColor.GREEN))
            .tooltip(Component.text(TranslationManager.get("dialog.category.tooltip_save")))
            .action(confirmAction)
            .build()

        val cancelAction = DialogAction.customClick({ _, audience ->
            val p = audience as? Player ?: player
            TranslationManager.send(p, "dialog.category.cancelled")
            val core = plugin ?: runCatching { LuminarisCorePlugin.instance }.getOrNull()
            if (core != null) {
                core.scheduler.runAtEntity(p) {
                    if (existingCategory != null) {
                        ContainerEditGui(core, existingCategory).open(p)
                    } else {
                        ContainerListGui(core).open(p)
                    }
                }
            }
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
