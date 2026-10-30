package com.luminaris.extraction

import com.luminaris.extraction.command.ExtCommand
import com.luminaris.extraction.localization.TranslationManager
import com.luminaris.extraction.storage.CategoriesConfigAdapter
import com.luminaris.extraction.storage.ItemsConfigAdapter
import com.luminaris.extraction.storage.SafeCasesConfigAdapter
import com.luminaris.extraction.util.FoliaScheduler
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

/**
 * Core engine plugin for Luminaris Operation Vanilla Extraction (L.O.V.E).
 * Folia and Paper multi-threaded regional execution compliant.
 */
class LuminarisCorePlugin : JavaPlugin() {

    companion object {
        private var _instance: LuminarisCorePlugin? = null

        /**
         * Singleton accessor to the active plugin instance.
         */
        val instance: LuminarisCorePlugin
            get() = _instance ?: throw IllegalStateException("LuminarisCorePlugin is not loaded or has been disabled!")

        /**
         * Safe nullable accessor to the active plugin instance.
         */
        val instanceOrNull: LuminarisCorePlugin?
            get() = _instance
    }

    private val miniMessage = MiniMessage.miniMessage()

    lateinit var scheduler: FoliaScheduler
        private set

    lateinit var translationManager: TranslationManager
        private set

    lateinit var itemsConfig: ItemsConfigAdapter
        private set

    lateinit var categoriesConfig: CategoriesConfigAdapter
        private set

    lateinit var safeCasesConfig: SafeCasesConfigAdapter
        private set

    lateinit var safeCaseManager: com.luminaris.extraction.core.safe.SafeCaseManager
        private set

    lateinit var safeCaseListener: com.luminaris.extraction.core.safe.SafeCaseListener
        private set

    lateinit var itemRegistry: com.luminaris.extraction.core.item.ItemRegistry
        private set

    lateinit var containerRegistry: com.luminaris.extraction.core.container.ContainerRegistry
        private set

    lateinit var editorCommand: com.luminaris.extraction.editor.EditorCommand
        private set

    lateinit var editorGuiListener: com.luminaris.extraction.editor.gui.EditorGuiListener
        private set

    lateinit var dropBoxManager: com.luminaris.extraction.core.container.DropBoxManager
        private set

    lateinit var timedSafeManager: com.luminaris.extraction.core.container.TimedSafeManager
        private set

    lateinit var templateStorage: com.luminaris.extraction.storage.TemplateStorage
        private set

    lateinit var containerMarkerListener: com.luminaris.extraction.editor.listeners.ContainerMarkerListener
        private set

    lateinit var containerHighlightService: com.luminaris.extraction.core.container.ContainerHighlightService
        private set

    lateinit var instanceWorldService: com.luminaris.extraction.core.world.InstanceWorldService
        private set

    lateinit var lootPopulator: com.luminaris.extraction.core.item.LootPopulator
        private set

    lateinit var spawnAllocator: com.luminaris.extraction.core.spawn.SpawnAllocator
        private set

    lateinit var extractionManager: com.luminaris.extraction.core.extraction.ExtractionManager
        private set

    lateinit var extractionListener: com.luminaris.extraction.core.extraction.ExtractionListener
        private set

    lateinit var corpseManager: com.luminaris.extraction.core.match.CorpseManager
        private set

    lateinit var matchManager: com.luminaris.extraction.core.match.MatchManager
        private set

    lateinit var deathListener: com.luminaris.extraction.core.match.DeathListener
        private set

    lateinit var webConfigServer: com.luminaris.extraction.web.WebConfigServer
        private set

    lateinit var clientGuardManager: com.luminaris.extraction.security.clientguard.ClientGuardManager
        private set

    lateinit var clientGuardListener: com.luminaris.extraction.security.clientguard.ClientGuardListener
        private set

    override fun onLoad() {
        _instance = this
    }

    override fun onEnable() {
        _instance = this
        scheduler = FoliaScheduler(this)

        // Initialize and load custom YAML configs
        initializeConfigurations()

        // Initialize registries
        initializeRegistries()

        // Register base command /ext (cleans up any previous stale mappings first)
        registerExtCommand()

        // Start embedded web configuration server
        webConfigServer = com.luminaris.extraction.web.WebConfigServer(this)
        webConfigServer.start()

        // Output Cyberpunk / High-Tech MiniMessage banner
        renderStartupBanner()
    }

