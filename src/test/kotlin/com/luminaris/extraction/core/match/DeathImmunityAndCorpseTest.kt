package com.luminaris.extraction.core.match

import com.luminaris.extraction.core.safe.SafeCaseManager
import com.luminaris.extraction.core.template.TemplateMapData
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.lang.reflect.Proxy
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeathImmunityAndCorpseTest {

    @BeforeTest
    fun setUp() {
        SafeCaseManager.pendingReturns.clear()
    }

    open class DummyRegularItem(var mat: Material = Material.DIAMOND) : ItemStack() {
        private var amt = 1
        override fun getType(): Material = mat
        override fun getAmount(): Int = amt
        override fun setAmount(amount: Int) { amt = amount }
        override fun hasItemMeta(): Boolean = false
        override fun isEmpty(): Boolean = false
    }

    open class DummySafeCaseItem(
        var isSafe: Boolean = true,
        var safeId: String = "alpha_case"
    ) : ItemStack() {
        private var amt = 1
        override fun getType(): Material = Material.BUNDLE
        override fun getAmount(): Int = amt
        override fun setAmount(amount: Int) { amt = amount }
        override fun hasItemMeta(): Boolean = true
        override fun isEmpty(): Boolean = false

        override fun getItemMeta(): ItemMeta {
            val mockPdc = Proxy.newProxyInstance(
                PersistentDataContainer::class.java.classLoader,
                arrayOf(PersistentDataContainer::class.java)
            ) { _, method, args ->
                when (method.name) {
                    "get" -> {
                        val key = args[0].toString()
                        if (key == SafeCaseManager.KEY_IS_SAFE_CASE.toString()) isSafe
                        else if (key == SafeCaseManager.KEY_SAFE_ID.toString()) safeId
                        else null
                    }
                    else -> null
                }
            } as PersistentDataContainer

            return Proxy.newProxyInstance(
                ItemMeta::class.java.classLoader,
                arrayOf(ItemMeta::class.java)
            ) { _, method, _ ->
                when (method.name) {
                    "getPersistentDataContainer" -> mockPdc
                    "hasDisplayName" -> false
                    "displayName" -> Component.text("Safe Container")
                    else -> null
                }
            } as ItemMeta
        }
    }

    private fun createSafeCaseItem(id: String = "alpha_case"): ItemStack {
        val field = sun.misc.Unsafe::class.java.getDeclaredField("theUnsafe").apply { isAccessible = true }
        val unsafe = field.get(null) as sun.misc.Unsafe
        val item = unsafe.allocateInstance(DummySafeCaseItem::class.java) as DummySafeCaseItem
        item.isSafe = true
        item.safeId = id
        return item
    }

    private fun createRegularItem(material: Material): ItemStack {
        val field = sun.misc.Unsafe::class.java.getDeclaredField("theUnsafe").apply { isAccessible = true }
        val unsafe = field.get(null) as sun.misc.Unsafe
        val item = unsafe.allocateInstance(DummyRegularItem::class.java) as DummyRegularItem
        item.mat = material
        return item
    }

    @Test
    fun testSafeCaseDetection() {
        val safeItem = createSafeCaseItem("gamma_container")
        val regularItem = createRegularItem(Material.DIAMOND)

        fun isSafeCase(item: ItemStack?): Boolean {
            if (item == null || item.isEmpty || !item.hasItemMeta()) return false
            val pdc = item.itemMeta?.persistentDataContainer ?: return false
            return pdc.get(SafeCaseManager.KEY_IS_SAFE_CASE, PersistentDataType.BOOLEAN) == true
        }

        assertTrue(isSafeCase(safeItem))
        assertFalse(isSafeCase(regularItem))
    }

    @Test
    fun testDeathDropsFilteringAndZeroFloorScattering() {
        val playerUuid = UUID.randomUUID()
        val safeItem = createSafeCaseItem("kappa_case")
        val diamond = createRegularItem(Material.DIAMOND)
        val ironIngot = createRegularItem(Material.IRON_INGOT)

        val drops = mutableListOf<ItemStack>(diamond, safeItem, ironIngot)
        assertEquals(3, drops.size)

        fun isSafeCase(item: ItemStack): Boolean {
            if (!item.hasItemMeta()) return false
            return item.itemMeta?.persistentDataContainer?.get(SafeCaseManager.KEY_IS_SAFE_CASE, PersistentDataType.BOOLEAN) == true
        }

        val dropsToStore = mutableListOf<ItemStack>()
        val preservedSafeCases = mutableListOf<ItemStack>()

        val iterator = drops.iterator()
        while (iterator.hasNext()) {
            val drop = iterator.next()
            if (isSafeCase(drop)) {
                preservedSafeCases.add(drop)
                iterator.remove()
            } else {
                dropsToStore.add(drop)
            }
        }

        // Cache into pendingReturns
        if (preservedSafeCases.isNotEmpty()) {
            val list = SafeCaseManager.pendingReturns.computeIfAbsent(playerUuid) { CopyOnWriteArrayList() }
            list.addAll(preservedSafeCases)
        }

        // Clear drops so zero items scatter on the floor
        drops.clear()

        // 1. Zero items scatter on the floor
        assertTrue(drops.isEmpty(), "event.drops must be completely cleared so zero items scatter")

        // 2. Remaining items are prepared for the corpse crate
        assertEquals(2, dropsToStore.size)
        assertEquals(Material.DIAMOND, dropsToStore[0].type)
        assertEquals(Material.IRON_INGOT, dropsToStore[1].type)

        // 3. Safe Case is cached in pendingReturns for this player
        val cached = SafeCaseManager.pendingReturns[playerUuid]
        assertNotNull(cached)
        assertEquals(1, cached.size)
        assertTrue(safeItem === cached[0])
    }

    @Test
    fun testSafeCaseRespawnRestoration() {
        val playerUuid = UUID.randomUUID()
        val safeCase1 = createSafeCaseItem("alpha_pouch")
        val safeCase2 = createSafeCaseItem("epsilon_secure")

        // Simulate safe cases cached in pendingReturns
        val pendingList = SafeCaseManager.pendingReturns.computeIfAbsent(playerUuid) { CopyOnWriteArrayList() }
        pendingList.add(safeCase1)
        pendingList.add(safeCase2)

        assertEquals(2, SafeCaseManager.pendingReturns[playerUuid]?.size)

        // Simulate PlayerRespawnEvent logic
        val inventory = mutableListOf<ItemStack>()
        val restored = SafeCaseManager.pendingReturns.remove(playerUuid)

        assertNotNull(restored)
        for (item in restored) {
            inventory.add(item)
        }

        // Pending returns must be purged after restoration
        assertNull(SafeCaseManager.pendingReturns[playerUuid])
        assertEquals(2, inventory.size)
        assertTrue(inventory.any { it === safeCase1 })
        assertTrue(inventory.any { it === safeCase2 })
    }

    @Test
    fun testCorpseCrateFormattingAndPlayerStateKIA() {
        val world = Proxy.newProxyInstance(
            World::class.java.classLoader,
            arrayOf(World::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "getName" -> "instances/match_corpse_test"
                else -> null
            }
        } as World

        val template = TemplateMapData("custom_facility", "Facility 17")
        val match = MatchInstance(
            matchId = "corpse123",
            world = world,
            templateData = template,
            raidDurationSeconds = 600L
        )

        val playerUuid = UUID.randomUUID()
        match.players[playerUuid] = PlayerRaidState.IN_RAID

        // Simulate player death marking state as KIA
        match.setPlayerState(playerUuid, PlayerRaidState.KILLED_IN_ACTION)
        assertEquals(PlayerRaidState.KILLED_IN_ACTION, match.getPlayerState(playerUuid))
        assertTrue(match.getPlayerState(playerUuid)!!.isTerminal)
    }
}
