package com.wardengambits.engine.story

data class ZoneInfo(val id: String, val name: String)

/** The fixed 24-zone route of the story (road to Mordor, spec 2026-10-16 §2). */
object Route {
    const val FINAL_ZONE_ID = "orodruin"

    /** Rest zones: full node trees without any encounter. */
    val REST_ZONES: Set<String> = setOf("rivendel", "lothlorien")

    val zones: List<ZoneInfo> = listOf(
        ZoneInfo("bree", "Bree"),
        ZoneInfo("chet", "Bosque de Chet"),
        ZoneInfo("moscagua", "Pantanos de Moscagua"),
        ZoneInfo("cima", "Cima de los Vientos"),
        ZoneInfo("puente", "Último Puente"),
        ZoneInfo("quebradas", "Quebradas de los Trolls"),
        ZoneInfo("bruinen", "Vado del Bruinen"),
        ZoneInfo("rivendel", "Rivendel"),
        ZoneInfo("eregion", "Eregion"),
        ZoneInfo("caradhras", "Caradhras"),
        ZoneInfo("durin", "Puertas de Durin"),
        ZoneInfo("moria", "Moria"),
        ZoneInfo("sombrio", "Valle del Arroyo Sombrío"),
        ZoneInfo("nimrodel", "Nimrodel"),
        ZoneInfo("lothlorien", "Lothlórien"),
        ZoneInfo("anduin", "Río Anduin"),
        ZoneInfo("argonath", "Sarn Gebir y los Argonath"),
        ZoneInfo("amonhen", "Amon Hen"),
        ZoneInfo("emynmuil", "Emyn Muil"),
        ZoneInfo("cienagas", "Ciénagas de los Muertos"),
        ZoneInfo("ithilien", "Ithilien"),
        ZoneInfo("cirithungol", "Valle de Morgul y Cirith Ungol"),
        ZoneInfo("gorgoroth", "Gorgoroth"),
        ZoneInfo(FINAL_ZONE_ID, "Monte del Destino"),
    )

    /** Every zone but the final one: the ones that have a node tree. */
    val treeZones: List<ZoneInfo> = zones.dropLast(1)

    /** Throws for an unknown id (programming error). */
    fun zone(id: String): ZoneInfo = zones[indexOf(id)]

    /** 0-based position in the route; throws for an unknown id. */
    fun indexOf(zoneId: String): Int {
        val index = zones.indexOfFirst { it.id == zoneId }
        require(index >= 0) { "Unknown zone id: $zoneId" }
        return index
    }
}
