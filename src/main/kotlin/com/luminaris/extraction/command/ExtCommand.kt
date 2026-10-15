package com.luminaris.extraction.command

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.util.FoliaScheduler
import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender

/**
 * Base command executor and tab completer for /ext command using Paper's BasicCommand API.
 */
class ExtCommand(private val plugin: LuminarisCorePlugin) : BasicCommand {

    private val miniMessage = MiniMessage.miniMessage()

    override fun execute(stack: CommandSourceStack, args: Array<out String>) {
        val sender = stack.sender
        val label = "ext"
        if (args.isEmpty()) {
            sendHelp(sender)
            return
        }

        when (args[0].lowercase()) {
            "editor" -> {
                plugin.editorCommand.execute(sender, label, args.drop(1).toTypedArray())
            }

            "reload" -> {
                if (!sender.hasPermission("luminaris.admin")) {
                    sender.sendMessage(miniMessage.deserialize("<red>You do not have permission to execute this command.</red>"))
                    return
                }
                plugin.reloadAllConfigurations()
                sender.sendMessage(miniMessage.deserialize("<green>✔ [LuminarisCore] All extraction configurations and registries reloaded successfully!</green>"))
            }

            "status" -> {
                val platformName = if (FoliaScheduler.isFolia) "<light_purple>Folia (Regionized Multi-Threaded)</light_purple>" else "<aqua>Paper (Universal Scheduler)</aqua>"
                val itemsCount = plugin.itemRegistry.getAllItems().size
                val categoriesCount = plugin.containerRegistry.getAllCategories().size
                val casesCount = plugin.safeCaseManager.getAllDefinitions().size
                val instancesCount = plugin.instanceWorldService.getActiveInstanceCount()

                val statusMessage = """
                    <dark_gray>----------------------------------------</dark_gray>
                    <gradient:#00FFA3:#00B8FF><bold>LUMINARIS EXTRACTION ENGINE STATUS</bold></gradient>
                    <gray>Version:</gray> <white>${plugin.pluginMeta.version}</white>
                    <gray>Threading Model:</gray> $platformName
                    <gray>Active Extraction Items:</gray> <yellow>$itemsCount</yellow>
                    <gray>Container Categories:</gray> <yellow>$categoriesCount</yellow>
                    <gray>Safe Container Profiles:</gray> <yellow>$casesCount</yellow>
                    <gray>Active Match Instances:</gray> <yellow>$instancesCount</yellow>
                    <dark_gray>----------------------------------------</dark_gray>
                """.trimIndent()
                sender.sendMessage(miniMessage.deserialize(statusMessage))
            }

            "case" -> {
                if (!sender.hasPermission("luminaris.admin")) {
                    sender.sendMessage(miniMessage.deserialize("<red>You do not have permission to execute this command.</red>"))
                    return
                }
                handleCaseCommand(sender, label, args.drop(1).toTypedArray())
            }

            "chest" -> {
                if (!sender.hasPermission("luminaris.admin") && !sender.hasPermission("luminaris.editor")) {
                    sender.sendMessage(miniMessage.deserialize("<red>You do not have permission to execute this command.</red>"))
                    return
                }
                handleChestCommand(sender, label, args.drop(1).toTypedArray())
            }

            "match" -> {
                if (!sender.hasPermission("luminaris.admin")) {
                    sender.sendMessage(miniMessage.deserialize("<red>You do not have permission to execute this command.</red>"))
                    return
                }
                plugin.matchManager.handleCommand(sender, label, args.drop(1).toTypedArray())
            }

            "help" -> sendHelp(sender)

            else -> {
                sender.sendMessage(miniMessage.deserialize("<red>Unknown subcommand: ${args[0]}. Use </red><yellow>/$label help</yellow><red> for available commands.</red>"))
            }
        }
    }

    private fun handleCaseCommand(sender: CommandSender, label: String, args: Array<out String>) {
        if (args.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize("<red>Usage: </red><yellow>/$label case <give|list></yellow>"))
            return
        }

