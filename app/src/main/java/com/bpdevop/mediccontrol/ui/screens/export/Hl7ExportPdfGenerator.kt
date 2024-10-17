package com.bpdevop.mediccontrol.ui.screens.export

import android.content.Context
import androidx.core.text.HtmlCompat
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.extensions.clearOldHl7Exports
import com.bpdevop.mediccontrol.core.extensions.formatToString
import com.bpdevop.mediccontrol.data.model.DoctorProfile
import com.bpdevop.mediccontrol.data.model.Patient
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Hl7ExportPdfGenerator(
    private val context: Context,
    private val doctorProfile: DoctorProfile,
) {

    fun createHl7ExportPdf(patients: List<Patient>): File {
        context.clearOldHl7Exports()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val pdfFile = File(context.cacheDir, "hl7_export_infectocontagiosos_$timestamp.pdf")

        val writer = PdfWriter(FileOutputStream(pdfFile))
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument, PageSize.A4)

        // Título del documento
        document.add(
            Paragraph(context.getString(R.string.pdf_title))
                .setBold().setFontSize(16f)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
        )
        document.add(
            Paragraph(context.getString(R.string.pdf_exported_on, SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())))
                .setFontSize(10f).setTextAlignment(TextAlignment.RIGHT)
        )

        // Datos del médico e institución
        document.add(Paragraph(context.getString(R.string.pdf_doctor_name, doctorProfile.name)).setFontSize(10f))
        document.add(Paragraph(context.getString(R.string.pdf_license, doctorProfile.registrationNumber)).setFontSize(10f))
        document.add(Paragraph(context.getString(R.string.pdf_email, doctorProfile.email)).setFontSize(10f))
        document.add(Paragraph(context.getString(R.string.pdf_phone, doctorProfile.phoneNumber)).setFontSize(10f))
        document.add(Paragraph("\n"))

        // Crear tabla HL7 para cada paciente
        for (patient in patients) {
            addPatientHl7Details(document, patient)
            document.add(Paragraph("\n\n")) // Espacio entre pacientes
        }

        document.close()
        return pdfFile
    }

    private fun addPatientHl7Details(document: Document, patient: Patient) {
        val fontSize = 10f

        val table = Table(UnitValue.createPercentArray(floatArrayOf(1f, 4f)))
            .useAllAvailableWidth()

        // PID - Identificador del Paciente
        table.addCell(createHl7HeaderCell(context.getString(R.string.pdf_patient_id), fontSize))
        table.addCell(createHl7DataCell(patient.id, fontSize))

        // PV1 - Información de Visita
        table.addCell(createHl7HeaderCell(context.getString(R.string.pdf_patient_visit), fontSize))
        table.addCell(createHl7DataCell(context.getString(R.string.pdf_patient_visit_info), fontSize))

        // DG1 - Información de Diagnóstico (CIE-11)
        val diseaseDescription = HtmlCompat.fromHtml(
            patient.diseaseTitle ?: context.getString(R.string.pdf_unknown),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        ).toString()
        table.addCell(createHl7HeaderCell(context.getString(R.string.pdf_diagnosis_info), fontSize))
        table.addCell(createHl7DataCell(context.getString(R.string.pdf_diagnosis_format, patient.diseaseCode ?: context.getString(R.string.pdf_not_specified), diseaseDescription), fontSize))

        // Nombre del paciente
        table.addCell(createHl7HeaderCell(context.getString(R.string.pdf_patient_name), fontSize))
        table.addCell(createHl7DataCell(patient.name, fontSize))

        // Fecha de nacimiento
        val birthDateFormatted = patient.birthDate?.formatToString("dd/MM/yyyy") ?: context.getString(R.string.pdf_unknown)
        table.addCell(createHl7HeaderCell(context.getString(R.string.pdf_birth_date), fontSize))
        table.addCell(createHl7DataCell(birthDateFormatted, fontSize))

        // Género
        table.addCell(createHl7HeaderCell(context.getString(R.string.pdf_gender), fontSize))
        table.addCell(createHl7DataCell(patient.gender ?: context.getString(R.string.pdf_not_specified), fontSize))

        // Tipo de sangre y factor RH
        val bloodInfo = "${patient.bloodType ?: context.getString(R.string.pdf_not_specified)} ${if (patient.rhFactor == true) "+" else "-"}"
        table.addCell(createHl7HeaderCell(context.getString(R.string.pdf_blood_type), fontSize))
        table.addCell(createHl7DataCell(bloodInfo, fontSize))

        document.add(table)
    }

    private fun createHl7HeaderCell(text: String, fontSize: Float): Cell {
        return Cell().add(Paragraph(text).setBold().setFontSize(fontSize))
            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
            .setTextAlignment(TextAlignment.CENTER)
            .setBorder(Border.NO_BORDER)
    }

    private fun createHl7DataCell(text: String, fontSize: Float): Cell {
        return Cell().add(Paragraph(text).setFontSize(fontSize))
            .setTextAlignment(TextAlignment.LEFT)
            .setBorder(Border.NO_BORDER)
    }

}