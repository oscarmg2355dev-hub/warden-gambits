# Story Content Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Author the 15 real story JSON files (309 nodes) and the tests that enforce the content rules, plus an image-prompt list for the user.

**Architecture:** Content lives in `engine/src/main/resources/story/<zoneId>.json` (format: story-engine spec §4). Test-side helpers (`ContentRules`, `Bestiary`) check spec §5 rules per zone; `RealContentTest` runs them for every zone listed in `WRITTEN_ZONES`, which each batch extends. The last task checks the full set through the real loader + validator.

**Tech Stack:** JSON content, Kotlin tests (kotlin.test/JUnit 5) in `:engine`.

**Spec:** `docs/superpowers/specs/2026-10-08-story-content-design.md` (format: `docs/superpowers/specs/2026-10-08-story-engine-design.md` §4)

## Global Constraints

- Content follows the content spec verbatim: §2 style (Spanish second person present, 60–120 words target, original text, no book quotes, canon untouched), §3 bestiary (exact pattern/everyNTicks), §4 zone baseLevel `1 + 3·(k−1)` and commons, §5 encounter rules.
- `imagePrompt` in English ending with exactly `, painterly dark fantasy illustration, Middle-earth, muted earthy palette, 2D game card art, no text`.
- JSON files UTF-8 without BOM, 2-space indentation, nodes ordered by level then index, keys exactly as story-engine spec §4 (no extra keys).
- Tests: `./gradlew :engine:test` with `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"`. No git.
- Test helpers live only in `engine/src/test/kotlin/com/wardengambits/engine/story/`; no main-code changes in this plan.

## Review Focus

1. A choiceLabel that only makes sense from one parent (spec §2) — reviewers read every level 5 and 6 label against all its parents. (Tasks 2–6)
2. Canon breaks: the Warden killing/joining canon characters or defeating the Balrog; quotations from the books. (Tasks 2–6)
3. Continuity across batches: level-1 text of zone k+1 follows the level-7 outcomes of zone k (any of them). (Tasks 3–6)
4. A JSON with a BOM or wrong id fails loudly — covered by existing loader tests; `RealContentTest` parses via `StoryContentLoader.parse`. (Task 1)
5. Rivendel's 0-combat rule and Lothlórien's single node are exceptions the helpers must not reject. (Task 1)

---

### Task 1: Content rule helpers + RealContentTest harness

**Files:**
- Create: `engine/src/test/kotlin/com/wardengambits/engine/story/ContentRules.kt`
- Create: `engine/src/test/kotlin/com/wardengambits/engine/story/ContentRulesTest.kt`
- Create: `engine/src/test/kotlin/com/wardengambits/engine/story/RealContentTest.kt`

**Interfaces:**
- Produces:
  - `data class BeastEntry(val name: String, val pattern: List<String>, val everyNTicks: Int)`
  - `object Bestiary { val common: Map<String, BeastEntry>; val bosses: Map<String, BeastEntry> /* zoneId → boss */; val zoneCommons: Map<String, Set<String>> /* zoneId → keys of common */ }` — keys for common: `bandido, lobo, moscardones, espia, huargo, crebain, loboSaruman, trasgo, orcoCapitan, trolCavernas, orcoMontanas, arquero`; all values verbatim from content spec §3 tables and §4 "Comunes" column.
  - `object ContentRules { const val PROMPT_SUFFIX: String; fun expectedBaseLevel(zoneId: String): Int; fun combatCounts(zone: ZoneContent): Set<Int>; fun errors(zone: ZoneContent): List<String> }`
    - `combatCounts`: over every path from `(zone,1,0)` to `(zone,7,0)` following `TreeShape.destinations` within the zone, the set of "number of nodes with encounter".
    - `errors`: all spec §5 rules 1–7 for one zone (lothlorien: only baseLevel 0, single node, no encounter, text 100–160 words, prompt suffix). Word count = `text.trim().split(Regex("\\s+")).size`. A rival matches the bestiary when `pattern == entry.pattern && everyNTicks == entry.everyNTicks`. Each error string starts with the node key or zone id.
  - `RealContentTest`: `val WRITTEN_ZONES = listOf<String>()`; `@Test fun writtenZonesAreValid()` — for each id: read `/story/$id.json` (UTF-8), `StoryContentLoader.parse("$id.json", text)`, assert `zone.id == id`, then assert empty: `ContentValidator.validate(StoryContent(listOf(zone)))` filtered to messages starting with `"$id:"` or `"${id}_"`, plus `ContentRules.errors(zone)`. Failure message lists all errors.

- [ ] **Step 1: Failing tests (`ContentRulesTest`, using `fakeContent` from `StoryFixtures.kt`)**

