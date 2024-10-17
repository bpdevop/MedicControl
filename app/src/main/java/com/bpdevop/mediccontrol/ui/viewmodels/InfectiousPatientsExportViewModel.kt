package com.bpdevop.mediccontrol.ui.viewmodels

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Patient
import com.bpdevop.mediccontrol.data.repository.PatientsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class InfectiousPatientsExportViewModel @Inject constructor(
    private val patientsRepository: PatientsRepository
) : ViewModel() {

    private val _infectiousPatients = MutableStateFlow<UiState<List<Patient>>>(UiState.Idle)
    val infectiousPatients: StateFlow<UiState<List<Patient>>> = _infectiousPatients

    fun loadInfectiousPatients() {
        viewModelScope.launch {
            _infectiousPatients.value = UiState.Loading
            _infectiousPatients.value = patientsRepository.getInfectiousPatients()
        }
    }

    fun exportToPDF(patients: List<Patient>, context: Context) {
        viewModelScope.launch {
            try {
                // Implementación de exportación a PDF en formato HL7
                val document = createHL7PDF(patients, context)
                // Notificar al usuario sobre la ubicación del archivo PDF
                UiState.Success("PDF exportado a ${document.absolutePath}")
            } catch (e: Exception) {
                _infectiousPatients.value = UiState.Error("Error al exportar a PDF: ${e.message}")
            }
        }
    }

    private fun createHL7PDF(patients: List<Patient>, context: Context): File {
        // Crear el archivo PDF
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply {
            textSize = 12f
        }

        var yPosition = 50
        patients.forEach { patient ->
            canvas.drawText(
                "PID|${patient.id}|${patient.name}|${patient.gender}|${patient.birthDate}|${patient.diseaseTitle}",
                50f,
                yPosition.toFloat(),
                paint
            )
            yPosition += 20
        }

        document.finishPage(page)

        // Guardar el archivo
        val file = File(context.getExternalFilesDir(null), "InfectiousPatients.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()

        return file
    }
}