package com.neborosoft.jnibridgegenerator

fun <T> List<T>.withPrependedItem(item: T): List<T> {
    return object : AbstractList<T>() {
        override val size: Int
            get() = this@withPrependedItem.size + 1

        override fun get(index: Int): T {
            if (index == 0) {
                return item
            } else {
                return this@withPrependedItem[index - 1]
            }
        }

    }
}

fun <T> List<T>.withAppendedItem(item: T): List<T> {
    return object : AbstractList<T>() {
        override val size: Int
            get() = this@withAppendedItem.size + 1

        override fun get(index: Int): T {
            if (index == size - 1) {
                return item
            } else {
                return this@withAppendedItem[index]
            }
        }

    }
}