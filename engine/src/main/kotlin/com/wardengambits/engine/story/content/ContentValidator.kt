package com.wardengambits.engine.story.content

import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.Route
import com.wardengambits.engine.story.TreeShape

/** Checks that a [StoryContent] is complete and playable; returns readable errors (empty means valid). */
object ContentValidator {
    private const val MIN_RIVALS = 1
    private const val MAX_RIVALS = 4

    fun validate(content: StoryContent): List<String> {
        val errors = mutableListOf<String>()
        val routeIds = Route.zones.map { it.id }.toSet()
        content.zones.map { it.id }.distinct()
            .filter { it !in routeIds }
            .forEach { errors += "$it: unexpected zone" }
        Route.zones.forEach { info ->
            val matching = content.zones.filter { it.id == info.id }
            when {
                matching.isEmpty() -> errors += "${info.id}: missing zone"
                else -> {
                    if (matching.size > 1) errors += "${info.id}: duplicated zone"
                    val zone = matching.first()
                    if (zone.name != info.name) {
                        errors += "${info.id}: name '${zone.name}' should be '${info.name}'"
                    }
                    errors += validateZone(zone)
                }
            }
        }
        return errors
    }

    /**
     * Checks one zone on its own (baseLevel and nodes), in or out of the route: a final zone is a single node (1, 0)
     * keyed `<id>_final`, any other zone is a full tree. Used by [validate] and for pending content.
     */
    fun validateZone(zone: ZoneContent, isFinal: Boolean = zone.id == Route.FINAL_ZONE_ID): List<String> {
        val errors = mutableListOf<String>()
        if (!isFinal && zone.baseLevel < 1) errors += "${zone.id}: baseLevel ${zone.baseLevel} must be >= 1"
        validateNodes(zone, isFinal, errors)
        return errors
    }

    private fun key(zone: ZoneContent, id: NodeId, isFinal: Boolean) =
        if (isFinal) "${zone.id}_final" else "${zone.id}_${id.level}_${id.index}"

    private fun validateNodes(zone: ZoneContent, isFinal: Boolean, errors: MutableList<String>) {
        val expected = if (isFinal) listOf(NodeId(zone.id, 1, 0)) else TreeShape.treeNodes(zone.id)
        val expectedPositions = expected.map { it.level to it.index }.toSet()
        val byPosition = zone.nodes.groupBy { it.level to it.index }
        expected.forEach { id ->
            val count = byPosition[id.level to id.index]?.size ?: 0
            if (count == 0) errors += "${key(zone, id, isFinal)}: missing node"
            if (count > 1) errors += "${key(zone, id, isFinal)}: duplicated node"
        }
        byPosition.keys.filter { it !in expectedPositions }.forEach { (level, index) ->
            errors += "${zone.id}_${level}_$index: node outside the tree shape"
        }
        zone.nodes.filter { (it.level to it.index) in expectedPositions }.forEach { node ->
            val id = NodeId(zone.id, node.level, node.index)
            validateNode(key(zone, id, isFinal), isFinal, node, errors)
            if (!isFinal) validateDistinctLabels(zone, id, errors)
        }
    }

    private fun validateDistinctLabels(zone: ZoneContent, id: NodeId, errors: MutableList<String>) {
        val destinations = TreeShape.nextInZone(id)
        if (destinations.size != 2) return
        val labels = destinations.map { dest -> zone.nodes.firstOrNull { it.level == dest.level && it.index == dest.index }?.choiceLabel }
        if (labels[0] != null && labels[0] == labels[1]) {
            errors += "${key(zone, id, false)}: both destinations share choiceLabel '${labels[0]}'"
        }
    }

    private fun validateNode(key: String, isFinal: Boolean, node: NodeContent, errors: MutableList<String>) {
        if (node.title.isBlank()) errors += "$key: missing title"
        if (node.text.isBlank()) errors += "$key: missing text"
        if (node.imagePrompt.isBlank()) errors += "$key: missing imagePrompt"
        if (!isFinal && node.level in 2..6 && node.choiceLabel.isNullOrBlank()) {
            errors += "$key: missing choiceLabel"
        }
        val encounter = node.encounter ?: return
        if (isFinal) {
            errors += "$key: the final node cannot have an encounter"
            return
        }
        if (encounter.rivals.size !in MIN_RIVALS..MAX_RIVALS) {
            errors += "$key: encounter needs $MIN_RIVALS to $MAX_RIVALS rivals, has ${encounter.rivals.size}"
        }
        encounter.rivals.forEachIndexed { i, rival ->
            if (rival.name.isBlank()) errors += "$key: rival $i has a blank name"
            if (rival.pattern.isEmpty()) errors += "$key: rival $i has an empty pattern"
            rival.pattern.filter { Combos.find(it) == null }
                .forEach { errors += "$key: rival $i uses unknown combo '$it'" }
            if (rival.everyNTicks < 1) errors += "$key: rival $i everyNTicks must be >= 1"
        }
    }
}
