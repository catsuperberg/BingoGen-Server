package dev.catsuperberg.bingogen.server.common

import kotlin.random.Random
class RandomFloatCache(size: Int) {
    private val cache = List(size) { Random.nextFloat() }
    private var index = 0

    fun next(): Float {
        val value = cache[index]
        index = (index + 1) % cache.size
        return value
    }

    fun nextInRange(min: Float, max: Float): Float {
        val part = cache[index]
        index = (index + 1) % cache.size
        val offset = (max-min)*part
        return min+offset
    }
}
