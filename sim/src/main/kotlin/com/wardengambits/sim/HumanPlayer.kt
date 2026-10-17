package com.wardengambits.sim

import com.wardengambits.engine.ai.PatternBrain
import com.wardengambits.engine.combo.ComboDefinition
import com.wardengambits.engine.combo.ComboFamily
import com.wardengambits.engine.combo.ComboTuning
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.model.BossMode
import com.wardengambits.engine.model.CombatState
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.Fighter
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.rules.DamageFormula

/** Skill profile of a simulated human. */
enum class Skill { NOVATO, MEDIO, EXPERTO, MAESTRO }

/** What the player does before the next tick: retarget (if [target] differs) and press [combo] + Execute. */
data class Decision(val target: FighterId?, val combo: ComboDefinition?)

/** What a simulated human remembers during one fight: its last 2132 and whether the Aturdir opening is done. */
data class PolicyMemory(val lastShieldTick: Int? = null, val dazeOpened: Boolean = false)

/**
 * A simulated human. Deterministic: it has no randomness of its own (the combat seed drives the engine rolls).
 *
 * Cadence: ticks are numbered from 1 (the tick in which the decision resolves). The player only decides
 * before "action ticks"; between them it does nothing.
 * - NOVATO: every 2 ticks (1, 3, 5, ...).
 * - MEDIO: alternating gaps of 1 and 2 ticks (1, 2, 4, 5, 7, 8, ...), average 1.5.
 * - EXPERTO and MAESTRO: every tick.
 *
 * Use one instance per fight: it keeps a [PolicyMemory]. [tuning] must be the engine's (for the 123 kill estimate).
 */
class HumanPlayer(val profile: Skill, private val tuning: ComboTuning = ComboTuning.DEFAULT) {
    private var memory = PolicyMemory()

    fun actsOnTick(tick: Int): Boolean = when (profile) {
        Skill.NOVATO -> tick % 2 == 1
        Skill.MEDIO -> tick % 3 != 0
        Skill.EXPERTO, Skill.MAESTRO -> true
    }

    /** Decision for the tick after [state] (state.tick + 1). */
    fun decide(state: CombatState): Decision {
        if (state.rivals.any { it.isBoss && it.has(EffectKind.DAZED) }) memory = memory.copy(dazeOpened = true)
        val decision = PlayerPolicy.choose(profile, state, memory, tuning)
        when (decision.combo?.sequence) {
            PlayerPolicy.SHIELD -> memory = memory.copy(lastShieldTick = state.tick + 1)
            PlayerPolicy.DAZE_OPENING_END -> memory = memory.copy(dazeOpened = true)
        }
        return decision
    }
}

