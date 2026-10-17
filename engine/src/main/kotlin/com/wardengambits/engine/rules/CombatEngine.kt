package com.wardengambits.engine.rules

import com.wardengambits.engine.action.CombatAction
import com.wardengambits.engine.combo.ComboTuning
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.event.RejectReason
import com.wardengambits.engine.model.CombatSetup
import com.wardengambits.engine.model.CombatState
import com.wardengambits.engine.model.Fighter
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.LevelStats
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.model.RivalScaling
import com.wardengambits.engine.model.Side
import com.wardengambits.engine.random.GameRandom
import com.wardengambits.engine.random.SeededRandom

data class StepResult(val state: CombatState, val events: List<CombatEvent>)

/**
 * [rivalScaling] shapes rival stats (the player always uses [LevelStats.forLevel]);
 * [tuning] sets the combo base percents (SPDMG, FIDMG, CUR).
 */
class CombatEngine(
    private val rivalScaling: RivalScaling = RivalScaling.DEFAULT,
    private val bossScaling: RivalScaling = RivalScaling.BOSS,
    val tuning: ComboTuning = ComboTuning.DEFAULT,
    private val randomFactory: (Long) -> GameRandom = ::SeededRandom,
) {
    fun start(setup: CombatSetup, seed: Long): CombatState {
        val playerStats = LevelStats.forLevel(setup.player.level)
        val player = Fighter(
            PLAYER_ID, setup.player.name, setup.player.level, Side.PLAYER, playerStats,
            hp = playerStats.maxHp, power = playerStats.maxPower,
        )
        val rivals = setup.rivals.mapIndexed { index, rival ->
            val scaling = if (rival.isBoss) bossScaling else rivalScaling
            val stats = LevelStats.forRival(rival.level, scaling)
            Fighter(
                FighterId(index + 1), rival.name, rival.level, Side.RIVAL, stats,
                hp = stats.maxHp, power = scaling.startPower(stats.maxPower), isBoss = rival.isBoss,
                portrait = rival.portrait,
            )
        }
        val brains = setup.rivals.mapIndexed { index, rival -> FighterId(index + 1) to rival.brain }.toMap()
        return CombatState(
            tick = 0,
            fighters = listOf(player) + rivals,
            targetId = rivals.first().id,
            brains = brains,
            rngState = seed,
        )
    }

    fun reduce(state: CombatState, action: CombatAction): StepResult =
        if (state.outcome != null) reject(state, PLAYER_ID, RejectReason.COMBAT_OVER) else step(state, action)

    private fun step(state: CombatState, action: CombatAction): StepResult = when (action) {
        is CombatAction.Press -> withActor(state, action.fighter) { actor ->
            if (actor.buffer.length >= MAX_BUFFER) {
                reject(state, actor.id, RejectReason.BUFFER_FULL)
            } else {
                StepResult(update(state, actor.copy(buffer = actor.buffer + action.input.code)), emptyList())
            }
        }
        is CombatAction.Execute -> withActor(state, action.fighter) { actor ->
            val combo = Combos.find(actor.buffer)
            val cleared = actor.copy(buffer = "")
            if (combo == null) {
                StepResult(
                    update(state, cleared),
                    listOf(CombatEvent.ComboRejected(actor.id, RejectReason.UNKNOWN_SEQUENCE)),
                )
            } else {
                StepResult(
                    update(state, cleared.copy(queued = combo)),
                    listOf(CombatEvent.ComboQueued(actor.id, combo)),
                )
            }
        }
        is CombatAction.ClearBuffer -> withActor(state, action.fighter) { actor ->
            StepResult(update(state, actor.copy(buffer = "")), emptyList())
        }
        is CombatAction.SelectTarget -> {
            val target = state.fighters.firstOrNull { it.id == action.target }
            if (target != null && target.side == Side.RIVAL && target.isAlive) {
                StepResult(state.copy(targetId = target.id), emptyList())
            } else {
                reject(state, PLAYER_ID, RejectReason.INVALID_TARGET)
            }
        }
        CombatAction.Tick -> {
            val random = randomFactory(state.rngState)
            val result = TickResolver.resolve(state, random, tuning)
            StepResult(result.state.copy(rngState = random.state), result.events)
        }
    }

    private inline fun withActor(
        state: CombatState,
        id: FighterId,
        block: (Fighter) -> StepResult,
    ): StepResult {
        val actor = state.fighters.firstOrNull { it.id == id }
        return if (actor == null || !actor.isAlive) reject(state, id, RejectReason.FIGHTER_DEFEATED) else block(actor)
    }

    private fun update(state: CombatState, fighter: Fighter): CombatState =
        state.copy(fighters = state.fighters.map { if (it.id == fighter.id) fighter else it })

    private fun reject(state: CombatState, id: FighterId, reason: RejectReason): StepResult =
        StepResult(state, listOf(CombatEvent.ComboRejected(id, reason)))

    private companion object {
        const val MAX_BUFFER = 5
    }
}
