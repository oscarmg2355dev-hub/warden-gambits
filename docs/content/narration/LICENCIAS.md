# Licencias de la narración

Voz sintetizada en local con `tools/narration/tts_generate.py` y el modelo **Kokoro-82M** (hexgrad,
https://huggingface.co/hexgrad/Kokoro-82M), con licencia **Apache 2.0**: uso comercial permitido. La app no
incluye el modelo, solo el audio generado. Voces: `ef_dora` (español, velocidad 0,9) y `af_heart` (inglés,
pronunciación americana, velocidad 0,9). La fonética española la calcula espeak-ng (vía `espeakng-loader`) al
generar; no se distribuye con la app.

Créditos en la app: «Acerca de» → «Créditos» (`about_credits_narration`), junto a la música (`about_credits_music`,
ver `docs/content/music/LICENCIAS.md`).