/**
 * Pure combo policy for combat v2 (spec 2026-10-12 §9). "Affordable" means power >= cost (power does not change
 * between the decision and the cast, which resolves first in the tick). Nothing while DAZED.
 *
 * A "chain step" plays the next tier of a family: tier t+1 when the player's chain is at tier t < max of that
 * family, otherwise tier 1 (a new cycle after the top tier, or a restart after the chain broke). When the next
 * tier of a running chain is not affordable the player waits (no cast), so the chain is kept.
 *
 * NOVATO (current target, no boss tricks, area, multipliers, shields, 123 or drain): hp < 30% -> Cura chain up to
 * tier 2 (21, 212); otherwise Lanza chain up to tier 3 (12, 121, 1212, then 12 again). Like MEDIO, it waits for the
 * next tier when it cannot pay it (combat v2 fix round 1: a real beginner sees the chain hint in the UI and chains).
 *
 * MEDIO / EXPERTO, target = lowest-HP alive rival; with >= 2 alive healers (rivals whose pattern has a Cura combo)
 * the lowest-HP healer instead (focus fire). In order:
 *  1. hp < 40% -> Cura chain;
 *  2. a boss is alive ([Fighter.isBoss]) and the opening is not done -> Aturdir chain on the boss, once, up to tier 3
 *     (31, 313, 3131; the 34-power 31313 made the opening cost more than it saved). It ends early as soon as the boss
 *     is dazed, and it is skipped while the boss has Escudo táctico (Daze immunity);
 *  3. (EXPERTO) "123" when it kills the target;
 *  4. a boss is alive and no Escudo maestro is active -> "2132" with power >= 40 and >= 8 ticks since the last one,
 *     otherwise "22" if Guardia is not active (skipped when unaffordable);
 *  5. >= 2 alive rivals and fewer than 2 healers -> Puño en área chain; EXPERTO plays the Robo de vida chain instead
 *     when its area chain just reached tier 4 and no Robo de vida is active;
 *  6. (EXPERTO) "11" before starting a spear chain when Furia has < 15 ticks left and Guardia is not active, so the
 *     tiers 3/4 get it;
 *  7. Lanza chain (SPEAR_A) on the target.
 * When the chain step chosen in 2, 5 or 7 is not affordable, MEDIO / EXPERTO cast "321" (Recuperar, cost 0) instead
 * of waiting, unless that would break a running chain whose next tier is at most [MAX_CHAIN_WAIT] ticks of regen
 * away. Never below 40% hp (immediate danger: the Cura branch waits) and never for NOVATO.
 *
 * MAESTRO (rival-healing spec §4) plays like EXPERTO with these changes, checked first in this order. It only uses
 * what a player sees or knows: HP, effects, whether a boss is in its defense and still shielding and healing (boss-modes
 * spec §2: it is visibly casting its shield and Cura chain every tick, see [shieldingOrHealing]), which commons still
 * have a reaction to spend, and whether a boss stuns (its moveset, learnt after one fight). It never peeks at the
 * brain's next combo
 * ([com.wardengambits.engine.ai.RivalBrain.nextCast]) or its pattern index:
 *  a. hp < 50% -> Cura chain up to tier 4, with "321" when it cannot pay (same chain-wait rule as above);
 *  b. a boss is in its defense, still shielding or healing, and not dazed -> Aturdir chain on the boss up to tier 3
 *     (31, 313, 3131, then 31 again): a Daze makes it skip the rest of its heals; skipped while the boss has Escudo
 *     táctico (Daze immunity, like the opening);
 *  c. a boss is alive and no shield of its own is active -> "2131" if the boss has Aturdir in its pattern, else
 *     "2132": at the start of the fight and again whenever the shield has expired. (The spec's "a boss with a damage
 *     combo of tier >= 2 queued" cannot be seen: a rival queues and casts inside the same tick, so its «En cola» is
 *     always empty when the player decides.) Its step 4 is only "22" (when no shield and no Guardia is active);
 *  d. with >= 2 alive rivals the target is the lowest-HP rival whose reaction still has an unused threshold (the
 *     healer), if any, instead of the lowest-HP rival (the "123" kill check keeps the lowest-HP rival).
 */
object PlayerPolicy {
    const val SHIELD = "2132"
    const val DAZE_OPENING_END = "3131"

    private val SPEAR = combos("12", "121", "1212", "12121")
    private val HEAL = combos("21", "212", "2121", "21212")
    private val NOVATO_SPEAR = SPEAR.take(3)
    private val NOVATO_HEAL = HEAL.take(2)
    private val DAZE_OPENING = combos("31", "313", DAZE_OPENING_END)
    private val AREA = combos("32", "323", "3232", "32323")
    private val DRAIN = combos("3123", "31232")
    private val MASTER_SHIELD = combo(SHIELD)
    private val TACTICS_SHIELD = combo("2131")
    private val GUARD = combo("22")
    private val FURY = combo("11")
    private val DIRECT = combo("123")
    private val RECOVER = combo("321")

    private const val SHIELD_COOLDOWN = 8
    private val SHIELD_MIN_POWER = maxOf(40, MASTER_SHIELD.cost + SPEAR.first().cost)
    private const val FURY_MIN_TICKS = 15
    private const val NOVATO_HEAL_PCT = 30
    private const val HEAL_PCT = 40
    private const val MAESTRO_HEAL_PCT = 50
    private const val MAX_CHAIN_WAIT = 3

