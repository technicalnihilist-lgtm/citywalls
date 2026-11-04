package com.citywalls.app.domain

enum class CatalogGroup(val key: String, val title: String, val subtitle: String) {
    CITIES("cities", "World Cities", "Twenty skylines, one collection"),
    ARCHITECTURE("architecture", "Architecture", "Glass, steel and stone"),
    PHOTOGRAPHY("photography", "City Photography", "Streets, weather and light"),
    LANDMARKS("landmarks", "Famous Landmarks", "Icons of the built world")
}

data class CatalogEntry(
    val key: String,
    val name: String,
    val groupKey: String,
    val searchQuery: String,
    val headerQuery: String,
    val blurb: String
)

/**
 * Static editorial catalog. Every entry maps to a Wikimedia Commons file search,
 * so the content itself is always live: nothing is bundled inside the app.
 */
object Catalog {

    const val ALL_GROUP = "all"

    private fun city(key: String, name: String, query: String, header: String, blurb: String) =
        CatalogEntry(
            key = key,
            name = name,
            groupKey = CatalogGroup.CITIES.key,
            searchQuery = query,
            headerQuery = header,
            blurb = blurb
        )

    private fun entry(
        key: String,
        name: String,
        group: CatalogGroup,
        query: String,
        blurb: String,
        header: String = query
    ) = CatalogEntry(
        key = key,
        name = name,
        groupKey = group.key,
        searchQuery = query,
        headerQuery = header,
        blurb = blurb
    )

    private val cities = listOf(
        city("tokyo", "Tokyo", "Tokyo skyline city", "Tokyo skyline night", "Neon grids above a quiet bay."),
        city("new-york", "New York", "New York City skyline", "New York City skyline night", "Vertical light on a rigid grid."),
        city("paris", "Paris", "Paris skyline rooftops", "Paris skyline sunset", "Zinc rooftops and pale stone."),
        city("london", "London", "London skyline Thames", "London skyline night", "Fog, river and iron bridges."),
        city("dubai", "Dubai", "Dubai skyline", "Dubai skyline night", "Desert air, impossible towers."),
        city("singapore", "Singapore", "Singapore skyline", "Singapore skyline night", "A garden wrapped around a city."),
        city("seoul", "Seoul", "Seoul skyline", "Seoul skyline night", "Mountains behind a river of light."),
        city("mumbai", "Mumbai", "Mumbai skyline", "Mumbai skyline sea", "Monsoon light on a crowded coast."),
        city("delhi", "Delhi", "Delhi city architecture", "Delhi cityscape", "Layers of empires in sandstone."),
        city("shanghai", "Shanghai", "Shanghai skyline Pudong", "Shanghai skyline night", "A skyline built in one generation."),
        city("hong-kong", "Hong Kong", "Hong Kong skyline", "Hong Kong skyline night", "Density pressed between hills and sea."),
        city("istanbul", "Istanbul", "Istanbul skyline Bosphorus", "Istanbul skyline sunset", "Two continents, one waterline."),
        city("rome", "Rome", "Rome cityscape", "Rome skyline sunset", "Ochre walls and umbrella pines."),
        city("barcelona", "Barcelona", "Barcelona city aerial", "Barcelona skyline", "A grid that runs into the sea."),
        city("amsterdam", "Amsterdam", "Amsterdam canal houses", "Amsterdam canal", "Brick, water and narrow windows."),
        city("sydney", "Sydney", "Sydney skyline harbour", "Sydney harbour skyline", "Harbour light and pale sandstone."),
        city("bangkok", "Bangkok", "Bangkok skyline", "Bangkok skyline night", "Heat haze over glass towers."),
        city("kyoto", "Kyoto", "Kyoto streets temples", "Kyoto temple street", "Timber, moss and quiet lanes."),
        city("venice", "Venice", "Venice canals", "Venice canal morning", "Stone reflected in still water."),
        city("los-angeles", "Los Angeles", "Los Angeles skyline", "Los Angeles skyline sunset", "Low light, long boulevards.")
    )

    private val architecture = listOf(
        entry("modern-architecture", "Modern Architecture", CatalogGroup.ARCHITECTURE, "modern architecture building", "Concrete, glass and clean edges."),
        entry("skyscrapers", "Skyscrapers", CatalogGroup.ARCHITECTURE, "skyscraper facade", "Height as a design language."),
        entry("historic-buildings", "Historic Buildings", CatalogGroup.ARCHITECTURE, "historic building architecture", "Stone that outlived its century."),
        entry("futuristic-buildings", "Futuristic Buildings", CatalogGroup.ARCHITECTURE, "futuristic architecture building", "Shapes that look forward."),
        entry("traditional-architecture", "Traditional Architecture", CatalogGroup.ARCHITECTURE, "traditional architecture building", "Craft passed down in wood and clay."),
        entry("luxury-buildings", "Luxury Buildings", CatalogGroup.ARCHITECTURE, "luxury architecture building", "Marble, brass and restraint."),
        entry("glass-buildings", "Glass Buildings", CatalogGroup.ARCHITECTURE, "glass facade building", "Reflections instead of walls."),
        entry("beautiful-houses", "Beautiful Houses", CatalogGroup.ARCHITECTURE, "modern house architecture", "Small buildings, careful details.")
    )

