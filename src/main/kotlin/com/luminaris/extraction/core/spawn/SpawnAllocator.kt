package com.luminaris.extraction.core.spawn

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import kotlin.math.abs
import kotlin.random.Random

/**
 * Dynamic Spawn Allocator engine with Anti-Clumping Optimization.
 *
 * Responsibilities:
 * - Reads and deserializes spawn points from `map_data.yml`.
 * - Implements Anti-Clumping Algorithm: Maximizes Euclidean distances between opposing
 *   squads/teams using max-min dispersion optimization.
 */
class SpawnAllocator {

    /**
     * Loads configured spawn points from a `map_data.yml` file.
     *
     * @param file Target YAML file.
     * @return List of parsed [SpawnPoint] objects.
     */
    fun loadSpawnsFromMapData(file: File): List<SpawnPoint> {
        if (!file.exists()) return emptyList()
        val yaml = YamlConfiguration.loadConfiguration(file)
        return loadSpawnsFromYaml(yaml)
    }

    /**
     * Loads configured spawn points from a YAML configuration section or root.
     *
     * Supports both list notation:
     * ```yaml
     * spawns:
     *   - id: "sp_1"
     *     name: "North Infiltration"
     *     x: 100.0
     *     y: 64.0
     *     z: -200.0
     *     yaw: 90.0
     *     pitch: 0.0
     * ```
     * and dictionary map notation:
     * ```yaml
     * spawns:
     *   sp_1:
     *     name: "North Infiltration"
     *     x: 100.0
     *     y: 64.0
     *     z: -200.0
     *     yaw: 90.0
     *     pitch: 0.0
     * ```
     */
    fun loadSpawnsFromYaml(yaml: ConfigurationSection): List<SpawnPoint> {
        val spawns = mutableListOf<SpawnPoint>()
        val rawSpawns = yaml.get("spawns")

        if (rawSpawns is List<*>) {
            for (item in rawSpawns) {
                if (item is Map<*, *>) {
                    val id = item["id"]?.toString() ?: continue
                    val name = item["name"]?.toString() ?: id
                    val x = (item["x"] as? Number)?.toDouble() ?: 0.0
                    val y = (item["y"] as? Number)?.toDouble() ?: 64.0
                    val z = (item["z"] as? Number)?.toDouble() ?: 0.0
                    val yaw = (item["yaw"] as? Number)?.toFloat() ?: 0.0f
                    val pitch = (item["pitch"] as? Number)?.toFloat() ?: 0.0f
                    spawns.add(SpawnPoint(id = id, x = x, y = y, z = z, yaw = yaw, pitch = pitch, name = name))
                }
            }
        } else if (yaml.isConfigurationSection("spawns")) {
            val sec = yaml.getConfigurationSection("spawns")!!
            for (key in sec.getKeys(false)) {
                val cSec = sec.getConfigurationSection(key)
                val id = cSec?.getString("id", key) ?: key
                val name = cSec?.getString("name", id) ?: id
                val x = cSec?.getDouble("x") ?: 0.0
                val y = cSec?.getDouble("y") ?: 64.0
                val z = cSec?.getDouble("z") ?: 0.0
                val yaw = (cSec?.getDouble("yaw") ?: 0.0).toFloat()
                val pitch = (cSec?.getDouble("pitch") ?: 0.0).toFloat()
                spawns.add(SpawnPoint(id = id, x = x, y = y, z = z, yaw = yaw, pitch = pitch, name = name))
            }
        }

        return spawns
    }

    /**
     * Anti-Clumping Algorithm:
     * Given a collection of squads/teams, calculates Euclidean distances between spawn candidates
     * and assigns squads to spawn locations maximizing the distance between opposing teams.
     *
     * @param squads Collection of squad or team identifiers.
     * @param candidates Available candidate spawn points.
     * @param random Optional Random generator for reproducible tests or shuffled seeds.
     * @return Map of squad identifier to assigned [SpawnPoint].
     */
    fun allocateSpawns(
        squads: Collection<String>,
        candidates: List<SpawnPoint>,
        random: Random = Random.Default
    ): Map<String, SpawnPoint> {
        if (squads.isEmpty() || candidates.isEmpty()) return emptyMap()

        val squadList = squads.distinct().toList()
        val numSquads = squadList.size

        // Edge Case: When squads > available spawn points, throw a graceful exception to prevent opposing squads clumping
        if (numSquads > candidates.size) {
            throw IllegalStateException(
                "Insufficient spawn points for Anti-Clumping: requested $numSquads squads but only ${candidates.size} spawn points exist."
            )
        }

        if (numSquads == 1) {
            val picked = candidates.random(random)
            return mapOf(squadList[0] to picked)
        }

        // Select the optimal subset of spawns maximizing min pairwise Euclidean distance
        val selectedSpawns = selectDispersedSpawns(candidates, numSquads, random)

        val result = mutableMapOf<String, SpawnPoint>()
        for (i in squadList.indices) {
            result[squadList[i]] = selectedSpawns[i]
        }

        return result
    }

