package com.citywalls.app.data.remote

data class LicenseCheck(val allowed: Boolean, val reason: String)

/**
 * The single gate every image must pass before it is shown in CityWalls.
 *
 * Allowed: Public Domain, CC0, CC BY (all versions), CC BY-SA (all versions).
 * Blocked: anything non-commercial or no-derivatives, non-free / fair use files,
 * files with additional restrictions, files in Commons maintenance categories
 * (freedom-of-panorama warnings, deletion requests, missing permission) and any
 * license that cannot be positively identified.
 */
object LicensePolicy {

    private val ccPattern = Regex("\\bcc[\\s-]?by([\\s-]?sa)?[\\s-]?\\d(\\.\\d)?", RegexOption.IGNORE_CASE)
    private val ccZeroPattern = Regex("\\bcc[\\s-]?(zero|0)\\b", RegexOption.IGNORE_CASE)
    private val publicDomainPattern = Regex("public\\s*domain", RegexOption.IGNORE_CASE)
    private val pdCodePattern = Regex("^pd([-_ ].*)?$", RegexOption.IGNORE_CASE)

    private val blockedCategoryHints = listOf(
        "non-free",
        "nonfree",
        "fair use",
        "copyright violations",
        "freedom of panorama",
        "deletion requests",
        "no license",
        "missing permission",
        "possibly unfree"
    )

    private val blockedTextHints = listOf(
        "non-commercial",
        "noncommercial",
        "non commercial",
        "-nc",
        " nc ",
        "no derivative",
        "noderiv",
        "-nd",
        " nd ",
        "fair use",
        "non-free",
        "nonfree",
        "all rights reserved",
        "copyrighted"
    )

    fun evaluate(
        licenseName: String,
        licenseCode: String,
        usageTerms: String,
        restrictions: String,
        categories: List<String>
    ): LicenseCheck {
        val haystack = listOf(licenseName, licenseCode, usageTerms)
            .joinToString(" ")
            .lowercase()
            .trim()

        if (haystack.isBlank()) return LicenseCheck(false, "missing license metadata")

        val blockedCategory = categories.firstOrNull { category ->
            val lower = category.lowercase()
            blockedCategoryHints.any { lower.contains(it) }
        }
        if (blockedCategory != null) {
            return LicenseCheck(false, "restricted Commons category: " + blockedCategory)
        }

        blockedTextHints.forEach { hint ->
            if (haystack.contains(hint)) {
                return LicenseCheck(false, "blocked license term: " + hint.trim())
            }
        }

        if (restrictions.isNotBlank()) {
            return LicenseCheck(false, "additional restrictions: " + restrictions)
        }

        val isPublicDomain = publicDomainPattern.containsMatchIn(haystack) ||
            pdCodePattern.matches(licenseCode.trim()) ||
            ccZeroPattern.containsMatchIn(haystack)

        if (isPublicDomain) return LicenseCheck(true, "public domain or CC0")

        if (ccPattern.containsMatchIn(haystack)) {
            return LicenseCheck(true, "creative commons attribution")
        }

        return LicenseCheck(false, "unrecognised license")
    }

    /** Fallback only: Commons normally supplies LicenseUrl in its metadata. */
    fun licenseUrlFor(code: String, name: String): String {
        val normalized = code.lowercase().trim()
        return when {
            normalized.startsWith("cc0") || normalized == "cc-zero" ->
                "https://creativecommons.org/publicdomain/zero/1.0/"
            normalized.startsWith("pd") || name.contains("public domain", true) ->
                "https://commons.wikimedia.org/wiki/Commons:Public_domain"
            normalized.startsWith("cc-by") -> {
                val segments = normalized.removePrefix("cc-").split("-")
                val kind = segments
                    .filter { it == "by" || it == "sa" || it == "nc" || it == "nd" }
                    .joinToString("-")
                val version = segments.lastOrNull { it.firstOrNull()?.isDigit() == true } ?: "4.0"
                "https://creativecommons.org/licenses/" + kind + "/" + version + "/"
            }
            else -> "https://commons.wikimedia.org/wiki/Commons:Licensing"
        }
    }
}
