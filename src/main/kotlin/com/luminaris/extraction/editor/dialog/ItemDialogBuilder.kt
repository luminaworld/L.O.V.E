package com.luminaris.extraction.editor.dialog

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.core.item.ExtractionItem
import com.luminaris.extraction.core.item.ItemRegistry
import com.luminaris.extraction.core.item.Rarity
import com.luminaris.extraction.editor.gui.ItemListGui
import com.luminaris.extraction.localization.TranslationManager
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.dialog.DialogResponseView
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import io.papermc.paper.registry.data.dialog.input.TextDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.time.Duration
import java.util.Locale

/**
 * Dialog builder for registering and editing extraction loot items
 * using the native Paper Dialog API.
 * Supports customizing item ID, display name, lore, rarity, price, drop chance (percentage), and drop containers
 * via text inputs instead of slider ranges.
 */
object ItemDialogBuilder {

    private val miniMessage = MiniMessage.miniMessage()

    private fun formatNumber(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "0"
        val safe = maxOf(0.0, value)
        return if (safe == safe.toLong().toDouble()) {
            safe.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", safe)
        }
    }

    /**
     * Constructs and displays the item registration/editor dialog to the player.
     *
     * @param player Target player configuring the item.
     * @param itemRegistry Registry service for extraction items.
     * @param itemStack Target ItemStack being configured (defaults to player's held hand item).
     * @param existingItem Optional existing extraction item definition being edited.
     * @param category Optional initial container category.
     * @param plugin Optional plugin instance for scheduling GUI navigation.
     */
    fun open(
        player: Player,
        itemRegistry: ItemRegistry,
        itemStack: ItemStack? = null,
        existingItem: ExtractionItem? = null,
        category: String? = null,
        plugin: LuminarisCorePlugin? = null
    ) {
        val baseStack = existingItem?.itemStack ?: itemStack ?: player.inventory.itemInMainHand
        if (baseStack.isEmpty || baseStack.type.isAir) {
            TranslationManager.send(player, "editor.hand_empty")
            return
        }

        // Clone item to capture its exact state
        val capturedStack = baseStack.clone()

        // 1. Initial ID
        val initialId = existingItem?.id ?: capturedStack.type.name.lowercase()

        // 2. Initial Display Name
        val initialDisplayName = if (capturedStack.hasItemMeta() && capturedStack.itemMeta.hasDisplayName()) {
            miniMessage.serialize(capturedStack.itemMeta.displayName()!!)
        } else {
            capturedStack.type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
        }

        // 3. Initial Lore
        val initialLore = if (capturedStack.hasItemMeta() && capturedStack.itemMeta.hasLore()) {
            capturedStack.itemMeta.lore()!!.joinToString("\n") { miniMessage.serialize(it) }
        } else {
            ""
        }

        // 4. Initial Price, Rarity, Weight (Drop Chance %), Containers
        val initialPrice = existingItem?.price ?: 100.0
        val initialRarity = existingItem?.rarity ?: Rarity.fromPrice(initialPrice)
        val initialWeight = existingItem?.weight ?: 20.0
        val initialContainers = when {
            existingItem != null -> if (existingItem.allowedContainers.isEmpty()) "" else existingItem.allowedContainers.joinToString(", ")
            !category.isNullOrBlank() -> category
            else -> ""
        }

        // Build Dialog Inputs
        val itemIdInput = DialogInput.text("item_id", Component.text(TranslationManager.get("dialog.item.input_id")))
            .initial(initialId)
            .maxLength(64)
            .build()

        val displayNameInput = DialogInput.text("display_name", Component.text(TranslationManager.get("dialog.item.input_display_name")))
            .initial(initialDisplayName)
            .maxLength(128)
            .build()

        val loreInput = DialogInput.text("lore", Component.text(TranslationManager.get("dialog.item.input_lore")))
            .initial(initialLore)
            .maxLength(512)
            .multiline(TextDialogInput.MultilineOptions.create(10, 4))
            .build()

        // Rarity Selection Dropdown
        val rarityOptions = Rarity.entries.map { r ->
            val localizedName = TranslationManager.get("rarity.${r.name.lowercase()}")
            SingleOptionDialogInput.OptionEntry.create(
                r.name.lowercase(),
                TranslationManager.parseComponent("${r.miniMessageTag}<bold>$localizedName</bold></color>"),
                r == initialRarity
            )
        }
        val rarityInput = DialogInput.singleOption(
            "rarity",
            Component.text(TranslationManager.get("dialog.item.input_rarity")),
            rarityOptions
        ).build()

        val priceInput = DialogInput.text("price", Component.text(TranslationManager.get("dialog.item.input_price")))
            .initial(formatNumber(initialPrice))
            .maxLength(16)
            .build()

        val chanceLabel = if (TranslationManager.instance.getRaw("dialog.item.input_chance") != "dialog.item.input_chance") {
            TranslationManager.get("dialog.item.input_chance")
        } else {
            TranslationManager.get("dialog.item.input_weight")
        }

        val weightInput = DialogInput.text("weight", Component.text(chanceLabel))
            .initial(formatNumber(initialWeight))
            .maxLength(10)
            .build()

        val containersInput = DialogInput.text("containers", Component.text(TranslationManager.get("dialog.item.input_containers")))
            .initial(initialContainers)
            .maxLength(256)
            .build()

        // Build DialogBase
        val dialogTitle = Component.text(TranslationManager.get("dialog.item.title"), NamedTextColor.GOLD)
        val descriptionBody = DialogBody.plainMessage(
            TranslationManager.render(
                "dialog.item.body",
                "material" to capturedStack.type.name,
                "category" to (if (initialContainers.isBlank()) TranslationManager.get("general.all") else initialContainers)
            )
        )

        val dialogBase = DialogBase.builder(dialogTitle)
            .body(listOf(descriptionBody))
            .inputs(
                listOf(
                    itemIdInput,
                    displayNameInput,
                    loreInput,
                    rarityInput,
                    priceInput,
                    weightInput,
                    containersInput
                )
            )
            .canCloseWithEscape(true)
            .pause(false)
            .build()

        val clickOptions = ClickCallback.Options.builder()
            .lifetime(Duration.ofMinutes(5))
            .build()

        // Confirm Action
        val confirmAction = DialogAction.customClick({ responseView: DialogResponseView, audience: Audience ->
            val targetPlayer = audience as? Player ?: player

            val rawId = responseView.getText("item_id")?.trim()
            if (rawId.isNullOrBlank()) {
                TranslationManager.send(targetPlayer, "dialog.item.error_empty_id")
                return@customClick
            }

            val sanitizedId = rawId.lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_-]"), "")
            if (sanitizedId.isEmpty()) {
                TranslationManager.send(targetPlayer, "dialog.item.error_empty_id")
                return@customClick
            }

            val customDisplayName = responseView.getText("display_name")?.trim()
            val rawLore = responseView.getText("lore")?.trim()
            val rawRarity = responseView.getText("rarity")?.trim()

            val rawPrice = responseView.getText("price")?.replace("$", "")?.replace(",", "")?.trim()
            val price = rawPrice?.toDoubleOrNull()?.coerceAtLeast(0.0) ?: (existingItem?.price ?: 100.0)

            val rawWeight = (responseView.getText("weight") ?: responseView.getText("chance"))
                ?.replace("%", "")?.replace(",", "")?.trim()
            val weight = rawWeight?.toDoubleOrNull()?.coerceAtLeast(0.01) ?: (existingItem?.weight ?: 20.0)

            val rawContainers = responseView.getText("containers")?.trim()

            // Prepare final stack with updated display name and lore
            val finalStack = capturedStack.clone()
            finalStack.editMeta { meta ->
                if (!customDisplayName.isNullOrBlank()) {
                    meta.displayName(TranslationManager.parseComponent(customDisplayName))
                } else {
                    meta.displayName(null)
                }

                if (!rawLore.isNullOrBlank()) {
                    val lines = rawLore.split("\n", "\\n")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    meta.lore(lines.map { TranslationManager.parseComponent(it) })
                } else {
                    meta.lore(emptyList())
                }
            }

            // Resolve rarity
            val rarity = if (!rawRarity.isNullOrBlank()) {
                Rarity.fromString(rawRarity)
            } else {
                Rarity.fromPrice(price)
            }

            // Resolve allowed containers
            val allowedContainers = if (!rawContainers.isNullOrBlank() &&
                !rawContainers.equals("all", ignoreCase = true) &&
                !rawContainers.equals("ทั้งหมด", ignoreCase = true)
            ) {
                rawContainers.split(",", ";", " ")
                    .map { it.trim().lowercase() }
                    .filter { it.isNotEmpty() }
                    .toMutableSet()
            } else {
                mutableSetOf()
            }

            // If ID renamed, unregister old ID
            val oldId = existingItem?.id
            if (oldId != null && !oldId.equals(sanitizedId, ignoreCase = true)) {
                itemRegistry.unregister(oldId, saveToDisk = false)
            }

            val extractionItem = ExtractionItem(
                id = sanitizedId,
                itemStack = finalStack,
                price = price,
                rarity = rarity,
                weight = weight,
                allowedContainers = allowedContainers
            )

            itemRegistry.register(extractionItem, saveToDisk = true)

            val containerLabel = if (allowedContainers.isEmpty()) {
                TranslationManager.get("general.all")
            } else {
                allowedContainers.joinToString(", ")
            }

            TranslationManager.send(
                targetPlayer,
                "dialog.item.success",
                "id" to sanitizedId,
                "display_name" to (customDisplayName ?: finalStack.type.name),
                "category" to containerLabel,
                "price" to formatNumber(price),
                "weight" to formatNumber(weight),
                "rarity_tag" to rarity.miniMessageTag,
                "rarity" to TranslationManager.get("rarity.${rarity.name.lowercase()}")
            )

            // Return to ItemListGui seamlessly
            val core = plugin ?: runCatching { LuminarisCorePlugin.instance }.getOrNull()
            if (core != null) {
                core.scheduler.runAtEntity(targetPlayer) {
                    ItemListGui(core).open(targetPlayer)
                }
            }
        }, clickOptions)

        val confirmButton = ActionButton.builder(Component.text(TranslationManager.get("dialog.item.btn_save"), NamedTextColor.GREEN))
            .tooltip(Component.text(TranslationManager.get("dialog.item.tooltip_save")))
            .action(confirmAction)
            .build()

        val cancelAction = DialogAction.customClick({ _, audience ->
            val p = audience as? Player ?: player
            TranslationManager.send(p, "dialog.item.cancelled")
            val core = plugin ?: runCatching { LuminarisCorePlugin.instance }.getOrNull()
            if (core != null) {
                core.scheduler.runAtEntity(p) {
                    ItemListGui(core).open(p)
                }
            }
        }, clickOptions)

        val cancelButton = ActionButton.builder(Component.text(TranslationManager.get("dialog.item.btn_cancel"), NamedTextColor.RED))
            .tooltip(Component.text(TranslationManager.get("dialog.item.tooltip_cancel")))
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

