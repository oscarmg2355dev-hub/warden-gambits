package com.wardengambits.app.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/** values (es) and values-en (en) declare the same strings and plurals, with the same format placeholders. */
class StringResourcesParityTest {
    private val res = listOf(File("src/main/res"), File("app/src/main/res")).first { it.isDirectory }

    /** Resource key ("name" or "name#quantity" for plural items) to its text. */
    private fun entries(dir: String): Map<String, String> {
        val file = File(res, "$dir/strings.xml")
        assertTrue("missing $file", file.isFile)
        val root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).documentElement
        val out = linkedMapOf<String, String>()
        val children = root.childNodes
        for (i in 0 until children.length) {
            val node = children.item(i) as? Element ?: continue
            val name = node.getAttribute("name")
            when (node.tagName) {
                "string" -> out[name] = node.textContent
                "plurals" -> {
                    val items = node.getElementsByTagName("item")
                    for (j in 0 until items.length) {
                        val item = items.item(j) as Element
                        out["$name#${item.getAttribute("quantity")}"] = item.textContent
                    }
                }
            }
        }
        return out
    }

    /** Format placeholders (`%d`, `%1$s`, …) in order of appearance; `%%` is a literal percent sign. */
    private fun placeholders(text: String): List<String> =
        Regex("""%(%|(\d+\$)?[a-z])""").findAll(text).map { it.value }.filter { it != "%%" }.toList()

    @Test fun englishHasExactlyTheSpanishKeys() {
        val es = entries("values")
        val en = entries("values-en")
        assertEquals("missing in values-en", emptySet<String>(), es.keys - en.keys)
        assertEquals("only in values-en", emptySet<String>(), en.keys - es.keys)
    }

    @Test fun placeholdersMatch() {
        val es = entries("values")
        val en = entries("values-en")
        val mismatched = es.keys.intersect(en.keys).filter { placeholders(es.getValue(it)).sorted() != placeholders(en.getValue(it)).sorted() }
        assertEquals(emptyList<String>(), mismatched)
    }

    @Test fun englishIsTranslated() {
        val es = entries("values")
        val en = entries("values-en")
        // Names and symbols that are the same in both languages.
        val same = setOf(
            "app_name", "menu_title", "options_script_tengwar", "combat_tick", "combat_combo_empty", "combat_combo_cost",
            "combat_boss", "combat_clear", "combo_describe", "character_name", "character_zone", "character_stat_power",
        )
        val untranslated = es.keys.intersect(en.keys).filter { it !in same && es[it] == en[it] }
        assertEquals(emptyList<String>(), untranslated)
    }
}
