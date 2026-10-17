package com.wardengambits.app.i18n.tengwar

/** CSUR codepoints (Alcarin Tengwar) used by the transcriber. */
internal object Csur {
    const val SHORT_CARRIER = ""
    const val LONG_CARRIER = ""
    const val BAR_BELOW = ""
    const val S_HOOK = ""
    const val PUSTA = ""
    const val DOUBLE_PUSTA = ""
}

/**
 * One consonant grapheme of a mode: [latin] (lowercase, one or more letters) is written as [tengwar]
 * (one or more CSUR codepoints); [name] is the tengwa's display name. A digraph with [onlyBefore]
 * applies only when the next letter is one of those characters (Spanish «qu»/«gu» before e/i).
 */
data class TengwaEntry(val latin: String, val tengwar: String, val name: String, val onlyBefore: String? = null)

/** One vowel letter: its [tehta] goes over the previous consonant, or over a carrier; [long] vowels always take the long carrier. */
data class VowelEntry(val latin: Char, val tehta: String, val long: Boolean, val name: String)

/** The full table of a mode. */
data class TengwarTable(val consonants: List<TengwaEntry>, val vowels: List<VowelEntry>)

enum class EquivalenceKind { CONSONANT, DIGRAPH, VOWEL }

/** A row of the equivalence table shown to the player: vowels are shown over their carrier. */
data class Equivalence(val latin: String, val tengwar: String, val kind: EquivalenceKind)

/**
 * Orthographic (letter-by-letter) tables for both modes.
 *
 * Shared rules (see [Tengwar.transcribe]): a vowel tehta goes on the preceding consonant; with no
 * consonant before it (word start, after another vowel) it goes on the short carrier; a long
 * (accented) vowel goes on the long carrier. A doubled letter with no entry of its own is its tengwa
 * plus the bar below. A word-final «s» is a hook on the previous tengwa (silmë when the word is just «s»).
 *
 * Spanish:
 * | latin | tengwa | latin | tengwa |
 * |---|---|---|---|
 * | p | parma | t | tinco |
 * | c, k, q | quessë | qu (before e, i) | quessë |
 * | ch | calma | b | umbar |
 * | d | ando | g | ungwë |
 * | gu (before e, i) | ungwë | j | hwesta |
 * | f | formen | v | ampa |
 * | z | áre | s | silmë |
 * | h (silent, still written) | hyarmen | x | quessë + silmë |
 * | m | malta | n | númen |
 * | ñ | nwalmë | l | lambë |
 * | ll | alda | r | rómen |
 * | rr | rómen + bar below | y | anna |
 * | w | vala | | |
 * Vowels a, e, i, o, u: three dots, acute, dot, right curl, left curl; ü as u; á é í ó ú on the long carrier.
 *
 * English:
 * | latin | tengwa | latin | tengwa |
 * |---|---|---|---|
 * | p | parma | t | tinco |
 * | c, k, q, ck | quessë | qu | Christopher qu |
 * | ch | calma | b | umbar |
 * | d | ando | g | ungwë |
 * | gh | unquë | j | anga |
 * | f, ph | formen | v | ampa |
 * | th | thúlë | sh | harma |
 * | z | áre | s | silmë |
 * | h | hyarmen | wh | hwesta sindarinwa |
 * | x | quessë + silmë | m | malta |
 * | n | númen | ng | noldo |
 * | l | lambë | r | rómen |
 * | y | anna | w | vala |
 * Vowels a, e, i, o, u as in Spanish; ll, ss, tt… are the tengwa plus the bar below.
 */
object TengwarTables {
    private const val TINCO = ""
    private const val PARMA = ""
    private const val CALMA = ""
    private const val QUESSE = ""
    private const val ANDO = ""
    private const val UMBAR = ""
    private const val ANGA = ""
    private const val UNGWE = ""
    private const val THULE = ""
    private const val FORMEN = ""
    private const val HARMA = ""
    private const val HWESTA = ""
    private const val AMPA = ""
    private const val UNQUE = ""
    private const val NUMEN = ""
    private const val MALTA = ""
    private const val NOLDO = ""
    private const val NWALME = ""
    private const val VALA = ""
    private const val ANNA = ""
    private const val ROMEN = ""
    private const val LAMBE = ""
    private const val ALDA = ""
    private const val SILME = ""
    private const val ARE = ""
    private const val HYARMEN = ""
    private const val HWESTA_SINDARINWA = ""
    private const val CHRISTOPHER_QU = ""

