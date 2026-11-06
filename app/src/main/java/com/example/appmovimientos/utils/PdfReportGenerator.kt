package com.example.appmovimientos.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

object PdfReportGenerator {

    fun generateCategoryReportPdf(
        context: Context,
        periodo: String,
        totalGastos: Double,
        categoryStats: List<Triple<String, Double, Double>>
    ): File? {
        return generatePdf(context, "Reporte_Gastos_Categoria_$periodo.pdf") { canvas, paint, titlePaint, subtitlePaint, bodyPaint ->
            var yPos = 50f
            
            // Title
            canvas.drawText("App Movimientos - Reporte por Categoría", 40f, yPos, titlePaint)
            yPos += 30f
            canvas.drawText("Período: $periodo", 40f, yPos, subtitlePaint)
            yPos += 20f
            canvas.drawText("Total Gastos: S/ %.2f".format(totalGastos), 40f, yPos, subtitlePaint)
            yPos += 40f

            // Table Header
            paint.color = Color.parseColor("#6366F1")
            canvas.drawRect(40f, yPos, 555f, yPos + 30f, paint)
            paint.color = Color.WHITE
            paint.isFakeBoldText = true
            canvas.drawText("Categoría", 50f, yPos + 20f, paint)
            canvas.drawText("Monto (S/)", 350f, yPos + 20f, paint)
            canvas.drawText("%", 480f, yPos + 20f, paint)
            yPos += 35f

            // Rows
            paint.isFakeBoldText = false
            paint.color = Color.BLACK
            for ((catName, monto, porcentaje) in categoryStats) {
                if (yPos > 750f) break
                canvas.drawText(catName, 50f, yPos, bodyPaint)
                canvas.drawText("S/ %.2f".format(monto), 350f, yPos, bodyPaint)
                canvas.drawText("%.1f%%".format(porcentaje), 480f, yPos, bodyPaint)
                yPos += 25f
            }
        }
    }

    fun generateMonthlyMovementsPdf(
        context: Context,
        anio: String,
        totalAnual: Double,
        totalMovimientos: Int,
        promedioMensual: Double,
        mesStats: List<Any>
    ): File? {
        return generatePdf(context, "Reporte_Movimientos_Mes_$anio.pdf") { canvas, paint, titlePaint, subtitlePaint, bodyPaint ->
            var yPos = 50f

            canvas.drawText("App Movimientos - Movimientos por Mes", 40f, yPos, titlePaint)
            yPos += 30f
            canvas.drawText("Año: $anio | Total Movimientos: $totalMovimientos", 40f, yPos, subtitlePaint)
            yPos += 20f
            canvas.drawText("Total Anual: S/ %.2f | Promedio Mensual: S/ %.2f".format(totalAnual, promedioMensual), 40f, yPos, subtitlePaint)
            yPos += 40f

            paint.color = Color.parseColor("#10B981")
            canvas.drawRect(40f, yPos, 555f, yPos + 30f, paint)
            paint.color = Color.WHITE
            paint.isFakeBoldText = true
            canvas.drawText("Mes", 50f, yPos + 20f, paint)
            canvas.drawText("Movimientos", 250f, yPos + 20f, paint)
            canvas.drawText("Monto (S/)", 380f, yPos + 20f, paint)
            canvas.drawText("%", 480f, yPos + 20f, paint)
            yPos += 35f

            paint.isFakeBoldText = false
            paint.color = Color.BLACK
            for (item in mesStats) {
                if (yPos > 750f) break
                canvas.drawText(item.toString(), 50f, yPos, bodyPaint)
                yPos += 25f
            }
        }
    }

    fun generateTotalMonthlyPdf(
        context: Context,
        anio: String,
        totalAnual: Double,
        totalMovimientos: Int,
        promedioMensual: Double,
        mesStats: List<Any>
    ): File? {
        return generatePdf(context, "Reporte_Total_Mensual_$anio.pdf") { canvas, paint, titlePaint, subtitlePaint, bodyPaint ->
            var yPos = 50f

            canvas.drawText("App Movimientos - Reporte Total Mensual", 40f, yPos, titlePaint)
            yPos += 30f
            canvas.drawText("Año: $anio | Total Gastos: S/ %.2f".format(totalAnual), 40f, yPos, subtitlePaint)
            yPos += 20f
            canvas.drawText("Total Movimientos: $totalMovimientos | Promedio Mensual: S/ %.2f".format(promedioMensual), 40f, yPos, subtitlePaint)
            yPos += 40f

            paint.color = Color.parseColor("#8B5CF6")
            canvas.drawRect(40f, yPos, 555f, yPos + 30f, paint)
            paint.color = Color.WHITE
            paint.isFakeBoldText = true
            canvas.drawText("Mes", 50f, yPos + 20f, paint)
            canvas.drawText("Gasto (S/)", 350f, yPos + 20f, paint)
            canvas.drawText("%", 480f, yPos + 20f, paint)
            yPos += 35f

            paint.isFakeBoldText = false
            paint.color = Color.BLACK
            for (item in mesStats) {
                if (yPos > 750f) break
                canvas.drawText(item.toString(), 50f, yPos, bodyPaint)
                yPos += 25f
            }
        }
    }

    private inline fun generatePdf(
        context: Context,
        fileName: String,
        onDraw: (Canvas, Paint, Paint, Paint, Paint) -> Unit
    ): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint()
            val titlePaint = Paint().apply {
                color = Color.parseColor("#0F0728")
                textSize = 20f
                isFakeBoldText = true
            }
            val subtitlePaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 12f
            }
            val bodyPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
            }

            onDraw(canvas, paint, titlePaint, subtitlePaint, bodyPaint)

            pdfDocument.finishPage(page)

            val file = File(context.cacheDir, fileName)
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
