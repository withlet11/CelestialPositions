package io.github.withlet11.celestialpositions

import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertEquals
import org.junit.Test

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
// @RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("io.github.withlet11.celestialpositions", appContext.packageName)
    }
}