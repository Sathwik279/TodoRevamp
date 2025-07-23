package com.example.todorevamp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import com.example.todorevamp.data.Todo
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.*
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.HorizontalAlignment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfExporter(private val context: Context) {
    
    fun exportTodoToPdf(todo: Todo): File? {
        return try {
            // Create PDF file in app's external files directory
            val externalFilesDir = context.getExternalFilesDir(null)
            val fileName = "Todo_${sanitizeFileName(todo.title)}_${System.currentTimeMillis()}.pdf"
            val pdfFile = File(externalFilesDir, fileName)
            
            val writer = PdfWriter(FileOutputStream(pdfFile))
            val pdf = PdfDocument(writer)
            val document = Document(pdf)
            
            // Add title
            val title = Paragraph("TodoRevamp Export")
                .setFontSize(24f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(DeviceRgb(76, 175, 80))
                .setMarginBottom(20f)
            document.add(title)
            
            // Add pin indicator if pinned
            if (todo.isPinned) {
                val pinIndicator = Paragraph("📌 PINNED")
                    .setFontSize(12f)
                    .setBold()
                    .setFontColor(DeviceRgb(255, 152, 0)) // Orange color
                    .setMarginBottom(10f)
                document.add(pinIndicator)
            }
            
            // Add todo title
            val todoTitle = Paragraph(todo.title)
                .setFontSize(20f)
                .setBold()
                .setFontColor(DeviceRgb(33, 150, 243))
                .setMarginBottom(15f)
            document.add(todoTitle)
            
            // Add status
            val status = if (todo.isDone) "✅ Completed" else "⏳ In Progress"
            val statusColor = if (todo.isDone) DeviceRgb(76, 175, 80) else DeviceRgb(255, 152, 0)
            val statusParagraph = Paragraph(status)
                .setFontSize(14f)
                .setBold()
                .setFontColor(statusColor)
                .setMarginBottom(15f)
            document.add(statusParagraph)
            
            // Add description
            if (todo.description.isNotBlank()) {
                val descTitle = Paragraph("Description:")
                    .setFontSize(14f)
                    .setBold()
                    .setMarginBottom(5f)
                document.add(descTitle)
                
                val descParagraph = Paragraph(todo.description)
                    .setFontSize(12f)
                    .setMarginBottom(20f)
                document.add(descParagraph)
            }
            
            // Add tags
            if (todo.tags.isNotBlank()) {
                val tagList = todo.tags.split(",").filter { it.isNotBlank() }
                if (tagList.isNotEmpty()) {
                    val tagsTitle = Paragraph("Tags:")
                        .setFontSize(14f)
                        .setBold()
                        .setMarginBottom(10f)
                    document.add(tagsTitle)
                    
                    val tagsText = tagList.joinToString(", ") { "#${it.trim()}" }
                    val tagsParagraph = Paragraph(tagsText)
                        .setFontSize(12f)
                        .setMarginBottom(20f)
                    document.add(tagsParagraph)
                }
            }
            
            // Add AI Enhanced Content
            if (todo.enhancedContent != null && todo.enhancementStatus == "completed") {
                val aiTitle = Paragraph("🤖 AI Enhanced Content:")
                    .setFontSize(14f)
                    .setBold()
                    .setFontColor(DeviceRgb(156, 39, 176))
                    .setMarginBottom(10f)
                document.add(aiTitle)
                
                val aiContent = Paragraph(todo.enhancedContent)
                    .setFontSize(11f)
                    .setMarginBottom(20f)
                document.add(aiContent)
            }
            
            // Add images with smart layout (side by side when possible)
            if (todo.imagePaths.isNotBlank()) {
                val imageList = todo.imagePaths.split(",").filter { it.isNotBlank() }
                if (imageList.isNotEmpty()) {
                    val imagesTitle = Paragraph("📷 Images (${imageList.size}):")
                        .setFontSize(14f)
                        .setBold()
                        .setMarginBottom(10f)
                    document.add(imagesTitle)
                    
                    addImagesWithSmartLayout(document, imageList)
                }
            }
            
            // Add footer
            val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
            val lastModified = Date(todo.lastUpdated)
            val dateInfo = Paragraph("Last updated: ${dateFormat.format(lastModified)}")
                .setFontSize(9f)
                .setFontColor(ColorConstants.GRAY)
                .setMarginTop(20f)
            document.add(dateInfo)
            
            val footer = Paragraph("Generated by TodoRevamp")
                .setFontSize(9f)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setMarginTop(10f)
            document.add(footer)
            
            document.close()
            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun addImagesWithSmartLayout(document: Document, imageList: List<String>) {
        val pageWidth = 500f
        val maxImageHeight = 250f
        val imageSpacing = 10f
        
        var i = 0
        while (i < imageList.size) {
            val currentBatch = mutableListOf<Pair<Image, Int>>() // Image and its index
            var currentRowWidth = 0f
            var maxHeightInRow = 0f
            var shouldBreak = false
            
            // Try to fit as many images as possible in current row
            while (i < imageList.size && currentBatch.size < 3 && !shouldBreak) { // Max 3 images per row
                try {
                    val bitmap = loadImageFromUri(imageList[i])
                    if (bitmap != null) {
                        val imageData = bitmapToByteArray(bitmap)
                        val image = Image(ImageDataFactory.create(imageData))
                        
                        // Calculate optimal size for this image
                        val aspectRatio = image.imageWidth / image.imageHeight
                        var targetWidth = minOf(image.imageWidth, pageWidth / 3f) // Max 1/3 of page width
                        var targetHeight = targetWidth / aspectRatio
                        
                        // Ensure height doesn't exceed limit
                        if (targetHeight > maxImageHeight) {
                            targetHeight = maxImageHeight
                            targetWidth = targetHeight * aspectRatio
                        }
                        
                        // Check if this image fits in current row
                        val totalWidthWithSpacing = currentRowWidth + targetWidth + 
                            (if (currentBatch.isNotEmpty()) imageSpacing else 0f)
                        
                        if (totalWidthWithSpacing <= pageWidth || currentBatch.isEmpty()) {
                            // Scale and add to current batch
                            val scale = targetWidth / image.imageWidth
                            image.scale(scale, scale)
                            
                            currentBatch.add(Pair(image, i + 1)) // +1 for 1-based indexing
                            currentRowWidth = totalWidthWithSpacing
                            maxHeightInRow = maxOf(maxHeightInRow, targetHeight)
                            i++
                        } else {
                            shouldBreak = true // Can't fit more images in this row
                        }
                    } else {
                        // Handle failed image load
                        i++
                    }
                } catch (e: Exception) {
                    i++
                }
            }
            
            // Add the current batch to document
            if (currentBatch.isNotEmpty()) {
                if (currentBatch.size == 1) {
                    // Single image - center it
                    val (image, index) = currentBatch[0]
                    image.setHorizontalAlignment(HorizontalAlignment.CENTER)
                    image.setMarginTop(5f).setMarginBottom(5f)
                    document.add(image)
                    
                    val caption = Paragraph("Image $index")
                        .setFontSize(9f)
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(10f)
                    document.add(caption)
                } else {
                    // Multiple images - use table for proper alignment
                    val table = Table(UnitValue.createPercentArray(currentBatch.size))
                        .setWidth(UnitValue.createPercentValue(100f))
                        .setMarginTop(5f)
                        .setMarginBottom(5f)
                    
                    // Add images to table
                    currentBatch.forEach { (image, index) ->
                        val cell = Cell()
                            .add(image)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setBorder(null)
                            .setPadding(2f)
                        table.addCell(cell)
                    }
                    document.add(table)
                    
                    // Add captions in a row
                    val captionTable = Table(UnitValue.createPercentArray(currentBatch.size))
                        .setWidth(UnitValue.createPercentValue(100f))
                        .setMarginBottom(10f)
                    
                    currentBatch.forEach { (_, index) ->
                        val captionCell = Cell()
                            .add(Paragraph("Image $index")
                                .setFontSize(9f)
                                .setFontColor(ColorConstants.GRAY)
                                .setTextAlignment(TextAlignment.CENTER))
                            .setBorder(null)
                            .setPadding(0f)
                        captionTable.addCell(captionCell)
                    }
                    document.add(captionTable)
                }
            }
        }
    }

    private fun sanitizeFileName(fileName: String): String {
        return fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_").take(50)
    }
    
    private fun loadImageFromUri(uriString: String): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        // Use high quality JPEG compression
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return stream.toByteArray()
    }
}
