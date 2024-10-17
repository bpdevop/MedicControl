package com.bpdevop.mediccontrol.ui.screens.prescription

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.VectorDrawable
import androidx.appcompat.content.res.AppCompatResources
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.extensions.clearFilesWithPrefix
import com.bpdevop.mediccontrol.data.model.DoctorProfile
import com.bpdevop.mediccontrol.data.model.Patient
import com.bpdevop.mediccontrol.data.model.Prescription
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.events.Event
import com.itextpdf.kernel.events.IEventHandler
import com.itextpdf.kernel.events.PdfDocumentEvent
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

class PrescriptionPdfGenerator(private val context: Context) {

    // Método principal para generar el PDF
    fun createPrescriptionPdf(
        doctor: DoctorProfile,
        patient: Patient,
        prescription: Prescription,
    ): File {
        context.clearFilesWithPrefix("prescription_")

        val pdfFile = File(context.cacheDir, "prescription_${prescription.id}.pdf")

        val pageSize = PageSize(5.5f * 72, 8.5f * 72) // 72 puntos por pulgada

        val writer = PdfWriter(FileOutputStream(pdfFile))
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument, pageSize)

        // Añadir el manejador de eventos para el encabezado
        pdfDocument.addEventHandler(PdfDocumentEvent.START_PAGE, HeaderEventHandler(doctor))

