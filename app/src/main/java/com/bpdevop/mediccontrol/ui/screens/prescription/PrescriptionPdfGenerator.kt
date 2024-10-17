package com.bpdevop.mediccontrol.ui.screens.prescription

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.VectorDrawable
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

        // Configurar el tamaño de la receta a 5.5 x 8.5 pulgadas
        val pageSize = PageSize(5.5f * 72, 8.5f * 72) // 72 puntos por pulgada

        // Inicializar PdfWriter con PdfDocument
        val writer = PdfWriter(FileOutputStream(pdfFile))
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument, pageSize)

        // Añadir el manejador de eventos para el encabezado
        pdfDocument.addEventHandler(PdfDocumentEvent.START_PAGE, HeaderEventHandler(doctor))


        // Añadir el manejador de eventos para el footer
        pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, FooterEventHandler(context, pdfDocument))

        addPatientDetails(document, prescription, patient)
        addMedicationsSection(document, prescription, context)
        // Cerrar el documento PDF
        document.close()

        return pdfFile
    }

    // Método privado para agregar el encabezado


    // Método para agregar la fecha y nombre del paciente
    private fun addPatientDetails(document: Document, prescription: Prescription, patient: Patient) {
        val fontSizeNormal = 8f
        val colorPrimary = DeviceRgb(63, 81, 181) // Color #3f51b5

        // Añadir un espacio en blanco (padding) para que no esté tan pegado al encabezado
        document.add(Paragraph("\n\n\n"))

        // Convertir la fecha usando LocalDate
        val currentDate = prescription.date?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate() ?: LocalDate.now()
        val day = currentDate.dayOfMonth.toString()
        val month = currentDate.month.getDisplayName(TextStyle.FULL, Locale("es"))
        val year = currentDate.year.toString()

        // Crear una tabla para la fecha: "Guatemala, __ de ____ de ____"
        val dateTable = Table(UnitValue.createPercentArray(floatArrayOf(0.5f, 0.2f, 0.1f, 0.3f, 0.1f, 1f))).useAllAvailableWidth()

        // Primera celda: "Guatemala,"
        dateTable.addCell(
            Cell().add(Paragraph("Guatemala,").setFontColor(colorPrimary))
                .setFontSize(fontSizeNormal)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.BOTTOM)
        )

        // Segunda celda: Día (subrayado)
        dateTable.addCell(
            Cell().add(Paragraph(day))
                .setFontSize(fontSizeNormal)
                .setBorder(Border.NO_BORDER)
                .setUnderline()
                .setTextAlignment(TextAlignment.CENTER)
        )

        // Tercera celda: "de"
        dateTable.addCell(
            Cell().add(Paragraph("de").setFontColor(colorPrimary))
                .setFontSize(fontSizeNormal)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.BOTTOM)
        )

        // Cuarta celda: Mes (subrayado)
        dateTable.addCell(
            Cell().add(Paragraph(month))
                .setFontSize(fontSizeNormal)
                .setBorder(Border.NO_BORDER)
                .setUnderline()
                .setTextAlignment(TextAlignment.CENTER)
        )

        // Quinta celda: "de"
        dateTable.addCell(
            Cell().add(Paragraph("de").setFontColor(colorPrimary))
                .setFontSize(fontSizeNormal)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.BOTTOM)
        )

        // Sexta celda: Año (subrayado)
        dateTable.addCell(
            Cell().add(Paragraph(year))
                .setFontSize(fontSizeNormal)
                .setBorder(Border.NO_BORDER)
                .setUnderline()
                .setTextAlignment(TextAlignment.CENTER)
        )

        document.add(dateTable)

        // Crear una tabla para el nombre del paciente con subrayado
        // Ajuste de proporciones: la primera columna ("Nombre:") tiene un ancho de 1f,
        // mientras que la segunda columna (el nombre del paciente) ocupa el resto del espacio.
        val patientTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 4f))).useAllAvailableWidth()

        // Celda "Nombre:"
        patientTable.addCell(
            Cell().add(Paragraph("Nombre:").setFontColor(colorPrimary))
                .setFontSize(fontSizeNormal)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.BOTTOM)
        )

        // Celda Nombre del paciente (subrayado)
        patientTable.addCell(
            Cell().add(Paragraph(patient.name))
                .setFontSize(fontSizeNormal)
                .setBorder(Border.NO_BORDER)
                .setUnderline()
                .setTextAlignment(TextAlignment.LEFT)
        )

        document.add(patientTable)
    }

    // Método para agregar los medicamentos y la marca de agua
    private fun addMedicationsSection(document: Document, prescription: Prescription, context: Context) {
        // Añadir espacio entre la sección anterior
        document.add(Paragraph("\n"))

        // Tamaño del documento 5.5 x 8.5 pulgadas en puntos (1 pulgada = 72 puntos)
        val pageWidth = 5.5f * 72
        val pageHeight = 8.5f * 72

        // Convertir el vector XML (ic_launcher_foreground) a Bitmap
        val bitmap = getBitmapFromVectorDrawable(context, R.drawable.ic_launcher_foreground)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val imageData = ImageDataFactory.create(stream.toByteArray())

        // Agregar la imagen como marca de agua, centrada y un poco más grande
        val watermark = Image(imageData)
        watermark.scaleToFit(pageWidth * 0.8f, pageHeight * 0.8f) // Ajustar tamaño de la imagen
        watermark.setFixedPosition(
            (pageWidth - watermark.imageScaledWidth) / 2,  // Centrar horizontalmente
            (pageHeight - watermark.imageScaledHeight) / 2  // Centrar verticalmente
        )
        watermark.setOpacity(0.1f) // Ajustar la opacidad para ser discreta
        document.add(watermark)

        // Añadir el título "Rp:" para la receta con tamaño 10f
        document.add(Paragraph("Rp:").setBold().setFontSize(10f))

        // Crear una tabla para los medicamentos
        val medicationsTable = Table(UnitValue.createPercentArray(floatArrayOf(3f, 1f, 2f, 2f))).useAllAvailableWidth()

        // Agregar los encabezados de la tabla con tamaño 10f
        medicationsTable.addHeaderCell(Cell().add(Paragraph("Medicamento").setBold().setFontSize(8f)).setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER))
        medicationsTable.addHeaderCell(Cell().add(Paragraph("Dosis").setBold().setFontSize(8f)).setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER))
        medicationsTable.addHeaderCell(Cell().add(Paragraph("Frecuencia").setBold().setFontSize(8f)).setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER))
        medicationsTable.addHeaderCell(Cell().add(Paragraph("Duración").setBold().setFontSize(8f)).setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER))

        // Añadir los medicamentos de la receta
        for (item in prescription.medications) {
            val medicamentoText = item.name ?: "N/A"
            val dosisText = item.dosage ?: "N/A"
            val frecuenciaText = "tomar ${item.frequency ?: "N/A"}"
            val duracionText = "Por ${item.duration ?: "N/A"}"

            medicationsTable.addCell(Cell().add(Paragraph(medicamentoText).setFontSize(8f)).setBorder(Border.NO_BORDER))
            medicationsTable.addCell(Cell().add(Paragraph(dosisText).setFontSize(8f)).setBorder(Border.NO_BORDER))
            medicationsTable.addCell(Cell().add(Paragraph(frecuenciaText).setFontSize(8f)).setBorder(Border.NO_BORDER))
            medicationsTable.addCell(Cell().add(Paragraph(duracionText).setFontSize(8f)).setBorder(Border.NO_BORDER))
        }

        // Añadir la tabla de medicamentos al documento
        document.add(medicationsTable)
    }

    // Método para convertir VectorDrawable a Bitmap
    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        val drawable = context.getDrawable(drawableId)
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