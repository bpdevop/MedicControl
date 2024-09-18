package com.bpdevop.mediccontrol.core.utils

import java.io.File

fun deleteImageFile(file: File?): Boolean {
    return file?.takeIf { it.exists() }?.delete() ?: false
}