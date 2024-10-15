package com.bpdevop.mediccontrol.data.repository

import com.bpdevop.mediccontrol.core.network.TokenManager
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.api.IcdService
import com.bpdevop.mediccontrol.data.model.Disease
import javax.inject.Inject

class IcdRepository @Inject constructor(
    private val icdService: IcdService,
    private val tokenManager: TokenManager,
) {

    suspend fun searchInfectiousDiseases(query: String): UiState<List<Disease>> =
        runCatching {
            val token = tokenManager.getAccessToken()
            val response = icdService.searchDiseases(
                authHeader = "Bearer $token",
                query = query,
                language = "es",
                chapterFilter = "01" // Capítulo 1: enfermedades infecciosas
            )

            val diseases = response.destinationEntities.map { entity ->
                Disease(
                    id = entity.id,
                    title = entity.title,
                    code = entity.code,
                    chapter = entity.chapter,
                    isLeaf = entity.isLeaf,
                    stemId = entity.stemId,
                    descendants = entity.descendants,
                    synonyms = entity.matchingPVs.map { removeHtmlTags(it.label) }
                )
            }
            UiState.Success(diseases)
        }.getOrElse {
            UiState.Error(it.message ?: "Error al buscar enfermedades")
        }
}


fun removeHtmlTags(text: String): String {
    return text.replace("<[^>]*>".toRegex(), "")
}