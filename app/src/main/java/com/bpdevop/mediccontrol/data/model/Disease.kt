package com.bpdevop.mediccontrol.data.model

data class Disease(
    val id: String,               // URL o ID del recurso de la enfermedad
    val title: String,            // Título o nombre de la enfermedad
    val code: String?,            // Código de la enfermedad (si está disponible)
    val chapter: String?,         // Capítulo o categoría general a la que pertenece
    val isLeaf: Boolean,          // Indica si es un nodo hoja en la jerarquía
    val stemId: String?,          // ID del recurso base o categoría superior
    val descendants: List<String>?, // IDs de las subcategorías (si existen)
    val synonyms: List<String>,    // Lista de sinónimos, si existen
)
