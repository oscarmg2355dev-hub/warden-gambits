package com.wardengambits.app.i18n.tengwar

import com.wardengambits.app.i18n.tengwar.TengwarMode.ENGLISH
import com.wardengambits.app.i18n.tengwar.TengwarMode.SPANISH
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TengwarTest {

    // Tengwar (CSUR codepoints, Alcarin Tengwar).
    private val TINCO = ""
    private val PARMA = ""
    private val CALMA = ""
    private val QUESSE = ""
    private val UMBAR = ""
    private val UNGWE = ""
    private val THULE = ""
    private val FORMEN = ""
    private val HARMA = ""
    private val HWESTA = ""
    private val AMPA = ""
    private val UNQUE = ""
    private val NUMEN = ""
    private val MALTA = ""
    private val NOLDO = ""
    private val NWALME = ""
    private val VALA = ""
    private val ANNA = ""
    private val ROMEN = ""
    private val LAMBE = ""
    private val ALDA = ""
    private val SILME = ""
    private val HYARMEN = ""
    private val HWESTA_SINDARINWA = ""
    private val LONG_CARRIER = ""
    private val SHORT_CARRIER = ""
    private val CHRISTOPHER_QU = ""

    // Tehtar and marks.
    private val A = ""
    private val E = ""
    private val I = ""
    private val O = ""
    private val U = ""
    private val BAR = ""
    private val HOOK = ""

    // Punctuation.
    private val PUSTA = ""
    private val DOUBLE_PUSTA = ""

    private fun es(text: String) = Tengwar.transcribe(text, SPANISH)
    private fun en(text: String) = Tengwar.transcribe(text, ENGLISH)

    @Test
    fun vowelGoesOnThePrecedingConsonant() {
        assertEquals(PARMA + A, es("pa"))
        assertEquals(QUESSE + A + SILME + A, es("casa"))
        assertEquals(TINCO + I + MALTA + O + NUMEN + E + UMBAR + U, es("timonebu"))
    }

    @Test
    fun initialVowelGoesOnTheShortCarrier() {
        assertEquals(SHORT_CARRIER + O + ROMEN + O, es("oro"))
        assertEquals(SHORT_CARRIER + E + NUMEN, en("en"))
    }

    @Test
    fun vowelAfterAnotherVowelGoesOnTheShortCarrier() {
        assertEquals(SHORT_CARRIER + A + SHORT_CARRIER + I + ROMEN + E, es("aire"))
        assertEquals(SHORT_CARRIER + A + UNGWE + U + SHORT_CARRIER + A, es("agua"))
    }

    @Test
    fun accentedVowelGoesOnTheLongCarrier() {
        assertEquals(QUESSE + A + FORMEN + LONG_CARRIER + E, es("café"))
        assertEquals(SHORT_CARRIER + A + QUESSE + BAR + I + LONG_CARRIER + O + NUMEN, es("acción"))
        // English has no accented letters of its own: they fall back to the base vowel on the long carrier.
        assertEquals(QUESSE + A + FORMEN + LONG_CARRIER + E, en("café"))
    }

    @Test
    fun doubledConsonantsUseTheBarBelow() {
        assertEquals(PARMA + E + ROMEN + BAR + O, es("perro"))
        assertEquals(UMBAR + A + LAMBE + BAR, en("ball"))
    }

    @Test
    fun finalSBecomesAHookOnThePreviousTengwa() {
        assertEquals(QUESSE + A + SILME + A + HOOK, es("casas"))
        assertEquals(QUESSE + A + TINCO + HOOK, en("cats"))
        assertEquals(SHORT_CARRIER + E + HOOK, es("es"))
        assertEquals(MALTA + LONG_CARRIER + A + HOOK, es("más"))
        assertEquals(SILME, es("s"))
    }

    @Test
    fun possessiveApostropheIsPartOfTheWord() {
        assertEquals(en("durins"), en("Durin's"))
        assertTrue(en("Durin's").endsWith(HOOK))
        assertEquals(en("wardens"), en("Warden's"))
    }

    @Test
    fun otherApostrophesInsideWordsAreDropped() {
        assertEquals(en("dont"), en("don't"))
        assertTrue("'" !in en("don't"))
    }

    @Test
    fun spanishDigraphs() {
        assertEquals(CALMA + I + QUESSE + O, es("chico"))
        assertEquals(ALDA + A + MALTA + A, es("llama"))
        assertEquals(NUMEN + I + NWALME + O, es("niño"))
        assertEquals(QUESSE + E + SILME + O, es("queso"))
        assertEquals(UNGWE + E + ROMEN + BAR + A, es("guerra"))
        assertEquals(HYARMEN + O + HWESTA + A, es("hoja"))
    }

    @Test
    fun englishDigraphs() {
        assertEquals(THULE + E, en("the"))
        assertEquals(HARMA + I + PARMA, en("ship"))
        assertEquals(SILME + I + NOLDO, en("sing"))
        assertEquals(HWESTA_SINDARINWA + E + NUMEN, en("when"))
        assertEquals(CALMA + I + NUMEN, en("chin"))
        assertEquals(FORMEN + O + NUMEN + E, en("phone"))
        assertEquals(NUMEN + I + UNQUE + TINCO, en("night"))
        assertEquals(UMBAR + A + QUESSE, en("back"))
        assertEquals(CHRISTOPHER_QU + E + SHORT_CARRIER + E + NUMEN, en("queen"))
        assertEquals(UMBAR + O + QUESSE + SILME, en("box"))
        assertEquals(ANNA + E + HOOK, en("yes"))
        assertEquals(VALA + O + VALA, en("wow"))
    }

    @Test
    fun commaAndPeriodBecomeTengwarPunctuation() {
        assertEquals(SHORT_CARRIER + A + PUSTA + " " + SHORT_CARRIER + E + DOUBLE_PUSTA, es("a, e."))
        assertEquals("¡" + HYARMEN + O + LAMBE + A + "!", es("¡hola!"))
    }

    @Test
    fun caseIsIgnored() {
        assertEquals(es("casa"), es("CASA"))
        assertEquals(en("the ship"), en("The Ship"))
    }

    @Test
    fun digitsAndProtectedTokensStayLatin() {
        for (token in listOf("SP2", "CU1", "GD", "PO", "DZ", "EM", "RV1", "AT3", "1", "2", "3", "1840", "1.5", "1,840")) {
            assertEquals(token, es(token))
            assertEquals(token, en(token))
        }
    }

    @Test
    fun mixedPhrases() {
        assertEquals("−1840 SP2", es("−1840 SP2"))
        assertEquals(NUMEN + I + AMPA + E + LAMBE + " 34", es("Nivel 34"))
        assertEquals("+3 CU1 " + SHORT_CARRIER + E + HOOK, es("+3 CU1 es"))
    }

    @Test
    fun cacheIsKeyedByModeAndSafeAcrossThreads() {
        // "c" maps to quessë in both modes, but "ll" is alda in Spanish and lambë + bar in English.
        assertEquals(ALDA + A, es("lla"))
        assertEquals(LAMBE + BAR + A, en("lla"))
        val words = (0 until 3000).map { "casa$it perro" }
        val expected = words.map { es(it) }
        val failures = java.util.concurrent.atomic.AtomicInteger()
        val threads = (0 until 8).map {
            Thread { words.forEachIndexed { i, w -> if (es(w) != expected[i]) failures.incrementAndGet() } }
        }
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        assertEquals(0, failures.get())
    }

    @Test
    fun equivalencesListConsonantsDigraphsAndVowels() {
        val spanish = Tengwar.equivalences(SPANISH)
        assertTrue(Equivalence("ch", CALMA, EquivalenceKind.DIGRAPH) in spanish)
        assertTrue(Equivalence("p", PARMA, EquivalenceKind.CONSONANT) in spanish)
        assertTrue(Equivalence("a", SHORT_CARRIER + A, EquivalenceKind.VOWEL) in spanish)
        assertTrue(Equivalence("á", LONG_CARRIER + A, EquivalenceKind.VOWEL) in spanish)
        val english = Tengwar.equivalences(ENGLISH)
        assertTrue(Equivalence("th", THULE, EquivalenceKind.DIGRAPH) in english)
        assertTrue(Equivalence("u", SHORT_CARRIER + U, EquivalenceKind.VOWEL) in english)
    }
}
