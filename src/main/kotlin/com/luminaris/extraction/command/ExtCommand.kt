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
                    TranslationManager.get("command.status.platform_folia")
                } else {
                    TranslationManager.get("command.status.platform_paper")
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

            "chest", "drop", "container", "box" -> {
                if (!sender.hasPermission("luminaris.admin") && !sender.hasPermission("luminaris.editor")) {
                    TranslationManager.send(sender, "general.no_permission")
                    return
                }
                handleChestCommand(sender, label, args.drop(1).toTypedArray())
            }

            "safe" -> {
                if (!sender.hasPermission("luminaris.admin") && !sender.hasPermission("luminaris.editor")) {
                    TranslationManager.send(sender, "general.no_permission")
                    return
                }
                handleSafeCommand(sender, label, args.drop(1).toTypedArray())
            }

            "match" -> {
                if (!sender.hasPermission("luminaris.admin")) {
                    TranslationManager.send(sender, "general.no_permission")
                    return
                }
                plugin.matchManager.handleCommand(sender, label, args.drop(1).toTypedArray())
            }

            "web" -> {
                if (!sender.hasPermission("luminaris.admin")) {
                    TranslationManager.send(sender, "general.no_permission")
                    return
                }
                if (args.size > 1 && args[1].lowercase() == "reload") {
                    plugin.webConfigServer.ensureWebAssets(force = true)
                    plugin.webConfigServer.reloadConfig()
                    val port = plugin.config.getInt("web_config.port", 8080)
                    TranslationManager.send(sender, "web.reloaded", "port" to port)
                    return
                }
                if (plugin.webConfigServer.isRunning) {
                    val port = plugin.config.getInt("web_config.port", 8080)
                    val bindAddr = plugin.config.getString("web_config.bind_address", "0.0.0.0") ?: "0.0.0.0"
                    val host = if (bindAddr == "0.0.0.0") "localhost" else bindAddr
                    TranslationManager.send(sender, "web.running", "host" to host, "port" to port)
                } else {
                    TranslationManager.send(sender, "web.disabled")
                }
            }

            "guard", "clientguard", "anticheat" -> {
                if (!sender.hasPermission("luminaris.admin") && !sender.hasPermission("luminaris.security.clientguard.admin")) {
                    TranslationManager.send(sender, "general.no_permission")
                    return
                }
                handleGuardCommand(sender, label, args.drop(1).toTypedArray())
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

    private fun handleSafeCommand(sender: CommandSender, label: String, args: Array<out String>) {
        val player = sender as? org.bukkit.entity.Player
        if (player == null) {
            TranslationManager.send(sender, "general.player_only")
            return
        }

        val targetCat = if (args.isNotEmpty()) args[0].lowercase().trim() else "safe_box"
        val tool = com.luminaris.extraction.editor.tool.SafeSetupTool.createTool(targetCat)

        plugin.scheduler.runAtEntity(player) {
            val leftover = player.inventory.addItem(tool)
            if (leftover.isNotEmpty()) {
                for (item in leftover.values) {
                    player.world.dropItemNaturally(player.location, item)
                }
            }
            TranslationManager.send(player, "safe.tool.received", "category" to targetCat)
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
                val material = if (args.size >= 3) {
                    val requested = args[2].uppercase().trim()
                    org.bukkit.Material.matchMaterial(requested) ?: org.bukkit.Material.CHEST
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

            "test" -> {
                val player = sender as? org.bukkit.entity.Player
                if (player == null) {
                    TranslationManager.send(sender, "general.player_only")
                    return
                }
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
                val categoryId = args[1].lowercase().trim()
                val material = if (args.size >= 3) {
                    org.bukkit.Material.matchMaterial(args[2].uppercase().trim()) ?: org.bukkit.Material.CHEST
                } else {
                    org.bukkit.Material.CHEST
                }
                plugin.dropBoxManager.openSimulation(player, categoryId, material)
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

    private fun handleGuardCommand(sender: CommandSender, label: String, args: Array<out String>) {
        if (args.isEmpty()) {
            sendGuardHelp(sender, label)
            return
        }

        when (args[0].lowercase()) {
            "reload" -> {
                plugin.clientGuardManager.reload()
                sender.sendMessage(TranslationManager.parseComponent(
                    "<gradient:#00FFA3:#00B8FF><bold>[ClientGuard]</bold></gradient> <green>โหลดไฟล์ client_guard.yml ใหม่สำเร็จแล้ว!</green>"
                ))
            }

            "check" -> {
                if (args.size < 2) {
                    sender.sendMessage(TranslationManager.parseComponent(
                        "<red>รูปแบบคำสั่ง: /$label guard check <player></red>"
                    ))
                    return
                }
                val targetName = args[1]
                val targetPlayer = org.bukkit.Bukkit.getPlayerExact(targetName)
                if (targetPlayer == null) {
                    TranslationManager.send(sender, "general.player_not_found", "player" to targetName)
                    return
                }
                val report = plugin.clientGuardManager.buildInspectionReport(targetPlayer)
                sender.sendMessage(TranslationManager.parseComponent(report))
            }

            "probe" -> {
                if (args.size < 2) {
                    sender.sendMessage(TranslationManager.parseComponent(
                        "<red>รูปแบบคำสั่ง: /$label guard probe <player> [force]</red>"
                    ))
                    return
                }
                val targetName = args[1]
                val targetPlayer = org.bukkit.Bukkit.getPlayerExact(targetName)
                if (targetPlayer == null) {
                    TranslationManager.send(sender, "general.player_not_found", "player" to targetName)
                    return
                }
                plugin.clientGuardManager.signTranslationProbe.schedule(targetPlayer, force = true)
                sender.sendMessage(TranslationManager.parseComponent(
                    "<gradient:#00FFA3:#00B8FF><bold>[ClientGuard]</bold></gradient> <gray>ส่ง Virtual Sign Probe ไปยังผู้เล่น <yellow>${targetPlayer.name}</yellow> แล้ว! ดูผลลัพธ์ใน Console...</gray>"
                ))
            }

            "bypass" -> {
                if (args.size < 2) {
                    sender.sendMessage(TranslationManager.parseComponent(
                        "<red>รูปแบบคำสั่ง: /$label guard bypass [add|remove|list] <player></red>"
                    ))
                    return
                }

                when (args[1].lowercase()) {
                    "list" -> {
                        val bypassed = plugin.clientGuardManager.configAdapter.getBypassedPlayers()
                        if (bypassed.isEmpty()) {
                            sender.sendMessage(TranslationManager.parseComponent(
                                "<gradient:#00FFA3:#00B8FF><bold>[ClientGuard]</bold></gradient> <gray>ยังไม่มีผู้เล่นในรายชื่อ Bypass</gray>"
                            ))
                        } else {
                            val listStr = bypassed.joinToString(", ") { "<yellow>$it</yellow>" }
                            sender.sendMessage(TranslationManager.parseComponent(
                                "<gradient:#00FFA3:#00B8FF><bold>[ClientGuard]</bold></gradient> <gray>รายชื่อผู้เล่นที่ได้รับการ Bypass (${bypassed.size}):</gray> $listStr"
                            ))
                        }
                    }

                    "add" -> {
                        if (args.size < 3) {
                            sender.sendMessage(TranslationManager.parseComponent(
                                "<red>รูปแบบคำสั่ง: /$label guard bypass add <player></red>"
                            ))
                            return
                        }
                        val targetName = args[2]
                        val added = plugin.clientGuardManager.configAdapter.addBypassedPlayer(targetName)
                        val onlinePlayer = org.bukkit.Bukkit.getPlayerExact(targetName)
                        if (onlinePlayer != null) {
                            val profile = plugin.clientGuardManager.getOrCreateProfile(onlinePlayer)
                            profile.isPunished = false
                        }
                        if (added) {
                            sender.sendMessage(TranslationManager.parseComponent(
                                "<gradient:#00FFA3:#00B8FF><bold>[ClientGuard]</bold></gradient> <green>เพิ่มผู้เล่น <yellow>$targetName</yellow> ในรายชื่อ Bypass สำเร็จ! (อนุญาตให้ใช้โปร/ม็อดโกงได้)</green>"
                            ))
                        } else {
                            sender.sendMessage(TranslationManager.parseComponent(
                                "<gradient:#00FFA3:#00B8FF><bold>[ClientGuard]</bold></gradient> <red>ผู้เล่น <yellow>$targetName</yellow> อยู่ในรายชื่อ Bypass อยู่แล้ว</red>"
                            ))
                        }
                    }

                    "remove" -> {
                        if (args.size < 3) {
                            sender.sendMessage(TranslationManager.parseComponent(
                                "<red>รูปแบบคำสั่ง: /$label guard bypass remove <player></red>"
                            ))
                            return
                        }
                        val targetName = args[2]
                        val removed = plugin.clientGuardManager.configAdapter.removeBypassedPlayer(targetName)
                        if (removed) {
                            sender.sendMessage(TranslationManager.parseComponent(
                                "<gradient:#00FFA3:#00B8FF><bold>[ClientGuard]</bold></gradient> <green>ลบผู้เล่น <yellow>$targetName</yellow> ออกจากรายชื่อ Bypass แล้ว (กลับมาถูกตรวจสอบตามปกติ)</green>"
                            ))
                        } else {
                            sender.sendMessage(TranslationManager.parseComponent(
                                "<gradient:#00FFA3:#00B8FF><bold>[ClientGuard]</bold></gradient> <red>ไม่พบผู้เล่น <yellow>$targetName</yellow> ในรายชื่อ Bypass</red>"
                            ))
                        }
                    }

                    else -> {
                        // Direct toggle shortcut: /ext guard bypass <player>
                        val targetName = args[1]
                        val isAlready = plugin.clientGuardManager.configAdapter.isBypassed(targetName)
                        if (isAlready) {
                            plugin.clientGuardManager.configAdapter.removeBypassedPlayer(targetName)
                            sender.sendMessage(TranslationManager.parseComponent(
                                "<gradient:#00FFA3:#00B8FF><bold>[ClientGuard]</bold></gradient> <yellow>ยกเลิกการ Bypass</yellow> สำหรับ <yellow>$targetName</yellow> แล้ว (ลบออกจากรายการ)"
                            ))
                        } else {
                            plugin.clientGuardManager.configAdapter.addBypassedPlayer(targetName)
                            val onlinePlayer = org.bukkit.Bukkit.getPlayerExact(targetName)
                            if (onlinePlayer != null) {
                                plugin.clientGuardManager.getOrCreateProfile(onlinePlayer).isPunished = false
                            }
                            sender.sendMessage(TranslationManager.parseComponent(
                                "<gradient:#00FFA3:#00B8FF><bold>[ClientGuard]</bold></gradient> <green>เปิดการ Bypass</green> สำหรับ <yellow>$targetName</yellow> สำเร็จ! (อนุญาตให้ใช้โปร/ม็อดโกงได้)"
                            ))
                        }
                    }
                }
            }

            "status" -> {
                val config = plugin.clientGuardManager.configAdapter
                val rulesCount = config.getAllRules().size
                val enabledRules = config.getAllRules().values.count { it.enabled }
                val totalViolations = plugin.clientGuardManager.getTotalViolationsCount()
                val violationsMap = plugin.clientGuardManager.getViolationsByClientMap()

                val statusText = buildString {
                    append(TranslationManager.get("general.divider")).append("\n")
                    append("<gradient:#00FFA3:#00B8FF><bold>[ClientGuard Protection Status]</bold></gradient>\n")
                    append("<gray>Status: </gray>${if (config.isGuardEnabled) "<green>ENABLED</green>" else "<red>DISABLED</red>"}\n")
                    append("<gray>Enforcement Mode: <yellow>${config.guardMode}</yellow></gray>\n")
                    append("<gray>Delayed Check Ticks: <white>${config.delayedCheckTicks} ticks</white></gray>\n")
                    append("<gray>Active Detection Rules: <white>$enabledRules / $rulesCount</white></gray>\n")
                    append("<gray>Bypassed Players (Whitelisted): <green>${config.getBypassedPlayers().size}</green></gray>\n")
                    append("<gray>Total Violations Intercepted: <red><bold>$totalViolations</bold></red></gray>\n")
                    if (violationsMap.isNotEmpty()) {
                        append("<gray>Violations by Client:</gray>\n")
                        for ((clientName, count) in violationsMap) {
                            append("  <gray>• <yellow>$clientName</yellow>: <white>$count</white></gray>\n")
                        }
                    }
                    append(TranslationManager.get("general.divider"))
                }
                sender.sendMessage(TranslationManager.parseComponent(statusText))
            }

            else -> {
                sendGuardHelp(sender, label)
            }
        }
    }

    private fun sendGuardHelp(sender: CommandSender, label: String) {
        val helpText = buildString {
            append(TranslationManager.get("general.divider")).append("\n")
            append("<gradient:#00FFA3:#00B8FF><bold>[ClientGuard Commands]</bold></gradient>\n")
            append("<gray>/$label guard status</gray> - <white>ดูสถานะและความปลอดภัยของระบบ</white>\n")
            append("<gray>/$label guard check <player></gray> - <white>ตรวจสอบ Client Brand และ Channel ของผู้เล่น</white>\n")
            append("<gray>/$label guard probe <player> [force]</gray> - <white>ยิง Sign Translation Probe ทดสอบผู้เล่นทันที</white>\n")
            append("<gray>/$label guard bypass add <player></gray> - <white>เพิ่มผู้เล่นให้ Bypass ใช้ม็อดโกงได้</white>\n")
            append("<gray>/$label guard bypass remove <player></gray> - <white>ลบผู้เล่นออกจากรายชื่อ Bypass</white>\n")
            append("<gray>/$label guard bypass list</gray> - <white>ดูรายชื่อผู้เล่นที่ได้รับการ Bypass ทั้งหมด</white>\n")
            append("<gray>/$label guard reload</gray> - <white>รีโหลดไฟล์ client_guard.yml</white>\n")
            append(TranslationManager.get("general.divider"))
        }
        sender.sendMessage(TranslationManager.parseComponent(helpText))
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
        sb.append(TranslationManager.get("command.help.editor")).append("\n")
        sb.append(TranslationManager.get("command.help.web")).append("\n")
        sb.append("<gray>/ext guard [status|check|probe|bypass|reload]</gray> - <white>จัดการระบบตรวจจับ Mod/Client โกง</white>\n")
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
            if (sender.hasPermission("luminaris.admin") || sender.hasPermission("luminaris.security.clientguard.admin")) {
                subcommands.add("guard")
            }
            if (sender.hasPermission("luminaris.admin") || sender.hasPermission("luminaris.editor")) {
                subcommands.add("case")
                subcommands.add("chest")
                subcommands.add("safe")
                subcommands.add("editor")
                subcommands.add("match")
                subcommands.add("reload")
                subcommands.add("web")
            }
            return subcommands.filter { it.startsWith(query, ignoreCase = true) }
        }

        if (args.size > 1 && (args[0].equals("guard", ignoreCase = true) || args[0].equals("clientguard", ignoreCase = true))) {
            if (!sender.hasPermission("luminaris.admin") && !sender.hasPermission("luminaris.security.clientguard.admin")) return emptyList()
            if (args.size == 2) {
                return listOf("status", "check", "probe", "bypass", "reload").filter { it.startsWith(args[1], ignoreCase = true) }
            }
            if (args.size == 3 && (args[1].equals("check", ignoreCase = true) || args[1].equals("probe", ignoreCase = true))) {
                return org.bukkit.Bukkit.getOnlinePlayers().map { it.name }.filter { it.startsWith(args[2], ignoreCase = true) }
            }
            if (args.size == 4 && args[1].equals("probe", ignoreCase = true)) {
                return listOf("force").filter { it.startsWith(args[3], ignoreCase = true) }
            }
            if (args.size == 3 && args[1].equals("bypass", ignoreCase = true)) {
                val opts = mutableListOf("add", "remove", "list")
                opts.addAll(org.bukkit.Bukkit.getOnlinePlayers().map { it.name })
                return opts.filter { it.startsWith(args[2], ignoreCase = true) }
            }
            if (args.size == 4 && args[1].equals("bypass", ignoreCase = true)) {
                if (args[2].equals("add", ignoreCase = true)) {
                    return org.bukkit.Bukkit.getOnlinePlayers().map { it.name }.filter { it.startsWith(args[3], ignoreCase = true) }
                }
                if (args[2].equals("remove", ignoreCase = true)) {
                    return plugin.clientGuardManager.configAdapter.getBypassedPlayers().filter { it.startsWith(args[3], ignoreCase = true) }
                }
            }
            return emptyList()
        }

        if (args.size > 1 && args[0].equals("safe", ignoreCase = true)) {
            if (!sender.hasPermission("luminaris.admin") && !sender.hasPermission("luminaris.editor")) return emptyList()
            if (args.size == 2) {
                return plugin.containerRegistry.getAllCategories().map { it.id }.filter { it.startsWith(args[1], ignoreCase = true) }
            }
            return emptyList()
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

        val chestAliases = setOf("chest", "drop", "container", "box")
        if (args.size > 1 && chestAliases.contains(args[0].lowercase())) {
            if (!sender.hasPermission("luminaris.admin") && !sender.hasPermission("luminaris.editor")) return emptyList()
            if (args.size == 2) {
                return listOf("get", "test", "list").filter { it.startsWith(args[1], ignoreCase = true) }
            }
            if (args.size == 3 && (args[1].equals("get", ignoreCase = true) || args[1].equals("test", ignoreCase = true))) {
                return plugin.containerRegistry.getAllCategories().map { it.id }.filter { it.startsWith(args[2], ignoreCase = true) }
            }
            if (args.size == 4 && (args[1].equals("get", ignoreCase = true) || args[1].equals("test", ignoreCase = true))) {
                val mats = com.luminaris.extraction.editor.tool.ContainerMarkerTool.ALL_SUPPORTED_MATERIALS.map { it.name.lowercase() }
                return mats.filter { it.startsWith(args[3], ignoreCase = true) }
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

        if (args.size > 1 && args[0].equals("web", ignoreCase = true)) {
            if (!sender.hasPermission("luminaris.admin")) return emptyList()
            if (args.size == 2) {
                return listOf("reload").filter { it.startsWith(args[1], ignoreCase = true) }
            }
            return emptyList()
        }

        return emptyList()
    }
}
