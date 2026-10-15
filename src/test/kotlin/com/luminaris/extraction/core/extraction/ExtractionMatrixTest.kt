package com.luminaris.extraction.core.extraction

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.bukkit.util.BoundingBox
import java.io.File
import java.lang.reflect.Proxy
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExtractionMatrixTest {

    private fun createMockPlugin(): Plugin {
        val tempFolder = File(System.getProperty("java.io.tmpdir"), "luminaris_matrix_test")
        tempFolder.mkdirs()
        return Proxy.newProxyInstance(
            Plugin::class.java.classLoader,
            arrayOf(Plugin::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "getDataFolder" -> tempFolder
                "getName" -> "LuminaExtractionCore"
                else -> null
            }
        } as Plugin
    }

    private fun createMockPlayer(inventoryItems: List<ItemStack?> = emptyList()): Player {
        val uuid = UUID.randomUUID()
        val itemsArray = arrayOfNulls<ItemStack>(36)
        inventoryItems.forEachIndexed { index, item ->
            if (index < itemsArray.size) itemsArray[index] = item
        }

        val mockInventory = Proxy.newProxyInstance(
            PlayerInventory::class.java.classLoader,
            arrayOf(PlayerInventory::class.java)
        ) { _, method, args ->
            when (method.name) {
                "getContents" -> itemsArray.clone()
                "getSize" -> itemsArray.size
                "getItem" -> {
                    val idx = args?.get(0) as Int
                    itemsArray.getOrNull(idx)
                }
                "setItem" -> {
                    val idx = args?.get(0) as Int
                    val item = args?.get(1) as? ItemStack
                    if (idx in itemsArray.indices) {
                        itemsArray[idx] = item
                    }
                    null
                }
                else -> null
            }
        } as PlayerInventory

        return Proxy.newProxyInstance(
            Player::class.java.classLoader,
            arrayOf(Player::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "getUniqueId" -> uuid
                "getName" -> "Operative_${uuid.toString().take(4)}"
                "isOnline" -> true
                "isValid" -> true
                "getInventory" -> mockInventory
                else -> null
            }
        } as Player

    }

    @Test
    fun testAlwaysOpenExtractionZone() {
        val manager = ExtractionManager(createMockPlugin())
        val player = createMockPlayer()

        val zone = ExtractionZone(
            id = "ext_always",
            displayName = Component.text("Highway Evac"),
            boundingBox = BoundingBox(0.0, 60.0, 0.0, 10.0, 70.0, 10.0),
            type = ExtractionType.ALWAYS_OPEN
        )

        val (eligible, reason) = manager.isPlayerEligible(player, zone, matchElapsedSeconds = 120, teamId = "squad_1")
        assertTrue(eligible)
        assertNull(reason)
    }

    @Test
    fun testTimedWindowExtractionZone() {
        val manager = ExtractionManager(createMockPlugin())
        val player = createMockPlayer()

        val zone = ExtractionZone(
            id = "ext_timed",
            displayName = Component.text("Train Yard Evac"),
            boundingBox = BoundingBox(0.0, 60.0, 0.0, 10.0, 70.0, 10.0),
            type = ExtractionType.TIMED_WINDOW,
            timeStartSeconds = 300L, // Opens at 5m
            timeEndSeconds = 600L    // Closes at 10m
        )

        // 1. Before window opens (elapsed 200s < 300s)
        val (earlyEligible, earlyReason) = manager.isPlayerEligible(player, zone, matchElapsedSeconds = 200L, teamId = "team_alpha")
        assertFalse(earlyEligible)
        assertNotNull(earlyReason)
        val earlyText = MiniMessage.miniMessage().serialize(earlyReason)
        assertTrue(earlyText.contains("inactive") && earlyText.contains("100s"))

        // 2. Inside window (elapsed 450s)
        val (activeEligible, activeReason) = manager.isPlayerEligible(player, zone, matchElapsedSeconds = 450L, teamId = "team_alpha")
        assertTrue(activeEligible)
        assertNull(activeReason)

        // 3. After window closed (elapsed 700s > 600s)
        val (lateEligible, lateReason) = manager.isPlayerEligible(player, zone, matchElapsedSeconds = 700L, teamId = "team_alpha")
        assertFalse(lateEligible)
        assertNotNull(lateReason)
        val lateText = MiniMessage.miniMessage().serialize(lateReason)
        assertTrue(lateText.contains("closed"))
    }

    @Test
    fun testTeamSpecificExtractionZone() {
        val manager = ExtractionManager(createMockPlugin())
        val player = createMockPlayer()

        val zone = ExtractionZone(
            id = "ext_team",
            displayName = Component.text("Squad Bravo Evac"),
            boundingBox = BoundingBox(0.0, 60.0, 0.0, 10.0, 70.0, 10.0),
            type = ExtractionType.TEAM_SPECIFIC,
            allowedTeamIds = mutableSetOf("squad_bravo", "squad_delta")
        )

        // 1. Unauthorized squad
        val (unauthEligible, unauthReason) = manager.isPlayerEligible(player, zone, matchElapsedSeconds = 50L, teamId = "squad_alpha")
        assertFalse(unauthEligible)
        assertNotNull(unauthReason)
        val unauthText = MiniMessage.miniMessage().serialize(unauthReason)
        assertTrue(unauthText.contains("restricted"))

        // 2. Authorized squad
        val (authEligible, authReason) = manager.isPlayerEligible(player, zone, matchElapsedSeconds = 50L, teamId = "squad_bravo")
        assertTrue(authEligible)
        assertNull(authReason)
    }

    open class DummyItemStack : ItemStack() {
        var dummyType: Material = Material.TRIPWIRE_HOOK
        var dummyAmount: Int = 1
        override fun getType(): Material = dummyType
        override fun getAmount(): Int = dummyAmount
        override fun setAmount(amount: Int) { dummyAmount = amount }
        override fun hasItemMeta(): Boolean = false
        override fun isEmpty(): Boolean = false
    }

    private fun createTestItemStack(material: Material = Material.TRIPWIRE_HOOK): ItemStack {
        val field = sun.misc.Unsafe::class.java.getDeclaredField("theUnsafe").apply { isAccessible = true }
        val unsafe = field.get(null) as sun.misc.Unsafe
        val stack = unsafe.allocateInstance(DummyItemStack::class.java) as DummyItemStack
        stack.dummyType = material
        stack.dummyAmount = 1
        return stack
    }

    @Test
    fun testConditionalItemExtractionZone() {
        val manager = ExtractionManager(createMockPlugin())

        val zone = ExtractionZone(
            id = "ext_bunker",
            displayName = Component.text("Bunker Blast Doors"),
            boundingBox = BoundingBox(0.0, 60.0, 0.0, 10.0, 70.0, 10.0),
            type = ExtractionType.CONDITIONAL_ITEM,
            requiredItemId = "TRIPWIRE_HOOK"
        )

        // 1. Player without required item
        val emptyPlayer = createMockPlayer(emptyList())
        val (noItemEligible, noItemReason) = manager.isPlayerEligible(emptyPlayer, zone, 50L, "team_1")
        assertFalse(noItemEligible)
        assertNotNull(noItemReason)
        val noItemText = MiniMessage.miniMessage().serialize(noItemReason)
        assertTrue(noItemText.contains("tripwire", ignoreCase = true) || noItemText.contains("hook", ignoreCase = true))

        // 2. Player with item identified
        val keyItem = createTestItemStack(Material.TRIPWIRE_HOOK)
        val equippedPlayer = createMockPlayer(listOf(keyItem))

        val (itemEligible, itemReason) = manager.isPlayerEligible(equippedPlayer, zone, 50L, "team_1")
        assertTrue(itemEligible)
        assertNull(itemReason)

        // 3. Test item consumption
        assertTrue(manager.hasRequiredItem(equippedPlayer, "TRIPWIRE_HOOK"))
        assertTrue(manager.consumeRequiredItem(equippedPlayer, "TRIPWIRE_HOOK"))
        assertFalse(manager.hasRequiredItem(equippedPlayer, "TRIPWIRE_HOOK"))
    }
}
