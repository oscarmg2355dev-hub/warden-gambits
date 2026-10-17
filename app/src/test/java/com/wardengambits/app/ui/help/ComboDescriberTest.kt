package com.wardengambits.app.ui.help

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import com.wardengambits.engine.combo.ComboFamily
import com.wardengambits.engine.combo.Combos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class ComboDescriberTest {
    private fun resources(language: String): Resources {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val configuration = Configuration(context.resources.configuration).apply { setLocale(Locale.forLanguageTag(language)) }
        return context.createConfigurationContext(configuration).resources
    }

    private val es = resources("es")
    private val en = resources("en")

    private fun d(seq: String, res: Resources = es) = ComboDescriber.describe(res, Combos.find(seq)!!)

    @Test fun spear() {
        assertEquals("Lanza I · 1× daño de lanza por tick durante 10 ticks", d("12"))
        assertEquals("Lanza II · 2× daño de lanza por tick durante 10 ticks (+5 si encadenas)", d("121"))
        assertEquals("Lanza cruzada IV · 4× daño de lanza por tick durante 10 ticks (+20 si encadenas)", d("13131"))
    }

    @Test fun daze() {
        assertEquals("Aturdir I · +5% de Daze durante 10 ticks", d("31"))
        assertEquals("Aturdir III · +5% de Daze durante 10 ticks (+5% en cadena completa)", d("3131"))
        assertEquals("Aturdir IV · +5% de Daze durante 10 ticks (+30% en cadena completa)", d("31313"))
    }

    @Test fun areaAndHeal() {
        assertEquals("Puño en área III · 3× daño de puño a todos por tick durante 10 ticks (+10 si encadenas)", d("3232"))
        assertEquals("Cura II · 2× cura por tick durante 10 ticks (+5 si encadenas)", d("212"))
        assertEquals("Cura I · 1× cura por tick durante 10 ticks", d("21"))
    }

    @Test fun drain() {
        assertEquals("Robo de vida I · roba vida (tu cura) a cada rival cada 5 ticks durante 30 ticks", d("3123"))
        assertEquals("Robo de vida II · roba vida (tu cura) a cada rival cada 5 ticks durante 30 ticks (×2 si encadenas)", d("31232"))
    }

    @Test fun buffsAndShields() {
        assertEquals("Furia · +20% daño de lanza durante 30 ticks", d("11"))
        assertEquals("Guardia · −20% daño recibido durante 30 ticks", d("22"))
        assertEquals("Brío · +20% regeneración de power durante 30 ticks", d("33"))
        assertEquals("Escudo maestro · −50% daño recibido durante 60 ticks", d("2132"))
        assertEquals("Escudo táctico · inmune a Daze durante 60 ticks", d("2131"))
    }

    @Test fun direct() = assertEquals("Golpe directo · 10× daño de lanza de una vez", d("123"))

    @Test fun recover() = assertEquals("Recuperar · recupera el 10% de tu power al instante", d("321"))

    @Test fun englishTextsKeepTheNumbers() {
        assertEquals("Spear II · 2× spear damage per tick for 10 ticks (+5 if chained)", d("121", en))
        assertEquals("Stun IV · +5% Daze chance for 10 ticks (+30% on a full chain)", d("31313", en))
        assertEquals("Area Fist III · 3× fist damage to all per tick for 10 ticks (+10 if chained)", d("3232", en))
        assertEquals("Life Drain II · drains life (your heal) from each rival every 5 ticks for 30 ticks (×2 if chained)", d("31232", en))
        assertEquals("Guard · −20% damage taken for 30 ticks", d("22", en))
        assertEquals("Vigour · +20% power regeneration for 30 ticks", d("33", en))
        assertEquals("Tactical Shield · immune to Daze for 60 ticks", d("2131", en))
        assertEquals("Direct Strike · 10× spear damage at once", d("123", en))
        assertEquals("Recover · restores 10% of your power at once", d("321", en))
    }

    @Test fun everyComboHasItsOwnNameInBothLanguages() {
        listOf(es, en).forEach { res ->
            val names = Combos.all.map { ComboDescriber.name(res, it) }
            assertEquals(names.size, names.toSet().size)
        }
        Combos.all.forEach {
            assertNotEquals(it.sequence, ComboDescriber.name(es, it), ComboDescriber.name(en, it))
        }
    }

    @Test fun groupedCoversAll29InSpecOrder() {
        val grouped = ComboDescriber.grouped()
        assertEquals(29, grouped.values.sumOf { it.size })
        assertEquals(ComboFamily.entries.toList(), grouped.keys.toList())
        assertEquals(listOf("12", "121", "1212", "12121"), grouped.getValue(ComboFamily.SPEAR_A).map { it.sequence })
        assertEquals(listOf("321"), grouped.getValue(ComboFamily.RECOVER).map { it.sequence })
    }

    @Test fun familyTitles() {
        assertEquals(
            listOf(
                "Lanza", "Lanza cruzada", "Aturdir", "Puño en área", "Cura", "Robo de vida",
                "Multiplicadores", "Escudos", "Golpe directo", "Recuperar",
            ),
            ComboFamily.entries.map { ComboDescriber.title(es, it) },
        )
        assertEquals(
            listOf(
                "Spear", "Cross Spear", "Stun", "Area Fist", "Heal", "Life Drain",
                "Multipliers", "Shields", "Direct Strike", "Recover",
            ),
            ComboFamily.entries.map { ComboDescriber.title(en, it) },
        )
    }
}
