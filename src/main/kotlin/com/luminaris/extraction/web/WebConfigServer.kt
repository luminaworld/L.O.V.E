package com.luminaris.extraction.web

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.util.FoliaScheduler
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import java.util.logging.Level

/**
 * Lightweight, embedded HTTP web server for live configuration management.
 * Provides a responsive monochrome web interface and REST API to view and edit all
 * plugin YAML files (master configs, registries, safe cases, localization).
 * All static UI files are stored in plugins/LuminaExtractionCore/web/ allowing server owners
 * to fully customize the web interface.
 */
class WebConfigServer(private val plugin: LuminarisCorePlugin) {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(java.util.Optional::class.java, com.google.gson.JsonSerializer<java.util.Optional<*>> { src, _, context ->
            if (src.isPresent) context.serialize(src.get()) else com.google.gson.JsonNull.INSTANCE
        })
        .registerTypeAdapter(java.util.OptionalInt::class.java, com.google.gson.JsonSerializer<java.util.OptionalInt> { src, _, context ->
            if (src.isPresent) context.serialize(src.asInt) else com.google.gson.JsonNull.INSTANCE
        })
        .registerTypeAdapter(java.util.OptionalLong::class.java, com.google.gson.JsonSerializer<java.util.OptionalLong> { src, _, context ->
            if (src.isPresent) context.serialize(src.asLong) else com.google.gson.JsonNull.INSTANCE
        })
        .registerTypeAdapter(java.util.OptionalDouble::class.java, com.google.gson.JsonSerializer<java.util.OptionalDouble> { src, _, context ->
            if (src.isPresent) context.serialize(src.asDouble) else com.google.gson.JsonNull.INSTANCE
        })
        .create()

    private var httpServer: HttpServer? = null
    private var executor = Executors.newCachedThreadPool { runnable ->
        Thread(runnable, "Lumina-WebConfig-Worker").apply { isDaemon = true }
    }

    var isRunning: Boolean = false
        private set

    val webDir: File = File(plugin.dataFolder, "web")

    /**
     * Extracts default web UI assets to the plugin data folder.
     * Automatically updates assets if version tag changes or if forced.
     */
    fun ensureWebAssets(force: Boolean = false) {
        if (!webDir.exists()) {
            webDir.mkdirs()
        }
        val cssDir = File(webDir, "css")
        val jsDir = File(webDir, "js")
        if (!cssDir.exists()) cssDir.mkdirs()
        if (!jsDir.exists()) jsDir.mkdirs()

        val versionFile = File(webDir, ".version")
        val versionTag = plugin.pluginMeta.version + "-v10"
        val needsUpdate = force || !versionFile.exists() || versionFile.readText().trim() != versionTag

        if (needsUpdate) {
            extractAsset("web/index.html", File(webDir, "index.html"), true)
            extractAsset("web/css/style.css", File(cssDir, "style.css"), true)
            extractAsset("web/js/app.js", File(jsDir, "app.js"), true)
            extractAsset("web/items_textures.json", File(webDir, "items_textures.json"), true)
            runCatching { versionFile.writeText(versionTag) }
        }
    }

    /**
     * Ensures all default YAML configuration files exist in the data folder so they are editable.
     */
    fun ensureDefaultConfigs() {
        val configs = listOf(
            "config.yml",
            "items.yml",
            "categories.yml",
            "safe_cases.yml",
            "lang/en.yml",
            "lang/th.yml"
        )
        for (relPath in configs) {
            val target = File(plugin.dataFolder, relPath)
            if (!target.exists() || target.length() == 0L) {
                try {
                    val parent = target.parentFile
                    if (parent != null && !parent.exists()) parent.mkdirs()
                    plugin.getResource(relPath)?.use { input ->
                        target.outputStream().use { output -> input.copyTo(output) }
                    }
                } catch (e: Exception) {
                    plugin.logger.log(Level.WARNING, "[WebConfig] Failed to initialize default config $relPath: ${e.message}")
                }
            }
        }
    }

