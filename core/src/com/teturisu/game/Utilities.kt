package com.teturisu.game

object Utilities {
    fun mod(x: Int, y: Int): Int {
        var remainder = x % y
        if (remainder < 0) remainder += y

//        return x // hack to remove border teleportation
        return remainder
    }

    fun map(num: Float, oldRangeStart: Float, oldRangeEnd: Float, newRangeStart: Float, newRangeEnd: Float): Float {
        val normalized = (num - oldRangeStart) / (oldRangeEnd - oldRangeStart)
        return normalized * (newRangeEnd - newRangeStart) + newRangeStart
    }
}