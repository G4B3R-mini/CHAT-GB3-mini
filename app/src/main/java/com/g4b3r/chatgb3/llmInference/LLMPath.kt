package com.g4b3r.google_app_ia.llmInference

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.g4b3r.google_app_ia.core.Base
import java.io.File

class LLMPath(var ctx: Context,var pathName: String ="models"): Base() {
    lateinit var LLMDir: File


    init {
        createSubDir()
    }

    fun createSubDir(){
        val cacheDir = ctx.cacheDir // Obtém o diretório de cache
        LLMDir = File(cacheDir, "minhaSubPasta") // Define o caminho da subpasta

        if (!LLMDir.exists()) { // Verifica se a pasta já existe
            val create = LLMDir.mkdirs() // Cria a subpasta
            if (create) {
                println("Subpasta criada com sucesso!")
            } else {
                println("Falha ao criar a subpasta.")
            }
        } else {
            println("A subpasta já existe.")
        }

    }
    fun getFileNameFromUri(contentResolver: ContentResolver, uri: Uri?, callback: (String) -> Unit): String? {
        callback("get Uri")
        var fileName: String? = null
        val cursor: Cursor? = uri?.let { contentResolver.query(it, null, null, null, null) }
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }
 fun   createLLMFile(name: String): File {
     return File(this.LLMDir, name)
    }
    fun getFileFronLLMDIR(fileName: String): File? {
      return this.LLMDir.listFiles()?.find { it.name == fileName }
    }
}