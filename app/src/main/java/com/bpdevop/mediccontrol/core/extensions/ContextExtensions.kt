package com.bpdevop.mediccontrol.core.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.bpdevop.mediccontrol.data.model.DoctorProfile
import com.bpdevop.mediccontrol.data.model.Patient
import com.bpdevop.mediccontrol.data.model.Prescription
import com.bpdevop.mediccontrol.ui.activities.LoginActivity
import com.bpdevop.mediccontrol.ui.activities.MainActivity
import com.bpdevop.mediccontrol.ui.screens.export.Hl7ExportPdfGenerator
import com.bpdevop.mediccontrol.ui.screens.prescription.PrescriptionPdfGenerator
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    return File.createTempFile(
        imageFileName,
        ".jpg",
        externalCacheDir
    )
}

fun Context.createVideoFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val videoFileName = "MP4_" + timeStamp + "_"
    return File.createTempFile(
        videoFileName,
        ".mp4",
        externalCacheDir
    )
}

fun Context.navigateToLoginActivity() {
    val intent = Intent(this, LoginActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
    (this as? Activity)?.finish()
}

fun Context.navigateToMainActivity() {
    val intent = Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
    (this as? Activity)?.finish()
}

fun Context.openUrlInCustomTab(url: String) {
    val customTabsIntent = CustomTabsIntent.Builder().build()
    customTabsIntent.launchUrl(this, Uri.parse(url))
}

fun Context.generatePrescriptionPdf(doctor: DoctorProfile, patient: Patient, prescription: Prescription): File {
    val generator = PrescriptionPdfGenerator(this)
    return generator.createPrescriptionPdf(doctor, patient, prescription)
}

fun Context.generateHl7ExportPdf(doctor: DoctorProfile, patients: List<Patient>): File {
    val generator = Hl7ExportPdfGenerator(this, doctor)
    return generator.createHl7ExportPdf(patients)
}

fun Context.clearOldHl7Exports() {
    cacheDir.listFiles { file -> file.name.startsWith("hl7_export_infectocontagiosos_") }
        ?.forEach { it.delete() }
}
