package com.luminaris.extraction.editor

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.editor.dialog.CategoryDialogBuilder
import com.luminaris.extraction.editor.dialog.ItemDialogBuilder
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
            sender.sendMessage(miniMessage.deserialize("<red>❌ You do not have permission to access the extraction editor.</red>"))
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
                sender.sendMessage(miniMessage.deserialize("<red>Unknown editor subcommand: ${args[0]}.</red> <yellow>Use </yellow><aqua>/$label editor help</aqua><yellow> for manual.</yellow>"))
            }
        }

        return true
    }

    private fun handleCreateItem(sender: CommandSender, label: String, args: Array<out String>) {
        if (sender !is Player) {
            sender.sendMessage(miniMessage.deserialize("<red>❌ This command can only be executed by in-game players.</red>"))
            return
        }

        if (args.size < 2 || args[1].isBlank()) {
            val availableCategories = plugin.containerRegistry.getAllCategories().map { it.id }
            val catHint = if (availableCategories.isNotEmpty()) {
                "<gray>Available categories: </gray><aqua>${availableCategories.joinToString(", ")}</aqua>"
            } else {
                "<yellow>Note: No categories registered yet. Create one with </yellow><aqua>/$label editor createcategory</aqua>"
            }

            sender.sendMessage(miniMessage.deserialize("""
                <red>❌ Missing category argument!</red>
                <yellow>Usage: </yellow><white>/$label editor createitem <category></white>
                $catHint
            """.trimIndent()))
            return
        }

        val categoryId = args[1].lowercase().trim()
        val heldItem = sender.inventory.itemInMainHand
        if (heldItem.isEmpty || heldItem.type.isAir) {
            sender.sendMessage(miniMessage.deserialize(
                "<red>❌ Your main hand is empty! Please hold the item you want to register in your main hand.</red>"
            ))
            return
        }

        if (!plugin.containerRegistry.hasCategory(categoryId)) {
            sender.sendMessage(miniMessage.deserialize(
                "<yellow>⚠ Note: Category </yellow><aqua>$categoryId</aqua><yellow> is not in ContainerRegistry yet. Registering anyway...</yellow>"
            ))
        }

        ItemDialogBuilder.open(sender, categoryId, plugin.itemRegistry)
    }

    private fun handleCreateCategory(sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(miniMessage.deserialize("<red>❌ This command can only be executed by in-game players.</red>"))
            return
        }

        CategoryDialogBuilder.open(sender, plugin.containerRegistry)
    }

    private fun handleList(sender: CommandSender) {
        val categories = plugin.containerRegistry.getAllCategories()
        val items = plugin.itemRegistry.getAllItems()

        val sb = StringBuilder()
        sb.append("<dark_gray>==================================================</dark_gray>\n")
        sb.append("<gradient:#00FFA3:#00B8FF><bold>LUMINARIS EXTRACTION REGISTRY OVERVIEW</bold></gradient>\n")
        sb.append("<gray>Total Categories: </gray><yellow>${categories.size}</yellow><gray> | Total Items: </gray><yellow>${items.size}</yellow>\n")
        sb.append("<dark_gray>--------------------------------------------------</dark_gray>\n")

        // 1. Categories Section
        sb.append("<gold><bold>[CONTAINER CATEGORIES]</bold></gold>\n")
        if (categories.isEmpty()) {
            sb.append("<gray>  <i>No container categories registered yet.</i></gray>\n")
        } else {
            for (cat in categories) {
                val catItems = items.filter { it.allowedContainers.isEmpty() || it.allowedContainers.contains(cat.id) }.size
                val serializedDisplay = miniMessage.serialize(cat.displayName)
                sb.append("  <yellow>•</yellow> <aqua><bold>${cat.id}</bold></aqua> <gray>-</gray> $serializedDisplay ")
                sb.append("<gray>(Rolls: </gray><green>${cat.minRolls}-${cat.maxRolls}</green><gray> | Linked Items: </gray><yellow>$catItems</yellow><gray>)</gray>\n")
            }
        }

        sb.append("<dark_gray>--------------------------------------------------</dark_gray>\n")

        // 2. Items Section
        sb.append("<gold><bold>[EXTRACTION ITEMS]</bold></gold>\n")
        if (items.isEmpty()) {
            sb.append("<gray>  <i>No extraction items registered yet.</i></gray>\n")
        } else {
            for (item in items) {
                val rarityTag = item.rarity.miniMessageTag
                val containersStr = if (item.allowedContainers.isEmpty()) "ALL" else item.allowedContainers.joinToString(",")
                val displayStr = miniMessage.serialize(item.resolveDisplayName())
                sb.append("  <yellow>•</yellow> <yellow>${item.id}</yellow> [${rarityTag}${item.rarity.name}</color>] $displayStr ")
                sb.append("<gray>-</gray> <gold>$${item.price}</gold> <gray>| W: </gray><white>${item.weight}</white> <gray>| In: </gray><aqua>[$containersStr]</aqua>\n")
            }
        }

        sb.append("<dark_gray>==================================================</dark_gray>")
        sender.sendMessage(miniMessage.deserialize(sb.toString()))
    }

    private fun sendEditorHelp(sender: CommandSender, label: String) {
        val help = """
            <dark_gray>----------------------------------------</dark_gray>
            <gradient:#00FFA3:#00B8FF><bold>LUMINARIS EXTRACTION IN-GAME EDITOR</bold></gradient>
            <yellow>/$label editor createitem <category></yellow>
            <gray>  └ Register item in your main hand into a category via Dialog UI</gray>
            <yellow>/$label editor createcategory</yellow>
            <gray>  └ Open category configuration Dialog (rolls, title, ID)</gray>
            <yellow>/$label editor list</yellow>
            <gray>  └ View comprehensive report of all registered items & categories</gray>
            <dark_gray>----------------------------------------</dark_gray>
        """.trimIndent()
        sender.sendMessage(miniMessage.deserialize(help))
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
