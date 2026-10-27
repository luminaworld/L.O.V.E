package com.luminaris.extraction.editor.dialog

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.core.item.ExtractionItem
import com.luminaris.extraction.core.item.ItemRegistry
import com.luminaris.extraction.core.item.Rarity
import com.luminaris.extraction.editor.gui.ItemEditorGui
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
 * Dialog builder for registering and editing extraction loot items using the native Paper Dialog API.
 * Contains fields for item ID, name, lore, rarity selector, and price.
 * Drop chance is determined by drop boxes, and drop boxes are configured via GUI.
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
     * Constructs and displays the item editor dialog to the player.
     *
     * @param player Target player configuring the item.
     * @param itemRegistry Registry service for extraction items.
     * @param itemStack Target ItemStack being configured.
     * @param existingItem Optional existing extraction item definition being edited.
     * @param plugin Optional plugin instance for scheduling GUI navigation.
     * @param currentAllowedContainers Optional allowed containers set to preserve.
     * @param onComplete Optional callback when the dialog successfully saves.
     */
    fun open(
        player: Player,
        itemRegistry: ItemRegistry,
        itemStack: ItemStack? = null,
        existingItem: ExtractionItem? = null,
        plugin: LuminarisCorePlugin? = null,
        currentAllowedContainers: Set<String>? = null,
        initialSearchTime: Int? = null,
        initialRevealSound: String? = null,
        onComplete: ((ExtractionItem) -> Unit)? = null
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

        // 4. Initial Price & Rarity
        val initialPrice = existingItem?.price ?: 100.0
        val initialRarity = existingItem?.rarity ?: Rarity.fromPrice(initialPrice)
        val defaultTime = initialSearchTime ?: existingItem?.searchTime ?: ExtractionItem.defaultSearchTime(initialRarity)
        val defaultSound = initialRevealSound ?: existingItem?.revealSound ?: ""

        // Build Dialog Inputs:
        // 1. Item ID
        val itemIdInput = DialogInput.text("item_id", Component.text(TranslationManager.get("dialog.item.input_id")))
            .initial(initialId)
            .maxLength(64)
            .width(300)
            .build()

        // 2. Display Name
        val displayNameInput = DialogInput.text("display_name", Component.text(TranslationManager.get("dialog.item.input_display_name")))
            .initial(initialDisplayName)
            .maxLength(128)
            .width(300)
            .build()

        // 3. Lore
        val loreInput = DialogInput.text("lore", Component.text(TranslationManager.get("dialog.item.input_lore")))
            .initial(initialLore)
            .maxLength(1024)
            .width(300)
            .multiline(TextDialogInput.MultilineOptions.create(16, 100))
            .build()

        // 4. Rarity Selection Dropdown
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
        ).width(300).build()

        // 5. Price
        val priceInput = DialogInput.text("price", Component.text(TranslationManager.get("dialog.item.input_price")))
            .initial(formatNumber(initialPrice))
            .maxLength(16)
            .width(300)
            .build()

        // 6. Search Duration
        val searchTimeInput = DialogInput.text("search_time", Component.text(TranslationManager.get("dialog.item.input_search_time")))
            .initial(defaultTime.toString())
            .maxLength(8)
            .width(300)
            .build()

        // 7. Reveal Sound
        val revealSoundInput = DialogInput.text("reveal_sound", Component.text(TranslationManager.get("dialog.item.input_reveal_sound")))
            .initial(defaultSound)
            .maxLength(64)
            .width(300)
            .build()

        // Build DialogBase
        val dialogTitle = Component.text(TranslationManager.get("dialog.item.title"), NamedTextColor.GOLD)
        val rawBody = TranslationManager.get("dialog.item.body", "material" to capturedStack.type.name)
            .replace(Regex("<color:[^>]+>\\s*สำหรับกล่อง:.*$", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*สำหรับกล่อง:.*$", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*for (?:categories|category|boxes|box):.*$", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\{category\\}", RegexOption.IGNORE_CASE), "")
            .trim()
        val descriptionBody = DialogBody.plainMessage(TranslationManager.parseComponent(rawBody))

        val dialogBase = DialogBase.builder(dialogTitle)
            .body(listOf(descriptionBody))
            .inputs(
                listOf(
                    itemIdInput,
                    displayNameInput,
                    loreInput,
                    rarityInput,
                    priceInput,
                    searchTimeInput,
                    revealSoundInput
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

            // Resolve search duration and reveal sound
            val rawSearchTime = responseView.getText("search_time")?.trim()
            val searchTime = rawSearchTime?.toIntOrNull()?.coerceAtLeast(0) ?: (initialSearchTime ?: existingItem?.searchTime ?: ExtractionItem.defaultSearchTime(rarity))
            val rawRevealSound = responseView.getText("reveal_sound")?.trim()
            val revealSound = if (!rawRevealSound.isNullOrBlank()) rawRevealSound else null

            // Preserve allowed containers
            val preservedAllowedContainers = when {
                currentAllowedContainers != null -> currentAllowedContainers.toMutableSet()
                existingItem != null -> existingItem.allowedContainers.toMutableSet()
                else -> mutableSetOf()
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
                weight = existingItem?.weight ?: 1.0,
                allowedContainers = preservedAllowedContainers,
                searchTime = searchTime,
                revealSound = revealSound
            )

            itemRegistry.register(extractionItem, saveToDisk = true)

            TranslationManager.send(
                targetPlayer,
                "dialog.item.success",
                "id" to sanitizedId,
                "display_name" to (customDisplayName ?: finalStack.type.name),
                "price" to formatNumber(price),
                "rarity_tag" to rarity.miniMessageTag,
                "rarity" to TranslationManager.get("rarity.${rarity.name.lowercase()}")
            )

            val core = plugin ?: runCatching { LuminarisCorePlugin.instance }.getOrNull()
            if (core != null) {
                core.scheduler.runAtEntity(targetPlayer) {
                    if (onComplete != null) {
                        onComplete(extractionItem)
                    } else {
                        ItemEditorGui(core, finalStack, extractionItem).open(targetPlayer)
                    }
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
                    ItemEditorGui(
                        plugin = core,
                        targetStack = capturedStack,
                        existingItem = existingItem,
                        initialRarity = initialRarity,
                        initialPrice = initialPrice,
                        initialAllowedContainers = currentAllowedContainers?.toMutableSet(),
                        initialItemId = existingItem?.id,
                        initialSearchTime = initialSearchTime,
                        initialRevealSound = initialRevealSound
                    ).open(p)
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