        when (args[0].lowercase()) {
            "list" -> {
                val cases = plugin.safeCaseManager.getAllDefinitions()
                if (cases.isEmpty()) {
                    sender.sendMessage(miniMessage.deserialize("<gray>No safe container definitions registered.</gray>"))
                    return
                }
                sender.sendMessage(miniMessage.deserialize("<gradient:#00FFA3:#00B8FF><bold>--- Registered Safe Containers (${cases.size}) ---</bold></gradient>"))
                for (caseDef in cases) {
                    val titleFormatted = miniMessage.serialize(caseDef.displayName)
                    sender.sendMessage(
                        miniMessage.deserialize(
                            "<dark_gray>▪ </dark_gray><yellow>${caseDef.id}</yellow> <gray>-</gray> $titleFormatted <gray>(Cap: </gray><aqua>${caseDef.capacity}</aqua><gray>, Mat: </gray><green>${caseDef.material.name}</green><gray>)</gray>"
                        )
                    )
                }
            }

            "give" -> {
                if (args.size < 3) {
                    sender.sendMessage(miniMessage.deserialize("<red>Usage: </red><yellow>/$label case give <player> <caseId></yellow>"))
                    return
                }
                val targetName = args[1]
                val targetPlayer = org.bukkit.Bukkit.getPlayerExact(targetName)
                if (targetPlayer == null) {
                    sender.sendMessage(miniMessage.deserialize("<red>Player not found: $targetName</red>"))
                    return
                }
                val caseId = args[2].lowercase()
                val safeItem = plugin.safeCaseManager.createSafeItemStack(caseId)
                if (safeItem == null) {
                    sender.sendMessage(miniMessage.deserialize("<red>Unknown safe container ID: $caseId. Use </red><yellow>/$label case list</yellow>"))
                    return
                }

                // Folia Safe: Grant item to target player within entity scheduler context
                plugin.scheduler.runAtEntity(targetPlayer, runnable = {
                    val leftover = targetPlayer.inventory.addItem(safeItem)
                    if (leftover.isNotEmpty()) {
                        for (item in leftover.values) {
                            targetPlayer.world.dropItemNaturally(targetPlayer.location, item)
                        }
                    }
                })

                sender.sendMessage(miniMessage.deserialize("<green>✔ Successfully granted Safe Container </green><yellow>$caseId</yellow><green> to </green><aqua>${targetPlayer.name}</aqua><green>!</green>"))
                targetPlayer.sendMessage(miniMessage.deserialize("<green>You received a secure safe container: </green>").append(safeItem.itemMeta?.displayName() ?: net.kyori.adventure.text.Component.text(caseId)))
            }

            else -> {
                sender.sendMessage(miniMessage.deserialize("<red>Unknown case action: ${args[0]}. Usage: </red><yellow>/$label case <give|list></yellow>"))
            }
        }
    }

    private fun handleChestCommand(sender: CommandSender, label: String, args: Array<out String>) {
        if (args.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize("<red>Usage: </red><yellow>/$label chest <get|list></yellow>"))
            return
        }

        when (args[0].lowercase()) {
            "get" -> {
                if (args.size < 2) {
                    val available = plugin.containerRegistry.getAllCategories().map { it.id }
                    val catHint = if (available.isNotEmpty()) {
                        "<gray>Available categories: </gray><aqua>${available.joinToString(", ")}</aqua>"
                    } else {
                        "<yellow>No categories registered. Create one with /ext editor createcategory</yellow>"
                    }
                    sender.sendMessage(
                        miniMessage.deserialize(
                            """
                            <red>Missing category ID!</red>
                            <yellow>Usage: </yellow><white>/$label chest get <category_id> [chest|barrel]</white>
                            $catHint
                            """.trimIndent()
                        )
                    )
                    return
                }

                val player = sender as? org.bukkit.entity.Player
                if (player == null) {
                    sender.sendMessage(miniMessage.deserialize("<red>This command can only be executed by an in-game player.</red>"))
                    return
                }

                val categoryId = args[1].lowercase().trim()
                val material = if (args.size >= 3 && args[2].equals("barrel", ignoreCase = true)) {
                    org.bukkit.Material.BARREL
                } else {
                    org.bukkit.Material.CHEST
                }

                if (!plugin.containerRegistry.hasCategory(categoryId)) {
                    sender.sendMessage(
                        miniMessage.deserialize(
                            "<yellow>⚠ Notice: Category </yellow><aqua>$categoryId</aqua><yellow> is not currently in ContainerRegistry, but marker will still be created.</yellow>"
                        )
                    )
                }

                val category = plugin.containerRegistry.getCategory(categoryId)
                val catDisplay = category?.let { miniMessage.serialize(it.displayName) }

                val markerItem = com.luminaris.extraction.editor.tool.ContainerMarkerTool.createContainerMarker(
                    categoryId = categoryId,
                    material = material,
                    categoryDisplayName = catDisplay
                )

                // Folia Safe: Grant item in entity scheduler context
                plugin.scheduler.runAtEntity(player, runnable = {
                    val leftover = player.inventory.addItem(markerItem)
                    if (leftover.isNotEmpty()) {
                        for (item in leftover.values) {
                            player.world.dropItemNaturally(player.location, item)
                        }
                    }
                })

                sender.sendMessage(
                    miniMessage.deserialize(
                        "<green>✔ Granted Container Marker tool for category </green><yellow>$categoryId</yellow> <gray>(${material.name})</gray><green> to </green><aqua>${player.name}</aqua><green>!</green>"
                    )
                )
            }

            "list" -> {
                val templates = plugin.templateStorage.getAllTemplates()
                val sb = StringBuilder()
                sb.append("<dark_gray>----------------------------------------</dark_gray>\n")
                sb.append("<gradient:#00FFA3:#00B8FF><bold>REGISTERED TEMPLATE CONTAINERS</bold></gradient>\n")
                if (templates.isEmpty()) {
                    sb.append("<gray>No template maps registered or loaded.</gray>\n")
                } else {
                    for (tmpl in templates) {
                        val count = tmpl.containers.size
                        sb.append("<dark_gray>▪ </dark_gray><yellow>${tmpl.templateId}</yellow> <gray>(${tmpl.displayName}): </gray><aqua>$count containers</aqua>\n")
                        for (c in tmpl.containers) {
                            sb.append("   <dark_gray>• </dark_gray><gray>ID: </gray><white>${c.id}</white> <gray>| Cat: </gray><yellow>${c.categoryId}</yellow> <gray>| Pos: </gray><aqua>(${c.x}, ${c.y}, ${c.z})</aqua>\n")
                        }
                    }
                }
                sb.append("<dark_gray>----------------------------------------</dark_gray>")
                sender.sendMessage(miniMessage.deserialize(sb.toString()))
            }

            else -> {
                sender.sendMessage(miniMessage.deserialize("<red>Unknown chest action: ${args[0]}. Usage: </red><yellow>/$label chest <get|list></yellow>"))
            }
        }
    }

    private fun sendHelp(sender: CommandSender) {
        val helpMessage = """
            <dark_gray>----------------------------------------</dark_gray>
            <gradient:#00FFA3:#00B8FF><bold>LUMINARIS (L.O.V.E) EXTRACTION COMMANDS</bold></gradient>
            <yellow>/ext match start <map> [players]</yellow>   <gray>- Initialize and start an extraction raid</gray>
            <yellow>/ext match stop <matchId></yellow>         <gray>- Terminate an active match instance</gray>
            <yellow>/ext match list</yellow>                    <gray>- View active extraction matches</gray>
            <yellow>/ext chest get <cat> [type]</yellow>         <gray>- Dispense Container Marker (chest/barrel)</gray>
            <yellow>/ext chest list</yellow>                    <gray>- View registered template container markers</gray>
            <yellow>/ext case list</yellow>                      <gray>- List all configured Safe Containers</gray>
            <yellow>/ext case give <player> <id></yellow>       <gray>- Give a Safe Container to a player</gray>
            <yellow>/ext editor createitem <cat></yellow>       <gray>- Register held item via Native Dialog UI</gray>
            <yellow>/ext editor createcategory</yellow>         <gray>- Configure loot category via Dialog UI</gray>
            <yellow>/ext editor list</yellow>                   <gray>- List registered items and categories</gray>
            <yellow>/ext status</yellow>                        <gray>- View active core engine telemetry & threading model</gray>
            <yellow>/ext reload</yellow>                        <gray>- Hot-reload items, categories, templates, safe cases</gray>
            <yellow>/ext help</yellow>                          <gray>- Display extraction command manual</gray>
            <dark_gray>----------------------------------------</dark_gray>
        """.trimIndent()
        sender.sendMessage(miniMessage.deserialize(helpMessage))
    }

    override fun suggest(
        stack: CommandSourceStack,
        args: Array<out String>
    ): Collection<String> {
        val sender = stack.sender
        if (args.isEmpty() || args.size == 1) {
            val query = args.firstOrNull() ?: ""
            val subcommands = mutableListOf("help", "status")
            if (sender.hasPermission("luminaris.admin") || sender.hasPermission("luminaris.editor")) {
                subcommands.add("case")
                subcommands.add("chest")
                subcommands.add("editor")
                subcommands.add("match")
                subcommands.add("reload")
            }
            return subcommands.filter { it.startsWith(query, ignoreCase = true) }
        }

        if (args.size > 1 && args[0].equals("match", ignoreCase = true)) {
            if (!sender.hasPermission("luminaris.admin")) return emptyList()
            if (args.size == 2) {
                return listOf("start", "stop", "list", "status").filter { it.startsWith(args[1], ignoreCase = true) }
            }
            if (args.size == 3 && args[1].equals("start", ignoreCase = true)) {
                return plugin.templateStorage.getAllTemplates().map { it.templateId }.filter { it.startsWith(args[2], ignoreCase = true) }
            }
            if (args.size >= 4 && args[1].equals("start", ignoreCase = true)) {
                return org.bukkit.Bukkit.getOnlinePlayers().map { it.name }.filter { it.startsWith(args.last(), ignoreCase = true) }
            }
            if (args.size == 3 && (args[1].equals("stop", ignoreCase = true) || args[1].equals("status", ignoreCase = true))) {
                return plugin.matchManager.getActiveMatches().map { it.matchId }.filter { it.startsWith(args[2], ignoreCase = true) }
            }
            return emptyList()
        }

        if (args.size > 1 && args[0].equals("chest", ignoreCase = true)) {
            if (!sender.hasPermission("luminaris.admin") && !sender.hasPermission("luminaris.editor")) return emptyList()
            if (args.size == 2) {
                return listOf("get", "list").filter { it.startsWith(args[1], ignoreCase = true) }
            }
            if (args.size == 3 && args[1].equals("get", ignoreCase = true)) {
                return plugin.containerRegistry.getAllCategories().map { it.id }.filter { it.startsWith(args[2], ignoreCase = true) }
            }
            if (args.size == 4 && args[1].equals("get", ignoreCase = true)) {
                return listOf("chest", "barrel").filter { it.startsWith(args[3], ignoreCase = true) }
            }
            return emptyList()
        }

        if (args.size > 1 && args[0].equals("case", ignoreCase = true)) {
            if (!sender.hasPermission("luminaris.admin")) return emptyList()
            if (args.size == 2) {
                return listOf("give", "list").filter { it.startsWith(args[1], ignoreCase = true) }
            }
            if (args.size == 3 && args[1].equals("give", ignoreCase = true)) {
                return org.bukkit.Bukkit.getOnlinePlayers().map { it.name }.filter { it.startsWith(args[2], ignoreCase = true) }
            }
            if (args.size == 4 && args[1].equals("give", ignoreCase = true)) {
                return plugin.safeCaseManager.getAllDefinitions().map { it.id }.filter { it.startsWith(args[3], ignoreCase = true) }
            }
            return emptyList()
        }

        if (args.size > 1 && args[0].equals("editor", ignoreCase = true)) {
            return plugin.editorCommand.complete(sender, args.drop(1).toTypedArray())
        }

        return emptyList()
    }
}
