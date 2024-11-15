package com.studo.campusqr.extensions

fun <K, V> Map<K, V?>.filterValuesNotNull(): Map<K, V> = mapNotNull { (key, value) -> value?.let { key to value } }.toMap()

fun <K, V> Map<K?, V>.filterKeysNotNull(): Map<K, V> = mapNotNull { (key, value) -> key?.let { key to value } }.toMap()

fun <K, V> mapOfNotNull(vararg pairs: Pair<K, V>?): Map<K, V> = pairs.filterNotNull().toMap()