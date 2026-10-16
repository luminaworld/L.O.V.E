package com.luminaris.extraction.storage

import com.luminaris.extraction.core.container.PlacedContainer
import com.luminaris.extraction.core.template.ExtractionZoneData
import com.luminaris.extraction.core.template.SpawnPoint
import com.luminaris.extraction.core.template.TemplateMapData
import com.luminaris.extraction.localization.TranslationManager
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Thread-safe persistence service managing template map markers, loot containers,
 * spawn points, and extraction evacuation zones.
 *
 * Persists data to `templates/<template_id>/map_data.yml`.
 */
class TemplateStorage(private val plugin: Plugin) {

    val templatesFolder: File = File(plugin.dataFolder, "templates")
    private val lock = ReentrantReadWriteLock()
    private val templates = ConcurrentHashMap<String, TemplateMapData>()

    /**
     * Initializes the storage directory and loads all configured map templates from disk.
     */
    fun initialize() {
        lock.write {
            if (!templatesFolder.exists()) {
                templatesFolder.mkdirs()
            }
            loadAll()
        }
    }

    /**
     * Scans the `templates` directory and loads all templates into the in-memory cache.
     */
    fun loadAll() {
        lock.write {
            templates.clear()
            val dirs = templatesFolder.listFiles { file -> file.isDirectory } ?: return
            for (dir in dirs) {
                val mapFile = File(dir, "map_data.yml")
                if (mapFile.exists()) {
                    runCatching {
                        loadTemplate(dir.name)
                    }.onFailure { ex ->
                        TranslationManager.logWarning("storage.log_template_load_error", "path" to mapFile.path, "error" to (ex.message ?: "unknown"))
                    }
                }
            }
        }
    }

    /**
     * Determines whether the given world is designated as a template world.
     */
    fun isTemplateWorld(world: World): Boolean {
        return resolveTemplateId(world) != null
    }

    /**
     * Resolves the template ID corresponding to a given Bukkit World.
     * Supports explicit world folder, prefix (`template_`), suffix (`_template`),
     * or active registered template caches.
     */
    fun resolveTemplateId(world: World): String? {
        val worldName = world.name

        // 1. Direct directory match: templates/<worldName>/map_data.yml exists
        val directFile = File(File(templatesFolder, worldName.lowercase()), "map_data.yml")
        if (directFile.exists()) {
            return worldName.lowercase()
        }

        // 2. Already cached template matching world name
        if (templates.containsKey(worldName.lowercase())) {
            return worldName.lowercase()
        }

        // 3. Prefix matching: template_<name> or template-<name>
        if (worldName.startsWith("template_", ignoreCase = true)) {
            val stripped = worldName.substring(9).lowercase()
            if (stripped.isNotBlank()) return stripped
        }
        if (worldName.startsWith("template-", ignoreCase = true)) {
            val stripped = worldName.substring(9).lowercase()
            if (stripped.isNotBlank()) return stripped
        }

        // 4. Suffix matching: <name>_template or <name>-template
        if (worldName.endsWith("_template", ignoreCase = true)) {
            val stripped = worldName.substring(0, worldName.length - 9).lowercase()
            if (stripped.isNotBlank()) return stripped
        }
        if (worldName.endsWith("-template", ignoreCase = true)) {
            val stripped = worldName.substring(0, worldName.length - 9).lowercase()
            if (stripped.isNotBlank()) return stripped
        }

        // 5. Contains keyword "template"
        if (worldName.contains("template", ignoreCase = true)) {
            return worldName.lowercase()
        }

        return null
    }

    /**
     * Retrieves or loads a [TemplateMapData] for the given [World], creating a new entry if in a template world.
     */
    fun getOrCreateTemplateForWorld(world: World): TemplateMapData? {
        val templateId = resolveTemplateId(world) ?: return null
        return getOrCreateTemplate(templateId, world.name)
    }

