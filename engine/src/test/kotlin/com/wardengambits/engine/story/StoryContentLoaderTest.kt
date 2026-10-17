package com.wardengambits.engine.story

import com.wardengambits.engine.story.content.RivalContent
import com.wardengambits.engine.story.content.StoryContent
import com.wardengambits.engine.story.content.StoryContentLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StoryContentLoaderTest {
    private fun resource(path: String): String =
        checkNotNull(javaClass.getResourceAsStream("/$path")) { "Missing test resource $path" }
            .use { String(it.readBytes(), Charsets.UTF_8) }

    private fun sample() = StoryContentLoader.parse("bree.json", resource("story-sample/bree.json"))

    @Test
    fun parsesSample() {
        val z = sample()
        assertEquals("bree", z.id)
        assertEquals(2, z.nodes.size)
        assertEquals(RivalContent("Bandido de Bree", "bandit", 0, listOf("12", "21"), 2), z.nodes[1].encounter!!.rivals.single())
        assertNull(z.nodes[0].choiceLabel)
        assertEquals("Entrar en la posada", z.nodes[1].choiceLabel)
    }

    @Test
    fun defaultsApplied() {
        val json = """{"id":"x","name":"X","baseLevel":1,"nodes":[{"level":1,"index":0,"title":"t","text":"t","imagePrompt":"p",
            "encounter":{"rivals":[{"name":"R","portrait":"bandit","pattern":["1"]}]}}]}"""
        val rival = StoryContentLoader.parse("x.json", json).nodes.single().encounter!!.rivals.single()
        assertEquals(0, rival.levelOffset)
        assertEquals(1, rival.everyNTicks)
    }

    @Test
    fun bomIsStripped() {
        val zone = StoryContentLoader.parse("bree.json", "﻿" + resource("story-sample/bree.json"))
        assertEquals("bree", zone.id)
    }

    @Test
    fun accentedTitleSurvives() {
        val json = "﻿" + """{"id":"x","name":"X","baseLevel":1,"nodes":[{"level":1,"index":0,"title":"Último Puente","text":"t","imagePrompt":"p"}]}"""
        assertEquals("Último Puente", StoryContentLoader.parse("x.json", json).nodes.single().title)
    }

    @Test
    fun idMismatchRejected() {
        val zone = sample()
        val e = assertFailsWith<IllegalStateException> { StoryContentLoader.checkId("chet.json", "chet", zone) }
        assertTrue("chet.json" in e.message!! && "'bree'" in e.message!!)
        StoryContentLoader.checkId("bree.json", "bree", zone)
    }

    @Test
    fun malformedNamesFile() {
        val e = assertFailsWith<IllegalStateException> { StoryContentLoader.parse("moria.json", "{ not json") }
        assertTrue("moria.json" in e.message!!)
    }

    @Test
    fun unknownKeyRejected() {
        assertFailsWith<IllegalStateException> {
            StoryContentLoader.parse("x.json", """{"id":"x","name":"X","baseLevel":1,"nodes":[],"extra":1}""")
        }
    }

    @Test
    fun missingResourceNamesFile() {
        val e = assertFailsWith<IllegalStateException> { StoryContentLoader.loadFromResources("story-sample") }
        assertTrue("story-sample/chet.json" in e.message!!)
    }

    @Test
    fun loadZoneReadsOneFile() {
        assertEquals(sample(), StoryContentLoader.loadZone("story-sample", "bree"))
        val e = assertFailsWith<IllegalStateException> { StoryContentLoader.loadZone("story-sample", "chet") }
        assertTrue("story-sample/chet.json" in e.message!!)
    }

    @Test
    fun contentLookup() {
        val z = sample()
        val content = StoryContent(listOf(z))
        assertEquals(z, content.zone("bree"))
        assertNull(content.zone("chet"))
        assertEquals(z.nodes[1].title, content.node(NodeId("bree", 2, 1))?.title)
        assertNotNull(content.node(NodeId("bree", 1, 0)))
        assertNull(content.node(NodeId("bree", 3, 0)))
        assertNull(content.node(NodeId("chet", 1, 0)))
    }

    @Test
    fun missingPortraitFailsToLoad() {
        val json = """{"id":"x","name":"X","baseLevel":1,"nodes":[{"level":1,"index":0,"title":"t","text":"t","imagePrompt":"p",
            "encounter":{"rivals":[{"name":"R","pattern":["1"]}]}}]}"""
        val e = assertFailsWith<IllegalStateException> { StoryContentLoader.parse("x.json", json) }
        assertTrue(e.cause is kotlinx.serialization.SerializationException, "cause was ${e.cause}")
        assertTrue("portrait" in e.message!!)
    }

    @Test
    fun englishOverlayTranslatesTexts() {
        val es = sample()
        val en = StoryContentLoader.loadZone("story-sample", "bree", language = "en")
        assertEquals("The Gates of Bree", en.nodes[0].title)
        assertEquals("You reach Bree at nightfall, your cloak soaked through by the rain.", en.nodes[0].text)
        assertNull(en.nodes[0].choiceLabel)
        assertEquals("Enter the inn", en.nodes[1].choiceLabel)
        assertEquals("Bree Bandit", en.nodes[1].encounter!!.rivals.single().name)
        // Mechanics and image prompts stay those of the Spanish file.
        assertEquals(es.nodes[1].encounter!!.rivals.single().copy(name = "Bree Bandit"), en.nodes[1].encounter!!.rivals.single())
        assertEquals(es.nodes.map { it.imagePrompt }, en.nodes.map { it.imagePrompt })
        assertEquals(es.baseLevel, en.baseLevel)
    }

    @Test
    fun spanishIsTheDefaultLanguage() {
        assertEquals(sample(), StoryContentLoader.loadZone("story-sample", "bree", language = "es"))
    }

    @Test
    fun missingTranslationFallsBackToSpanish() {
        assertEquals(sample(), StoryContentLoader.loadZone("story-sample", "bree", language = "fr"))
    }

    @Test
    fun translationParsesAndRejectsUnknownKeys() {
        val t = StoryContentLoader.parseTranslation("bree.json", resource("story-sample/en/bree.json"))
        assertEquals("bree", t.id)
        assertEquals("Bree Bandit", t.rivals["Bandido de Bree"])
        assertFailsWith<IllegalStateException> {
            StoryContentLoader.parseTranslation("x.json", """{"id":"x","name":"X","nodes":{},"extra":1}""")
        }
    }

    @Test
    fun finalNodeUsesFinalKey() {
        val zone = StoryContentLoader.parse("orodruin.json", """{"id":"orodruin","name":"Monte","baseLevel":0,"nodes":[
            {"level":1,"index":0,"title":"t","text":"x","imagePrompt":"p"}]}""")
        val t = StoryContentLoader.parseTranslation("orodruin.json", """{"id":"orodruin","name":"Mount Doom",
            "nodes":{"orodruin_final":{"title":"The End","text":"Epilogue"}}}""")
        val en = StoryContentLoader.translate(zone, t)
        assertEquals("Mount Doom", en.name)
        assertEquals("The End", en.nodes.single().title)
    }

    @Test
    fun realEnglishContentKeepsSpanishMechanics() {
        val es = StoryContentLoader.loadFromResources()
        val en = StoryContentLoader.loadFromResources(language = "en")
        assertEquals(es.zones.map { it.id }, en.zones.map { it.id })
        es.zones.zip(en.zones).forEach { (zs, ze) ->
            assertEquals(zs.baseLevel, ze.baseLevel)
            assertEquals(zs.nodes.size, ze.nodes.size)
            zs.nodes.zip(ze.nodes).forEach { (ns, ne) ->
                assertEquals(ns.level to ns.index, ne.level to ne.index)
                assertEquals(ns.imagePrompt, ne.imagePrompt)
                assertEquals(ns.choiceLabel == null, ne.choiceLabel == null, "choiceLabel presence at ${zs.id} ${ns.level}/${ns.index}")
                val rs = ns.encounter?.rivals.orEmpty()
                val re = ne.encounter?.rivals.orEmpty()
                assertEquals(rs.map { it.copy(name = "") }, re.map { it.copy(name = "") }, "mechanics at ${zs.id} ${ns.level}/${ns.index}")
            }
        }
        val bree = en.zone("bree")!!
        val breeEs = es.zone("bree")!!
        assertTrue(bree.nodes.zip(breeEs.nodes).all { (e, s) -> e.text != s.text }, "bree texts must be translated")
    }
}
