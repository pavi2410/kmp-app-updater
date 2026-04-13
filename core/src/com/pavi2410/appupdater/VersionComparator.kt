package com.pavi2410.appupdater

object VersionComparator {
    /**
     * Compares two semantic version strings.
     * Returns negative if [current] < [other], positive if [current] > [other], 0 if equal.
     * Handles "v" prefix, 2/3/4-segment versions (e.g. "1.0", "1.0.0", "1.0.0.1").
     */
    fun compare(current: String, other: String): Int {
        val currentParts = normalize(current)
        val otherParts = normalize(other)

        val maxLength = maxOf(currentParts.size, otherParts.size)
        for (i in 0 until maxLength) {
            val c = currentParts.getOrElse(i) { 0 }
            val o = otherParts.getOrElse(i) { 0 }
            if (c != o) return c.compareTo(o)
        }
        return 0
    }

    /**
     * Returns true if [latest] is newer than [current].
     */
    fun isNewerVersion(current: String, latest: String): Boolean {
        return compare(current, latest) < 0
    }

    private fun normalize(version: String): List<Int> {
        return version
            .removePrefix("v")
            .split(".")
            .mapNotNull { it.toIntOrNull() }
    }
}
