package com.wardengambits.app.i18n.tengwar

import java.text.Normalizer

/**
 * Orthographic tengwar transcriber (CSUR codepoints for Alcarin Tengwar). Pure Kotlin.
 *
 * - Letters follow [TengwarTables] for the [TengwarMode], letter by letter; case is ignored.
 * - Digits, whitespace, math signs, non-letter Unicode and protected combo codes ([PROTECTED]: SP1–SP4,
 *   LC, AT, PA, CU, RV with an optional tier, DZ, EM, ET, FU, GU, BR, GD, PO; uppercase, whole token)
 *   are left in Latin.
 * - Comma and period become pusta and double pusta, except between two digits («1,840», «1.5»).
 *   Other punctuation is kept.
 * - Letters outside the table fall back to their base letter (an accented vowel is long); otherwise they pass through.
 *
 * Results are cached (LRU, thread-safe) by (text, mode).
 */
object Tengwar {
    private const val CACHE_SIZE = 2048

    private val PROTECTED = Regex(
        "(?<![\\p{L}\\p{N}])(?:(?:SP|LC|AT|PA|CU|RV)[1-4]?|DZ|EM|ET|FU|GU|BR|GD|PO)(?![\\p{L}\\p{N}])",
    )

    /** Consonant entries of each table, digraphs first, for greedy matching. */
    private val longestFirst: Map<TengwarMode, List<TengwaEntry>> = TengwarMode.entries
        .associateWith { mode -> TengwarTables.table(mode).consonants.sortedByDescending { it.latin.length } }

    private val cache = object : LinkedHashMap<Pair<TengwarMode, String>, String>(256, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Pair<TengwarMode, String>, String>?): Boolean =
            size > CACHE_SIZE
    }

    fun transcribe(text: String, mode: TengwarMode): String {
        val key = mode to text
        synchronized(cache) { cache[key]?.let { return it } }
        val result = compute(text, mode)
        synchronized(cache) { cache[key] = result }
        return result
    }

    /** Rows for the equivalence table: consonants, digraphs and vowels (shown over their carrier). */
    fun equivalences(mode: TengwarMode): List<Equivalence> {
        val table = TengwarTables.table(mode)
        val consonants = table.consonants.sortedBy { it.latin.length }.map {
            Equivalence(it.latin, it.tengwar, if (it.latin.length == 1) EquivalenceKind.CONSONANT else EquivalenceKind.DIGRAPH)
        }
        val vowels = table.vowels.map {
            Equivalence(it.latin.toString(), (if (it.long) Csur.LONG_CARRIER else Csur.SHORT_CARRIER) + it.tehta, EquivalenceKind.VOWEL)
        }
        return consonants + vowels
    }

    private fun compute(text: String, mode: TengwarMode): String {
        val out = StringBuilder(text.length)
        var last = 0
        for (match in PROTECTED.findAll(text)) {
            transcribeSegment(text, last, match.range.first, mode, out)
            out.append(match.value)
            last = match.range.last + 1
        }
        transcribeSegment(text, last, text.length, mode, out)
        return out.toString()
    }

    private fun transcribeSegment(text: String, start: Int, end: Int, mode: TengwarMode, out: StringBuilder) {
        var i = start
        while (i < end) {
            val c = text[i]
            if (c.isLetter()) {
                var j = i
                while (j < end && (text[j].isLetter() || isInnerApostrophe(text, j, end))) j++
                // An apostrophe inside a word (Durin's, don't) is dropped: a final 's then takes the s-hook.
                out.append(transcribeWord(text.substring(i, j).replace("'", "").replace("’", "").lowercase(), mode))
                i = j
                continue
            }
            val betweenDigits = i > 0 && text[i - 1].isDigit() && i + 1 < text.length && text[i + 1].isDigit()
            out.append(
                when {
                    c == ',' && !betweenDigits -> Csur.PUSTA
                    c == '.' && !betweenDigits -> Csur.DOUBLE_PUSTA
                    else -> c.toString()
                },
            )
            i++
        }
    }

    private fun isInnerApostrophe(text: String, at: Int, end: Int): Boolean =
        (text[at] == '\'' || text[at] == '’') && at + 1 < end && text[at + 1].isLetter()

    private sealed interface Piece {
        data class Consonant(val entry: TengwaEntry, val doubled: Boolean) : Piece
        data class Vowel(val tehta: String, val long: Boolean) : Piece
        data class Raw(val text: String) : Piece
    }

    private fun transcribeWord(word: String, mode: TengwarMode): String {
        val units = tokenize(word, mode)
        val out = StringBuilder()
        var canTakeTehta = false
        units.forEachIndexed { index, unit ->
            when (unit) {
                is Piece.Consonant -> {
                    val finalS = unit.entry.latin == "s" && !unit.doubled && index == units.lastIndex &&
                        index > 0 && units[index - 1] !is Piece.Raw
                    if (finalS) {
                        out.append(Csur.S_HOOK)
                        canTakeTehta = false
                    } else {
                        out.append(unit.entry.tengwar)
                        if (unit.doubled) out.append(Csur.BAR_BELOW)
                        canTakeTehta = true
                    }
                }
                is Piece.Vowel -> {
                    if (unit.long) {
                        out.append(Csur.LONG_CARRIER)
                    } else if (!canTakeTehta) {
                        out.append(Csur.SHORT_CARRIER)
                    }
                    out.append(unit.tehta)
                    canTakeTehta = false
                }
                is Piece.Raw -> {
                    out.append(unit.text)
                    canTakeTehta = false
                }
            }
        }
        return out.toString()
    }

    private fun tokenize(word: String, mode: TengwarMode): List<Piece> {
        val table = TengwarTables.table(mode)
        val consonants = longestFirst.getValue(mode)
        val units = ArrayList<Piece>()
        var i = 0
        while (i < word.length) {
            val entry = consonants.firstOrNull { matches(word, i, it) }
            if (entry != null) {
                val end = i + entry.latin.length
                val doubled = entry.latin.length == 1 && end < word.length && word[end] == word[i] &&
                    consonants.none { it.latin.length > 1 && word.startsWith(it.latin, i) }
                units += Piece.Consonant(entry, doubled)
                i = if (doubled) end + 1 else end
                continue
            }
            val vowel = table.vowels.firstOrNull { it.latin == word[i] }
            units += vowel?.let { Piece.Vowel(it.tehta, it.long) } ?: fallback(word[i], table)
            i++
        }
        return units
    }

    private fun matches(word: String, at: Int, entry: TengwaEntry): Boolean {
        if (!word.startsWith(entry.latin, at)) return false
        val next = word.getOrNull(at + entry.latin.length)
        return entry.onlyBefore == null || (next != null && next in entry.onlyBefore)
    }

    /** A letter outside the table: its base letter (accented vowels become long), or the letter itself. */
    private fun fallback(letter: Char, table: TengwarTable): Piece {
        val base = Normalizer.normalize(letter.toString(), Normalizer.Form.NFD).firstOrNull()
        if (base != null && base != letter) {
            table.vowels.firstOrNull { it.latin == base && !it.long }?.let { return Piece.Vowel(it.tehta, long = true) }
            table.consonants.firstOrNull { it.latin == base.toString() }?.let { return Piece.Consonant(it, doubled = false) }
        }
        return Piece.Raw(letter.toString())
    }
}
