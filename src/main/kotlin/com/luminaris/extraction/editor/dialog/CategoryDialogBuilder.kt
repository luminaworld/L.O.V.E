package com.luminaris.extraction.editor.dialog

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.core.container.ContainerCategory
import com.luminaris.extraction.core.container.ContainerRegistry
import com.luminaris.extraction.core.item.Rarity
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
import org.bukkit.entity.Player
import java.time.Duration
import java.util.Locale

/**
 * Dialog builder for creating and configuring container / drop box categories via native Paper Dialog API.
 * Allows customizing container ID, display name, item rolls, and drop percentages per rarity tier
 * via text inputs instead of slider ranges.
 */
object CategoryDialogBuilder {

    private val miniMessage = MiniMessage.miniMessage()

    private fun formatNumber(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "0"
        val safe = maxOf(0.0, value)
        return if (safe == safe.toLong().toDouble()) {
            safe.toLong().toString()
        } else {
            String.format(Locale.US, "%.1f", safe)
        }
    }

    /**
     * Constructs and opens the container category dialog for the player.
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
            .build()

        val displayNameInput = DialogInput.text("display_name", Component.text(TranslationManager.get("dialog.category.input_display_name")))
            .initial(defaultDisplay)
            .maxLength(128)
            .build()

        val minRollsInput = DialogInput.text("min_rolls", Component.text(TranslationManager.get("dialog.category.input_min_rolls")))
            .initial((existingCategory?.minRolls ?: 1).toString())
            .maxLength(10)
            .build()

        val maxRollsInput = DialogInput.text("max_rolls", Component.text(TranslationManager.get("dialog.category.input_max_rolls")))
            .initial((existingCategory?.maxRolls ?: 4).toString())
            .maxLength(10)
            .build()

        val defaultCommonPct = if (existingCategory != null) {
            formatNumber(existingCategory.getRarityPercentage(Rarity.COMMON))
        } else "70"

        val defaultUncommonPct = if (existingCategory != null) {
            formatNumber(existingCategory.getRarityPercentage(Rarity.UNCOMMON))
        } else "20"

        val defaultRarePct = if (existingCategory != null) {
            formatNumber(existingCategory.getRarityPercentage(Rarity.RARE))
        } else "10"

        val defaultEpicPct = if (existingCategory != null) {
            formatNumber(existingCategory.getRarityPercentage(Rarity.EPIC))
        } else "0"

        val defaultLegPct = if (existingCategory != null) {
            formatNumber(existingCategory.getRarityPercentage(Rarity.LEGENDARY))
        } else "0"

        fun getRarityLabel(pctKey: String, weightKey: String): String {
            val raw = TranslationManager.instance.getRaw(pctKey)
            return if (raw != pctKey) TranslationManager.get(pctKey) else TranslationManager.get(weightKey)
        }

        val commonPctInput = DialogInput.text(
            "pct_common",
            Component.text(getRarityLabel("dialog.category.input_pct_common", "dialog.category.input_weight_common"))
        ).initial(defaultCommonPct).maxLength(10).build()

        val uncommonPctInput = DialogInput.text(
            "pct_uncommon",
            Component.text(getRarityLabel("dialog.category.input_pct_uncommon", "dialog.category.input_weight_uncommon"))
        ).initial(defaultUncommonPct).maxLength(10).build()

        val rarePctInput = DialogInput.text(
            "pct_rare",
            Component.text(getRarityLabel("dialog.category.input_pct_rare", "dialog.category.input_weight_rare"))
        ).initial(defaultRarePct).maxLength(10).build()

        val epicPctInput = DialogInput.text(
            "pct_epic",
            Component.text(getRarityLabel("dialog.category.input_pct_epic", "dialog.category.input_weight_epic"))
        ).initial(defaultEpicPct).maxLength(10).build()

        val legPctInput = DialogInput.text(
            "pct_legendary",
            Component.text(getRarityLabel("dialog.category.input_pct_legendary", "dialog.category.input_weight_legendary"))
        ).initial(defaultLegPct).maxLength(10).build()

        val dialogTitle = Component.text(TranslationManager.get("dialog.category.title"), NamedTextColor.AQUA)
        val descriptionBody = DialogBody.plainMessage(
            TranslationManager.render("dialog.category.body")
        )

        val dialogBase = DialogBase.builder(dialogTitle)
            .body(listOf(descriptionBody))
            .inputs(
                listOf(
                    idInput,
                    displayNameInput,
                    minRollsInput,
                    maxRollsInput,
                    commonPctInput,
                    uncommonPctInput,
                    rarePctInput,
                    epicPctInput,
                    legPctInput
                )
            )
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

            fun parseNumber(keys: List<String>, fallback: Double): Double {
                for (key in keys) {
                    val raw = responseView.getText(key)?.replace("%", "")?.replace(",", "")?.trim()
                    val parsed = raw?.toDoubleOrNull()
                    if (parsed != null) return parsed
                }
                return fallback
            }

            val minRolls = parseNumber(listOf("min_rolls"), (existingCategory?.minRolls ?: 1).toDouble()).toInt().coerceAtLeast(1)
            val rawMaxRolls = parseNumber(listOf("max_rolls"), (existingCategory?.maxRolls ?: 4).toDouble()).toInt()
            val maxRolls = maxOf(minRolls, rawMaxRolls)

            val weights = mutableMapOf<Rarity, Double>()
            weights[Rarity.COMMON] = parseNumber(listOf("pct_common", "weight_common"), 70.0).coerceAtLeast(0.0)
            weights[Rarity.UNCOMMON] = parseNumber(listOf("pct_uncommon", "weight_uncommon"), 20.0).coerceAtLeast(0.0)
            weights[Rarity.RARE] = parseNumber(listOf("pct_rare", "weight_rare"), 10.0).coerceAtLeast(0.0)
            weights[Rarity.EPIC] = parseNumber(listOf("pct_epic", "weight_epic"), 0.0).coerceAtLeast(0.0)
            weights[Rarity.LEGENDARY] = parseNumber(listOf("pct_legendary", "weight_legendary"), 0.0).coerceAtLeast(0.0)

            // If ID was renamed, unregister the old category ID
            val oldId = existingCategory?.id
            if (oldId != null && !oldId.equals(sanitizedId, ignoreCase = true)) {
                containerRegistry.unregister(oldId, saveToDisk = false)
                // Also update any items that specifically allowed oldId to now allow sanitizedId
                val core = plugin ?: runCatching { LuminarisCorePlugin.instance }.getOrNull()
                if (core != null) {
                    for (item in core.itemRegistry.getAllItems()) {
                        if (item.allowedContainers.remove(oldId.lowercase())) {
                            item.allowedContainers.add(sanitizedId.lowercase())
                        }
                    }
                    core.itemRegistry.save()
                }
            }

            val category = ContainerCategory(
                id = sanitizedId,
                displayName = displayName,
                minRolls = minRolls,
                maxRolls = maxRolls,
                rarityWeights = weights
            )

            containerRegistry.register(category, saveToDisk = true)

            TranslationManager.send(
                targetPlayer,
                "dialog.category.success",
                "id" to sanitizedId,
                "display_name" to (rawDisplayName ?: sanitizedId),
                "min_rolls" to minRolls,
                "max_rolls" to maxRolls,
                "wc" to formatNumber(weights[Rarity.COMMON] ?: 0.0),
                "wu" to formatNumber(weights[Rarity.UNCOMMON] ?: 0.0),
                "wr" to formatNumber(weights[Rarity.RARE] ?: 0.0),
                "we" to formatNumber(weights[Rarity.EPIC] ?: 0.0),
                "wl" to formatNumber(weights[Rarity.LEGENDARY] ?: 0.0)
            )

            // Return to ContainerListGui seamlessly
            val core = plugin ?: runCatching { LuminarisCorePlugin.instance }.getOrNull()
            if (core != null) {
                core.scheduler.runAtEntity(targetPlayer) {
                    ContainerListGui(core).open(targetPlayer)
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
                    ContainerListGui(core).open(p)
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