    fun choose(
        skill: Skill,
        state: CombatState,
        memory: PolicyMemory = PolicyMemory(),
        tuning: ComboTuning = ComboTuning.DEFAULT,
    ): Decision {
        val player = state.player
        val nextTick = state.tick + 1
        val alive = state.rivals.filter { it.isAlive }
        if (alive.isEmpty() || player.isActive(EffectKind.DAZED, nextTick)) return Decision(state.targetId, null)

        if (skill == Skill.NOVATO) {
            val target = state.targetId?.let { state.fighter(it) }?.takeIf { it.isAlive } ?: alive.first()
            val novato = Ctx(player)
            val combo = if (hpBelow(player, NOVATO_HEAL_PCT)) {
                novato.chainStep(NOVATO_HEAL, ComboFamily.HEAL)
            } else {
                novato.chainStep(NOVATO_SPEAR, ComboFamily.SPEAR_A)
            }
            return Decision(target.id, combo)
        }

        val healers = alive.filter { isHealer(state, it) }
        val lowest = (if (healers.size >= 2) healers else alive).minBy { it.hp }
        val boss = alive.firstOrNull { it.isBoss }
        val maestro = skill == Skill.MAESTRO
        val expert = skill == Skill.EXPERTO || maestro
        val focus = if (maestro && alive.size >= 2) {
            alive.filter { hasUnusedReaction(state, it) }.minByOrNull { it.hp } ?: lowest
        } else {
            lowest
        }
        val ctx = Ctx(player)

        val dazeable = boss != null && !boss.has(EffectKind.DAZED) && !boss.isActive(EffectKind.SHIELD_TACTICS, nextTick)
        if (maestro && hpBelow(player, MAESTRO_HEAL_PCT)) {
            return Decision(focus.id, ctx.stepOrRecover(HEAL, ComboFamily.HEAL))
        }
        if (maestro && boss != null && dazeable && shieldingOrHealing(state, boss)) {
            return Decision(boss.id, ctx.stepOrRecover(DAZE_OPENING, ComboFamily.DAZE))
        }
        if (hpBelow(player, HEAL_PCT)) return Decision(lowest.id, ctx.chainStep(HEAL, ComboFamily.HEAL))
        val shielded = player.has(EffectKind.SHIELD_MASTER) || player.has(EffectKind.SHIELD_TACTICS)
        if (maestro && boss != null && !shielded) {
            val shield = if (dazes(state, boss)) TACTICS_SHIELD else MASTER_SHIELD
            if (player.power >= shield.cost) return Decision(focus.id, shield)
        }
        if (boss != null && dazeable && !memory.dazeOpened) {
            return Decision(boss.id, ctx.stepOrRecover(DAZE_OPENING, ComboFamily.DAZE))
        }
        if (expert && player.power >= DIRECT.cost && kills(player, lowest, nextTick, tuning)) {
            return Decision(lowest.id, DIRECT)
        }
        if (maestro) {
            if (boss != null && !shielded && !player.has(EffectKind.GUARD) && player.power >= GUARD.cost) {
                return Decision(focus.id, GUARD)
            }
        } else if (boss != null && !player.has(EffectKind.SHIELD_MASTER)) {
            val shieldReady = memory.lastShieldTick == null || nextTick - memory.lastShieldTick >= SHIELD_COOLDOWN
            val defense = if (player.power >= SHIELD_MIN_POWER && shieldReady) {
                MASTER_SHIELD
            } else {
                GUARD.takeIf { !player.has(EffectKind.GUARD) }
            }
            if (defense != null && player.power >= defense.cost) return Decision(lowest.id, defense)
        }
        if (alive.size >= 2 && healers.size < 2) {
            val draining = player.chain.family == ComboFamily.DRAIN && player.chain.tier == 1
            val areaDone = player.chain.family == ComboFamily.AREA && player.chain.tier == AREA.size
            val combo = if (expert && (draining || areaDone && !player.has(EffectKind.DRAIN))) {
                ctx.stepOrRecover(DRAIN, ComboFamily.DRAIN)
            } else {
                ctx.stepOrRecover(AREA, ComboFamily.AREA)
            }
            return Decision(focus.id, combo)
        }
        val startsSpear = ctx.nextTier(SPEAR, ComboFamily.SPEAR_A) == SPEAR.first()
        val furyLeft = player.effects.filter { it.kind == EffectKind.FURY }.maxOfOrNull { it.remainingTicks } ?: 0
        if (expert && startsSpear && furyLeft < FURY_MIN_TICKS && !player.has(EffectKind.GUARD) &&
            player.power >= FURY.cost + SPEAR.first().cost
        ) {
            return Decision(focus.id, FURY)
        }
        return Decision(focus.id, ctx.stepOrRecover(SPEAR, ComboFamily.SPEAR_A))
    }

