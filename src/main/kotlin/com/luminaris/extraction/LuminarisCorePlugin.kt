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
        /**
         * Singleton accessor to the active plugin instance.
         */
        lateinit var instance: LuminarisCorePlugin
            private set
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

    lateinit var templateStorage: com.luminaris.extraction.storage.TemplateStorage
        private set

    lateinit var containerMarkerListener: com.luminaris.extraction.editor.listeners.ContainerMarkerListener
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

    override fun onLoad() {
        instance = this
    }

    override fun onEnable() {
        instance = this
        scheduler = FoliaScheduler(this)

        // Initialize and load custom YAML configs
        initializeConfigurations()

        // Initialize registries
        initializeRegistries()

        // Register base command /ext
        registerExtCommand()

        // Output Cyberpunk / High-Tech MiniMessage banner
        renderStartupBanner()
    }

    override fun onDisable() {
        renderShutdownBanner()

        // Clean up match manager and all active instances
        if (::matchManager.isInitialized) {
            matchManager.shutdown()
        }

        // Clean up extraction manager and active countdown tasks safely
        if (::extractionManager.isInitialized) {
            extractionManager.shutdown()
        }

        // Cancel all scheduled tasks safely
        if (::scheduler.isInitialized) {
            scheduler.cancelAll()
        }
    }

    /**
     * Initializes all storage adapters safely.
     */
    private fun initializeConfigurations() {
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
    }

    /**
     * Reloads all configuration adapters and registries in memory.
     */
    fun reloadAllConfigurations() {
        translationManager.reload()
        itemsConfig.reload()
        categoriesConfig.reload()
        safeCasesConfig.reload()
        templateStorage.loadAll()
        containerRegistry.load()
        itemRegistry.load()
        safeCaseManager.load()
    }

    /**
     * Registers the base /ext command using Paper's registerCommand API.
     */
    private fun registerExtCommand() {
        val extCommandExecutor = ExtCommand(this)
        registerCommand(
            "ext",
            "Luminaris Extraction Engine core command",
            listOf("extraction", "love"),
            extCommandExecutor
        )
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
