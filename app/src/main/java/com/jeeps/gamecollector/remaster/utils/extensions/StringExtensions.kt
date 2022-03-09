package com.jeeps.gamecollector.remaster.utils.extensions

fun String.bearer(): String {
    return "Bearer $this"
}

fun String.similarity(target: String): Double {
    var longer: String = this
    var shorter: String? = target
    if (this.length < target.length) { // longer should always have greater length
        longer = target
        shorter = this
    }
    val longerLength = longer.length
    return if (longerLength == 0) {
        1.0 /* both strings are zero length */
    } else (longerLength - editDistance(longer, shorter)) / longerLength.toDouble()
}

private fun editDistance(s1: String, s2: String?): Int {
    val string1 = s1.lowercase()
    val string2 = s2?.lowercase() ?: ""
    val costs = IntArray(string2.length + 1)
    for (i in 0..string1.length) {
        var lastValue = i
        for (j in 0..string2.length) {
            if (i == 0) costs[j] = j else {
                if (j > 0) {
                    var newValue = costs[j - 1]
                    if (string1[i - 1] != string2[j - 1]) newValue = Math.min(
                        Math.min(newValue, lastValue),
                        costs[j]
                    ) + 1
                    costs[j - 1] = lastValue
                    lastValue = newValue
                }
            }
        }
        if (i > 0) costs[string2.length] = lastValue
    }
    return costs[string2.length]
}