    /**
     * Retrieves a cached [TemplateMapData] by its template ID, or loads from disk if available.
     */
    fun getTemplate(templateId: String): TemplateMapData? {
        val key = templateId.lowercase()
        templates[key]?.let { return it }

        return lock.read {
            templates[key] ?: run {
                val file = getMapFile(key)
                if (file.exists()) {
                    loadTemplate(key)
                } else {
                    null
                }
            }
        }
    }

    /**
     * Gets an existing template or creates and persists a new [TemplateMapData].
     */
    fun getOrCreateTemplate(templateId: String, displayName: String = templateId): TemplateMapData {
        val key = templateId.lowercase()
        getTemplate(key)?.let { return it }

        return lock.write {
            templates.getOrPut(key) {
                val file = getMapFile(key)
                if (file.exists()) {
                    loadTemplate(key)
                } else {
                    val newTemplate = TemplateMapData(templateId = key, displayName = displayName)
                    saveTemplate(newTemplate)
                    newTemplate
                }
            }
        }
    }

    /**
     * Appends a placed container entry into the specified template, persisting changes to disk.
     *
     * @param templateId Target template identifier.
     * @param container Container placement data model.
     * @param saveToDisk Whether to write immediately to disk (default true).
     * @return True if added successfully.
     */
    fun addContainer(templateId: String, container: PlacedContainer, saveToDisk: Boolean = true): Boolean {
        val key = templateId.lowercase()
        return lock.write {
            val data = getOrCreateTemplate(key)
            // Remove any existing container at the exact same coordinates
            data.containers.removeIf { it.x == container.x && it.y == container.y && it.z == container.z }
            data.containers.add(container)
            if (saveToDisk) {
                saveTemplate(data)
            }
            true
        }
    }

    /**
     * Removes a container marker at the given coordinates from the template.
     *
     * @param templateId Target template identifier.
     * @param x Block coordinate X.
     * @param y Block coordinate Y.
     * @param z Block coordinate Z.
     * @param saveToDisk Whether to persist changes to disk (default true).
     * @return The removed [PlacedContainer], or null if not found.
     */
    fun removeContainer(templateId: String, x: Int, y: Int, z: Int, saveToDisk: Boolean = true): PlacedContainer? {
        val key = templateId.lowercase()
        return lock.write {
            val data = getTemplate(key) ?: return@write null
            val index = data.containers.indexOfFirst { it.x == x && it.y == y && it.z == z }
            if (index != -1) {
                val removed = data.containers.removeAt(index)
                if (saveToDisk) {
                    saveTemplate(data)
                }
                removed
            } else {
                null
            }
        }
    }

    /**
     * Finds a placed container registered at specific coordinates within a template.
     */
    fun getContainerAt(templateId: String, x: Int, y: Int, z: Int): PlacedContainer? {
        val key = templateId.lowercase()
        return lock.read {
            val data = getTemplate(key) ?: return@read null
            data.containers.find { it.x == x && it.y == y && it.z == z }
        }
    }

    /**
     * Retrieves an unmodifiable snapshot list of all containers registered for a template.
     */
    fun getContainers(templateId: String): List<PlacedContainer> {
        val key = templateId.lowercase()
        return lock.read {
            getTemplate(key)?.containers?.toList() ?: emptyList()
        }
    }

