package com.wardengambits.sim

import com.wardengambits.engine.model.Difficulty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class DifficultyOptionTest {
    @Test
    fun `simulator setups carry the difficulty attack percent`() {
        val content = com.wardengambits.engine.story.content.StoryContentLoader.loadFromResources()
        val story = com.wardengambits.engine.story.StoryEngine(content)
        val node = com.wardengambits.engine.story.TreeShape.nodesOf("bree").first { story.node(it).encounter != null }
        val profile = com.wardengambits.engine.story.StoryProfile(
            com.wardengambits.engine.story.RunState(
                1, 1, 1, 0, node, listOf(node), com.wardengambits.engine.story.Phase.COMBAT_PENDING,
            ),
            emptyList(),
        )
        assertEquals(50, Simulator(content, difficulty = Difficulty.SHIRE).combatSetup(profile).rivalAttackPct)
        assertEquals(100, Simulator(content).combatSetup(profile).rivalAttackPct)
    }

    @Test
    fun `option parsing is case-insensitive and optional`() {
        assertEquals(Difficulty.MARK, parseDifficulty(listOf("--fixed-roll", "--difficulty", "mark")))
        assertNull(parseDifficulty(listOf("--fixed-roll")))
        assertFailsWith<IllegalStateException> { parseDifficulty(listOf("--difficulty", "nope")) }
    }
}
