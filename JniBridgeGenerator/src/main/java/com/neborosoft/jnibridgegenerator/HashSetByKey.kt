package com.neborosoft.jnibridgegenerator

class HashSetByKey<T>(
    private val keyProvider: (T) -> Any
) : AbstractMutableSet<T>() {
    private val map = HashMap<Any, T>()

    override val size: Int
        get() = map.size

    override fun iterator(): MutableIterator<T> {
        return map.iterator().map {
            it.value
        }
    }

    override fun contains(element: T): Boolean {
        return map.containsKey(keyProvider(element))
    }

    override fun add(element: T): Boolean {
        return map.put(keyProvider(element), element) == null
    }
}