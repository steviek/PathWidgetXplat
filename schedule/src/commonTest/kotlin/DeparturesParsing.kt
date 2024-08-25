internal fun parseDeparturesFromNwk(raw: String): String {
    val lines = raw.split("\n")
    return lines.filter { !it.trim().startsWith("-") }.joinToString(separator = "\n")
}

internal fun parseDeparturesToNwk(raw: String): String {
    val lines = raw.split("\n")
    return lines.filter { !it.trim().endsWith("-") }.joinToString(separator = "\n")
}

internal fun parseDeparturesFromJsq(raw: String): String {
    val lines = raw.split("\n")
    return lines
        .filter { it.trim().startsWith("-") }.joinToString(separator = "\n") { line ->
            val firstDigit = line.indexOfFirst { it.isDigit() }
            line.substring(startIndex = firstDigit)
        }
}

internal fun parseDeparturesToJsq(raw: String): String {
    val lines = raw.split("\n")
    return lines.filter { it.trim().endsWith("-") }.joinToString(separator = "\n")
}
