package com.g4b3r.google_app_ia.llmInference

import android.content.Context
import android.net.Uri
import android.util.Log
import com.g4b3r.google_app_ia.manager.Status
import java.io.FileOutputStream

class Models(
    var llmPath: LLMPath,
    var ctx: Context,
    override var inferenceCall: ((Boolean, String) -> Unit)?
) : Status(inferenceCall) {
    val TAG: String? = "MainActivity"

    fun getFilePathFromUri(
        context: Context,
        uri: Uri?,
        finish: () -> Unit
    ): String? {
        Log.d(TAG, "getFilePathFromUri() started")

        if (uri == null) {
            Log.e(TAG, "Uri is null, cannot proceed")
            return null
        }
        var name: String = "temp_model_file.task"
        var fileName: String? = llmPath.getFileNameFromUri(ctx.contentResolver, uri, { str ->
            callback(statusStr = str)
        })
        if (fileName != null) {
            name = fileName
        }
        // Cria um arquivo temporário no diretório de cache do app
        val tempFile = llmPath.createLLMFile(name)
        Log.d(TAG, "Created temp file: ${tempFile.absolutePath}")

        return try {
            Log.d(TAG, "Starting file copy from Uri to temp file")
            callback(statusStr = "Copy")
            // Usa o ContentResolver para abrir um InputStream da Uri
            uri.let { ctx.contentResolver.openInputStream(it) }?.use { inputStream ->
                Log.d(TAG, "InputStream opened successfully")

                // Copia o conteúdo do InputStream para o arquivo temporário
                FileOutputStream(tempFile).use { outputStream ->
                    val bytesWritten = inputStream.copyTo(outputStream)
                    Log.d(TAG, "File copied successfully. Bytes written: $bytesWritten")
                }
            }

            val filePath = tempFile.absolutePath
            Log.d(TAG, "File copy completed. File path: $filePath")
            Log.d(TAG, "File size: ${tempFile.length()} bytes")
            finish.invoke()
            // Retorna o caminho do arquivo temporário
            filePath
        } catch (e: Exception) {
            Log.e(TAG, "Error copying file from Uri: ${e.message}", e)
            // callback(statusStr = "Error Copy")
            e.printStackTrace()
            null
        }
    }
}