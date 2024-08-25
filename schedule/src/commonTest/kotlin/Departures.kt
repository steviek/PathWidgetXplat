import kotlinx.datetime.LocalTime

internal interface Departures {
    val nwkWtc: String

    val wtcNewark: String

    val jsq33s: String get() = ""

    val s33Jsq: String get() = ""

    val jsqHob33s: String

    val s33HobJsq: String

    val hob33s: String get() = ""

    val s33Hob: String get() = ""

    val hobWtc: String get() = ""

    val wtcHob: String get() = ""

    val wtcJsq: String get() = ""

    val jsqWtc: String get() = ""

    val nwkHar: String get() = ""

    val harNwk: String get() = ""

    val firstSlowDepartureTime: LocalTime? get() = null

    val lastSlowDepartureTime: LocalTime? get() = null
}
