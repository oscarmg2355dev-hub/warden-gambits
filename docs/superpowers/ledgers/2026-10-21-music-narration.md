# SDD ledger — plan: docs/superpowers/plans/2026-10-21-music-narration.md

Spec: docs/superpowers/specs/2026-10-21-music-narration-design.md
Ruling: no commits; diffs between working-tree snapshots (audio binaries excluded from review diffs) — project rule; cost if wrong: no commit messages.
Base snapshot: 0228a07c9540cf9f68328ff71d6ae7e183456908

## Pre-flight scan
| Pair / task | Produces vs consumes | Finding |
|---|---|---|
| T2→T3 | musicEnabled → MusicController | consistent |
| T2→T4 | narrationEnabled → NarrationController | consistent |
| T3↔T4 | MusicController.update(narrating) ← NarrationPlayer.state | T3 wires narrating=false until T4; consistent |
| T2/T3/T4 | all edit AppContainer, WardenNavHost, strings.xml | sequential; CRLF warned |
| T1 self | tests use temp dirs, not docs/assets | consistent |
| T3 self | web music sourcing needs licence verification | ruling: implementer records URL+licence; controller checks LICENCIAS.md |
| T4 self | manual step uses a temporary test audio that must be removed | consistent |
Task 1 snapshot: 6626c5c327c4e795289449699f02ff8219de9faa
Task 1: minor (deferred): vacuous assertion test_narration.py:73; mtime-only skip (bitrate change needs --force, note in usage); tools/narration/__pycache__ not gitignored.
Task 1: complete (review clean)
Task 2 snapshot: 7786b27ad75822713a981c7223a641abd05be873
Task 2: ⚠️ CRLF verified by controller (both strings.xml CRLF).
Task 2: minor (deferred): stale KDoc in GatedSoundPlayer ("Sonido"); vacuous "Sonido: sí" assertion in MenuScreenTest; no direct test explicit sfx over legacy.
Task 2: complete (review clean)
Task 3 snapshot: 1651cf1cbe827f126de33291c329146d29b91894
Ruling: music total 2.5 MB (< 5–8 MB target) accepted — spec range was an estimate, smaller is better; cost if wrong: short loops repeat more often.
Task 3: licence verified by controller from LICENCIAS.md (3× RandomMind CC0, OpenGameArt URLs).
Task 3: minor (deferred): onTrackEnded lacks released check / no retry if next track fails; no OnErrorListener/audio focus; musicEnabled initial true before DataStore; onTerminate dead code; import order; no pause→play test; WAV originals 33 MB in docs (consider FLAC).
Task 3: complete (review clean)
Task 4 snapshot: 2c8651bfc1ad1e257cabea0a77a0930b4ee99f87
Task 4: strings.xml CRLF re-verified by controller (implementer's LF note was wrong).
Ruling: Leer/Parar button shown when audio exists even with Narración off (only auto-play obeys the toggle) — lets the player read on demand; cost if wrong: one visible button with narration disabled.
Task 4: minor (deferred): languageChangeStops test passes without en audio (weak); global onLeave on ON_STOP/dispose could silence a screen entered during transition (latent); failed assets.list cached; hasAudio on main thread first call; one-frame «Leer» flash; import order; NarrationController.release never called.
Task 4: controller manual check on emulator with a temporary Windows-TTS test audio converted by convert.py: narration auto-starts on bree_1_0 (mono 24 kHz player started next to music), header button «■ Parar» → «▶ Leer» at the end, music started in menu, paused in combat (combat ambience playing), resumed after; test audio removed, pending restored.
Task 4: complete (review clean)
Final snapshot: 980338362fbcd398232dcf8f711a7043e01bbc6e
Final review: with fixes. Important 3 (music in background) verified by controller on emulator: Home → paused, back → started, screen off → paused, on → started. No code change needed.
Ruling: user narration source audio (docs/content/narration/<es|en>/*.{wav,mp3,m4a,ogg,flac}) is gitignored — ~1.8 GB of WAV would bloat the public repo permanently; only the compressed app assets are committed; cost if wrong: sources live only on the user's PC (they can be regenerated from prompts).
Ruling: music originals stored as FLAC instead of WAV (lossless, ~half size); cost if wrong: none.
Final fix snapshot: 8ef3776c5d4867bb0df9a26eec8659b5609f2c38
Final: fix wave addressed; parked minors: weak tests noNarrationUntilTheSettingHasLoaded / languageChangeStops naming, MenuScreenTest name, LICENCIAS wording. Controller: .venv-tts/.venv-kokoro (uv venvs, unknown origin) left in place, .venv*/ gitignored. Full verification green, AAB 73.0 MB without narration.