    private fun kills(player: Fighter, target: Fighter, tick: Int, tuning: ComboTuning): Boolean =
        DamageFormula.directHit(
            DamageFormula.spearUnit(player.stats.attack, tuning),
            player.isActive(EffectKind.FURY, tick),
            target.stats.defense,
            target.isActive(EffectKind.GUARD, tick),
            target.isActive(EffectKind.SHIELD_MASTER, tick),
        ) >= target.hp

    private fun isHealer(state: CombatState, rival: Fighter): Boolean =
        (state.brains[rival.id] as? PatternBrain)?.pattern.orEmpty().any { Combos.find(it)?.family == ComboFamily.HEAL }

    /**
     * True while [boss] is in its defense (boss-modes spec §2) and still in its shield and heals, before its final damage
     * combos ([com.wardengambits.engine.ai.BossModes.dazedRestart]): what the player sees it casting every tick. A brain
     * without known modes counts as healing for its whole defense.
     */
    private fun shieldingOrHealing(state: CombatState, boss: Fighter): Boolean {
        if (boss.bossMode != BossMode.DEFENSE) return false
        val modes = (state.brains[boss.id] as? PatternBrain)?.modes ?: return true
        return boss.modeCasts % modes.defense.size < modes.dazedRestart
    }

    /** True while [rival]'s HP reaction still has a threshold it has not used. */
    private fun hasUnusedReaction(state: CombatState, rival: Fighter): Boolean {
        val thresholds = (state.brains[rival.id] as? PatternBrain)?.reaction?.atHpPct?.size ?: return false
        return rival.reactionUsed != (1 shl thresholds) - 1
    }

    /**
     * True when [boss] has an Aturdir combo in its pattern (its rival then shields with Escudo táctico, which gives
     * Daze immunity).
     */
    private fun dazes(state: CombatState, boss: Fighter): Boolean =
        (state.brains[boss.id] as? PatternBrain)?.pattern.orEmpty().any { Combos.find(it)?.family == ComboFamily.DAZE }

    private fun hpBelow(f: Fighter, pct: Int): Boolean = f.hp * 100 < f.stats.maxHp * pct

    private class Ctx(val player: Fighter) {
        /** Tier after the player's current chain in [family] ([tiers] lowest first), or tier 1. */
        fun nextTier(tiers: List<ComboDefinition>, family: ComboFamily): ComboDefinition {
            val chain = player.chain
            return if (chain.family == family && chain.tier in 1 until tiers.size) tiers[chain.tier] else tiers.first()
        }

        /** The next tier if affordable; null (wait) otherwise. */
        fun chainStep(tiers: List<ComboDefinition>, family: ComboFamily): ComboDefinition? =
            nextTier(tiers, family).takeIf { player.power >= it.cost }

        /**
         * The next tier if affordable; otherwise "321" unless it would break a running chain whose next tier is
         * only a short wait away (then null, wait). Only for MEDIO / EXPERTO outside immediate danger.
         */
        fun stepOrRecover(tiers: List<ComboDefinition>, family: ComboFamily): ComboDefinition? {
            val next = nextTier(tiers, family)
            if (player.power >= next.cost) return next
            val waitTicks = (next.cost - player.power + player.stats.powerRegen - 1) / player.stats.powerRegen
            val breaksChain = next.tier >= 2
            return RECOVER.takeIf { !breaksChain || waitTicks > MAX_CHAIN_WAIT }
        }
    }

    private fun combo(seq: String): ComboDefinition = requireNotNull(Combos.find(seq)) { "Unknown combo $seq" }

    private fun combos(vararg seqs: String): List<ComboDefinition> = seqs.map(::combo)
}
