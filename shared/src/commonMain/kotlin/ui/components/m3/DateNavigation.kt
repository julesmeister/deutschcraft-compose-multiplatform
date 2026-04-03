package ui.components.m3

/** Step a 1-based month (Jan=1..Dec=12) by [delta], returning (newMonth, newYear). */
fun stepMonth(month: Int, year: Int, delta: Int): Pair<Int, Int> {
    // Convert to 0-based, add delta, then convert back
    val total = (month - 1) + delta
    val m = (total % 12).let { if (it < 0) it + 12 else it } + 1
    val y = total / 12 + year
    return m to y
}

/** Step a 0-based month (Jan=0..Nov=11) by [delta], returning (newMonth, newYear). */
fun stepMonth0(month: Int, year: Int, delta: Int): Pair<Int, Int> {
    val total = month + delta
    val m = (total % 12).let { if (it < 0) it + 12 else it }
    val y = total / 12 + year
    return m to y
}
