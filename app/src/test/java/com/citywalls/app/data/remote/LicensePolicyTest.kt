package com.citywalls.app.data.remote

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LicensePolicyTest {

    @Test
    fun acceptsCreativeCommonsAttribution() {
        val check = LicensePolicy.evaluate(
            licenseName = "CC BY 4.0",
            licenseCode = "cc-by-4.0",
            usageTerms = "",
            restrictions = "",
            categories = emptyList()
        )
        assertTrue(check.allowed)
    }

    @Test
    fun acceptsShareAlike() {
        val check = LicensePolicy.evaluate(
            licenseName = "CC BY-SA 4.0",
            licenseCode = "cc-by-sa-4.0",
            usageTerms = "",
            restrictions = "",
            categories = emptyList()
        )
        assertTrue(check.allowed)
    }

    @Test
    fun acceptsPublicDomain() {
        val check = LicensePolicy.evaluate(
            licenseName = "Public domain",
            licenseCode = "pd",
            usageTerms = "",
            restrictions = "",
            categories = emptyList()
        )
        assertTrue(check.allowed)
    }

    @Test
    fun acceptsCc0() {
        val check = LicensePolicy.evaluate(
            licenseName = "CC0",
            licenseCode = "cc0",
            usageTerms = "",
            restrictions = "",
            categories = emptyList()
        )
        assertTrue(check.allowed)
    }

    @Test
    fun rejectsNonCommercial() {
        val check = LicensePolicy.evaluate(
            licenseName = "CC BY-NC 4.0",
            licenseCode = "cc-by-nc-4.0",
            usageTerms = "",
            restrictions = "",
            categories = emptyList()
        )
        assertFalse(check.allowed)
    }

    @Test
    fun rejectsNoDerivatives() {
        val check = LicensePolicy.evaluate(
            licenseName = "CC BY-ND 4.0",
            licenseCode = "cc-by-nd-4.0",
            usageTerms = "",
            restrictions = "",
            categories = emptyList()
        )
        assertFalse(check.allowed)
    }

    @Test
    fun rejectsFairUse() {
        val check = LicensePolicy.evaluate(
            licenseName = "Fair use",
            licenseCode = "",
            usageTerms = "",
            restrictions = "",
            categories = emptyList()
        )
        assertFalse(check.allowed)
    }

    @Test
    fun rejectsMissingMetadata() {
        val check = LicensePolicy.evaluate(
            licenseName = "",
            licenseCode = "",
            usageTerms = "",
            restrictions = "",
            categories = emptyList()
        )
        assertFalse(check.allowed)
    }

    @Test
    fun rejectsAdditionalRestrictions() {
        val check = LicensePolicy.evaluate(
            licenseName = "CC BY-SA 3.0",
            licenseCode = "cc-by-sa-3.0",
            usageTerms = "",
            restrictions = "trademarked",
            categories = emptyList()
        )
        assertFalse(check.allowed)
    }

    @Test
    fun rejectsFreedomOfPanoramaCategory() {
        val check = LicensePolicy.evaluate(
            licenseName = "CC BY-SA 4.0",
            licenseCode = "cc-by-sa-4.0",
            usageTerms = "",
            restrictions = "",
            categories = listOf("Category:Freedom of panorama warnings")
        )
        assertFalse(check.allowed)
    }

    @Test
    fun rejectsUnrecognisedLicense() {
        val check = LicensePolicy.evaluate(
            licenseName = "Some rights reserved",
            licenseCode = "",
            usageTerms = "",
            restrictions = "",
            categories = emptyList()
        )
        assertFalse(check.allowed)
    }
}