    private fun extractAsset(resourcePath: String, targetFile: File, force: Boolean = false) {
        if (targetFile.exists() && targetFile.length() > 0 && !force) return
        try {
            plugin.getResource(resourcePath)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: plugin.logger.warning("[WebConfig] Resource $resourcePath not found in plugin JAR.")
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "Failed to extract web asset $resourcePath", e)
        }
    }

    /**
     * Starts the HTTP server based on settings in config.yml.
     */
    @Synchronized
    fun start() {
        if (isRunning) {
            return
        }

        val enabled = plugin.config.getBoolean("web_config.enabled", true)
        if (!enabled) {
            plugin.logger.info("[WebConfig] Web configuration server is disabled in config.yml.")
            return
        }

        val port = plugin.config.getInt("web_config.port", 8080)
        val bindAddress = plugin.config.getString("web_config.bind_address", "0.0.0.0") ?: "0.0.0.0"

        try {
            ensureDefaultConfigs()
            ensureWebAssets()

            val socketAddress = if (bindAddress == "0.0.0.0") {
                InetSocketAddress(port)
            } else {
                InetSocketAddress(bindAddress, port)
            }

            val server = HttpServer.create(socketAddress, 0)
            server.executor = executor

            // Register API routes
            server.createContext("/api/status", StatusHandler())
            server.createContext("/api/files", FilesHandler())
            server.createContext("/api/config", ConfigHandler())
            server.createContext("/api/materials", MaterialsHandler())
            server.createContext("/api/reload", ReloadHandler())

            // Register static web assets route
            server.createContext("/", StaticFileHandler())

            server.start()
            httpServer = server
            isRunning = true

            val hostDisplay = if (bindAddress == "0.0.0.0") "localhost" else bindAddress
            plugin.logger.info("[WebConfig] Embedded web configuration server started at http://$hostDisplay:$port/ (Port: $port)")
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "[WebConfig] Failed to start web configuration server on port $port: ${e.message}", e)
        }
    }

    /**
     * Stops the HTTP server cleanly.
     */
    @Synchronized
    fun stop() {
        if (!isRunning || httpServer == null) {
            return
        }
        try {
            httpServer?.stop(1)
            httpServer = null
            isRunning = false
            plugin.logger.info("[WebConfig] Embedded web configuration server stopped.")
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "[WebConfig] Error stopping web server: ${e.message}", e)
        }
    }

    /**
     * Reloads configuration and restarts server if port or enabled status changed.
     */
    @Synchronized
    fun reloadConfig() {
        stop()
        start()
    }

    // --- Authentication & Security Helpers ---

    private fun checkAuth(exchange: HttpExchange): Boolean {
        val configuredToken = plugin.config.getString("web_config.auth_token", "")?.trim() ?: ""
        if (configuredToken.isEmpty()) {
            return true
        }

        // Check Authorization header: "Bearer <token>" or raw token
        val authHeader = exchange.requestHeaders.getFirst("Authorization")
        if (authHeader != null) {
            val token = if (authHeader.startsWith("Bearer ", ignoreCase = true)) {
                authHeader.substring(7).trim()
            } else {
                authHeader.trim()
            }
            if (token == configuredToken) return true
        }

        // Check custom header "X-Auth-Token"
        val customHeader = exchange.requestHeaders.getFirst("X-Auth-Token")?.trim()
        if (customHeader == configuredToken) return true

        // Check query parameter "?token=<token>"
        val query = exchange.requestURI.query ?: ""
        for (param in query.split("&")) {
            val parts = param.split("=", limit = 2)
            if (parts.size == 2 && parts[0] == "token" && parts[1] == configuredToken) {
                return true
            }
        }

        return false
    }

    private fun handleCors(exchange: HttpExchange): Boolean {
        val headers = exchange.responseHeaders
        headers.set("Access-Control-Allow-Origin", "*")
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        headers.set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Auth-Token")
        if (exchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
            exchange.sendResponseHeaders(204, -1)
            exchange.close()
            return true
        }
        return false
    }

    private fun sendJson(exchange: HttpExchange, statusCode: Int, data: Any) {
        try {
            val json = gson.toJson(data)
            val bytes = json.toByteArray(StandardCharsets.UTF_8)
            val headers = exchange.responseHeaders
            headers.set("Access-Control-Allow-Origin", "*")
            headers.set("Content-Type", "application/json; charset=utf-8")
            headers.set("Cache-Control", "no-cache, no-store, must-revalidate")
            exchange.sendResponseHeaders(statusCode, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "[WebConfig] Error sending JSON response: ${e.message}")
        }
    }

    private fun sendError(exchange: HttpExchange, statusCode: Int, message: String) {
        sendJson(exchange, statusCode, mapOf("success" to false, "error" to message))
    }

    /**
     * Validates that requested relative path is safe (no traversal) and within plugin data folder.
     */
    private fun resolveSafeFile(relativePath: String): File? {
        val cleanPath = relativePath.trim().removePrefix("/").replace('\\', '/')
        if (cleanPath.contains("..") || cleanPath.startsWith("/")) {
            return null
        }
        val file = File(plugin.dataFolder, cleanPath)
        val canonicalFile = runCatching { file.canonicalFile }.getOrNull() ?: return null
        val canonicalData = runCatching { plugin.dataFolder.canonicalFile }.getOrNull() ?: return null

        if (!canonicalFile.path.startsWith(canonicalData.path)) {
            return null
        }
        return canonicalFile
    }

    // --- HTTP Handlers ---

    /**
     * /api/status - Returns server health, active platform, version, and auth requirement.
     */
    private inner class StatusHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            try {
                if (handleCors(exchange)) return
                val token = plugin.config.getString("web_config.auth_token", "")?.trim() ?: ""
                val platform = if (FoliaScheduler.isFolia) "Folia" else "Paper"

                val response = mapOf(
                    "success" to true,
                    "plugin" to plugin.name,
                    "version" to plugin.pluginMeta.version,
                    "platform" to platform,
                    "language" to (plugin.config.getString("language", "th") ?: "th"),
                    "authRequired" to token.isNotEmpty(),
                    "serverTime" to System.currentTimeMillis()
                )
                sendJson(exchange, 200, response)
            } catch (t: Throwable) {
                plugin.logger.log(Level.SEVERE, "[WebConfig] StatusHandler exception: ${t.message}", t)
                sendError(exchange, 500, "Internal Server Error: ${t.message}")
            }
        }
    }

    /**
     * /api/files - Returns all editable YAML configuration files found in the plugin data directory.
     */
    private inner class FilesHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            try {
                if (handleCors(exchange)) return
                if (!checkAuth(exchange)) {
                    sendError(exchange, 401, "จำเป็นต้องเข้าสู่ระบบ / Unauthorized: Authentication required.")
                    return
                }

                ensureDefaultConfigs()

                val filesList = mutableListOf<Map<String, Any>>()
                val dataFolder = plugin.dataFolder

                fun scanDir(dir: File) {
                    val files = dir.listFiles() ?: return
                    for (file in files) {
                        if (file.isDirectory) {
                            if (file.name != "web" && !file.name.startsWith(".")) {
                                scanDir(file)
                            }
                        } else if (file.isFile && (file.name.endsWith(".yml") || file.name.endsWith(".yaml"))) {
                            val relPath = file.relativeTo(dataFolder).path.replace('\\', '/')
                            val category = when {
                                relPath == "config.yml" -> "Master"
                                relPath == "items.yml" -> "Items"
                                relPath == "categories.yml" -> "Categories"
                                relPath == "safe_cases.yml" -> "Safe Cases"
                                relPath.startsWith("lang/") -> "Language"
                                else -> "Custom"
                            }
                            filesList.add(
                                mapOf(
                                    "name" to file.name,
                                    "path" to relPath,
                                    "size" to file.length(),
                                    "lastModified" to file.lastModified(),
                                    "category" to category
                                )
                            )
                        }
                    }
                }

                scanDir(dataFolder)
                // Sort: config.yml first, then items, categories, safe_cases, lang, others
                filesList.sortBy {
                    val p = it["path"] as String
                    when (p) {
                        "config.yml" -> "0_$p"
                        "items.yml" -> "1_$p"
                        "categories.yml" -> "2_$p"
                        "safe_cases.yml" -> "3_$p"
                        "lang/th.yml" -> "4_$p"
                        "lang/en.yml" -> "5_$p"
                        else -> "6_$p"
                    }
                }

                sendJson(exchange, 200, mapOf("success" to true, "files" to filesList))
            } catch (t: Throwable) {
                plugin.logger.log(Level.SEVERE, "[WebConfig] FilesHandler exception: ${t.message}", t)
                sendError(exchange, 500, "Internal Server Error: ${t.message}")
            }
        }
    }

    /**
     * /api/config?file=<relative-path> - GET to read file content & parsed YAML, POST to save & hot reload.
     */
    private inner class ConfigHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            try {
                if (handleCors(exchange)) return
                if (!checkAuth(exchange)) {
                    sendError(exchange, 401, "จำเป็นต้องเข้าสู่ระบบ / Unauthorized: Authentication required.")
                    return
                }

                val query = exchange.requestURI.query ?: ""
                var targetRelPath = ""
                for (part in query.split("&")) {
                    val kv = part.split("=", limit = 2)
                    if (kv.size == 2 && kv[0] == "file") {
                        targetRelPath = java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8.name())
                        break
                    }
                }

                if (targetRelPath.isEmpty()) {
                    sendError(exchange, 400, "ระบุพารามิเตอร์ไฟล์ไม่ถูกต้อง (Missing 'file' query parameter)")
                    return
                }

                val file = resolveSafeFile(targetRelPath)
                if (file == null || (!file.name.endsWith(".yml") && !file.name.endsWith(".yaml"))) {
                    sendError(exchange, 403, "ไม่อนุญาตให้เข้าถึงไฟล์นี้นอกโฟลเดอร์ปลั๊กอิน (Forbidden path)")
                    return
                }

                when (exchange.requestMethod.uppercase()) {
                    "GET" -> {
                        // If file not on disk yet, extract it automatically if present in plugin resources
                        if (!file.exists()) {
                            val parent = file.parentFile
                            if (parent != null && !parent.exists()) parent.mkdirs()
                            plugin.getResource(targetRelPath)?.use { input ->
                                file.outputStream().use { output -> input.copyTo(output) }
                            }
                        }

                        val rawContent = if (file.exists()) {
                            file.readText(StandardCharsets.UTF_8)
                        } else {
                            plugin.getResource(targetRelPath)?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }
                        }

                        if (rawContent == null) {
                            sendError(exchange, 404, "ไม่พบไฟล์คอนฟิก: $targetRelPath")
                            return
                        }

                        val yaml = YamlConfiguration()
                        val parsed = runCatching {
                            yaml.loadFromString(rawContent)
                            yamlSectionToMap(yaml)
                        }.getOrDefault(emptyMap<String, Any?>())

                        sendJson(
                            exchange,
                            200,
                            mapOf(
                                "success" to true,
                                "file" to targetRelPath,
                                "raw" to rawContent,
                                "parsed" to parsed
                            )
                        )
                    }

                    "POST" -> {
                        val requestBody = exchange.requestBody.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
                        if (requestBody.isBlank()) {
                            sendError(exchange, 400, "ข้อมูลที่ส่งมาว่างเปล่า (Empty request body)")
                            return
                        }

                        // Content can be passed as JSON { "content": "..." } or plain raw YAML
                        val contentToSave: String = if (requestBody.trimStart().startsWith("{")) {
                            try {
                                val jsonMap = gson.fromJson(requestBody, Map::class.java)
                                (jsonMap["content"] as? String) ?: requestBody
                            } catch (_: Exception) {
                                requestBody
                            }
                        } else {
                            requestBody
                        }

                        // Validate YAML syntax before writing to prevent corrupting server files
                        val validationYaml = YamlConfiguration()
                        try {
                            validationYaml.loadFromString(contentToSave)
                        } catch (e: Exception) {
                            sendError(exchange, 400, "ไวยากรณ์ YAML ไม่ถูกต้อง: ${e.message}")
                            return
                        }

                        // Write to disk
                        try {
                            val parent = file.parentFile
                            if (parent != null && !parent.exists()) parent.mkdirs()
                            file.writeText(contentToSave, StandardCharsets.UTF_8)

                            // Trigger in-game hot reload on global region scheduler
                            plugin.scheduler.runGlobal {
                                plugin.reloadAllConfigurations()
                            }

                            plugin.logger.info("[WebConfig] Configuration file '$targetRelPath' was updated and reloaded via web interface.")

                            sendJson(
                                exchange,
                                200,
                                mapOf(
                                    "success" to true,
                                    "message" to "บันทึกไฟล์ '$targetRelPath' และนำไปใช้งานในเกมสำเร็จเรียบร้อย!"
                                )
                            )
                        } catch (e: Exception) {
                            plugin.logger.log(Level.SEVERE, "[WebConfig] Failed to save config file: $targetRelPath", e)
                            sendError(exchange, 500, "ไม่สามารถบันทึกไฟล์ลงดิสก์ได้: ${e.message}")
                        }
                    }

                    "DELETE" -> {
                        val protectedFiles = setOf(
                            "config.yml",
                            "items.yml",
                            "categories.yml",
                            "safe_cases.yml",
                            "lang/th.yml",
                            "lang/en.yml"
                        )
                        if (targetRelPath.lowercase() in protectedFiles) {
                            sendError(exchange, 400, "ไม่อนุญาตให้ลบไฟล์ระบบหลัก ($targetRelPath)")
                            return
                        }

                        if (!file.exists()) {
                            sendError(exchange, 404, "ไม่พบไฟล์ที่ต้องการลบ: $targetRelPath")
                            return
                        }

                        if (file.delete()) {
                            plugin.scheduler.runGlobal {
                                plugin.reloadAllConfigurations()
                            }
                            sendJson(
                                exchange,
                                200,
                                mapOf("success" to true, "message" to "ลบไฟล์ '$targetRelPath' เรียบร้อยแล้ว")
                            )
                        } else {
                            sendError(exchange, 500, "ไม่สามารถลบไฟล์ '$targetRelPath' ได้ (ไฟล์ถูกล็อคหรือใช้งานอยู่)")
                        }
                    }

                    else -> sendError(exchange, 405, "Method Not Allowed")
                }
            } catch (t: Throwable) {
                plugin.logger.log(Level.SEVERE, "[WebConfig] ConfigHandler exception: ${t.message}", t)
                sendError(exchange, 500, "Internal Server Error: ${t.message}")
            }
        }
    }

    /**
     * /api/reload - Triggers a hot reload of all configurations without saving new data.
     */
    private inner class ReloadHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            try {
                if (handleCors(exchange)) return
                if (!checkAuth(exchange)) {
                    sendError(exchange, 401, "จำเป็นต้องเข้าสู่ระบบ / Unauthorized: Authentication required.")
                    return
                }

                if (!exchange.requestMethod.equals("POST", ignoreCase = true)) {
                    sendError(exchange, 405, "Method Not Allowed. Use POST.")
                    return
                }

                plugin.scheduler.runGlobal {
                    plugin.reloadAllConfigurations()
                }
                plugin.logger.info("[WebConfig] Full configuration hot-reload requested via web interface.")

                sendJson(
                    exchange,
                    200,
                    mapOf(
                        "success" to true,
                        "message" to "รีโหลดการตั้งค่าและฐานข้อมูลทั้งหมดในเกมสำเร็จแล้ว"
                    )
                )
            } catch (t: Throwable) {
                plugin.logger.log(Level.SEVERE, "[WebConfig] ReloadHandler exception: ${t.message}", t)
                sendError(exchange, 500, "Internal Server Error: ${t.message}")
            }
        }
    }

    /**
     * /api/materials - Returns all valid in-game Minecraft materials from Bukkit.
     */
    private inner class MaterialsHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            try {
                if (handleCors(exchange)) return
                if (!checkAuth(exchange)) {
                    sendError(exchange, 401, "จำเป็นต้องเข้าสู่ระบบ / Unauthorized: Authentication required.")
                    return
                }

                val materialsList = Material.entries
                    .filter { !it.name.startsWith("LEGACY_") }
                    .map { mat ->
                        val clean = mat.name.lowercase()
                        val isItem = runCatching { mat.isItem }.getOrDefault(true)
                        val isBlock = runCatching { mat.isBlock }.getOrDefault(false)
                        val maxStack = runCatching { mat.maxStackSize }.getOrDefault(64)
                        mapOf(
                            "name" to mat.name,
                            "id" to "minecraft:$clean",
                            "isItem" to isItem,
                            "isBlock" to isBlock,
                            "maxStackSize" to maxStack
                        )
                    }

                sendJson(
                    exchange,
                    200,
                    mapOf(
                        "success" to true,
                        "version" to "26.3",
                        "count" to materialsList.size,
                        "materials" to materialsList
                    )
                )
            } catch (t: Throwable) {
                plugin.logger.log(Level.SEVERE, "[WebConfig] MaterialsHandler exception: ${t.message}", t)
                sendError(exchange, 500, "Internal Server Error: ${t.message}")
            }
        }
    }

    /**
     * Static file handler serving HTML/CSS/JS directly from plugins/LuminaExtractionCore/web/
     * Allows server administrators to completely redesign or modify the web frontend.
     */
    private inner class StaticFileHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            try {
                if (handleCors(exchange)) return

                var path = exchange.requestURI.path
                if (path == "/" || path.isBlank()) {
                    path = "/index.html"
                }

                val cleanRel = path.trim().removePrefix("/").replace('\\', '/')
                if (cleanRel.contains("..")) {
                    sendError(exchange, 403, "Forbidden")
                    return
                }

                val file = File(webDir, cleanRel)
                val canonicalWeb = runCatching { webDir.canonicalFile }.getOrNull()
                val canonicalTarget = runCatching { file.canonicalFile }.getOrNull()

                if (canonicalWeb == null || canonicalTarget == null || !canonicalTarget.path.startsWith(canonicalWeb.path)) {
                    sendError(exchange, 403, "Forbidden")
                    return
                }

                if (!file.exists() || file.isDirectory) {
                    // If requesting a sub-path that doesn't exist, fallback to index.html for SPA routing
                    val indexFile = File(webDir, "index.html")
                    if (indexFile.exists()) {
                        serveFile(exchange, indexFile, "text/html; charset=utf-8")
                    } else {
                        sendError(exchange, 404, "Not Found")
                    }
                    return
                }

                val mimeType = getMimeType(file.name)
                serveFile(exchange, file, mimeType)
            } catch (t: Throwable) {
                plugin.logger.log(Level.SEVERE, "[WebConfig] StaticFileHandler exception: ${t.message}", t)
                sendError(exchange, 500, "Internal Server Error: ${t.message}")
            }
        }

        private fun serveFile(exchange: HttpExchange, file: File, contentType: String) {
            val bytes = file.readBytes()
            val headers = exchange.responseHeaders
            headers.set("Access-Control-Allow-Origin", "*")
            headers.set("Content-Type", contentType)
            // Always prevent browser caching for web config UI assets to avoid stale versions
            headers.set("Cache-Control", "no-cache, no-store, must-revalidate")
            headers.set("Pragma", "no-cache")
            headers.set("Expires", "0")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }

        private fun getMimeType(name: String): String {
            val lower = name.lowercase()
            return when {
                lower.endsWith(".html") || lower.endsWith(".htm") -> "text/html; charset=utf-8"
                lower.endsWith(".css") -> "text/css; charset=utf-8"
                lower.endsWith(".js") -> "application/javascript; charset=utf-8"
                lower.endsWith(".json") -> "application/json; charset=utf-8"
                lower.endsWith(".svg") -> "image/svg+xml"
                lower.endsWith(".png") -> "image/png"
                lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
                lower.endsWith(".ico") -> "image/x-icon"
                lower.endsWith(".woff2") -> "font/woff2"
                lower.endsWith(".woff") -> "font/woff"
                lower.endsWith(".ttf") -> "font/ttf"
                else -> "application/octet-stream"
            }
        }
    }

    companion object {
        /**
         * Recursively converts a Bukkit [ConfigurationSection] into a clean nested [Map] safe for Gson.
         */
        fun yamlSectionToMap(section: ConfigurationSection): Map<String, Any?> {
            val map = mutableMapOf<String, Any?>()
            for (key in section.getKeys(false)) {
                val value = section.get(key)
                map[key] = cleanYamlValue(value)
            }
            return map
        }

        private fun cleanYamlValue(value: Any?): Any? {
            if (value == null) return null
            if (value is java.util.Optional<*>) {
                return if (value.isPresent) cleanYamlValue(value.get()) else null
            }
            if (value is java.util.OptionalInt) {
                return if (value.isPresent) value.asInt else null
            }
            if (value is java.util.OptionalLong) {
                return if (value.isPresent) value.asLong else null
            }
            if (value is java.util.OptionalDouble) {
                return if (value.isPresent) value.asDouble else null
            }
            return when (value) {
                is ConfigurationSection -> yamlSectionToMap(value)
                is List<*> -> value.map { cleanYamlValue(it) }
                is Map<*, *> -> value.entries.associate { it.key.toString() to cleanYamlValue(it.value) }
                is Number, is Boolean, is String, is Char -> value
                else -> value.toString()
            }
        }
    }
}