    /**
     * Reads `templates/<template_id>/map_data.yml` from disk, deserializing containers,
     * spawns, and extractions into a [TemplateMapData].
     */
    fun loadTemplate(templateId: String): TemplateMapData {
        val key = templateId.lowercase()
        val file = getMapFile(key)

        return lock.write {
            if (!file.exists()) {
                val empty = TemplateMapData(templateId = key, displayName = key)
                templates[key] = empty
                return@write empty
            }

            val yaml = YamlConfiguration.loadConfiguration(file)
            val resolvedId = yaml.getString("template_id") ?: yaml.getString("templateId") ?: key
            val displayName = yaml.getString("display_name") ?: yaml.getString("displayName") ?: resolvedId

            // Deserialization: Containers
            val containers = mutableListOf<PlacedContainer>()
            val rawContainers = yaml.get("containers")
            if (rawContainers is List<*>) {
                for (item in rawContainers) {
                    if (item is Map<*, *>) {
                        val id = item["id"]?.toString() ?: continue
                        val catId = item["category_id"]?.toString() ?: item["categoryId"]?.toString() ?: item["category"]?.toString() ?: "default"
                        val x = (item["x"] as? Number)?.toInt() ?: 0
                        val y = (item["y"] as? Number)?.toInt() ?: 0
                        val z = (item["z"] as? Number)?.toInt() ?: 0
                        containers.add(PlacedContainer(id = id, categoryId = catId, x = x, y = y, z = z))
                    }
                }
            } else if (yaml.isConfigurationSection("containers")) {
                val sec = yaml.getConfigurationSection("containers")!!
                for (secKey in sec.getKeys(false)) {
                    val cSec = sec.getConfigurationSection(secKey) ?: continue
                    val id = cSec.getString("id", secKey) ?: secKey
                    val catId = cSec.getString("category_id") ?: cSec.getString("categoryId") ?: cSec.getString("category", "default")!!
                    val x = cSec.getInt("x")
                    val y = cSec.getInt("y")
                    val z = cSec.getInt("z")
                    containers.add(PlacedContainer(id = id, categoryId = catId, x = x, y = y, z = z))
                }
            }

            // Deserialization: Spawn Points (Step 5 placeholder)
            val spawns = mutableListOf<SpawnPoint>()
            val rawSpawns = yaml.get("spawns")
            if (rawSpawns is List<*>) {
                for (item in rawSpawns) {
                    if (item is Map<*, *>) {
                        val id = item["id"]?.toString() ?: continue
                        val name = item["name"]?.toString() ?: "Spawn"
                        val x = (item["x"] as? Number)?.toDouble() ?: 0.0
                        val y = (item["y"] as? Number)?.toDouble() ?: 64.0
                        val z = (item["z"] as? Number)?.toDouble() ?: 0.0
                        val yaw = (item["yaw"] as? Number)?.toFloat() ?: 0.0f
                        val pitch = (item["pitch"] as? Number)?.toFloat() ?: 0.0f
                        spawns.add(SpawnPoint(id = id, name = name, x = x, y = y, z = z, yaw = yaw, pitch = pitch))
                    }
                }
            }

            // Deserialization: Extraction Zones
            val extractions = mutableListOf<ExtractionZoneData>()
            val rawExtractions = yaml.get("extractions")
            if (rawExtractions is List<*>) {
                for (item in rawExtractions) {
                    if (item is Map<*, *>) {
                        val id = item["id"]?.toString() ?: continue
                        val name = item["name"]?.toString() ?: "Extraction Zone"
                        val minX = (item["min_x"] as? Number)?.toDouble() ?: 0.0
                        val minY = (item["min_y"] as? Number)?.toDouble() ?: 0.0
                        val minZ = (item["min_z"] as? Number)?.toDouble() ?: 0.0
                        val maxX = (item["max_x"] as? Number)?.toDouble() ?: 0.0
                        val maxY = (item["max_y"] as? Number)?.toDouble() ?: 0.0
                        val maxZ = (item["max_z"] as? Number)?.toDouble() ?: 0.0
                        val countdown = (item["countdown_seconds"] as? Number)?.toInt() ?: 10
                        val type = item["type"]?.toString() ?: "ALWAYS_OPEN"
                        val requiredItemId = item["required_item_id"]?.toString() ?: item["requiredItemId"]?.toString()
                        val timeStart = (item["time_start_seconds"] as? Number)?.toLong() ?: (item["timeStartSeconds"] as? Number)?.toLong() ?: 0L
                        val timeEnd = (item["time_end_seconds"] as? Number)?.toLong() ?: (item["timeEndSeconds"] as? Number)?.toLong() ?: Long.MAX_VALUE
                        val teams = (item["allowed_team_ids"] as? List<*>)?.mapNotNull { it?.toString() }
                            ?: (item["allowedTeamIds"] as? List<*>)?.mapNotNull { it?.toString() }
                            ?: emptyList()
                        extractions.add(
                            ExtractionZoneData(
                                id = id,
                                name = name,
                                minX = minX,
                                minY = minY,
                                minZ = minZ,
                                maxX = maxX,
                                maxY = maxY,
                                maxZ = maxZ,
                                countdownSeconds = countdown,
                                type = type,
                                requiredItemId = requiredItemId,
                                timeStartSeconds = timeStart,
                                timeEndSeconds = timeEnd,
                                allowedTeamIds = teams
                            )
                        )
                    }
                }
            }

            val result = TemplateMapData(
                templateId = resolvedId,
                displayName = displayName,
                containers = containers,
                spawns = spawns,
                extractions = extractions
            )
            templates[key] = result
            result
        }
    }

