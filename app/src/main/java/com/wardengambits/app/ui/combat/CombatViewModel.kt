package com.wardengambits.app.ui.combat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wardengambits.engine.action.CombatAction
import com.wardengambits.engine.combo.ChainRules
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.combo.Input
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.model.CombatState
import com.wardengambits.app.ui.common.badgesFor
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.Fighter
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.LevelStats
import com.wardengambits.engine.model.Outcome
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.rules.CombatEngine
import com.wardengambits.engine.story.StoryEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Runs one real-time combat: wraps [CombatEngine] state, ticks it every [tickMillis] while the screen is
 * active and not paused, and reports the result once through the [source] (a story combat reports to the
 * session; a practice combat reports nothing and can be retried). The fight comes from [CombatSource.setup];
 * [story] is the story in the app's language, and only the zone and rival names shown come from it,
 * following it even in the middle of the fight.
 */
class CombatViewModel(
    private val source: CombatSource,
    story: StateFlow<StoryEngine>,
    private val combatEngine: CombatEngine,
    private val clock: () -> Long,
    private val tickMillis: Long = 1000L,
) : ViewModel() {
    private val setup = source.setup

    /** True for a practice fight: no report, and the result overlay offers a retry. */
    val isPractice: Boolean = source is PracticeCombatSource

    /** Rival names in the story's current language, in encounter order (rival `FighterId(i + 1)` is entry `i`). */
    private var rivalNames: List<String> = source.rivalNames(story.value)

    /** True when the profile had no combat to fight; the screen navigates back. */
    val invalid: Boolean = setup == null

    private var combat: CombatState? = setup?.let { combatEngine.start(it, clock()) }
    private var finished = false

    private val active = MutableStateFlow(false)

    /** Set once the result was sent to the story (finish or abandon): no more ticks. */
    private val stopped = MutableStateFlow(invalid)

    private val _state: MutableStateFlow<CombatUiState>
    val state: StateFlow<CombatUiState>

    private val _events = MutableSharedFlow<CombatEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<CombatEvent> = _events.asSharedFlow()

    init {
        val progress = source.progress(story.value)
        val level = source.levelBefore
        val initial = CombatUiState(
            zoneName = progress.zone.name,
            levelLabel = "${progress.level}/${progress.total}",
            tick = 0,
            paused = false,
            rivals = emptyList(),
            player = placeholderPlayer(level),
            buffer = "",
            bufferMatch = null,
            outcome = null,
            levelBefore = level,
            winsBefore = source.winsBefore,
        )
        _state = MutableStateFlow(combat?.let { withCombat(initial, it) } ?: initial)
        state = _state.asStateFlow()

        viewModelScope.launch { story.collect(::relabel) }
        viewModelScope.launch {
            combine(active, stopped, _state) { isActive, isStopped, ui ->
                isActive && !isStopped && !ui.paused && ui.outcome == null
            }
                .distinctUntilChanged()
                .collectLatest { running ->
                    while (running) {
                        delay(tickMillis)
                        reduce(CombatAction.Tick)
                    }
                }
        }
    }

    fun press(input: Input) = launchInput(CombatAction.Press(PLAYER_ID, input))

    fun execute() = launchInput(CombatAction.Execute(PLAYER_ID))

    fun clearBuffer() = launchInput(CombatAction.ClearBuffer(PLAYER_ID))

    fun selectTarget(id: FighterId) = playerInput(CombatAction.SelectTarget(id))

    fun togglePause() {
        val ui = _state.value
        val paused = !ui.paused
        _state.value = ui.copy(paused = paused, inputsEnabled = combat?.let { it.outcome == null && !paused && !it.player.isDazed() } ?: false)
    }

    /**
     * Called by the screen: true while at least STARTED. Going to the background mid-fight latches the
     * visible pause; coming back does not unpause (the player resumes with the pause button).
     */
    fun setActive(active: Boolean) {
        val ui = _state.value
        if (!active && this.active.value && !stopped.value && ui.outcome == null) {
            _state.value = ui.copy(paused = true, inputsEnabled = false)
        }
        this.active.value = active
    }

    /** Sends the combat outcome to the story; only the first call after the combat ended does anything. */
    fun finish() {
        val outcome = _state.value.outcome ?: return
        report(outcome)
    }

    /** Leaving mid-combat counts as a defeat; leaving after the end reports the real outcome. */
    fun abandon() {
        report(_state.value.outcome ?: Outcome.DEFEAT)
    }

    /** Practice only: starts a brand new fight (same setup), clearing the result and resuming the ticks. */
    fun retry() {
        val setup = setup ?: return
        if (!isPractice) return
        val fresh = combatEngine.start(setup, clock())
        combat = fresh
        stopped.value = false
        finished = false
        _state.value = withCombat(_state.value.copy(paused = false, outcome = null), fresh)
    }

    private fun report(outcome: Outcome) {
        if (invalid || finished) return
        finished = true
        stopped.value = true
        when (source) {
            is StoryCombatSource -> source.report(outcome, clock())
            is PracticeCombatSource -> Unit
        }
    }

    /** Launching inputs (keys, cast, clear) are also ignored while the Warden is Dazed. */
    private fun launchInput(action: CombatAction) {
        if (combat?.player?.isDazed() == true) return
        playerInput(action)
    }

    /** Player inputs are ignored while paused, once the combat is over, or after the result was reported. */
    private fun playerInput(action: CombatAction) {
        val ui = _state.value
        if (ui.paused || ui.outcome != null || stopped.value) return
        reduce(action)
    }

    private fun reduce(action: CombatAction) {
        val current = combat ?: return
        val step = combatEngine.reduce(current, action)
        combat = step.state
        _state.value = withCombat(_state.value, step.state)
        step.events.forEach { _events.tryEmit(it) }
    }

    private fun displayName(id: FighterId, fallback: String): String = rivalNames.getOrNull(id.value - 1) ?: fallback

    /** Shows the zone and rival names of [engine] (the same story in another language). */
    private fun relabel(engine: StoryEngine) {
        rivalNames = source.rivalNames(engine)
        _state.update { ui ->
            ui.copy(
                zoneName = source.progress(engine).zone.name,
                rivals = ui.rivals.map { rival -> rival.copy(name = displayName(rival.id, rival.name)) },
            )
        }
    }

    private fun withCombat(ui: CombatUiState, combat: CombatState): CombatUiState {
        val player = combat.player
        val match = Combos.find(player.buffer)
        return ui.copy(
            tick = combat.tick,
            rivals = combat.rivals.map { rival -> fighterUi(rival, combat, displayName(rival.id, rival.name)) },
            player = fighterUi(player, combat),
            buffer = player.buffer,
            bufferMatch = match,
            chainContinues = match != null && ChainRules.next(player.chain, match).second,
            playerChain = player.chain,
            outcome = combat.outcome,
            inputsEnabled = combat.outcome == null && !ui.paused && !player.isDazed(),
        )
    }

    private fun fighterUi(fighter: Fighter, combat: CombatState, name: String = fighter.name) = FighterUi(
        id = fighter.id,
        name = name,
        level = fighter.level,
        hp = fighter.hp,
        maxHp = fighter.stats.maxHp,
        power = if (fighter.isAlive) fighter.power else 0,
        maxPower = fighter.stats.maxPower,
        badges = if (fighter.isAlive) {
            badgesFor(fighter.effects, combat.tick, fighter.dazeChancePct(combat.tick))
        } else {
            emptyList()
        },
        isAlive = fighter.isAlive,
        isTarget = fighter.id == combat.targetId,
        queued = fighter.queued,
        attack = fighter.stats.attack,
        defense = fighter.stats.defense,
        heal = fighter.stats.heal,
        isBoss = fighter.isBoss,
        portrait = fighter.portrait,
        dazedTicksLeft = if (fighter.isAlive) dazedTicksLeft(fighter, combat.tick) else 0,
    )

    /**
     * Ticks until [fighter] can act again after [tick]: the Daze's remaining duration, plus any tick it still waits
     * beyond the next (a Daze pending for the next tick already counts it down); 0 when not Dazed.
     */
    private fun dazedTicksLeft(fighter: Fighter, tick: Int): Int =
        fighter.effects.filter { it.kind == EffectKind.DAZED }
            .maxOfOrNull { (it.activeFrom - tick - 1).coerceAtLeast(0) + it.remainingTicks } ?: 0

    private fun placeholderPlayer(level: Int): FighterUi {
        val stats = LevelStats.forLevel(level)
        return FighterUi(
            PLAYER_ID, "Warden", level, stats.maxHp, stats.maxHp, stats.maxPower, stats.maxPower,
            emptyList(), isAlive = true, isTarget = false,
            attack = stats.attack, defense = stats.defense, heal = stats.heal,
        )
    }
}
