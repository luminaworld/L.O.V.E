package com.luminaris.extraction.util

import com.luminaris.extraction.LuminarisCorePlugin
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler
import io.papermc.paper.threadedregions.scheduler.EntityScheduler
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler
import io.papermc.paper.threadedregions.scheduler.RegionScheduler
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * Thread-safe scheduling abstraction layer for Folia and modern Paper regionalized architectures.
 * Strictly uses non-deprecated Folia regional schedulers (Global, Region, Entity, Async).
 */
class FoliaScheduler(private val plugin: Plugin = LuminarisCorePlugin.instance) {

    companion object {
        /**
         * Identifies whether the current server implementation is Folia (threaded-regions).
         */
        val isFolia: Boolean by lazy {
            runCatching {
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
            }.isSuccess
        }

        private var defaultScheduler: FoliaScheduler? = null

        /**
         * Accessor for default global singleton scheduler.
         */
        val default: FoliaScheduler
            get() = defaultScheduler ?: synchronized(this) {
                defaultScheduler ?: FoliaScheduler().also { defaultScheduler = it }
            }
    }

    private val globalRegionScheduler: GlobalRegionScheduler
        get() = Bukkit.getGlobalRegionScheduler()

    private val regionScheduler: RegionScheduler
        get() = Bukkit.getRegionScheduler()

    private val asyncScheduler: AsyncScheduler
        get() = Bukkit.getAsyncScheduler()

    /**
     * Executes a task once synchronously on the global region.
     */
    fun runGlobal(runnable: () -> Unit): ScheduledTask {
        return globalRegionScheduler.run(plugin, Consumer { runnable() })
    }

    /**
     * Executes a task once synchronously on the global region after a tick delay.
     * @param delayTicks Delay before execution in ticks (minimum 1L).
     */
    fun runGlobalDelayed(delayTicks: Long, runnable: () -> Unit): ScheduledTask {
        val safeDelay = maxOf(1L, delayTicks)
        return globalRegionScheduler.runDelayed(plugin, Consumer { runnable() }, safeDelay)
    }

    /**
     * Executes a repeating task synchronously on the global region.
     * @param delayTicks Initial delay in ticks (minimum 1L).
     * @param periodTicks Repetition period in ticks (minimum 1L).
     */
    fun runGlobalRepeating(delayTicks: Long, periodTicks: Long, task: () -> Unit): ScheduledTask {
        val safeDelay = maxOf(1L, delayTicks)
        val safePeriod = maxOf(1L, periodTicks)
        return globalRegionScheduler.runAtFixedRate(plugin, Consumer { task() }, safeDelay, safePeriod)
    }

    /**
     * Executes a task on the thread owning the specified [Location].
     */
    fun runAtLocation(location: Location, runnable: () -> Unit): ScheduledTask {
        return regionScheduler.run(plugin, location, Consumer { runnable() })
    }

    /**
     * Executes a task on the thread owning the specified chunk coordinates.
     */
    fun runAtChunk(world: World, chunkX: Int, chunkZ: Int, runnable: () -> Unit): ScheduledTask {
        return regionScheduler.run(plugin, world, chunkX, chunkZ, Consumer { runnable() })
    }

    /**
     * Executes a task delayed at the specified [Location].
     */
    fun runAtLocationDelayed(location: Location, delayTicks: Long, runnable: () -> Unit): ScheduledTask {
        val safeDelay = maxOf(1L, delayTicks)
        return regionScheduler.runDelayed(plugin, location, Consumer { runnable() }, safeDelay)
    }

    /**
     * Executes a repeating task on the thread owning the specified [Location].
     */
    fun runAtLocationRepeating(
        location: Location,
        delayTicks: Long,
        periodTicks: Long,
        task: () -> Unit
    ): ScheduledTask {
        val safeDelay = maxOf(1L, delayTicks)
        val safePeriod = maxOf(1L, periodTicks)
        return regionScheduler.runAtFixedRate(plugin, location, Consumer { task() }, safeDelay, safePeriod)
    }

    /**
     * Executes a task on the thread owning the given [Entity].
     * If the entity is invalid or retired, returns `null` or calls [retired].
     */
    fun runAtEntity(
        entity: Entity,
        runnable: () -> Unit
    ): ScheduledTask? = runAtEntity(entity, runnable, null)

    fun runAtEntity(
        entity: Entity,
        runnable: () -> Unit,
        retired: (() -> Unit)?
    ): ScheduledTask? {
        val entityScheduler: EntityScheduler = entity.scheduler
        val retiredCallback = if (retired != null) Runnable { retired() } else null
        return entityScheduler.run(plugin, Consumer { runnable() }, retiredCallback)
    }

    /**
     * Executes a task delayed on the thread owning the given [Entity].
     */
    fun runAtEntityDelayed(
        entity: Entity,
        delayTicks: Long,
        runnable: () -> Unit,
        retired: (() -> Unit)? = null
    ): ScheduledTask? {
        val entityScheduler: EntityScheduler = entity.scheduler
        val safeDelay = maxOf(1L, delayTicks)
        val retiredCallback = if (retired != null) Runnable { retired() } else null
        return entityScheduler.runDelayed(plugin, Consumer { runnable() }, retiredCallback, safeDelay)
    }

    /**
     * Executes a repeating task on the thread owning the given [Entity].
     */
    fun runAtEntityRepeating(
        entity: Entity,
        delayTicks: Long,
        periodTicks: Long,
        task: () -> Unit,
        retired: (() -> Unit)? = null
    ): ScheduledTask? {
        val entityScheduler: EntityScheduler = entity.scheduler
        val safeDelay = maxOf(1L, delayTicks)
        val safePeriod = maxOf(1L, periodTicks)
        val retiredCallback = if (retired != null) Runnable { retired() } else null
        return entityScheduler.runAtFixedRate(plugin, Consumer { task() }, retiredCallback, safeDelay, safePeriod)
    }

    /**
     * Executes a task asynchronously on the shared Folia/Paper async thread pool.
     */
    fun runAsync(runnable: () -> Unit): ScheduledTask {
        return asyncScheduler.runNow(plugin, Consumer { runnable() })
    }

    /**
     * Executes a delayed task asynchronously.
     */
    fun runAsyncDelayed(
        delay: Long,
        unit: TimeUnit = TimeUnit.MILLISECONDS,
        runnable: () -> Unit
    ): ScheduledTask {
        return asyncScheduler.runDelayed(plugin, Consumer { runnable() }, delay, unit)
    }

    /**
     * Executes a repeating task asynchronously.
     */
    fun runAsyncRepeating(
        delay: Long,
        period: Long,
        unit: TimeUnit = TimeUnit.MILLISECONDS,
        task: () -> Unit
    ): ScheduledTask {
        return asyncScheduler.runAtFixedRate(plugin, Consumer { task() }, delay, period, unit)
    }

    /**
     * Cancels all scheduled tasks belonging to this plugin across all schedulers.
     */
    fun cancelAll() {
        runCatching { globalRegionScheduler.cancelTasks(plugin) }
        runCatching { asyncScheduler.cancelTasks(plugin) }
    }
}
