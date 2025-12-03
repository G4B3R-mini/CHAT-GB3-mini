package com.g4b3r.google_app_ia.llmInference

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.ProgressListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull


/**
 * A wrapper class for MediaPipe's `LlmInference` to manage and execute local Large Language Models (LLMs).
 *
 * This class simplifies the process of loading an LLM from a given file path, configuring its settings,
 * and running inference tasks both synchronously and asynchronously. It provides status updates
 * throughout the lifecycle of model loading and inference via a callback mechanism inherited from the `Status` class.
 *
 * @property ctx The application context, required for accessing files and creating the LlmInference instance.
 * @property llm_settiggs A map of settings for the LLM, such as `max_tokens` and `top_k`.
 * @property system A system prompt or initial instruction to prepend to user inputs during inference.
 * @property inferenceCall A callback lambda function that is invoked to report status changes.
 *                         It receives a Boolean indicating overall success and a String for detailed status messages.
 */
@SuppressLint("FileEndsWithExt")
class Engine(
    var ctx: Context,
    var llm_settiggs: Map<String, Any>?,
    var system: String = "",
     var inferenceCall: ((Boolean, String) -> Unit)?
): Interface //:Status(inferenceCall)
{

    lateinit var llmInference: LlmInference
    var llmPath: LLMPath = LLMPath(ctx)
    var llm_name: String = ""
    var buildStatus = false
    var modelsManager = Models(
        llmPath, ctx,
        inferenceCall = inferenceCall
    )

    companion object {
        private const val TAG = "LLM_Manager"
    }

    fun callback(status: Boolean =false, statusStr: String =""){
        Log.w("STATUS", "new statusStr:  $statusStr $status")
        inferenceCall?.invoke(status, statusStr)
    }


    override fun inference(
        uri: Uri?,
        fileCache: String?,
        statusFun: ((Boolean) -> Unit)?
    ): Boolean {
        var status = false
        CoroutineScope(Dispatchers.IO).launch {


            // Função para copiar a Uri para um arquivo temporário e retornar o caminho


            // Dentro da sua atividade ou fragmento, use assim:
            // val uri: Uri = // sua Uri aqui, obtida via SAF

            var filePath: String? = null
            Log.d(TAG, "Starting coroutine to copy file")

            if (uri is Uri) {

                Log.d(TAG, "Coroutine started on IO dispatcher")

                val startTime = System.currentTimeMillis()
                filePath = getFilePathFromUri(ctx, uri)
                val endTime = System.currentTimeMillis()

                Log.d(TAG, "File copy operation completed in ${endTime - startTime}ms")


            } else if (fileCache is String) {
                filePath = fileCache
            } else {
                buildStatus = false
            }

            if (filePath != null) {
                llm_name = filePath
            }

            if (filePath != null) {
                Log.d(TAG, "File path obtained successfully: $filePath")

                try {
                    callback(statusStr = "Building")
                    Log.d(TAG, "Building LlmInferenceOptions...")
                    val taskOption = LlmInference.LlmInferenceOptions.builder()
                        .setModelPath(filePath)
                        .setMaxTokens((llm_settiggs?.get("mxx_tokens") ?: 500) as Int)// O número máximo de tokens (tokens de entrada + tokens de saída) que o modelo processa.
                        .setMaxTopK((llm_settiggs?.get("topk") ?: 40) as Int) // O número de tokens que o modelo considera em cada etapa da geração. Limita as previsões aos k tokens mais prováveis.
                        .build()
                    Log.d(TAG, "LlmInferenceOptions built successfully")

                    Log.d(TAG, "Creating LlmInference instance...")
                    callback(statusStr = "LlmInference instance")
                    // O arquivo temporário será usado aqui
                    try {
                        System.loadLibrary("llm_inference_engine_jni")
                        Log.i("LLM", "✅ Biblioteca JNI carregada com sucesso!")
                    } catch (e: UnsatisfiedLinkError) {
                        Log.e("LLM", "❌ Falha ao carregar JNI: ${e.message}")
                    }

                    llmInference = LlmInference.createFromOptions(ctx, taskOption)
                    Log.d(TAG, "LlmInference instance created successfully")

                    //  Log.d(TAG, "Calling task() with hello prompt")
                    //  this@llm_debug.task("hello")
                    status = true
                    buildStatus = true
                    callback(status)
                } catch (e: Exception) {
                    buildStatus = false
                    Log.e(TAG, "Error creating LlmInference: ${e.message}", e)
                    callback(statusStr = "Error creating llm inference")
                }

            } else {
                Log.e(TAG, "Failed to get file path from Uri")
                buildStatus = false
                callback(statusStr = "Failed file")

            }

            callback(status)
        }
        return false
    }




    override  fun task(inputPrompt: String, action: () -> Unit): String? {
        Log.d(TAG, "task() called with prompt: '$inputPrompt'")
        callback(statusStr = "Not init")
        try {
            if (!::llmInference.isInitialized) {
                Log.e(TAG, "LlmInference not initialized yet")
                return null
            }

            Log.d(TAG, "Generating response...")
            callback(statusStr = "Generating...")
            val startTime = System.currentTimeMillis()

            val result = llmInference.generateResponse("$system ,to inglish: $inputPrompt")

            val endTime = System.currentTimeMillis()
            val responseTime = endTime - startTime

            Log.d(TAG, "Response generated in ${responseTime}ms")
            Log.d(TAG, "Response length: ${result?.length ?: 0} characters")
            Log.d(TAG, "Result: $result")
            action.invoke()
            callback(statusStr = "Generated")
            CoroutineScope(Dispatchers.Default).launch {
                withTimeoutOrNull(1500, { callback(status = true) })
            }
            return result

        } catch (e: Exception) {
            Log.e(TAG, "Error generating response: ${e.message}", e)
            callback(statusStr = "Error generating")
        }
        return "undefined"
    }


    override   fun taskAsync(inputPrompt: String, progressListener: ProgressListener<String>, action: () -> Unit) {
        Log.d(TAG, "task() called with prompt: '$inputPrompt'")
        callback(statusStr = "Not init")
        try {
            if (!::llmInference.isInitialized) {
                Log.e(TAG, "LlmInference not initialized yet")
                return
            }

            Log.d(TAG, "Generating response...")
            callback(statusStr = "Generating...")
            val startTime = System.currentTimeMillis()
            var fullResponse = ""

            llmInference.generateResponseAsync(inputPrompt, progressListener)

            val endTime = System.currentTimeMillis()
            val responseTime = endTime - startTime

            Log.d(TAG, "Response generated in ${responseTime}ms")
//            Log.d(TAG, "Response length: ${result?.length ?: 0} characters")
//            Log.d(TAG, "Result: $result")
            action?.invoke()
            callback(statusStr = "Generated")
            CoroutineScope(Dispatchers.Default).launch {
                withTimeoutOrNull(1500, { callback(status = true) })
            }


        } catch (e: Exception) {
            Log.e(TAG, "Error generating response: ${e.message}", e)
            callback(statusStr = "Error generating")
        }

    }

    override fun getFilePathFromUri(ctx: Context, uri: Uri, callback:(()->Unit)?): String? {
        return modelsManager.getFilePathFromUri(ctx, uri, { callback?.invoke() })
    }

}


