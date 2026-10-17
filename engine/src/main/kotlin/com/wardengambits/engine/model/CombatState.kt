package com.wardengambits.engine.model

import com.wardengambits.engine.ai.BossModes
import com.wardengambits.engine.ai.PatternBrain
import com.wardengambits.engine.ai.RivalBrain

data class FighterSpec(val name: String, val level: Int)

/** [isBoss] rivals use the boss scaling of [com.wardengambits.engine.rules.CombatEngine]. */
data class RivalSpec(
    val name: String,
    val level: Int,
    val brain: RivalBrain,
    val isBoss: Boolean = false,
    val portrait: String = "unknown",
) {
    /** The HP reaction of the [brain] (rival-healing spec §2); null when it never reacts. */
    val reaction: Reaction? get() = (brain as? PatternBrain)?.reaction

    /** The attack and defense modes of the [brain] (boss-modes spec §2); null for rivals without modes. */
    val modes: BossModes? get() = (brain as? PatternBrain)?.modes
}

data class CombatSetup(val player: FighterSpec, val rivals: List<RivalSpec>) {
    init {
        require(rivals.size in 1..4) { "A combat needs 1 to 4 rivals, got ${rivals.size}" }
    }
}

enum class Outcome { VICTORY, DEFEAT }

data class CombatState(
    val tick: Int,
    val fighters: List<Fighter>,
    val targetId: FighterId?,
    val brains: Map<FighterId, RivalBrain>,
    val rngState: Long,
    val outcome: Outcome? = null,
) {
    val player: Fighter get() = fighters[0]
    val rivals: List<Fighter> get() = fighters.drop(1)

    fun fighter(id: FighterId): Fighter = fighters.first { it.id == id }
}
