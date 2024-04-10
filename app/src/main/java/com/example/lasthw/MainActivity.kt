package com.example.lasthw

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {

    private lateinit var responseTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.api_post);

        responseTextView = findViewById(R.id.responseTextView)

        // coroutine to send POST request
        GlobalScope.launch(Dispatchers.IO) {
            val response = sendPostRequest()
            val message = parseMessage(response)
            updateUI(message)
        }
    }

    private suspend fun sendPostRequest(): String {
        val url = URL("https://efa59163ec054efcbfc6ec50cfe922f1.api.mockbin.io/")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")

        // Create JSON object
        val jsonObject = JSONObject()
        jsonObject.put("creator", "The last home work!!!")
        jsonObject.put("success", true)

        // Convert JSON object to string
        val jsonInputString = jsonObject.toString()

        return try {
            // Write JSON data to output stream
            val outputStream = BufferedOutputStream(connection.outputStream)
            outputStream.write(jsonInputString.toByteArray())
            outputStream.flush()

            // Get response from server
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                response.toString()
            } else {
                "Error: ${connection.responseMessage}"
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseMessage(response: String): String {
        // Parse JSON response and extract message
        return try {
            val jsonObject = JSONObject(response)
            jsonObject.getString("message")
        } catch (e: Exception) {
            "Error parsing response"
        }
    }

    private fun updateUI(message: String) {
        // Update UI in the main thread
        runOnUiThread {
            responseTextView.text = message
        }
    }
}