```kotlin
@Test fun bestiaryTables() { assertEquals(12, Bestiary.common.size); assertEquals(13, Bestiary.bosses.size); assertEquals(BeastEntry("Trol de piedra", listOf("11", "1212", "12121"), 3), Bestiary.bosses["quebradas"]); assertEquals(setOf("trasgo", "orcoCapitan", "trolCavernas"), Bestiary.zoneCommons["moria"]); assertEquals(emptySet(), Bestiary.zoneCommons["rivendel"]) }
@Test fun baseLevels() { assertEquals(1, ContentRules.expectedBaseLevel("bree")); assertEquals(40, ContentRules.expectedBaseLevel("nimrodel")); assertEquals(0, ContentRules.expectedBaseLevel("lothlorien")) }
@Test fun combatCountsNoEncounters() = assertEquals(setOf(0), ContentRules.combatCounts(fakeContent().zone("bree")!!))
@Test fun combatCountsMixed() { /* encounters on bree_7_0 and bree_4_0 → {1, 2} */ }
@Test fun validZonePasses() { /* build a bree zone via fakeContent with: baseLevel 1; encounters: bree_7_0 = boss(bree) offset +2; every level-3 node one "bandido" offset 0; texts of 60 words; prompts ending with suffix → errors empty */ }
@Test fun detectsEachRule() { /* starting from the valid zone, one mutation each → at least one error containing the node key: encounter on level 1; level-7 without boss; boss not first; level-3 single rival offset 2; two rivals offset 0; unknown pattern; common not of the zone (huargo in bree); path with 5 combats (encounters on all levels 2..7) → zone error; baseLevel 2; prompt without suffix; text 20 words */ }
@Test fun rivendelZeroCombatsAllowed() { /* rivendel zone without encounters, baseLevel 22, valid texts/prompts → no errors; any encounter → error */ }
@Test fun lothlorienRules() { /* single FINAL node, 120 words, suffix, baseLevel 0 → no errors; 80 words → error */ }
```

  (Helpers to build such zones — e.g. `fun zoneWith(id, encounters, words)` — go in `ContentRulesTest.kt`; fakeContent texts are too short, so override `text`/`imagePrompt` via `copy`.)
- [ ] **Step 2–4:** RED; implement; GREEN (`RealContentTest` passes trivially with an empty list).

---

### Tasks 2–6: Content batches

Each batch task writes 3 files (Task 6 also finishes the plan). Same procedure for every batch:

**Files:**
- Create: `engine/src/main/resources/story/<id>.json` for each zone of the batch
- Modify: `RealContentTest.kt` — append the batch ids to `WRITTEN_ZONES`

**Inputs for the writer:** the content spec (all sections), the story-engine spec §3–§4 (tree shape, JSON format), and — for continuity — the level-7 nodes of the previous zone (from the already written file).

- [ ] **Step 1:** Append the batch ids to `WRITTEN_ZONES`; run `./gradlew :engine:test --tests "*RealContentTest*"` → FAIL (missing resource) — RED evidence.
- [ ] **Step 2:** Design each zone's encounter map first (which nodes fight, rivals, offsets) so that `combatCounts` ⊆ 2..4 (rivendel: {0}); then write all 22 nodes per zone (title, text, imagePrompt, choiceLabel on levels 2–6, encounter).
- [ ] **Step 3:** Run `./gradlew :engine:test --tests "*RealContentTest*"` → PASS; then the full `./gradlew :engine:test` → PASS.
- [ ] **Step 4:** Self-review against Review Focus 1–3 (re-read every level 5–6 label against all its parents).

| Task | Zones |
|---|---|
| 2 | bree, chet, moscagua |
| 3 | cima, puente, quebradas |
| 4 | bruinen, rivendel, eregion |
| 5 | caradhras, durin, moria |
| 6 | sombrio, nimrodel, lothlorien |

**Task 6 extra steps:**
- [ ] Add `@Test fun allContentLoadsAndValidates()` to `RealContentTest`: `ContentValidator.validate(StoryContentLoader.loadFromResources())` is empty and `WRITTEN_ZONES` equals all 15 `Route.zones` ids in order. RED before the last files exist, GREEN after.
- [ ] Generate `docs/content/prompts-imagenes.md` (Spanish header, one row per node in route order: `| archivo | prompt |` with `archivo` = `<node key>.webp`) with a one-off script (e.g. Python reading the 15 JSON files); do not add the script to the project.
- [ ] Update `CLAUDE.md` "Estado": contenido completo (15 zonas, 309 nodos), imágenes pendientes del usuario (`docs/content/prompts-imagenes.md`), siguiente: app Android.
- [ ] Full `./gradlew :engine:build` → BUILD SUCCESSFUL, no `w:` lines.
