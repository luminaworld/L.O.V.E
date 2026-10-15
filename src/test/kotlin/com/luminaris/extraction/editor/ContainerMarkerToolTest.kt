package com.luminaris.extraction.editor

import com.luminaris.extraction.editor.tool.ContainerMarkerTool
import org.bukkit.NamespacedKey
import kotlin.test.Test
import kotlin.test.assertEquals

class ContainerMarkerToolTest {

    @Test
    fun testNamespacedKey() {
        assertEquals("luminaris:container_category", ContainerMarkerTool.KEY_CONTAINER_CATEGORY.toString())
        assertEquals("luminaris", ContainerMarkerTool.KEY_CONTAINER_CATEGORY.namespace)
        assertEquals("container_category", ContainerMarkerTool.KEY_CONTAINER_CATEGORY.key)
    }
}
