package com.luminaris.extraction.editor

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.editor.dialog.CategoryDialogBuilder
import com.luminaris.extraction.editor.dialog.ItemDialogBuilder
import com.luminaris.extraction.localization.TranslationManager
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

/**
 * Command handler and tab completer for extraction editor operations (/ext editor ...).
 */
class EditorCommand(private val plugin: LuminarisCorePlugin) : CommandExecutor, TabCompleter {

    private val miniMessage = MiniMessage.miniMessage()

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        return execute(sender, label, args)
    }

    /**
     * Executes the editor subcommand logic.
     *
     * @param sender Command issuer.
     * @param label Command alias invoked.
     * @param args Subcommand arguments after 'editor'.
     */
    fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("luminaris.admin")) {
            TranslationManager.send(sender, "editor.no_permission")
            return true
        }

        if (args.isEmpty()) {
            sendEditorHelp(sender, label)
            return true
        }

        when (args[0].lowercase()) {
            "createitem" -> handleCreateItem(sender, label, args)
            "createcategory" -> handleCreateCategory(sender)
            "list" -> handleList(sender)
            "help" -> sendEditorHelp(sender, label)
            else -> {
                TranslationManager.send(sender, "editor.unknown_subcommand", "subcommand" to args[0], "label" to label)
            }
        }

        return true
    }

    private fun handleCreateItem(sender: CommandSender, label: String, args: Array<out String>) {
        if (sender !is Player) {
            TranslationManager.send(sender, "general.player_only")
            return
        }

        if (args.size < 2 || args[1].isBlank()) {
            val availableCategories = plugin.containerRegistry.getAllCategories().map { it.id }
            val catHint = if (availableCategories.isNotEmpty()) {
                TranslationManager.get("editor.available_categories", "categories" to availableCategories.joinToString(", "))
            } else {
                TranslationManager.get("editor.no_categories_note", "label" to label)
            }

            TranslationManager.send(sender, "editor.missing_category", "label" to label, "hint" to catHint)
            return
        }

        val categoryId = args[1].lowercase().trim()
        val heldItem = sender.inventory.itemInMainHand
        if (heldItem.isEmpty || heldItem.type.isAir) {
            TranslationManager.send(sender, "editor.hand_empty")
            return
        }

        if (!plugin.containerRegistry.hasCategory(categoryId)) {
            TranslationManager.send(sender, "editor.category_unregistered_note", "category" to categoryId)
        }

        ItemDialogBuilder.open(sender, categoryId, plugin.itemRegistry)
    }

    private fun handleCreateCategory(sender: CommandSender) {
        if (sender !is Player) {
            TranslationManager.send(sender, "general.player_only")
            return
        }

        CategoryDialogBuilder.open(sender, plugin.containerRegistry)
    }

    private fun handleList(sender: CommandSender) {
        val categories = plugin.containerRegistry.getAllCategories()
        val items = plugin.itemRegistry.getAllItems()

        val sb = StringBuilder()
        sb.append(TranslationManager.get("general.divider_editor")).append("\n")
        sb.append(TranslationManager.get("editor.list.title")).append("\n")
        sb.append(TranslationManager.get("editor.list.summary", "categories_count" to categories.size, "items_count" to items.size)).append("\n")
        sb.append(TranslationManager.get("general.divider_editor_sub")).append("\n")

        // 1. Categories Section
        sb.append(TranslationManager.get("editor.list.categories_header")).append("\n")
        if (categories.isEmpty()) {
            sb.append(TranslationManager.get("editor.list.categories_empty")).append("\n")
        } else {
            for (cat in categories) {
                val catItems = items.filter { it.allowedContainers.isEmpty() || it.allowedContainers.contains(cat.id) }.size
                val serializedDisplay = miniMessage.serialize(cat.displayName)
                sb.append(TranslationManager.get(
                    "editor.list.category_entry",
                    "id" to cat.id,
                    "display_name" to serializedDisplay,
                    "min_rolls" to cat.minRolls,
                    "max_rolls" to cat.maxRolls,
                    "linked_items" to catItems
                )).append("\n")
            }
        }

        sb.append(TranslationManager.get("general.divider_editor_sub")).append("\n")

        // 2. Items Section
        sb.append(TranslationManager.get("editor.list.items_header")).append("\n")
        if (items.isEmpty()) {
            sb.append(TranslationManager.get("editor.list.items_empty")).append("\n")
        } else {
            for (item in items) {
                val rarityTag = item.rarity.miniMessageTag
                val rarityName = TranslationManager.get("rarity.${item.rarity.name.lowercase()}")
                val containersStr = if (item.allowedContainers.isEmpty()) TranslationManager.get("general.all") else item.allowedContainers.joinToString(",")
                val displayStr = miniMessage.serialize(item.resolveDisplayName())
                sb.append(TranslationManager.get(
                    "editor.list.item_entry",
                    "id" to item.id,
                    "rarity_tag" to rarityTag,
                    "rarity" to rarityName,
                    "display_name" to displayStr,
                    "price" to item.price,
                    "weight" to item.weight,
                    "containers" to containersStr
                )).append("\n")
            }
        }

        sb.append(TranslationManager.get("general.divider_editor"))
        sender.sendMessage(TranslationManager.parseComponent(sb.toString()))
    }

    private fun sendEditorHelp(sender: CommandSender, label: String) {
        val help = buildString {
            append(TranslationManager.get("general.divider")).append("\n")
            append(TranslationManager.get("editor.help.title")).append("\n")
            append(TranslationManager.get("editor.help.createitem", "label" to label)).append("\n")
            append(TranslationManager.get("editor.help.createcategory", "label" to label)).append("\n")
            append(TranslationManager.get("editor.help.list", "label" to label)).append("\n")
            append(TranslationManager.get("general.divider"))
        }
        sender.sendMessage(TranslationManager.parseComponent(help))
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return complete(sender, args)
    }

    /**
     * Provides tab-completion options for the editor subcommand.
     */
    fun complete(sender: CommandSender, args: Array<out String>): List<String> {
        if (!sender.hasPermission("luminaris.admin")) {
            return emptyList()
        }

        if (args.isEmpty()) {
            return listOf("createitem", "createcategory", "list", "help")
        }

        if (args.size == 1) {
            val subcommands = listOf("createitem", "createcategory", "list", "help")
            return subcommands.filter { it.startsWith(args[0], ignoreCase = true) }
        }

        if (args.size == 2 && args[0].equals("createitem", ignoreCase = true)) {
            val categories = plugin.containerRegistry.getAllCategories().map { it.id }
            return categories.filter { it.startsWith(args[1], ignoreCase = true) }
        }

        return emptyList()
    }
}
