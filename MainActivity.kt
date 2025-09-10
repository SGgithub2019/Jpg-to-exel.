package com.example.ocrtoexcel

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var selectedUri: Uri? = null
    private lateinit var tvStatus: TextView

    private val pickImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            selectedUri = it
            tvStatus.text = "Kiválasztva: ${getFileName(it)}"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnPick = findViewById<Button>(R.id.btnPick)
        val btnProcess = findViewById<Button>(R.id.btnProcess)
        tvStatus = findViewById(R.id.tvStatus)

        btnPick.setOnClickListener { pickImage.launch(arrayOf("image/*")) }
        btnProcess.setOnClickListener { selectedUri?.let { processImageAndSave(it) } ?: run { tvStatus.text = "Először válassz egy képet!" } }
    }

    private fun processImageAndSave(uri: Uri) {
        tvStatus.text = "OCR folyamat..."
        try {
            val image = InputImage.fromFilePath(this, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    tvStatus.text = "OCR kész, mentés Excel-be..."
                    saveTextAsExcel(text)
                }
                .addOnFailureListener { e ->
                    tvStatus.text = "OCR hiba: ${e.message}"
                    e.printStackTrace()
                }
        } catch (e: Exception) {
            tvStatus.text = "Hiba: ${e.message}"
            e.printStackTrace()
        }
    }

    private fun saveTextAsExcel(text: String) {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Data")

            val rows = text.split('\n')
            for ((i, row) in rows.withIndex()) {
                val excelRow = sheet.createRow(i)
                val cols = row.trim().split(Regex("\t|\s{2,}|\s"))
                for ((j, col) in cols.withIndex()) {
                    excelRow.createCell(j).setCellValue(col)
                }
            }

            val downloads = getExternalFilesDir(null)?.parentFile?.parentFile?.absolutePath ?: filesDir.absolutePath
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val outPath = "$downloads/Download/ocr_output_$timestamp.xlsx"
            val outFile = File(outPath)
            outFile.parentFile?.mkdirs()

            FileOutputStream(outFile).use { fos ->
                workbook.write(fos)
            }
            workbook.close()

            tvStatus.text = "Mentve: $outPath"

        } catch (e: Exception) {
            tvStatus.text = "Mentés hiba: ${e.message}"
            e.printStackTrace()
        }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            try {
                cursor?.let {
                    if (it.moveToFirst()) {
                        result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }
}
