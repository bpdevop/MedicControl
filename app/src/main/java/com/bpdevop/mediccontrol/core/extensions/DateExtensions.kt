package com.bpdevop.mediccontrol.core.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Date.formatToString(pattern: String = "dd/MM/yyyy", locale: Locale = Locale.getDefault()): String {
    val dateFormat = SimpleDateFormat(pattern, locale)
    return dateFormat.format(this)
}

fun String.toDate(pattern: String = "dd/MM/yyyy", locale: Locale = Locale.getDefault()): Date? {
    return try {
        val dateFormat = SimpleDateFormat(pattern, locale)
        dateFormat.parse(this)
    } catch (e: Exception) {
        null
    }
}