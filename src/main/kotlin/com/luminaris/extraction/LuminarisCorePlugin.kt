package com.luminaris.extraction

import com.luminaris.extraction.command.ExtCommand
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
     * Prints the MiniMessage startup banner to the console.
     */
    private fun renderStartupBanner() {
        val platformLabel = if (FoliaScheduler.isFolia) {
            "<gradient:#FF0055:#FFAA00><bold>FOLIA</bold></gradient> <gray>(Regionized Multi-Threaded)</gray>"
        } else {
            "<gradient:#00D2FF:#3A7BD5><bold>PAPER</bold></gradient> <gray>(Universal Regional Scheduler)</gray>"
        }

        val jvmVersion = System.getProperty("java.version")
        val banner = """
            
            <gradient:#00FFA3:#00B8FF><bold> __         ______     __   __     ______    </bold></gradient>
            <gradient:#00FFA3:#00B8FF><bold>/\ \       /\  __ \   /\ \ / /    /\  ___\   </bold></gradient>
            <gradient:#00FFA3:#00B8FF><bold>\ \ \____  \ \ \/\ \  \ \ \'/     \ \  __\   </bold></gradient>
            <gradient:#00FFA3:#00B8FF><bold> \ \_____\  \ \_____\  \ \__|      \ \_____\ </bold></gradient>
            <gradient:#00FFA3:#00B8FF><bold>  \/_____/   \/_____/   \/_/        \/_____/ </bold></gradient>
            <dark_gray>================================================================</dark_gray>
            <gradient:#00FFA3:#00B8FF><bold> LUMINARIS OPERATION VANILLA EXTRACTION (L.O.V.E) </bold></gradient>
            <gray> Core Engine Version: </gray><aqua>${pluginMeta.version}</aqua>
            <gray> Target Runtime:      </gray><yellow>JVM $jvmVersion (Kotlin 2.1+)</yellow>
            <gray> Engine Platform:    </gray>$platformLabel
            <gray> Loaded Modules:      </gray><green>Storage</green>, <green>ItemRegistry</green>, <green>ContainerRegistry</green>, <green>SafeContainerManager</green>, <green>TemplateStorage</green>, <green>ContainerMarker</green>, <green>NativeDialogUI</green>, <green>EditorCommand</green>, <green>InstanceWorldService</green>, <green>LootPopulator</green>, <green>SpawnAllocator</green>, <green>ExtractionMatrix</green>, <green>MatchEngine</green>, <green>CorpseManager</green>
            <gray> Status:              </gray><green><bold>ACTIVE & READY</bold></green>
            <dark_gray>================================================================</dark_gray>
        """.trimIndent()

        Bukkit.getConsoleSender().sendMessage(miniMessage.deserialize(banner))
    }

    /**
     * Prints graceful shutdown notification to console.
     */
    private fun renderShutdownBanner() {
        val shutdownMessage = """
            <dark_gray>================================================================</dark_gray>
            <gradient:#FF416C:#8A2387><bold>[LuminarisCore] Disabling Extraction Engine (L.O.V.E)...</bold></gradient>
            <gray>Cancelling active regional and asynchronous tasks...</gray>
            <red><bold>Status: OFFLINE</bold></red>
            <dark_gray>================================================================</dark_gray>
        """.trimIndent()

        Bukkit.getConsoleSender().sendMessage(miniMessage.deserialize(shutdownMessage))
    }
}
