package com.bitnetika.commons.utils

fun String?.isEquals(other: String?): Boolean {
    return equals(other, true)
}

fun CharSequence?.isContains(other: String?): Boolean {
    if (this == null || other == null) {
        return false
    }

    return contains(other, true)
}
