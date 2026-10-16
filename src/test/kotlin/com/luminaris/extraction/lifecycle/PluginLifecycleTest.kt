package com.luminaris.extraction.lifecycle

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.localization.TranslationManager
import com.luminaris.extraction.util.FoliaScheduler
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PluginLifecycleTest {

    @Test
    fun testTranslationManagerResetLifecycle() {
        // Calling TranslationManager.instance bootstraps standalone fallback
        val instanceBefore = TranslationManager.instance
        assertNotNull(instanceBefore)

        // Reset clears the singleton
        TranslationManager.reset()

        // Accessing instance again creates a fresh instance without retaining stale references
        val instanceAfter = TranslationManager.instance
        assertNotNull(instanceAfter)
    }

    @Test
    fun testFoliaSchedulerReset() {
        FoliaScheduler.reset()
    }

    @Test
    fun testLuminarisCorePluginInstanceWhenUnloaded() {
        // When not initialized, instanceOrNull returns null gracefully
        val instance = LuminarisCorePlugin.instanceOrNull
        assertNull(instance)

        // And accessing instance throws IllegalStateException
        assertFailsWith<IllegalStateException> {
            LuminarisCorePlugin.instance
        }
    }
}
