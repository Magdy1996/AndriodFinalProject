package com.example.yumyum.genai

import android.util.Log
import com.example.yumyum.BuildConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Minimal HTTP client to call OpenAI's chat completions API without external dependencies.
 * Note: Requires an API key (pass as parameter). In production keep keys out of source.
 */
@Singleton
class GenAiRepository @Inject constructor() {

    /**
     * Call OpenAI chat completions and return the primary generated text (choices[0].message.content).
     * Throws an exception with informative message for non-2xx responses or network errors.
     */
    suspend fun callOpenAi(prompt: String, apiKey: String, maxTokens: Int = 150): String {
        // Validate API key early so callers (and UI) get a clear, non-network error
        if (apiKey.isBlank()) {
            throw IllegalArgumentException("OpenAI API key is not configured")
        }

        // Debug-only: log a masked version of the key so we can confirm the APK was built with the expected value
        if (BuildConfig.DEBUG) {
            try {
                val masked = if (apiKey.length > 12) apiKey.substring(0, 8) + "..." + apiKey.takeLast(4) else "REDACTED"
                Log.d("GenAiRepository", "OPENAI_API_KEY (masked) = $masked")
            } catch (ignored: Exception) {
            }
        }

        // Perform blocking network I/O on the IO dispatcher to avoid NetworkOnMainThreadException
        return withContext(Dispatchers.IO) {
            val endpoint = "https://api.openai.com/v1/chat/completions"
            val url = URL(endpoint)
            var conn: HttpURLConnection? = null
            try {
                conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Authorization", "Bearer $apiKey")
                    setRequestProperty("User-Agent", "MagdyDiner/1.0")
                    doOutput = true
                    connectTimeout = 15000
                    readTimeout = 15000
                }

                // Escape JSON string characters for the user content
                val escapedPrompt = prompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")

                // Use the Chat Completions API with gpt-3.5-turbo (recommended replacement for older completions models)
                val payload = """
                    {"model":"gpt-3.5-turbo","messages":[{"role":"user","content":"$escapedPrompt"}],"max_tokens":$maxTokens}
                """.trimIndent()

                OutputStreamWriter(conn.outputStream).use { writer ->
                    writer.write(payload)
                    writer.flush()
                }

                val responseCode = conn.responseCode
                val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream

                val response = BufferedReader(InputStreamReader(stream)).use { br ->
                    val sb = StringBuilder()
                    var line = br.readLine()
                    while (line != null) {
                        sb.append(line).append('\n')
                        line = br.readLine()
                    }
                    sb.toString()
                }

                if (responseCode !in 200..299) {
                    // Provide the HTTP status and response body to help debugging (do NOT expose API keys)
                    throw Exception("HTTP $responseCode: $response")
                }

                // Try to parse JSON and extract the generated text from choices[0].message.content
                return@withContext try {
                    val json = JSONObject(response)
                    val choices = json.optJSONArray("choices")
                    if (choices != null && choices.length() > 0) {
                        val choice = choices.getJSONObject(0)
                        // Chat completions provide a `message` object with `content`
                        val message = choice.optJSONObject("message")
                        val content = message?.optString("content", null)
                        if (!content.isNullOrEmpty()) {
                            content.trim()
                        } else {
                            // Fallback: older completions used `text`
                            val text = choice.optString("text", null)
                            if (!text.isNullOrEmpty()) text.trim() else response
                        }
                    } else {
                        response
                    }
                } catch (e: Exception) {
                    // Parsing failed — return raw response for debugging
                    response
                }
            } catch (e: Exception) {
                // Wrap and rethrow to give clearer context to the caller
                throw Exception("GenAI call failed: ${e.message ?: e.javaClass.simpleName}")
            } finally {
                conn?.disconnect()
            }
        }
    }
}
