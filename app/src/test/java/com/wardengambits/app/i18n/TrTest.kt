package com.wardengambits.app.i18n

import com.wardengambits.app.i18n.tengwar.Tengwar
import com.wardengambits.app.i18n.tengwar.TengwarMode
import com.wardengambits.app.ui.theme.AlcarinFamily
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrTest {
    private fun isTengwar(c: Char) = c in ''..''

    @Test fun latinScriptLeavesTheTextAlone() {
        val text = "Tick 5 · SP2"
        assertEquals(text, scriptString(text, AppScript.LATIN, AppLanguage.ES))
        val annotated = scriptAnnotated(text, AppScript.LATIN, AppLanguage.ES)
        assertEquals(text, annotated.text)
        assertTrue(annotated.spanStyles.isEmpty())
    }

    @Test fun tengwarScriptTranscribesWithTheModeOfTheLanguage() {
        assertEquals(Tengwar.transcribe("Moria", TengwarMode.SPANISH), scriptString("Moria", AppScript.TENGWAR, AppLanguage.ES))
        assertEquals(Tengwar.transcribe("the", TengwarMode.ENGLISH), scriptString("the", AppScript.TENGWAR, AppLanguage.EN))
    }

    @Test fun onlyTengwarCodepointsGetTheTengwarFontAndNumbersAndCodesStayLatin() {
        val annotated = scriptAnnotated("Turno 12 · daño −18 SP2 y CU1", AppScript.TENGWAR, AppLanguage.ES)
        val text = annotated.text
        assertTrue("numbers and codes are kept", listOf("12", "−18", "SP2", "CU1").all { it in text })
        val covered = BooleanArray(text.length)
        annotated.spanStyles.forEach { range ->
            assertEquals(AlcarinFamily, range.item.fontFamily)
            for (i in range.start until range.end) covered[i] = true
        }
        text.forEachIndexed { i, c ->
            assertEquals("char '$c' at $i", isTengwar(c), covered[i])
        }
        assertTrue(text.any(::isTengwar))
    }

    @Test fun textsWithOnlyNumbersOrCodesHaveNoTengwarSpans() {
        val annotated = scriptAnnotated("3/7 · 1500 · GD", AppScript.TENGWAR, AppLanguage.EN)
        assertEquals("3/7 · 1500 · GD", annotated.text)
        assertTrue(annotated.spanStyles.isEmpty())
    }
}
