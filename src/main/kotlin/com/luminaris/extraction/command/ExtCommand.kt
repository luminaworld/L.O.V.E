package com.luminaris.extraction.command

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.localization.TranslationManager
import com.luminaris.extraction.util.FoliaScheduler
import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
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
                    TranslationManager.send(sender, "general.no_permission")
                    return
                }
                plugin.reloadAllConfigurations()
                TranslationManager.send(sender, "command.reload.success")
            }

            "status" -> {
                val platformName = if (FoliaScheduler.isFolia) {
                    TranslationManager.get("banner.platform.folia")
                } else {
                    TranslationManager.get("banner.platform.paper")
                }
                val itemsCount = plugin.itemRegistry.getAllItems().size
                val categoriesCount = plugin.containerRegistry.getAllCategories().size
                val casesCount = plugin.safeCaseManager.getAllDefinitions().size
                val instancesCount = plugin.instanceWorldService.getActiveInstanceCount()

                val statusMessage = buildString {
                    append(TranslationManager.get("general.divider")).append("\n")
                    append(TranslationManager.get("command.status.title")).append("\n")
                    append(TranslationManager.get("command.status.version", "version" to plugin.pluginMeta.version)).append("\n")
                    append(TranslationManager.get("command.status.threading", "platform" to platformName)).append("\n")
                    append(TranslationManager.get("command.status.items", "count" to itemsCount)).append("\n")
                    append(TranslationManager.get("command.status.categories", "count" to categoriesCount)).append("\n")
                    append(TranslationManager.get("command.status.cases", "count" to casesCount)).append("\n")
                    append(TranslationManager.get("command.status.instances", "count" to instancesCount)).append("\n")
                    append(TranslationManager.get("general.divider"))
                }
                sender.sendMessage(TranslationManager.parseComponent(statusMessage))
            }

            "case" -> {
                if (!sender.hasPermission("luminaris.admin")) {
                    TranslationManager.send(sender, "general.no_permission")
                    return
                }
                handleCaseCommand(sender, label, args.drop(1).toTypedArray())
            }

            "chest" -> {
                if (!sender.hasPermission("luminaris.admin") && !sender.hasPermission("luminaris.editor")) {
                    TranslationManager.send(sender, "general.no_permission")
                    return
                }
                handleChestCommand(sender, label, args.drop(1).toTypedArray())
            }

            "match" -> {
                if (!sender.hasPermission("luminaris.admin")) {
                    TranslationManager.send(sender, "general.no_permission")
                    return
                }
                plugin.matchManager.handleCommand(sender, label, args.drop(1).toTypedArray())
            }

            "help" -> sendHelp(sender)

            else -> {
                TranslationManager.send(sender, "command.unknown_subcommand", "subcommand" to args[0], "label" to label)
            }
        }
    }

    private fun handleCaseCommand(sender: CommandSender, label: String, args: Array<out String>) {
        if (args.isEmpty()) {
            TranslationManager.send(sender, "command.case.usage", "label" to label)
            return
        }

        when (args[0].lowercase()) {
            "list" -> {
                val cases = plugin.safeCaseManager.getAllDefinitions()
                if (cases.isEmpty()) {
                    TranslationManager.send(sender, "command.case.empty_list")
                    return
                }
                TranslationManager.send(sender, "command.case.list_header", "count" to cases.size)
                for (caseDef in cases) {
                    val titleFormatted = miniMessage.serialize(caseDef.displayName)
                    TranslationManager.send(
                        sender,
                        "command.case.list_entry",
                        "id" to caseDef.id,
                        "display_name" to titleFormatted,
                        "capacity" to caseDef.capacity,
                        "material" to caseDef.material.name
                    )
                }
            }

            "give" -> {
                if (args.size < 3) {
                    TranslationManager.send(sender, "command.case.give_usage", "label" to label)
                    return
                }
                val targetName = args[1]
                val targetPlayer = org.bukkit.Bukkit.getPlayerExact(targetName)
                if (targetPlayer == null) {
                    TranslationManager.send(sender, "general.player_not_found", "player" to targetName)
                    return
                }
                val caseId = args[2].lowercase()
                val safeItem = plugin.safeCaseManager.createSafeItemStack(caseId)
                if (safeItem == null) {
                    TranslationManager.send(sender, "command.case.unknown_id", "id" to caseId, "label" to label)
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

                TranslationManager.send(sender, "command.case.give_success_sender", "id" to caseId, "player" to targetPlayer.name)
                targetPlayer.sendMessage(
                    TranslationManager.render("command.case.give_success_target")
                        .append(safeItem.itemMeta?.displayName() ?: Component.text(caseId))
                )
            }

            else -> {
                TranslationManager.send(sender, "command.case.unknown_action", "action" to args[0], "label" to label)
            }
        }
    }

    private fun handleChestCommand(sender: CommandSender, label: String, args: Array<out String>) {
        if (args.isEmpty()) {
            TranslationManager.send(sender, "command.chest.usage", "label" to label)
            return
        }

        when (args[0].lowercase()) {
            "get" -> {
                if (args.size < 2) {
                    val available = plugin.containerRegistry.getAllCategories().map { it.id }
                    val catHint = if (available.isNotEmpty()) {
                        TranslationManager.get("command.chest.available_categories", "categories" to available.joinToString(", "))
                    } else {
                        TranslationManager.get("command.chest.no_categories")
                    }
                    TranslationManager.send(sender, "command.chest.missing_category", "label" to label, "hint" to catHint)
                    return
                }

                val player = sender as? org.bukkit.entity.Player
                if (player == null) {
                    TranslationManager.send(sender, "general.player_only")
                    return
                }

                val categoryId = args[1].lowercase().trim()
                val material = if (args.size >= 3 && args[2].equals("barrel", ignoreCase = true)) {
                    org.bukkit.Material.BARREL
                } else {
                    org.bukkit.Material.CHEST
                }

                if (!plugin.containerRegistry.hasCategory(categoryId)) {
                    TranslationManager.send(sender, "command.chest.category_notice", "category" to categoryId)
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

                TranslationManager.send(
                    sender,
                    "command.chest.give_success",
                    "category" to categoryId,
                    "material" to material.name,
                    "player" to player.name
                )
            }

            "list" -> {
                val templates = plugin.templateStorage.getAllTemplates()
                val sb = StringBuilder()
                sb.append(TranslationManager.get("general.divider")).append("\n")
                sb.append(TranslationManager.get("command.chest.list_header")).append("\n")
                if (templates.isEmpty()) {
                    sb.append(TranslationManager.get("command.chest.list_empty")).append("\n")
                } else {
                    for (tmpl in templates) {
                        val count = tmpl.containers.size
                        sb.append(TranslationManager.get("command.chest.list_template_entry", "template" to tmpl.templateId, "display_name" to tmpl.displayName, "count" to count)).append("\n")
                        for (c in tmpl.containers) {
                            sb.append(TranslationManager.get("command.chest.list_container_entry", "id" to c.id, "category" to c.categoryId, "x" to c.x, "y" to c.y, "z" to c.z)).append("\n")
                        }
                    }
                }
                sb.append(TranslationManager.get("general.divider"))
                sender.sendMessage(TranslationManager.parseComponent(sb.toString()))
            }

            else -> {
                TranslationManager.send(sender, "command.chest.unknown_action", "action" to args[0], "label" to label)
            }
        }
    }

    private fun sendHelp(sender: CommandSender) {
        val sb = StringBuilder()
        sb.append(TranslationManager.get("general.divider")).append("\n")
        sb.append(TranslationManager.get("command.help.title")).append("\n")
        sb.append(TranslationManager.get("command.help.match_start")).append("\n")
        sb.append(TranslationManager.get("command.help.match_stop")).append("\n")
        sb.append(TranslationManager.get("command.help.match_list")).append("\n")
        sb.append(TranslationManager.get("command.help.chest_get")).append("\n")
        sb.append(TranslationManager.get("command.help.chest_list")).append("\n")
        sb.append(TranslationManager.get("command.help.case_list")).append("\n")
        sb.append(TranslationManager.get("command.help.case_give")).append("\n")
        sb.append(TranslationManager.get("command.help.editor_createitem")).append("\n")
        sb.append(TranslationManager.get("command.help.editor_createcategory")).append("\n")
        sb.append(TranslationManager.get("command.help.editor_list")).append("\n")
        sb.append(TranslationManager.get("command.help.status")).append("\n")
        sb.append(TranslationManager.get("command.help.reload")).append("\n")
        sb.append(TranslationManager.get("command.help.help")).append("\n")
        sb.append(TranslationManager.get("general.divider"))
        sender.sendMessage(TranslationManager.parseComponent(sb.toString()))
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