    /**
     * Convenience method to load spawns from map_data.yml and assign them using Anti-Clumping.
     */
    fun allocateSpawnsFromMapData(
        squads: Collection<String>,
        mapDataFile: File,
        random: Random = Random.Default
    ): Map<String, SpawnPoint> {
        val spawns = loadSpawnsFromMapData(mapDataFile)
        return allocateSpawns(squads, spawns, random)
    }

    /**
     * Selects [count] spawn points from [candidates] maximizing Euclidean distance between all pairs.
     * Evaluates multi-seed farthest-point trajectories and picks the set with the highest minimum
     * pairwise distance and highest average pairwise distance.
     *
     * @param candidates List of available spawn points.
     * @param count Target number of spawn points to select.
     * @param random Random generator used for seed shuffling and tie-breaking.
     * @return Dispersed list of selected [SpawnPoint]s.
     */
    fun selectDispersedSpawns(
        candidates: List<SpawnPoint>,
        count: Int,
        random: Random = Random.Default
    ): List<SpawnPoint> {
        if (candidates.isEmpty() || count <= 0) return emptyList()
        if (count == 1) return listOf(candidates.random(random))
        if (candidates.size <= count) {
            return candidates.shuffled(random)
        }

        var bestSubset: List<SpawnPoint> = emptyList()
        var bestMinDist = -1.0
        var bestAvgDist = -1.0

        val shuffledSeeds = candidates.shuffled(random)

        for (seed in shuffledSeeds) {
            val selected = ArrayList<SpawnPoint>(count)
            selected.add(seed)

            val remaining = candidates.filter { it.id != seed.id }.toMutableList()

            while (selected.size < count && remaining.isNotEmpty()) {
                var bestCandidate: SpawnPoint? = null
                var maxMinDist = -1.0
                var maxAvgDist = -1.0

                for (cand in remaining) {
                    var candMinDist = Double.MAX_VALUE
                    var candSumDist = 0.0

                    for (sel in selected) {
                        val d = cand.distance(sel)
                        if (d < candMinDist) {
                            candMinDist = d
                        }
                        candSumDist += d
                    }
                    val candAvgDist = candSumDist / selected.size

                    if (candMinDist > maxMinDist + 1e-6) {
                        maxMinDist = candMinDist
                        maxAvgDist = candAvgDist
                        bestCandidate = cand
                    } else if (abs(candMinDist - maxMinDist) <= 1e-6 && candAvgDist > maxAvgDist) {
                        maxAvgDist = candAvgDist
                        bestCandidate = cand
                    }
                }

                if (bestCandidate != null) {
                    selected.add(bestCandidate)
                    remaining.removeIf { it.id == bestCandidate.id }
                } else {
                    break
                }
            }

            // Calculate dispersion metrics for this subset
            var subsetMinDist = Double.MAX_VALUE
            var subsetSumDist = 0.0
            var pairs = 0

            for (i in 0 until selected.size) {
                for (j in i + 1 until selected.size) {
                    val d = selected[i].distance(selected[j])
                    if (d < subsetMinDist) {
                        subsetMinDist = d
                    }
                    subsetSumDist += d
                    pairs++
                }
            }

            val subsetAvgDist = if (pairs > 0) subsetSumDist / pairs else 0.0

            if (subsetMinDist > bestMinDist + 1e-6) {
                bestMinDist = subsetMinDist
                bestAvgDist = subsetAvgDist
                bestSubset = selected
            } else if (abs(subsetMinDist - bestMinDist) <= 1e-6 && subsetAvgDist > bestAvgDist + 1e-6) {
                bestAvgDist = subsetAvgDist
                bestSubset = selected
            }
        }

        return bestSubset
    }
}