    /**
     * Persists [TemplateMapData] to `templates/<template_id>/map_data.yml`.
     */
    fun saveTemplate(data: TemplateMapData) {
        val key = data.templateId.lowercase()
        val file = getMapFile(key)

        val yaml = YamlConfiguration()
        yaml.set("template_id", data.templateId)
        yaml.set("display_name", data.displayName)

        lock.write {
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }

            // Containers serialization
            val containerList = data.containers.map { c ->
                mapOf(
                    "id" to c.id,
                    "category_id" to c.categoryId,
                    "x" to c.x,
                    "y" to c.y,
                    "z" to c.z
                )
            }
            yaml.set("containers", containerList)

            // Spawn points serialization
            val spawnList = data.spawns.map { s ->
                mapOf(
                    "id" to s.id,
                    "name" to s.name,
                    "x" to s.x,
                    "y" to s.y,
                    "z" to s.z,
                    "yaw" to s.yaw.toDouble(),
                    "pitch" to s.pitch.toDouble()
                )
            }
            yaml.set("spawns", spawnList)

            // Extraction zones serialization
            val extractionList = data.extractions.map { e ->
                val map = mutableMapOf<String, Any>(
                    "id" to e.id,
                    "name" to e.name,
                    "min_x" to e.minX,
                    "min_y" to e.minY,
                    "min_z" to e.minZ,
                    "max_x" to e.maxX,
                    "max_y" to e.maxY,
                    "max_z" to e.maxZ,
                    "countdown_seconds" to e.countdownSeconds,
                    "type" to e.type,
                    "time_start_seconds" to e.timeStartSeconds,
                    "time_end_seconds" to e.timeEndSeconds,
                    "allowed_team_ids" to e.allowedTeamIds
                )
                if (e.requiredItemId != null) {
                    map["required_item_id"] = e.requiredItemId
                }
                map
            }
            yaml.set("extractions", extractionList)

            templates[key] = data
        }

        // Offload disk I/O asynchronously to avoid stalling server region ticks
        runCatching {
            com.luminaris.extraction.util.FoliaScheduler.default.runAsync {
                try {
                    if (!file.parentFile.exists()) {
                        file.parentFile.mkdirs()
                    }
                    yaml.save(file)
                } catch (t: Throwable) {
                    TranslationManager.logWarning("storage.log_template_save_error", "template" to data.templateId, "error" to (t.message ?: "unknown"))
                }
            }
        }.onFailure {
            // Fallback for tests or shutdown environments where Folia scheduler is not initialized
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            yaml.save(file)
        }
    }

    /**
     * Returns an unmodifiable snapshot list of all currently loaded templates.
     */
    fun getAllTemplates(): List<TemplateMapData> = lock.read {
        templates.values.toList()
    }

    /**
     * Helper to retrieve the target map file for a template.
     */
    fun getMapFile(templateId: String): File {
        val folder = File(templatesFolder, templateId.lowercase())
        return File(folder, "map_data.yml")
    }
}
