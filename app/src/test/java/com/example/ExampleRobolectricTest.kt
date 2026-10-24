package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.NumberToWords
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AVA Invoices", appName)
  }

  @Test
  fun `verify uae currency number to words conversion`() {
    val words = NumberToWords.convertToUaeCurrency(17627.88)
    assertTrue(words.startsWith("UAE Dirham"))
    assertTrue(words.contains("seventeen thousand"))
    assertTrue(words.contains("six hundred"))
    assertTrue(words.contains("twenty-seven"))
    assertTrue(words.contains("eighty-eight Fils only"))
  }
}