        // Añadir el manejador de eventos para el footer
        pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, FooterEventHandler(context, pdfDocument))

        // Añadir la marca de agua a cada página del documento
        addWatermark(pdfDocument, pageSize)

        addPatientDetails(document, prescription, patient)
        addMedicationsSection(document, prescription)
        // Cerrar el documento PDF
        document.close()

        return pdfFile
    }

    // Método para añadir la marca de agua en cada página
    private fun addWatermark(pdfDocument: PdfDocument, pageSize: PageSize) {
        // Convertir el vector XML a Bitmap
        val bitmap = getBitmapFromVectorDrawable(context, R.drawable.ic_launcher_foreground)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val imageData = ImageDataFactory.create(stream.toByteArray())

        // Configuración de la imagen de la marca de agua
        val watermark = Image(imageData)
        watermark.scaleToFit(pageSize.width * 0.8f, pageSize.height * 0.8f)
        watermark.setOpacity(0.1f)

        // Añadir la marca de agua en cada página
        for (i in 1..pdfDocument.numberOfPages) {
            watermark.setFixedPosition(
                (pageSize.width - watermark.imageScaledWidth) / 2,
                (pageSize.height - watermark.imageScaledHeight) / 2
            )
            Document(pdfDocument).add(watermark)
        }
    }


    // Método para agregar la fecha y nombre del paciente
    private fun addPatientDetails(document: Document, prescription: Prescription, patient: Patient) {
        val fontSizeNormal = 8f
        val colorPrimary = DeviceRgb(63, 81, 181)

        // Añadir un espacio en blanco (padding) para que no esté tan pegado al encabezado
        document.add(Paragraph("\n\n\n"))

        val currentDate = prescription.date?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate() ?: LocalDate.now()
        val day = currentDate.dayOfMonth.toString()
        val month = currentDate.month.getDisplayName(TextStyle.FULL, Locale("es"))
        val year = currentDate.year.toString()

        val dateTable = Table(UnitValue.createPercentArray(floatArrayOf(0.5f, 0.2f, 0.1f, 0.3f, 0.1f, 1f))).useAllAvailableWidth()
        dateTable.addCell(Cell().add(Paragraph("Guatemala,").setFontColor(colorPrimary)).setFontSize(fontSizeNormal).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.BOTTOM))
        dateTable.addCell(Cell().add(Paragraph(day)).setFontSize(fontSizeNormal).setBorder(Border.NO_BORDER).setUnderline().setTextAlignment(TextAlignment.CENTER))
        dateTable.addCell(Cell().add(Paragraph("de").setFontColor(colorPrimary)).setFontSize(fontSizeNormal).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.BOTTOM))
        dateTable.addCell(Cell().add(Paragraph(month)).setFontSize(fontSizeNormal).setBorder(Border.NO_BORDER).setUnderline().setTextAlignment(TextAlignment.CENTER))
        dateTable.addCell(Cell().add(Paragraph("de").setFontColor(colorPrimary)).setFontSize(fontSizeNormal).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.BOTTOM))
        dateTable.addCell(Cell().add(Paragraph(year)).setFontSize(fontSizeNormal).setBorder(Border.NO_BORDER).setUnderline().setTextAlignment(TextAlignment.CENTER))

        document.add(dateTable)

        val patientTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 4f))).useAllAvailableWidth()
        patientTable.addCell(Cell().add(Paragraph("Nombre:").setFontColor(colorPrimary)).setFontSize(fontSizeNormal).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.BOTTOM))
        patientTable.addCell(Cell().add(Paragraph(patient.name)).setFontSize(fontSizeNormal).setBorder(Border.NO_BORDER).setUnderline().setTextAlignment(TextAlignment.LEFT))

        document.add(patientTable)
    }

    // Método para agregar los medicamentos y la marca de agua
    private fun addMedicationsSection(document: Document, prescription: Prescription) {
        document.add(Paragraph("\n"))
        document.add(Paragraph("Rp:").setBold().setFontSize(10f))

        val medicationsTable = Table(UnitValue.createPercentArray(floatArrayOf(3f, 1f, 2f, 2f))).useAllAvailableWidth()

        // Agregar los encabezados de la tabla con tamaño 10f
        medicationsTable.addHeaderCell(Cell().add(Paragraph("Medicamento").setBold().setFontSize(8f)).setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER))
        medicationsTable.addHeaderCell(Cell().add(Paragraph("Dosis").setBold().setFontSize(8f)).setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER))
        medicationsTable.addHeaderCell(Cell().add(Paragraph("Frecuencia").setBold().setFontSize(8f)).setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER))
        medicationsTable.addHeaderCell(Cell().add(Paragraph("Duración").setBold().setFontSize(8f)).setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER))

        for (item in prescription.medications) {
            medicationsTable.addCell(Cell().add(Paragraph(item.name ?: "N/A").setFontSize(8f)).setBorder(Border.NO_BORDER))
            medicationsTable.addCell(Cell().add(Paragraph(item.dosage ?: "N/A").setFontSize(8f)).setBorder(Border.NO_BORDER))
            medicationsTable.addCell(Cell().add(Paragraph("tomar ${item.frequency ?: "N/A"}").setFontSize(8f)).setBorder(Border.NO_BORDER))
            medicationsTable.addCell(Cell().add(Paragraph("Por ${item.duration ?: "N/A"}").setFontSize(8f)).setBorder(Border.NO_BORDER))
        }

        document.add(medicationsTable)
    }

    // Método para convertir VectorDrawable a Bitmap
    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        val drawable = AppCompatResources.getDrawable(context, drawableId)
        if (drawable is VectorDrawable) {
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
        throw IllegalArgumentException("Drawable is not a VectorDrawable")
    }
}