    override fun onDisable() {
        renderShutdownBanner()

        // 0. Stop embedded web configuration server
        if (::webConfigServer.isInitialized) {
            runCatching { webConfigServer.stop() }
        }

        // 1. Close all active custom inventories (Editor GUI, Drop Box, Safe Case)
        closeAllCustomInventories()

        // 2. Clean up match manager, active instances, and ephemeral worlds
        if (::matchManager.isInitialized) {
            runCatching { matchManager.shutdown() }
        }

        // 3. Clean up extraction manager and active countdown tasks safely
        if (::extractionManager.isInitialized) {
            runCatching { extractionManager.shutdown() }
        }

        // 4. Return pending safe cases to online players and clear safe case state
        if (::safeCaseManager.isInitialized) {
            runCatching { safeCaseManager.shutdown() }
        }

        // 5. Clean up corpse manager
        if (::corpseManager.isInitialized) {
            runCatching { corpseManager.clear() }
        }

        // 6. Clean up drop box container states and container highlights
        if (::timedSafeManager.isInitialized) {
            runCatching { timedSafeManager.shutdown() }
        }
        if (::containerHighlightService.isInitialized) {
            runCatching { containerHighlightService.shutdown() }
        }
        if (::dropBoxManager.isInitialized) {
            runCatching { dropBoxManager.shutdown() }
        }
        if (::clientGuardManager.isInitialized) {
            runCatching { clientGuardManager.shutdown() }
        }

        // 7. Clean up registered commands from CommandMap to prevent PlugManX command conflicts
        unregisterCommands()

        // 8. Explicitly unregister all event listeners from HandlerList
        runCatching { org.bukkit.event.HandlerList.unregisterAll(this as org.bukkit.plugin.Plugin) }

        // 9. Cancel all scheduled tasks safely (Folia regional + async + Bukkit)
        if (::scheduler.isInitialized) {
            runCatching { scheduler.cancelAll() }
        }

        // 10. Reset static singletons
        FoliaScheduler.reset()
        TranslationManager.reset()
        _instance = null
    }

    /**
     * Initializes all storage adapters safely.
     */
    private fun initializeConfigurations() {
        runCatching { saveDefaultConfig() }
        translationManager = TranslationManager(this)
        translationManager.initialize()
        TranslationManager.instance = translationManager

        itemsConfig = ItemsConfigAdapter(this)
        itemsConfig.initialize()

        categoriesConfig = CategoriesConfigAdapter(this)
        categoriesConfig.initialize()

        safeCasesConfig = SafeCasesConfigAdapter(this)
        safeCasesConfig.initialize()

        templateStorage = com.luminaris.extraction.storage.TemplateStorage(this)
        templateStorage.initialize()
    }

    /**
     * Initializes and loads item and container registries.
     */
    private fun initializeRegistries() {
        containerRegistry = com.luminaris.extraction.core.container.ContainerRegistry(this)
        containerRegistry.load()

        itemRegistry = com.luminaris.extraction.core.item.ItemRegistry(this)
        itemRegistry.load()

        safeCaseManager = com.luminaris.extraction.core.safe.SafeCaseManager(this)
        safeCaseManager.load()

        safeCaseListener = com.luminaris.extraction.core.safe.SafeCaseListener(this, safeCaseManager)
        server.pluginManager.registerEvents(safeCaseListener, this)

        dropBoxManager = com.luminaris.extraction.core.container.DropBoxManager(this)
        timedSafeManager = com.luminaris.extraction.core.container.TimedSafeManager(this, scheduler)

        containerHighlightService = com.luminaris.extraction.core.container.ContainerHighlightService(this, scheduler)
        containerHighlightService.start()

        containerMarkerListener = com.luminaris.extraction.editor.listeners.ContainerMarkerListener(this, templateStorage)
        server.pluginManager.registerEvents(containerMarkerListener, this)

        editorGuiListener = com.luminaris.extraction.editor.gui.EditorGuiListener(this)
        server.pluginManager.registerEvents(editorGuiListener, this)

        editorCommand = com.luminaris.extraction.editor.EditorCommand(this)

        instanceWorldService = com.luminaris.extraction.core.world.InstanceWorldService(this, scheduler)
        lootPopulator = com.luminaris.extraction.core.item.LootPopulator(this, itemRegistry, containerRegistry, scheduler)

        // Dynamic Spawns and Extraction Matrix
        spawnAllocator = com.luminaris.extraction.core.spawn.SpawnAllocator()
        extractionManager = com.luminaris.extraction.core.extraction.ExtractionManager(this, itemRegistry, safeCaseManager)
        extractionListener = com.luminaris.extraction.core.extraction.ExtractionListener(extractionManager)
        server.pluginManager.registerEvents(extractionListener, this)

        // Ephemeral Match and Corpse Crate Engine
        corpseManager = com.luminaris.extraction.core.match.CorpseManager(this, scheduler)
        matchManager = com.luminaris.extraction.core.match.MatchManager(
            plugin = this,
            instanceWorldService = instanceWorldService,
            lootPopulator = lootPopulator,
            spawnAllocator = spawnAllocator,
            templateStorage = templateStorage,
            extractionManager = extractionManager,
            scheduler = scheduler
        )
        deathListener = com.luminaris.extraction.core.match.DeathListener(
            plugin = this,
            matchManager = matchManager,
            corpseManager = corpseManager,
            safeCaseManager = safeCaseManager,
            scheduler = scheduler
        )
        server.pluginManager.registerEvents(deathListener, this)

        // Client Guard Anti-Mod and Cheat Detection Engine
        clientGuardManager = com.luminaris.extraction.security.clientguard.ClientGuardManager(this, scheduler)
        clientGuardManager.initialize()
        clientGuardListener = com.luminaris.extraction.security.clientguard.ClientGuardListener(clientGuardManager, scheduler)
        server.pluginManager.registerEvents(clientGuardListener, this)
        server.pluginManager.registerEvents(clientGuardManager.signTranslationProbe, this)
    }

