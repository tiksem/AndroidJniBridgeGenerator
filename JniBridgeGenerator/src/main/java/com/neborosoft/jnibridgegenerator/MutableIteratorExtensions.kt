package com.neborosoft.jnibridgegenerator

fun <From, To> MutableIterator<From>.map(transformer: (From) -> To): MutableIterator<To> {
    return object : MutableIterator<To> {
        override fun hasNext(): Boolean {
            return this@map.hasNext()
        }

        override fun next(): To {
            return transformer(this@map.next())
        }

        override fun remove() {
            this@map.remove()
        }
    }
}