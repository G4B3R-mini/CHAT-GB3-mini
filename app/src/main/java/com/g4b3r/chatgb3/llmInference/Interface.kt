package com.g4b3r.google_app_ia.llmInference

import android.content.Context
import android.net.Uri
import com.google.mediapipe.tasks.genai.llminference.ProgressListener

interface Interface {


    fun getFilePathFromUri(ctx: Context, uri: Uri, callback: (() -> Unit)? = null): String?

    fun inference(
        uri: Uri?,
        fileCache: String? = null,
        statusFun: ((Boolean) -> Unit)? = null
    ): Boolean

    fun task(inputPrompt: String, action: () -> Unit = {}): String?
    fun taskAsync(
        inputPrompt: String,
        progressListener: ProgressListener<String>,
        action: () -> Unit = {}
    )
}