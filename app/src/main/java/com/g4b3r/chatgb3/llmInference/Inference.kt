package com.g4b3r.chatgb3.llmInference//package com.g4b3r.google_app_ia.llmInference
//
//import android.net.Uri
//import android.util.Log
//
//import com.google.mediapipe.tasks.genai.llminference.LlmInference
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//
//class Inference(val uri: Uri?,
//                val fileCache: String?,
//                val statusFun: ((Boolean) -> Unit)?,
//    val TAG: String
//) {
//    init
//
//           {
//            var status = false
//            CoroutineScope(Dispatchers.IO).launch {
//
//
//                // Função para copiar a Uri para um arquivo temporário e retornar o caminho
//
//
//                // Dentro da sua atividade ou fragmento, use assim:
//                // val uri: Uri = // sua Uri aqui, obtida via SAF
//
//                var filePath: String? = null
//                Log.d(TAG, "Starting coroutine to copy file")
//
//                if (uri is Uri) {
//
//                    Log.d(TAG, "Coroutine started on IO dispatcher")
//
//                    val startTime = System.currentTimeMillis()
//                    filePath = getFilePathFromUri(ctx, uri)
//                    val endTime = System.currentTimeMillis()
//
//                    Log.d(TAG, "File copy operation completed in ${endTime - startTime}ms")
//
//
//                } else if (fileCache is String) {
//                    filePath = fileCache
//                } else {
//                    buildStatus = false
//                }
//
//                if (filePath != null) {
//                    llm_name = filePath
//                }
//
//                if (filePath != null) {
//                    Log.d(TAG, "File path obtained successfully: $filePath")
//
//                    try {
//                        callback(statusStr = "Building")
//                        Log.d(TAG, "Building LlmInferenceOptions...")
//                        val taskOption = LlmInference.LlmInferenceOptions.builder()
//                            .setModelPath(filePath)
//                            .setMaxTokens((llm_settiggs?.get("mxx_tokens") ?: 500) as Int)// O número máximo de tokens (tokens de entrada + tokens de saída) que o modelo processa.
//                            .setMaxTopK((llm_settiggs?.get("topk") ?: 40) as Int) // O número de tokens que o modelo considera em cada etapa da geração. Limita as previsões aos k tokens mais prováveis.
//                            .build()
//                        Log.d(TAG, "LlmInferenceOptions built successfully")
//
//                        Log.d(TAG, "Creating LlmInference instance...")
//                        callback(statusStr = "LlmInference instance")
//                        // O arquivo temporário será usado aqui
//                        try {
//                            System.loadLibrary("llm_inference_engine_jni")
//                            Log.i("LLM", "✅ Biblioteca JNI carregada com sucesso!")
//                        } catch (e: UnsatisfiedLinkError) {
//                            Log.e("LLM", "❌ Falha ao carregar JNI: ${e.message}")
//                        }
//
//                        llmInference = LlmInference.createFromOptions(ctx, taskOption)
//                        Log.d(TAG, "LlmInference instance created successfully")
//
//                        //  Log.d(TAG, "Calling task() with hello prompt")
//                        //  this@llm_debug.task("hello")
//                        status = true
//                        buildStatus = true
//                        callback(status)
//                    } catch (e: Exception) {
//                        buildStatus = false
//                        Log.e(TAG, "Error creating LlmInference: ${e.message}", e)
//                        callback(statusStr = "Error creating llm inference")
//                    }
//
//                } else {
//                    Log.e(TAG, "Failed to get file path from Uri")
//                    buildStatus = false
//                    callback(statusStr = "Failed file")
//
//                }
//
//                callback(status)
//            }
//
//        }
//
//}