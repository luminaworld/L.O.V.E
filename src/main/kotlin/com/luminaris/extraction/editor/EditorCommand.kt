package com.luminaris.extraction.editor

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.editor.gui.EditorMainMenuGui
import com.luminaris.extraction.localization.TranslationManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

/**
 * Command handler for extraction editor operations (/ext editor).
 * Opens the comprehensive all-in-one In-Game Editor GUI.
 */
class EditorCommand(private val plugin: LuminarisCorePlugin) : CommandExecutor, TabCompleter {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        return execute(sender, label, args)
    }

    /**
     * Executes the editor command logic, supporting direct subcommands and the master editor GUI.
     *
     * @param sender Command issuer.
     * @param label Command alias invoked.
     * @param args Command arguments.
     */
    fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("luminaris.admin") && !sender.hasPermission("luminaris.editor")) {
            TranslationManager.send(sender, "editor.no_permission")
            return true
        }

        if (args.isEmpty()) {
            if (sender !is Player) {
                TranslationManager.send(sender, "general.player_only")
                return true
            }
            EditorMainMenuGui.open(sender, plugin)
            return true
        }

        val sub = args[0].lowercase()
        when (sub) {
            "items", "item" -> {
                if (sender !is Player) {
                    TranslationManager.send(sender, "general.player_only")
                    return true
                }
                com.luminaris.extraction.editor.gui.ItemListGui(plugin).open(sender)
            }
            "containers", "container", "boxes" -> {
                if (sender !is Player) {
                    TranslationManager.send(sender, "general.player_only")
                    return true
                }
                com.luminaris.extraction.editor.gui.ContainerListGui(plugin).open(sender)
            }
            "createitem", "additem" -> {
                if (sender !is Player) {
                    TranslationManager.send(sender, "general.player_only")
                    return true
                }
                val category = if (args.size > 1) args[1].lowercase() else null
                com.luminaris.extraction.editor.dialog.ItemDialogBuilder.open(
                    player = sender,
                    itemRegistry = plugin.itemRegistry,
                    plugin = plugin,
                    currentAllowedContainers = if (category != null) setOf(category) else null
                )
            }
            "createcategory", "addcategory", "createbox" -> {
                if (sender !is Player) {
                    TranslationManager.send(sender, "general.player_only")
                    return true
                }
                com.luminaris.extraction.editor.dialog.CategoryDialogBuilder.open(
                    player = sender,
                    containerRegistry = plugin.containerRegistry,
                    plugin = plugin
                )
            }
            "reload" -> {
                plugin.reloadAllConfigurations()
                TranslationManager.send(sender, "editor.gui.reload_success")
            }
            "list" -> {
                renderRegistryList(sender)
            }
            "help" -> {
                TranslationManager.send(sender, "editor.help.title")
                TranslationManager.send(sender, "editor.help.createitem", "label" to label)
                TranslationManager.send(sender, "editor.help.createcategory", "label" to label)
                TranslationManager.send(sender, "editor.help.list", "label" to label)
            }
            else -> {
                TranslationManager.send(sender, "editor.unknown_subcommand", "subcommand" to sub, "label" to label)
            }
        }
        return true
    }

    private fun renderRegistryList(sender: CommandSender) {
        val categories = plugin.containerRegistry.getAllCategories()
        val items = plugin.itemRegistry.getAllItems()

        TranslationManager.send(sender, "editor.list.title")
        TranslationManager.send(
            sender,
            "editor.list.summary",
            "categories_count" to categories.size,
            "items_count" to items.size
        )

        TranslationManager.send(sender, "editor.list.categories_header")
        if (categories.isEmpty()) {
            TranslationManager.send(sender, "editor.list.categories_empty")
        } else {
            for (cat in categories) {
                val linked = plugin.itemRegistry.getItemsForCategory(cat.id).size
                TranslationManager.send(
                    sender,
                    "editor.list.category_entry",
                    "id" to cat.id,
                    "display_name" to TranslationManager.serializeComponent(cat.displayName),
                    "min_rolls" to cat.minRolls,
                    "max_rolls" to cat.maxRolls,
                    "linked_items" to linked
                )
            }
        }

        TranslationManager.send(sender, "editor.list.items_header")
        if (items.isEmpty()) {
            TranslationManager.send(sender, "editor.list.items_empty")
        } else {
            for (item in items) {
                val disp = if (item.itemStack.hasItemMeta() && item.itemStack.itemMeta.hasDisplayName()) {
                    TranslationManager.serializeComponent(item.itemStack.itemMeta.displayName()!!)
                } else {
                    item.itemStack.type.name
                }
                val rarityName = TranslationManager.get("rarity.${item.rarity.name.lowercase()}")
                val catText = if (item.allowedContainers.isEmpty()) TranslationManager.get("general.all") else item.allowedContainers.joinToString(", ")
                TranslationManager.send(
                    sender,
                    "editor.list.item_entry",
                    "id" to item.id,
                    "rarity_tag" to item.rarity.miniMessageTag,
                    "rarity" to rarityName,
                    "display_name" to disp,
                    "price" to String.format("%.2f", item.price),
                    "weight" to item.weight,
                    "containers" to catText
                )
            }
        }
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
     * Tab completion for /ext editor subcommands.
     */
    fun complete(sender: CommandSender, args: Array<out String>): List<String> {
        if (!sender.hasPermission("luminaris.admin") && !sender.hasPermission("luminaris.editor")) {
            return emptyList()
        }

        if (args.size == 1) {
            val subcommands = listOf("items", "containers", "createitem", "createcategory", "list", "reload", "help")
            return subcommands.filter { it.startsWith(args[0], ignoreCase = true) }
        }

        if (args.size == 2 && args[0].equals("createitem", ignoreCase = true)) {
            val categories = plugin.containerRegistry.getAllCategories().map { it.id }
            return categories.filter { it.startsWith(args[1], ignoreCase = true) }
        }

        return emptyList()
    }
}
