package com.pavi2410.appupdater

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VersionComparatorTest {

    @Test
    fun olderVersionIsLessThanNewer() {
        assertTrue(VersionComparator.compare("0.2.0", "0.3.1") < 0)
    }

    @Test
    fun newerVersionIsGreaterThanOlder() {
        assertTrue(VersionComparator.compare("1.0.0", "0.9.9") > 0)
    }

    @Test
    fun sameVersionsAreEqual() {
        assertEquals(0, VersionComparator.compare("1.0.0", "1.0.0"))
    }

    @Test
    fun twoSegmentEqualsThreeSegment() {
        assertEquals(0, VersionComparator.compare("1.0", "1.0.0"))
    }

    @Test
    fun handlesVPrefix() {
        assertTrue(VersionComparator.compare("v0.2.0", "v0.3.1") < 0)
    }

    @Test
    fun handlesMixedVPrefix() {
        assertEquals(0, VersionComparator.compare("v1.0.0", "1.0.0"))
    }

    @Test
    fun fourSegmentVersions() {
        assertTrue(VersionComparator.compare("1.0.0.0", "1.0.0.1") < 0)
    }

    @Test
    fun majorVersionDifference() {
        assertTrue(VersionComparator.compare("1.9.9", "2.0.0") < 0)
    }

    @Test
    fun isNewerVersionReturnsTrueWhenLatestIsNewer() {
        assertTrue(VersionComparator.isNewerVersion("0.2.0", "0.3.1"))
    }

    @Test
    fun isNewerVersionReturnsFalseWhenSame() {
        assertFalse(VersionComparator.isNewerVersion("1.0.0", "1.0.0"))
    }

    @Test
    fun isNewerVersionReturnsFalseWhenCurrentIsNewer() {
        assertFalse(VersionComparator.isNewerVersion("2.0.0", "1.0.0"))
    }

    @Test
    fun singleSegment() {
        assertTrue(VersionComparator.compare("1", "2") < 0)
    }
}