    private val photography = listOf(
        entry("night-city", "Night City", CatalogGroup.PHOTOGRAPHY, "city night", "After hours, the city changes tone."),
        entry("city-lights", "City Lights", CatalogGroup.PHOTOGRAPHY, "city lights long exposure", "Long exposures and moving colour."),
        entry("rainy-streets", "Rainy Streets", CatalogGroup.PHOTOGRAPHY, "rainy street city", "Wet asphalt holds the light."),
        entry("neon-streets", "Neon Streets", CatalogGroup.PHOTOGRAPHY, "neon signs street night", "Signage as architecture."),
        entry("aerial-city-views", "Aerial City Views", CatalogGroup.PHOTOGRAPHY, "aerial view city", "The grid, seen from above."),
        entry("rooftop-views", "Rooftop Views", CatalogGroup.PHOTOGRAPHY, "rooftop view city", "Above the noise line."),
        entry("city-skyline", "City Skyline", CatalogGroup.PHOTOGRAPHY, "city skyline", "The classic long view."),
        entry("sunset-city", "Sunset in the City", CatalogGroup.PHOTOGRAPHY, "sunset city skyline", "Warm hours on cold facades."),
        entry("sunrise-city", "Sunrise in the City", CatalogGroup.PHOTOGRAPHY, "sunrise city skyline", "Empty streets, early light."),
        entry("empty-streets", "Empty Streets", CatalogGroup.PHOTOGRAPHY, "empty street city", "Cities with nobody in them."),
        entry("busy-streets", "Busy Streets", CatalogGroup.PHOTOGRAPHY, "crowded street city", "Movement at eye level."),
        entry("city-reflections", "City Reflections", CatalogGroup.PHOTOGRAPHY, "city reflection water", "Two cities, one frame.")
    )

    private val landmarks = listOf(
        entry("eiffel-tower", "Eiffel Tower", CatalogGroup.LANDMARKS, "Eiffel Tower Paris", "Iron lace over the Champ de Mars."),
        entry("burj-khalifa", "Burj Khalifa", CatalogGroup.LANDMARKS, "Burj Khalifa Dubai", "The tallest silhouette in the world."),
        entry("tokyo-tower", "Tokyo Tower", CatalogGroup.LANDMARKS, "Tokyo Tower", "Red lattice against grey sky."),
        entry("statue-of-liberty", "Statue of Liberty", CatalogGroup.LANDMARKS, "Statue of Liberty", "Copper turned sea green."),
        entry("big-ben", "Big Ben", CatalogGroup.LANDMARKS, "Big Ben London", "Gothic detail on the Thames."),
        entry("taj-mahal", "Taj Mahal", CatalogGroup.LANDMARKS, "Taj Mahal Agra", "Marble that changes colour with the hour."),
        entry("sydney-opera-house", "Sydney Opera House", CatalogGroup.LANDMARKS, "Sydney Opera House", "Shells on the harbour edge."),
        entry("golden-gate-bridge", "Golden Gate Bridge", CatalogGroup.LANDMARKS, "Golden Gate Bridge", "Fog rolling under orange steel."),
        entry("colosseum", "Colosseum", CatalogGroup.LANDMARKS, "Colosseum Rome", "Two thousand years of wear."),
        entry("marina-bay-sands", "Marina Bay Sands", CatalogGroup.LANDMARKS, "Marina Bay Sands", "Three towers and a ship."),
    )

    val all: List<CatalogEntry> = cities + architecture + photography + landmarks

    fun group(groupKey: String): List<CatalogEntry> = all.filter { it.groupKey == groupKey }

    fun forGroup(groupKey: String): List<CatalogEntry> =
        if (groupKey == ALL_GROUP) all else group(groupKey)

    fun find(key: String): CatalogEntry? = all.firstOrNull { it.key == key }

    fun featuredCities(): List<CatalogEntry> = listOf("tokyo", "dubai", "new-york", "singapore", "paris", "hong-kong")
        .mapNotNull { find(it) }

    fun relatedFor(entry: CatalogEntry): List<CatalogEntry> {
        val pool = architecture + photography
        if (pool.isEmpty()) return emptyList()
        val start = (entry.key.hashCode() and Int.MAX_VALUE) % pool.size
        return (0 until 4)
            .map { offset -> pool[(start + offset * 3) % pool.size] }
            .distinct()
            .filter { it.key != entry.key }
    }
}
