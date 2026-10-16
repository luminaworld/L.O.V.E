package com.luminaris.extraction.core.world

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.localization.TranslationManager
import com.luminaris.extraction.util.FoliaScheduler
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.util.Collections
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * High-performance Asynchronous Ephemeral Instance World Service.
 * Compliant with Folia regionized multi-threading and modern Paper architectures.
 *
 * Responsibilities:
 * - Asynchronously copies template world directories using Java/Kotlin NIO (zero blocking I/O on region threads).
 * - Strips `uid.dat` and session lock files to prevent Bukkit world collisions.
 * - Dispatches world loading to Folia's Global Region Scheduler.
 * - Configures isolated match world rules (keepInventory, doDaylightCycle, doMobSpawning).
 * - Safely evacuates players via EntitySchedulers before unloading.
 * - Unloads world without saving chunks and asynchronously purges instance data with Windows/Linux retry logic.
 */
class InstanceWorldService(
    plugin: Plugin? = null,
    scheduler: FoliaScheduler? = null
) {

    private val activePlugin: Plugin by lazy {
        plugin ?: runCatching { LuminarisCorePlugin.instance }.getOrNull()
            ?: error("LuminarisCorePlugin instance has not been initialized.")
    }

    private val activeScheduler: FoliaScheduler by lazy {
        scheduler ?: (plugin as? LuminarisCorePlugin)?.scheduler
            ?: runCatching { LuminarisCorePlugin.instance.scheduler }.getOrNull()
            ?: runCatching { FoliaScheduler.default }.getOrNull()
            ?: FoliaScheduler(activePlugin)
    }

    private val miniMessage = MiniMessage.miniMessage()
    private val activeInstances = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    /**
     * Dedicated root directory holding persistent template worlds.
     * Defaults to `<server_root>/templates_worlds`.
     */
    var templatesWorldsFolder: File = runCatching {
        File(Bukkit.getWorldContainer(), "templates_worlds")
    }.getOrNull() ?: File("templates_worlds")

    /**
     * Dedicated root directory where match instance world folders are generated.
     * Defaults to `<server_root>/instances`.
     */
    var instancesFolder: File = runCatching {
        File(Bukkit.getWorldContainer(), "instances")
    }.getOrNull() ?: File("instances")

    /**
     * Explicit fallback lobby location for evacuating players when matches conclude.
     * When null, resolves to the default server world spawn location.
     */
    var lobbyLocation: Location? = null

    /**
     * Asynchronously creates and registers an ephemeral match instance from a template world.
     *
     * Workflow:
     * 1. Resolves `templates_worlds/<templateId>`.
     * 2. Offloads disk copy to the async thread pool using NIO.
     * 3. Purges `uid.dat` and session locks in `instances/match_<matchId>`.
     * 4. Hops to the Folia Global Region Scheduler to register the world via [WorldCreator.createWorld].
     * 5. Configures game rules: `keepInventory = false`, `doDaylightCycle = false`, `doMobSpawning = false`.
     *
     * @param templateId Identifier of the map template (e.g., "factory").
     * @param matchId Unique match identifier for isolation (e.g., "550e8400").
     * @return [CompletableFuture] resolving to the loaded Bukkit [World].
     */
    fun createInstance(templateId: String, matchId: String): CompletableFuture<World> {
        val future = CompletableFuture<World>()
        val instanceWorldName = "instances/match_$matchId"

        logInfo("world.log_preparing", "instance" to instanceWorldName, "template" to templateId)

        // Perform disk operations strictly on the async scheduler
        activeScheduler.runAsync {
            try {
                val sourceDir = resolveTemplateWorldDirectory(templateId)
                if (!sourceDir.exists() || !sourceDir.isDirectory) {
                    val notFound = FileNotFoundException(
                        "Template world directory '$templateId' does not exist (searched: ${sourceDir.absolutePath})"
                    )
                    logError("world.log_template_not_found", notFound, "template" to templateId)
                    future.completeExceptionally(notFound)
                    return@runAsync
                }

                val targetDir = getInstanceWorldFolder(matchId)

                // Clean target folder if a previous dirty state exists
                if (targetDir.exists()) {
                    deleteDirectoryRecursively(targetDir.toPath())
                }

                // Asynchronously copy template world folder using NIO
                copyTemplateWorldFolder(sourceDir.toPath(), targetDir.toPath())

                // Purge uid.dat and session lock files to prevent Bukkit world collision
                purgeWorldCollisionFiles(targetDir.toPath())

                // Hop to Folia Global Region Scheduler for world registration and game rule configuration
                activeScheduler.runGlobal {
                    try {
                        val creator = WorldCreator(instanceWorldName)
                        val world = creator.createWorld()

                        if (world == null) {
                            val error = IllegalStateException("Bukkit WorldCreator returned null for $instanceWorldName")
                            logError("world.log_world_creator_null", error, "instance" to instanceWorldName)
                            future.completeExceptionally(error)
                            return@runGlobal
                        }

                        // Apply mandatory match game rules
                        @Suppress("DEPRECATION")
                        world.setGameRule(GameRule.KEEP_INVENTORY, false)
                        @Suppress("DEPRECATION")
                        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
                        @Suppress("DEPRECATION")
                        world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
                        world.isAutoSave = false // Ephemeral instance, chunks should never be committed to disk

                        activeInstances.add(instanceWorldName)

                        logInfo("world.log_mounted", "instance" to instanceWorldName, "world" to world.name)
                        future.complete(world)
                    } catch (t: Throwable) {
                        logError("world.log_load_error", t, "instance" to instanceWorldName, "error" to (t.message ?: "unknown"))
                        future.completeExceptionally(t)
                    }
                }
            } catch (t: Throwable) {
                logError("world.log_copy_error", t, "template" to templateId, "error" to (t.message ?: "unknown"))
                future.completeExceptionally(t)
            }
        }

        return future
    }

    /**
     * Safely dismantles and deletes an ephemeral match instance world.
     *
     * Workflow:
     * 1. Evacuates all remaining players in the world to the lobby location using their respective EntitySchedulers.
     * 2. Hops to the Global Region Scheduler to unload the world without saving chunks ([Bukkit.unloadWorld]).
     * 3. Offloads asynchronous directory deletion to disk with retry logic on file locks.
     *
     * @param world The instance world to delete.
     * @return [CompletableFuture] that completes when the world is unloaded and its directory deleted from disk.
     */
    fun deleteInstance(world: World): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        val worldName = world.name
        val worldFolder = world.worldFolder
        val targetLobby = getEffectiveLobbyLocation(excludeWorld = world)

        logInfo("world.log_decommissioning", "world" to worldName)

        // Step 1: Evacuate all players currently inside this world via their EntitySchedulers
        val players = world.players.toList()
        val evacuationFutures = players.map { player -> evacuatePlayer(player, targetLobby) }

        // When all players are safely transported out
        CompletableFuture.allOf(*evacuationFutures.toTypedArray()).whenComplete { _, _ ->
            // Step 2: Unload world on Folia Global Region Scheduler
            activeScheduler.runGlobal {
                try {
                    logInfo("world.log_unloading", "world" to worldName)
                    val unloaded = Bukkit.unloadWorld(world, false)
                    if (!unloaded) {
                        logWarning("world.log_unload_false", "world" to worldName)
                    }
                    activeInstances.remove(worldName)

                    // Step 3: Asynchronously delete directory from disk with retry logic
                    activeScheduler.runAsync {
                        deleteWorldFolderWithRetry(worldFolder, future)
                    }
                } catch (t: Throwable) {
                    logError("world.log_unload_error", t, "world" to worldName, "error" to (t.message ?: "unknown"))
                    future.completeExceptionally(t)
                }
            }
        }

        return future
    }

    /**
     * Overload to delete an instance by match ID.
     */
    fun deleteInstance(matchId: String): CompletableFuture<Void> {
        val instanceWorldName = "instances/match_$matchId"
        val world = Bukkit.getWorld(instanceWorldName) ?: Bukkit.getWorld("match_$matchId")
        return if (world != null) {
            deleteInstance(world)
        } else {
            val future = CompletableFuture<Void>()
            val targetDir = getInstanceWorldFolder(matchId)
            activeScheduler.runAsync {
                deleteWorldFolderWithRetry(targetDir, future)
            }
            future
        }
    }

    /**
     * Evacuates a single player to the lobby location using their Folia [io.papermc.paper.threadedregions.scheduler.EntityScheduler].
     */
    private fun evacuatePlayer(player: Player, lobby: Location): CompletableFuture<Void> {
        val playerFuture = CompletableFuture<Void>()
        val task = activeScheduler.runAtEntity(
            entity = player,
            runnable = {
                player.teleportAsync(lobby).whenComplete { _, ex ->
                    if (ex != null) {
                        logWarning("world.log_evacuation_error", "player" to player.name, "error" to (ex.message ?: "unknown"))
                    }
                    playerFuture.complete(null)
                }
            },
            retired = {
                // Player disconnected or entity retired during evacuation
                playerFuture.complete(null)
            }
        )

        if (task == null) {
            playerFuture.complete(null)
        }
        return playerFuture
    }

    /**
     * Asynchronously copies a world directory tree using Java NIO while skipping collision files.
     */
    private fun copyTemplateWorldFolder(sourcePath: Path, targetPath: Path) {
        if (!Files.exists(targetPath)) {
            Files.createDirectories(targetPath)
        }

        Files.walkFileTree(sourcePath, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                val relative = sourcePath.relativize(dir)
                val targetDir = targetPath.resolve(relative)
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir)
                }
                return FileVisitResult.CONTINUE
            }

            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                val fileName = file.fileName.toString()
                // Skip uid.dat and session lock files during copy
                if (isWorldCollisionFile(fileName)) {
                    return FileVisitResult.CONTINUE
                }

                val relative = sourcePath.relativize(file)
                val destFile = targetPath.resolve(relative)
                Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)
                return FileVisitResult.CONTINUE
            }
        })
    }

    /**
     * Recursively traverses the directory and deletes any `uid.dat` or session lock files.
     */
    fun purgeWorldCollisionFiles(rootPath: Path) {
        if (!Files.exists(rootPath)) return

        Files.walkFileTree(rootPath, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (isWorldCollisionFile(file.fileName.toString())) {
                    runCatching { Files.deleteIfExists(file) }
                }
                return FileVisitResult.CONTINUE
            }
        })
    }

    /**
     * Checks if a filename is a collision-inducing Minecraft world file.
     */
    private fun isWorldCollisionFile(fileName: String): Boolean {
        val lower = fileName.lowercase()
        return lower == "uid.dat" || lower == "session.lock" || lower.startsWith("session.lock.")
    }

    /**
     * Deletes a directory recursively using Java NIO.
     */
    private fun deleteDirectoryRecursively(rootPath: Path) {
        if (!Files.exists(rootPath)) return

        Files.walkFileTree(rootPath, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                runCatching { Files.deleteIfExists(file) }
                return FileVisitResult.CONTINUE
            }

            override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                runCatching { Files.deleteIfExists(dir) }
                return FileVisitResult.CONTINUE
            }
        })
        runCatching { Files.deleteIfExists(rootPath) }
    }

    /**
     * Deletes the world directory asynchronously with retry logic and garbage collection
     * to safely release Windows/Linux chunk file locks and memory-mapped buffers.
     */
    private fun deleteWorldFolderWithRetry(folder: File, future: CompletableFuture<Void>) {
        val maxRetries = 10
        var attempt = 0
        var lastError: Exception? = null

        while (attempt < maxRetries) {
            attempt++
            try {
                if (!folder.exists()) {
                    logInfo("world.log_folder_deleted", "folder" to folder.name)
                    future.complete(null)
                    return
                }

                deleteDirectoryRecursively(folder.toPath())

                if (!folder.exists()) {
                    logInfo("world.log_folder_deleted_attempt", "folder" to folder.name, "attempt" to attempt)
                    future.complete(null)
                    return
                }
            } catch (e: Exception) {
                lastError = e
            }

            if (attempt < maxRetries) {
                // Request garbage collection to help release unclosed memory-mapped file handles
                System.gc()
                // Exponential backoff with jitter: 50ms, 100ms, 200ms, 400ms, 800ms... up to 1500ms
                val backoffMs = minOf(50L * (1L shl (attempt - 1)), 1500L)
                try {
                    Thread.sleep(backoffMs)
                } catch (ie: InterruptedException) {
                    Thread.currentThread().interrupt()
                    lastError = ie
                    break
                }
            }
        }

        if (!folder.exists()) {
            future.complete(null)
        } else {
            logWarning("world.log_delete_retry_warning", "folder" to folder.path, "retries" to maxRetries)
            folder.deleteOnExit()
            future.completeExceptionally(
                lastError ?: IOException("Failed to delete instance folder '${folder.path}' after $maxRetries attempts.")
            )
        }
    }

    /**
     * Resolves the source template directory for a given template ID across known locations.
     */
    fun resolveTemplateWorldDirectory(templateId: String): File {
        val cleanId = templateId.trim()

        // 1. Primary path: <templatesWorldsFolder>/<templateId>
        val primary = File(templatesWorldsFolder, cleanId)
        if (primary.exists() && primary.isDirectory) return primary

        // 2. Relative "templates_worlds/<templateId>"
        val relative = File("templates_worlds", cleanId)
        if (relative.exists() && relative.isDirectory) return relative

        // 3. Plugin data directory: <plugin.dataFolder>/templates_worlds/<templateId>
        val pluginFolder = runCatching { activePlugin.dataFolder }.getOrNull()
        if (pluginFolder != null) {
            val pluginTemplates = File(File(pluginFolder, "templates_worlds"), cleanId)
            if (pluginTemplates.exists() && pluginTemplates.isDirectory) return pluginTemplates

            // 4. Plugin data directory: <plugin.dataFolder>/templates/<templateId>
            val pluginFallback = File(File(pluginFolder, "templates"), cleanId)
            if (pluginFallback.exists() && pluginFallback.isDirectory) return pluginFallback
        }

        return primary
    }

    /**
     * Resolves the target directory for a match instance world.
     */
    fun getInstanceWorldFolder(matchId: String): File {
        return File(instancesFolder, "match_$matchId")
    }

    /**
     * Resolves the effective lobby evacuation location.
     */
    fun getEffectiveLobbyLocation(excludeWorld: World? = null): Location {
        lobbyLocation?.let { loc ->
            if (excludeWorld == null || loc.world != excludeWorld) {
                return loc
            }
        }

        val fallbackWorld = Bukkit.getWorlds().firstOrNull { it != excludeWorld }
            ?: Bukkit.getWorlds().firstOrNull()

        return fallbackWorld?.spawnLocation ?: Location(fallbackWorld, 0.0, 64.0, 0.0)
    }

    /**
     * Returns an unmodifiable set of all active ephemeral match instances.
     */
    fun getActiveInstances(): Set<String> = Collections.unmodifiableSet(activeInstances)

    /**
     * Returns count of currently active ephemeral match instances.
     */
    fun getActiveInstanceCount(): Int = activeInstances.size

    /**
     * Cleanly dismantles all active instances during plugin shutdown or maintenance.
     */
    fun cleanupAllInstances(): CompletableFuture<Void> {
        val futures = activeInstances.mapNotNull { worldName ->
            Bukkit.getWorld(worldName)?.let { deleteInstance(it) }
        }
        return CompletableFuture.allOf(*futures.toTypedArray())
    }

    private fun logInfo(key: String, vararg placeholders: Pair<String, Any?>) {
        TranslationManager.logInfo(key, *placeholders)
    }

    private fun logWarning(key: String, vararg placeholders: Pair<String, Any?>) {
        TranslationManager.logWarning(key, *placeholders)
    }

    private fun logError(key: String, throwable: Throwable? = null, vararg placeholders: Pair<String, Any?>) {
        TranslationManager.logError(key, throwable, *placeholders)
    }
}
