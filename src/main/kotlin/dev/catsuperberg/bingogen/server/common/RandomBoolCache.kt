package dev.catsuperberg.bingogen.server.common

import kotlin.random.Random
class RandomBoolCache(size: Int) {
    private val cache = List(size) { Random.nextBoolean() }
    private var index = 0

    fun next(): Boolean {
        val value = cache[index]
        index = (index + 1) % cache.size
        return value
    }
}
