package com.dicoding.ticketingsystem.DataSource.Util

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Base64
import android.graphics.BitmapFactory
// Uncomment these when you add ZXing dependency
// import com.google.zxing.BarcodeFormat
// import com.google.zxing.qrcode.QRCodeWriter

/**
 * QR Code Generator Utility
 *
 * To use this, add this dependency to your app's build.gradle:
 * implementation 'com.journeyapps:zxing-android-embedded:4.3.0'
 * implementation 'com.google.zxing:core:3.5.1'
 */
object QRCodeGenerator {

    /**
     * Generate QR Code bitmap from text
     * Requires ZXing library
     */
    fun generateQRCode(text: String, width: Int = 200, height: Int = 200): Bitmap? {
        return try {
            // Uncomment this code when you add ZXing dependency:
            /*
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
            */

            // For now, return a better placeholder
            generatePlaceholderQR(text, width)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Decode base64 string to bitmap
     */
    fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            // Remove data URL prefix if present (data:image/png;base64,)
            val cleanBase64 = if (base64String.startsWith("data:")) {
                base64String.substring(base64String.indexOf(",") + 1)
            } else {
                base64String
            }

            val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generate a better placeholder QR code bitmap with text
     */
    fun generatePlaceholderQR(text: String, size: Int = 200): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        // Fill with white background
        canvas.drawColor(Color.WHITE)

        // Draw black border
        val borderPaint = android.graphics.Paint().apply {
            color = Color.BLACK
            strokeWidth = 8f
            style = android.graphics.Paint.Style.STROKE
        }
        canvas.drawRect(4f, 4f, (size - 4).toFloat(), (size - 4).toFloat(), borderPaint)

        // Draw QR-like pattern
        val gridPaint = android.graphics.Paint().apply {
            color = Color.BLACK
        }

        val margin = 20
        val gridArea = size - (margin * 2)
        val gridSize = 12
        val cellSize = gridArea / gridSize

        // Create a pattern based on text hash
        val hash = text.hashCode()
        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                // Create corner squares (QR code positioning markers)
                if ((i < 3 && j < 3) || (i < 3 && j >= gridSize - 3) || (i >= gridSize - 3 && j < 3)) {
                    if ((i == 0 || i == 2 || j == 0 || j == 2) || (i == 1 && j == 1)) {
                        canvas.drawRect(
                            (margin + i * cellSize).toFloat(),
                            (margin + j * cellSize).toFloat(),
                            (margin + (i + 1) * cellSize).toFloat(),
                            (margin + (j + 1) * cellSize).toFloat(),
                            gridPaint
                        )
                    }
                } else {
                    // Random pattern for the rest
                    if ((hash + i * gridSize + j) % 3 == 0) {
                        canvas.drawRect(
                            (margin + i * cellSize).toFloat(),
                            (margin + j * cellSize).toFloat(),
                            (margin + (i + 1) * cellSize).toFloat(),
                            (margin + (j + 1) * cellSize).toFloat(),
                            gridPaint
                        )
                    }
                }
            }
        }

        return bitmap
    }
}