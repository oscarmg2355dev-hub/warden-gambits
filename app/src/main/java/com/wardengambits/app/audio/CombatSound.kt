package com.wardengambits.app.audio

import com.wardengambits.engine.combo.Input
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.Outcome

/** Which sound effect a combat event plays, if any. */
fun soundFor(event: CombatEvent, playerId: FighterId): Sfx? = when (event) {
    is CombatEvent.FighterDefeated -> if (event.id == playerId) null else Sfx.DEFEAT_ENEMY
    is CombatEvent.CombatEnded -> when (event.outcome) {
        Outcome.VICTORY -> Sfx.VICTORY
        Outcome.DEFEAT -> Sfx.DEFEAT
    }
    is CombatEvent.ComboQueued,
    is CombatEvent.ComboRejected,
    is CombatEvent.ComboResolved,
    is CombatEvent.EffectResolved,
    is CombatEvent.EffectApplied,
    is CombatEvent.EffectExpired,
    is CombatEvent.PotionUsed,
    -> null
}

/**
 * Combat-screen sound logic: effects for events, the 1/2/3 keys and the ⚔ press, and the ambience lifecycle (plays while the
 * screen is active and the fight runs, pauses on pause or background, stops at the end or when leaving).
 */
class CombatSound(private val player: SoundPlayer, private val playerId: FighterId) {
    private enum class Ambient { STOPPED, PLAYING, PAUSED }

    private var ambient = Ambient.STOPPED

    fun onAction() = player.playSfx(Sfx.ACTION)

    /** Key sound of an input press (sword, shield, punch); it plays even if the engine then rejects the press. */
    fun onKey(input: Input) = player.playSfx(
        when (input) {
            Input.SPEAR -> Sfx.KEY_SWORD
            Input.SHIELD -> Sfx.KEY_SHIELD
            Input.FIST -> Sfx.KEY_PUNCH
        },
    )

    fun onEvent(event: CombatEvent) {
        soundFor(event, playerId)?.let(player::playSfx)
    }

    fun update(active: Boolean, paused: Boolean, outcome: Outcome?) {
        val next = when {
            outcome != null -> Ambient.STOPPED
            active && !paused -> Ambient.PLAYING
            ambient == Ambient.STOPPED -> Ambient.STOPPED
            else -> Ambient.PAUSED
        }
        if (next == ambient) return
        ambient = next
        when (next) {
            Ambient.PLAYING -> player.startAmbient()
            Ambient.PAUSED -> player.pauseAmbient()
            Ambient.STOPPED -> player.stopAmbient()
        }
    }

    fun dispose() {
        if (ambient != Ambient.STOPPED) player.stopAmbient()
        ambient = Ambient.STOPPED
    }
}
