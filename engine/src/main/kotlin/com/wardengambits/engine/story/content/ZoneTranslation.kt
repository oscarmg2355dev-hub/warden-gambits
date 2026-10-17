package com.wardengambits.engine.story.content

import kotlinx.serialization.Serializable

/**
 * Text overlay of one zone in another language (languages spec §4): `story/<language>/<zone>.json`.
 *
 * ```json
 * {
 *   "id": "bree",
 *   "name": "Bree",
 *   "nodes": {
 *     "bree_1_0": { "title": "…", "text": "…" },
 *     "bree_2_0": { "title": "…", "text": "…", "choiceLabel": "…" }
 *   },
 *   "rivals": { "Ratero de Bree": "Bree Cutpurse" }
 * }
 * ```
 *
 * - [id]: the zone id, same as the file name and the Spanish file.
 * - [name]: the translated zone name.
 * - [nodes]: by node key (`<zone>_<level>_<index>`, or `orodruin_final` for the final node); `choiceLabel`
 *   only where the Spanish node has one.
 * - [rivals]: Spanish rival name → translated name, once per distinct name in the zone.
 *
 * Only texts live here: mechanics (encounters, patterns, offsets, modes, reactions) and image prompts always
 * come from the Spanish file.
 */
@Serializable
data class ZoneTranslation(
    val id: String,
    val name: String,
    val nodes: Map<String, NodeTranslation>,
    val rivals: Map<String, String> = emptyMap(),
)

@Serializable
data class NodeTranslation(val title: String, val text: String, val choiceLabel: String? = null)
