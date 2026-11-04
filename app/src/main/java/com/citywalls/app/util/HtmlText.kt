package com.citywalls.app.util

/** Wikimedia metadata values arrive as HTML fragments. Strip them for display. */
object HtmlText {

    private val tagRegex = Regex("<[^>]*>")
    private val numericEntity = Regex("&#(\\d+);")
    private val whitespace = Regex("\\s+")

    private val entities = mapOf(
        "&amp;" to "&",
        "&lt;" to "<",
        "&gt;" to ">",
        "&quot;" to "\"",
        "&apos;" to "'",
        "&#39;" to "'",
        "&nbsp;" to " "
    )

    fun clean(input: String?): String {
        if (input.isNullOrBlank()) return ""
        var text = tagRegex.replace(input, " ")
        entities.forEach { (key, value) -> text = text.replace(key, value) }
        text = numericEntity.replace(text) { match ->
            match.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: " "
        }
        return whitespace.replace(text, " ").trim()
    }
}