// Evento de página para manejar el encabezado en cada página
class HeaderEventHandler(val doctor: DoctorProfile) : IEventHandler {
    override fun handleEvent(event: Event) {
        val docEvent = event as PdfDocumentEvent
        val pdfPage = docEvent.page
        val pageSize = pdfPage.pageSize

        // Ajustar la posición del encabezado para que no se corte
        val x = pageSize.left + 40f  // Posición X para el encabezado
        val y = pageSize.top - 90f   // Ajustar Posición Y (más abajo para evitar que se corte)

        // Crear contenido del encabezado
        val fontSizeTitle = 12f
        val fontSizeNormal = 8f
        val fontSizeSmall = 6f
        val colorPrimary = DeviceRgb(63, 81, 181) // Color azul

        val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f, 1f))).useAllAvailableWidth()

        // Primera columna: Información del Doctor
        val doctorName = "Dr. ${doctor.name}"
        val doctorDetails = """
            Colegiado No. ${doctor.registrationNumber}
            Fisiatra, Médico Cirujano
        """.trimIndent()

        // Nombre del doctor en la parte superior
        val doctorCell = Cell()
        doctorCell.setBorder(Border.NO_BORDER)
        doctorCell.add(
            Paragraph(doctorName)
                .setFontSize(fontSizeTitle)
                .setBold()
                .setFontColor(colorPrimary)
        )
        // Colegiado y especialidad debajo
        doctorCell.add(
            Paragraph(doctorDetails)
                .setFontSize(fontSizeSmall)
                .setFontColor(colorPrimary)
        )
        headerTable.addCell(doctorCell)

        // Segunda columna: Especialidades (centrado)
        val specialties = """
            Rehabilitación,
            Medicina del Deporte
            Nutrición del Deporte
            Traumatología, Ortopedia
            Medicina General y Biológica
        """.trimIndent()

        headerTable.addCell(
            Cell().add(
                Paragraph(specialties)
                    .setFontSize(fontSizeNormal)
                    .setTextAlignment(TextAlignment.CENTER)
            )
                .setBorder(Border.NO_BORDER)
        )

        // Tercera columna: Dirección y contacto del hospital (alineado a la derecha)
        val hospitalInfo = """
            Hospital San Pablo
            8a. Calle 1-43 Zona 1
            5719-9295
            4928-3239
        """.trimIndent()

        headerTable.addCell(
            Cell().add(
                Paragraph(hospitalInfo)
                    .setFontSize(fontSizeNormal)
                    .setTextAlignment(TextAlignment.RIGHT)
            )
                .setBorder(Border.NO_BORDER)
        )

        // Añadir la tabla del encabezado en la parte superior
        val doc = Document(docEvent.document)
        doc.add(headerTable.setFixedPosition(x, y, pageSize.width - 80f)) // Ajuste del ancho
    }
}


// Evento de página para manejar el footer en cada página
class FooterEventHandler(val context: Context, private val pdfDoc: PdfDocument) : IEventHandler {
    override fun handleEvent(event: Event) {
        val docEvent = event as PdfDocumentEvent
        val pdfPage = docEvent.page
        val pageSize = pdfPage.pageSize

        // Posición del footer en la parte inferior de la página
        val x = pageSize.left + 40f  // Posición X para el footer
        val y = pageSize.bottom + 30f // Posición Y para el footer

        // Crear contenido del footer
        val fontSizeNormal = 10f
        val colorPrimary = DeviceRgb(63, 81, 181) // Color azul

        // Ajustar la posición de la línea más arriba, antes del footer
        val doc = Document(pdfDoc)

        // Tabla del footer
        val footerTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 3f, 1.5f, 3f))).useAllAvailableWidth()

        // Celda "F."
        footerTable.addCell(
            Cell().add(Paragraph("F.").setFontColor(colorPrimary))
                .setFontSize(fontSizeNormal)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.BOTTOM)
        )

        // Celda para el subrayado después de "F." sin borde
        footerTable.addCell(
            Cell().add(Paragraph(""))
                .setFontSize(fontSizeNormal)
                .setBorderBottom(SolidBorder(colorPrimary, 0.5f)) // Solo subrayado
                .setBorderTop(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.LEFT)
                .setVerticalAlignment(VerticalAlignment.BOTTOM)
        )

        // Celda "Próxima Cita:"
        footerTable.addCell(
            Cell().add(Paragraph("Próxima Cita:").setFontColor(colorPrimary))
                .setFontSize(fontSizeNormal)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.BOTTOM)
        )

        // Celda para el subrayado después de "Próxima Cita" sin borde
        footerTable.addCell(
            Cell().add(Paragraph(""))
                .setFontSize(fontSizeNormal)
                .setBorderBottom(SolidBorder(colorPrimary, 0.5f)) // Solo subrayado
                .setBorderTop(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.LEFT)
                .setVerticalAlignment(VerticalAlignment.BOTTOM)
        )

        // Dibujar el footer en la parte inferior de cada página
        doc.add(footerTable.setFixedPosition(x, y, pageSize.width - 80f)) // Ajuste del ancho
    }
}