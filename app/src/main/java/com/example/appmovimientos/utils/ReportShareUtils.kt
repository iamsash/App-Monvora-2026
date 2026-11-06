package com.example.appmovimientos.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object ReportShareUtils {

    fun sharePdfViaWhatsApp(context: Context, file: File, title: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "📊 *Reporte Financiero:* $title\nGenerado con App Movimientos.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
            }

            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // WhatsApp not installed or failed, fallback to general chooser
                intent.setPackage(null)
                context.startActivity(Intent.createChooser(intent, "Compartir Reporte PDF"))
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al compartir archivo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openPdfFile(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Abrir Reporte PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, "No hay aplicaciones para abrir PDF", Toast.LENGTH_SHORT).show()
        }
    }
}