    /**
     * Reloads all configuration adapters and registries in memory.
     */
    fun reloadAllConfigurations() {
        runCatching { reloadConfig() }
        translationManager.reload()
        itemsConfig.reload()
        categoriesConfig.reload()
        safeCasesConfig.reload()
        if (::clientGuardManager.isInitialized) {
            clientGuardManager.reload()
        }
        templateStorage.loadAll()
        containerRegistry.load()
        itemRegistry.load()
        safeCaseManager.load()
        if (::containerHighlightService.isInitialized) {
            containerHighlightService.reloadConfig()
        }
        if (::webConfigServer.isInitialized) {
            webConfigServer.reloadConfig()
        }
    }

    /**
     * Registers the base /ext command using Paper's registerCommand API,
     * ensuring any existing conflicting command mappings are removed first.
     */
    private fun registerExtCommand() {
        unregisterCommands()

        val extCommandExecutor = ExtCommand(this)
        registerCommand(
            "ext",
            "Luminaris Extraction Engine core command",
            listOf("extraction", "love"),
            extCommandExecutor
        )
    }

    /**
     * Closes all active custom GUI inventories for online players
     * before unregistering listeners, guaranteeing that save hooks execute cleanly
     * and players cannot manipulate or steal items while the plugin is being unloaded.
     */
    private fun closeAllCustomInventories() {
        for (player in Bukkit.getOnlinePlayers()) {
            runCatching {
                val top = player.openInventory.topInventory
                val holder = top.holder
                if (holder is com.luminaris.extraction.editor.gui.EditorGuiHolder ||
                    holder is com.luminaris.extraction.core.safe.SafeCaseHolder
                ) {
                    if (FoliaScheduler.isFolia) {
                        player.scheduler.run(this, { _ -> player.closeInventory() }, null)
                    } else {
                        player.closeInventory()
                    }
                }
            }
        }
    }

    /**
     * Cleans up registered /ext commands and aliases from Bukkit's SimpleCommandMap
     * so that PlugManX or server reloads do not retain stale command instances.
     */
    private fun unregisterCommands() {
        try {
            val server = Bukkit.getServer()
            val getCommandMapMethod = runCatching {
                server.javaClass.getMethod("getCommandMap")
            }.getOrNull()

            val commandMap = if (getCommandMapMethod != null) {
                getCommandMapMethod.invoke(server) as? org.bukkit.command.CommandMap
            } else {
                val field = server.javaClass.getDeclaredField("commandMap").apply { isAccessible = true }
                field.get(server) as? org.bukkit.command.CommandMap
            } ?: return

            val knownCommandsField = runCatching {
                commandMap.javaClass.getDeclaredField("knownCommands").apply { isAccessible = true }
            }.getOrNull() ?: return

            @Suppress("UNCHECKED_CAST")
            val knownCommands = knownCommandsField.get(commandMap) as? MutableMap<String, org.bukkit.command.Command> ?: return

            val pluginPrefix = name.lowercase()
            val targetNames = setOf("ext", "extraction", "love")

            val toRemove = knownCommands.filter { (key, cmd) ->
                val lowerKey = key.lowercase()
                targetNames.contains(lowerKey) ||
                targetNames.any { lowerKey == "$pluginPrefix:$it" } ||
                (cmd is org.bukkit.command.PluginIdentifiableCommand && cmd.plugin == this)
            }

            for ((key, cmd) in toRemove) {
                runCatching { cmd.unregister(commandMap) }
                knownCommands.remove(key)
            }
        } catch (_: Throwable) {
            // Silently ignore reflection restrictions in test or custom environments
        }
    }

    /**
     * Prints the MiniMessage startup banner to the console line by line
     * so each line displays with the standard server timestamp and log prefix.
     */
    private fun renderStartupBanner() {
        translationManager.sendLines(
            Bukkit.getConsoleSender(),
            "banner.startup",
            "version" to pluginMeta.version
        )
    }

    /**
     * Prints graceful shutdown notification to console line by line
     * so each line displays with the standard server timestamp and log prefix.
     */
    private fun renderShutdownBanner() {
        translationManager.sendLines(
            Bukkit.getConsoleSender(),
            "banner.shutdown"
        )
    }
}