    private const val TEHTA_A = ""
    private const val TEHTA_E = ""
    private const val TEHTA_I = ""
    private const val TEHTA_O = ""
    private const val TEHTA_U = ""

    private const val FRONT_VOWELS = "eiéí"

    private val SHORT_VOWELS = listOf(
        VowelEntry('a', TEHTA_A, long = false, name = "tres puntos"),
        VowelEntry('e', TEHTA_E, long = false, name = "acento"),
        VowelEntry('i', TEHTA_I, long = false, name = "punto"),
        VowelEntry('o', TEHTA_O, long = false, name = "rizo derecho"),
        VowelEntry('u', TEHTA_U, long = false, name = "rizo izquierdo"),
    )

    private val COMMON_CONSONANTS = listOf(
        TengwaEntry("p", PARMA, "parma"),
        TengwaEntry("t", TINCO, "tinco"),
        TengwaEntry("c", QUESSE, "quessë"),
        TengwaEntry("k", QUESSE, "quessë"),
        TengwaEntry("q", QUESSE, "quessë"),
        TengwaEntry("ch", CALMA, "calma"),
        TengwaEntry("b", UMBAR, "umbar"),
        TengwaEntry("d", ANDO, "ando"),
        TengwaEntry("g", UNGWE, "ungwë"),
        TengwaEntry("f", FORMEN, "formen"),
        TengwaEntry("v", AMPA, "ampa"),
        TengwaEntry("z", ARE, "áre"),
        TengwaEntry("s", SILME, "silmë"),
        TengwaEntry("h", HYARMEN, "hyarmen"),
        TengwaEntry("x", QUESSE + SILME, "quessë + silmë"),
        TengwaEntry("m", MALTA, "malta"),
        TengwaEntry("n", NUMEN, "númen"),
        TengwaEntry("l", LAMBE, "lambë"),
        TengwaEntry("r", ROMEN, "rómen"),
        TengwaEntry("y", ANNA, "anna"),
        TengwaEntry("w", VALA, "vala"),
    )

    private val SPANISH = TengwarTable(
        consonants = COMMON_CONSONANTS + listOf(
            TengwaEntry("qu", QUESSE, "quessë", onlyBefore = FRONT_VOWELS),
            TengwaEntry("gu", UNGWE, "ungwë", onlyBefore = FRONT_VOWELS),
            TengwaEntry("j", HWESTA, "hwesta"),
            TengwaEntry("ñ", NWALME, "nwalmë"),
            TengwaEntry("ll", ALDA, "alda"),
        ),
        vowels = SHORT_VOWELS + listOf(
            VowelEntry('ü', TEHTA_U, long = false, name = "rizo izquierdo"),
            VowelEntry('á', TEHTA_A, long = true, name = "tres puntos, portador largo"),
            VowelEntry('é', TEHTA_E, long = true, name = "acento, portador largo"),
            VowelEntry('í', TEHTA_I, long = true, name = "punto, portador largo"),
            VowelEntry('ó', TEHTA_O, long = true, name = "rizo derecho, portador largo"),
            VowelEntry('ú', TEHTA_U, long = true, name = "rizo izquierdo, portador largo"),
        ),
    )

    private val ENGLISH = TengwarTable(
        consonants = COMMON_CONSONANTS + listOf(
            TengwaEntry("j", ANGA, "anga"),
            TengwaEntry("th", THULE, "thúlë"),
            TengwaEntry("sh", HARMA, "harma"),
            TengwaEntry("ng", NOLDO, "noldo"),
            TengwaEntry("wh", HWESTA_SINDARINWA, "hwesta sindarinwa"),
            TengwaEntry("ph", FORMEN, "formen"),
            TengwaEntry("gh", UNQUE, "unquë"),
            TengwaEntry("ck", QUESSE, "quessë"),
            TengwaEntry("qu", CHRISTOPHER_QU, "qu de Christopher"),
        ),
        vowels = SHORT_VOWELS,
    )

    fun table(mode: TengwarMode): TengwarTable = when (mode) {
        TengwarMode.SPANISH -> SPANISH
        TengwarMode.ENGLISH -> ENGLISH
    }
}
