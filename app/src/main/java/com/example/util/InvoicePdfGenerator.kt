package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.InvoiceEntity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale

object InvoicePdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard width in points (72 dpi)
    private const val PAGE_HEIGHT = 842 // A4 standard height in points
    private const val MARGIN = 36f

    fun generatePdfFile(context: Context, invoice: InvoiceEntity): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawInvoiceOnCanvas(canvas, invoice)

        document.finishPage(page)

        val outputDir = File(context.cacheDir, "invoices")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        val safeInvoiceNo = invoice.invoiceNo.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val file = File(outputDir, "TAX_INVOICE_$safeInvoiceNo.pdf")

        try {
            val fos = FileOutputStream(file)
            document.writeTo(fos)
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            document.close()
        }

        return file
    }

    private fun drawInvoiceOnCanvas(canvas: Canvas, invoice: InvoiceEntity) {
        // Background
        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

        val textPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 8.5f
            isAntiAlias = true
        }

        val boldPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val subTitlePaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
        }

        val thBgPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }

        var y = MARGIN + 12f

        // Header - Company Details (Left)
        boldPaint.textSize = 9.5f
        canvas.drawText("AVA Passengers Transport by Rental Buses L.L.C.", MARGIN, y, boldPaint)
        y += 11f
        textPaint.textSize = 8f
        canvas.drawText("P.O. Box 91977, Dubai – United Arab Emirates", MARGIN, y, textPaint)
        y += 10f
        canvas.drawText("Phone: 00971 4 3439666  |  Email: Contact@avarental.ae", MARGIN, y, textPaint)

        // Header - TAX INVOICE Title (Right)
        val taxInvText = "TAX INVOICE"
        val arabicTaxInv = "فاتورة ضريبية"
        val titleWidth = titlePaint.measureText(taxInvText)
        val arabicWidth = subTitlePaint.measureText(arabicTaxInv)

        canvas.drawText(taxInvText, PAGE_WIDTH - MARGIN - titleWidth, MARGIN + 12f, titlePaint)
        canvas.drawText(arabicTaxInv, PAGE_WIDTH - MARGIN - arabicWidth, MARGIN + 28f, subTitlePaint)

        y = MARGIN + 40f

        // Meta Box: 4 columns
        val metaTop = y
        val metaHeight = 24f
        val metaWidth = PAGE_WIDTH - 2 * MARGIN
        val colWidth = metaWidth / 4f

        canvas.drawRect(MARGIN, metaTop, MARGIN + metaWidth, metaTop + metaHeight, borderPaint)
        for (i in 1..3) {
            val x = MARGIN + i * colWidth
            canvas.drawLine(x, metaTop, x, metaTop + metaHeight, borderPaint)
        }

        val metaHeaders = arrayOf("INVOICE NO.:", "INVOICE DATE:", "TRN:", "DUE DATE:")
        val metaValues = arrayOf(invoice.invoiceNo, invoice.invoiceDate, invoice.companyTrn, invoice.dueDate)

        val metaLabelPaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 7f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val metaValPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 8.5f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        for (i in 0..3) {
            val cx = MARGIN + i * colWidth + colWidth / 2f
            val labelW = metaLabelPaint.measureText(metaHeaders[i])
            canvas.drawText(metaHeaders[i], cx - labelW / 2f, metaTop + 9f, metaLabelPaint)

            val valW = metaValPaint.measureText(metaValues[i])
            canvas.drawText(metaValues[i], cx - valW / 2f, metaTop + 20f, metaValPaint)
        }

        y = metaTop + metaHeight + 14f

        // BILL TO Section
        boldPaint.textSize = 9.5f
        canvas.drawText("■ BILL TO", MARGIN, y, boldPaint)
        y += 12f

        boldPaint.textSize = 9.5f
        canvas.drawText(invoice.clientName, MARGIN + 8f, y, boldPaint)
        y += 11f
        textPaint.textSize = 8.5f
        if (invoice.clientAddress.isNotEmpty()) {
            canvas.drawText(invoice.clientAddress, MARGIN + 8f, y, textPaint)
            y += 10f
        }
        if (invoice.clientPoBox.isNotEmpty()) {
            canvas.drawText(invoice.clientPoBox, MARGIN + 8f, y, textPaint)
            y += 10f
        }
        val clientContact = "Phone: ${invoice.clientPhone}   TRN: ${invoice.clientTrn}"
        canvas.drawText(clientContact, MARGIN + 8f, y, textPaint)
        y += 14f

        // Items Table
        val colDescW = metaWidth - 55f - 80f - 85f
        val colQtyW = 55f
        val colPriceW = 80f
        val colAmountW = 85f

        val tableLeft = MARGIN
        val xQty = tableLeft + colDescW
        val xPrice = xQty + colQtyW
        val xAmount = xPrice + colPriceW
        val tableRight = tableLeft + metaWidth

        // Table Header
        val thHeight = 18f
        canvas.drawRect(tableLeft, y, tableRight, y + thHeight, thBgPaint)
        canvas.drawLine(tableLeft, y, tableRight, y, borderPaint)
        canvas.drawLine(tableLeft, y + thHeight, tableRight, y + thHeight, borderPaint)

        boldPaint.textSize = 8f
        canvas.drawText("DESCRIPTION", tableLeft + 6f, y + 12f, boldPaint)
        val qtyHdrW = boldPaint.measureText("QTY")
        canvas.drawText("QTY", xQty + (colQtyW - qtyHdrW) / 2f, y + 12f, boldPaint)
        val prcHdrW = boldPaint.measureText("UNIT PRICE")
        canvas.drawText("UNIT PRICE", xPrice + colPriceW - prcHdrW - 6f, y + 12f, boldPaint)
        val amtHdrW = boldPaint.measureText("AMOUNT (AED)")
        canvas.drawText("AMOUNT (AED)", xAmount + colAmountW - amtHdrW - 6f, y + 12f, boldPaint)

        y += thHeight

        // Rows
        val items = invoice.items
        for (item in items) {
            val descLines = item.description.split("\n")
            val rowHeight = maxOf(20f, (descLines.size * 10f) + 8f)

            var lineY = y + 10f
            for (line in descLines) {
                canvas.drawText(line.trim(), tableLeft + 6f, lineY, textPaint)
                lineY += 10f
            }

            val qtyStr = if (item.qty % 1.0 == 0.0) item.qty.toInt().toString() else String.format(Locale.US, "%.1f", item.qty)
            val qtyW = textPaint.measureText(qtyStr)
            canvas.drawText(qtyStr, xQty + (colQtyW - qtyW) / 2f, y + 12f, textPaint)

            val priceStr = String.format(Locale.US, "%,.2f", item.unitPrice)
            val prcW = textPaint.measureText(priceStr)
            canvas.drawText(priceStr, xPrice + colPriceW - prcW - 6f, y + 12f, textPaint)

            val amountStr = String.format(Locale.US, "%,.2f", item.totalAmount)
            val amtW = boldPaint.measureText(amountStr)
            canvas.drawText(amountStr, xAmount + colAmountW - amtW - 6f, y + 12f, boldPaint)

            y += rowHeight
            canvas.drawLine(tableLeft, y, tableRight, y, borderPaint)
        }

        y += 12f

        // Amount in Words (Left) & Totals (Right)
        val wordsWidth = metaWidth * 0.58f
        val totalsWidth = metaWidth - wordsWidth - 12f
        val totalsLeft = tableLeft + wordsWidth + 12f

        // Amount in Words
        boldPaint.textSize = 8.5f
        canvas.drawText("AMOUNT IN WORDS", tableLeft, y, boldPaint)
        y += 10f

        val wordsBg = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }
        val wordsText = NumberToWords.convertToUaeCurrency(invoice.totalDue)
        val wordsRect = Rect(tableLeft.toInt(), y.toInt(), (tableLeft + wordsWidth).toInt(), (y + 36f).toInt())
        canvas.drawRect(wordsRect, wordsBg)
        canvas.drawRect(wordsRect, borderPaint)

        textPaint.textSize = 8f
        // Wrap words text into 2-3 lines
        val wordsWords = wordsText.split(" ")
        var curLine = ""
        var curY = y + 12f
        for (w in wordsWords) {
            val testLine = if (curLine.isEmpty()) w else "$curLine $w"
            if (textPaint.measureText(testLine) > wordsWidth - 16f) {
                canvas.drawText(curLine, tableLeft + 6f, curY, textPaint)
                curY += 10f
                curLine = w
            } else {
                curLine = testLine
            }
        }
        if (curLine.isNotEmpty()) {
            canvas.drawText(curLine, tableLeft + 6f, curY, textPaint)
        }

        // Totals Box (Right)
        var totY = y - 10f
        val subtotalStr = String.format(Locale.US, "AED %,.2f", invoice.subtotal)
        val vatStr = String.format(Locale.US, "AED %,.2f", invoice.vatAmount)
        val totalDueStr = String.format(Locale.US, "AED %,.2f", invoice.totalDue)

        textPaint.textSize = 8.5f
        canvas.drawText("Subtotal:", totalsLeft, totY + 8f, textPaint)
        val subW = boldPaint.measureText(subtotalStr)
        canvas.drawText(subtotalStr, tableRight - subW, totY + 8f, boldPaint)
        canvas.drawLine(totalsLeft, totY + 12f, tableRight, totY + 12f, borderPaint)

        totY += 16f
        canvas.drawText("VAT (5%):", totalsLeft, totY + 8f, textPaint)
        val vatW = boldPaint.measureText(vatStr)
        canvas.drawText(vatStr, tableRight - vatW, totY + 8f, boldPaint)
        canvas.drawLine(totalsLeft, totY + 12f, tableRight, totY + 12f, borderPaint)

        totY += 18f
        boldPaint.textSize = 10f
        canvas.drawText("TOTAL DUE:", totalsLeft, totY + 8f, boldPaint)
        val totalW = boldPaint.measureText(totalDueStr)
        canvas.drawText(totalDueStr, tableRight - totalW, totY + 8f, boldPaint)
        canvas.drawLine(totalsLeft, totY + 12f, tableRight, totY + 12f, borderPaint)

        y = maxOf(y + 45f, totY + 25f)

        // Payment Terms & Bank Details (Two columns)
        val halfW = (metaWidth - 12f) / 2f
        val rightColLeft = tableLeft + halfW + 12f

        boldPaint.textSize = 9f
        canvas.drawText("■ PAYMENT TERMS", tableLeft, y, boldPaint)
        canvas.drawText("■ BANK DETAILS", rightColLeft, y, boldPaint)

        y += 11f
        textPaint.textSize = 8f
        canvas.drawText("Payment due within 45 days — by ${invoice.dueDate}.", tableLeft, y, textPaint)
        canvas.drawText("Bank: RAKBANK", rightColLeft, y, textPaint)

        y += 10f
        canvas.drawText("Checks payable to AVA Passengers Transport by Rental Buses L.L.C.", tableLeft, y, textPaint)
        canvas.drawText("Account: AVA Passengers Transport by Rental Buses L.L.C", rightColLeft, y, textPaint)

        y += 10f
        val ibanPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 8.5f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }
        canvas.drawText("IBAN: AE11 0400 0083 7152 7349 901", rightColLeft, y, ibanPaint)

        y += 24f

        // Notes & Computer Generated Disclaimer
        canvas.drawLine(tableLeft, y, tableRight, y, borderPaint)
        y += 11f
        val notesPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 7.5f
            isAntiAlias = true
        }
        val noteText = "■ NOTES: Thank you for your business! Please contact Contact@avarental.ae for queries."
        val noteW = notesPaint.measureText(noteText)
        canvas.drawText(noteText, (PAGE_WIDTH - noteW) / 2f, y, notesPaint)

        y += 9f
        val discText = "This is a computer-generated tax e-invoice and does not require a physical signature."
        val discW = notesPaint.measureText(discText)
        canvas.drawText(discText, (PAGE_WIDTH - discW) / 2f, y, notesPaint)
    }

    fun sharePdf(context: Context, invoice: InvoiceEntity) {
        try {
            val file = generatePdfFile(context, invoice)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Tax Invoice ${invoice.invoiceNo} - AVA Passengers Transport")
                putExtra(Intent.EXTRA_TEXT, "Please find attached Tax Invoice ${invoice.invoiceNo} for ${invoice.clientName} (Total: AED ${String.format(Locale.US, "%,.2f", invoice.totalDue)}).")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Share Tax Invoice via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not share PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun printInvoice(context: Context, invoice: InvoiceEntity) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            Toast.makeText(context, "Print service unavailable on this device", Toast.LENGTH_SHORT).show()
            return
        }

        val jobName = "AVA_Invoice_${invoice.invoiceNo}"
        printManager.print(
            jobName,
            object : PrintDocumentAdapter() {
                private var generatedFile: File? = null

                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }

                    generatedFile = generatePdfFile(context, invoice)

                    val info = PrintDocumentInfo.Builder("Tax_Invoice_${invoice.invoiceNo}.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build()

                    callback?.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor?,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    val file = generatedFile ?: return
                    var input: FileInputStream? = null
                    var output: FileOutputStream? = null

                    try {
                        input = FileInputStream(file)
                        output = FileOutputStream(destination?.fileDescriptor)

                        val buffer = ByteArray(1024)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } >= 0) {
                            if (cancellationSignal?.isCanceled == true) {
                                callback?.onWriteCancelled()
                                return
                            }
                            output.write(buffer, 0, bytesRead)
                        }

                        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message)
                    } finally {
                        try {
                            input?.close()
                            output?.close()
                        } catch (_: IOException) {}
                    }
                }
            },
            PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .build()
        )
    }
}
