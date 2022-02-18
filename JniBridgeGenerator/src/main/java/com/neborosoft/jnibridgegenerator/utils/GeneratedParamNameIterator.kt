package com.neborosoft.jnibridgegenerator.utils

class GeneratedParamNameIterator : Iterator<String> {
    private var current = 'a'

    override fun hasNext(): Boolean {
        return true
    }

    override fun next(): String {
        return current++.toString()
    }
}