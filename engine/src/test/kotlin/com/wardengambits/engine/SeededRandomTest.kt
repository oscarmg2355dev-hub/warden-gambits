package com.wardengambits.engine

import com.wardengambits.engine.random.SeededRandom
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SeededRandomTest {
    @Test
    fun sameSeedSameSequence() {
        val a = SeededRandom(42)
        val b = SeededRandom(42)
        assertEquals(List(20) { a.nextInt(100) }, List(20) { b.nextInt(100) })
    }

    @Test
    fun stateResumesSequence() {
        val a = SeededRandom(7)
        repeat(5) { a.nextInt(100) }
        val b = SeededRandom(a.state)
        assertEquals(List(10) { a.nextInt(100) }, List(10) { b.nextInt(100) })
    }

    @Test
    fun valuesWithinBound() {
        val r = SeededRandom(1)
        repeat(1000) { assertTrue(r.nextInt(100) in 0..99) }
    }

    @Test
    fun differentSeedsDiffer() {
        val a = SeededRandom(1)
        val b = SeededRandom(2)
        assertNotEquals(List(10) { a.nextInt(1000) }, List(10) { b.nextInt(1000) })
    }
}
