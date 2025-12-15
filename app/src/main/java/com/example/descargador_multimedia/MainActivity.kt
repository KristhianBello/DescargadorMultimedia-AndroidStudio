package com.example.descargador_multimedia

import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    // ⚠️ IMPORTANTE: IP de tu máquina en la red local
    // Tienes dos opciones:
    // - 192.168.1.5 (Ethernet - Cable)
    // - 192.168.1.6 (WiFi - Inalámbrico) ← RECOMENDADO
    // Asegúrate que el servidor está corriendo: python -m http.server 8000
    private val BASE_URL = "http://192.168.1.6:8000"

    private lateinit var urlEdit: EditText
    private lateinit var btnGetInfo: Button
    private lateinit var infoText: TextView
    private lateinit var btnMp4: Button
    private lateinit var btnMp3: Button
    private var progressBar: ProgressBar? = null
    private var progressText: TextView? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "onCreate: Layout inflated")

        // Solicitar permisos si es necesario
        requestStoragePermissions()

        try {
            Log.d("MainActivity", "Iniciando findViewById...")
            urlEdit = findViewById(R.id.urlEdit)
            Log.d("MainActivity", "urlEdit encontrado")
            btnGetInfo = findViewById(R.id.btnGetInfo)
            Log.d("MainActivity", "btnGetInfo encontrado")
            infoText = findViewById(R.id.infoText)
            Log.d("MainActivity", "infoText encontrado")
            btnMp4 = findViewById(R.id.btnDownloadMp4)
            Log.d("MainActivity", "btnMp4 encontrado")
            btnMp3 = findViewById(R.id.btnDownloadMp3)
            Log.d("MainActivity", "btnMp3 encontrado")
            progressBar = findViewById(R.id.progressBar)
            Log.d("MainActivity", "progressBar encontrado: $progressBar")
            progressText = findViewById(R.id.progressText)
            Log.d("MainActivity", "progressText encontrado")
            Log.d("MainActivity", "Todas las vistas encontradas correctamente")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error inicializando vistas", e)
            Toast.makeText(this, "Error inicializando vistas: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }

        btnGetInfo.setOnClickListener {
            val url = urlEdit.text.toString().trim()
            if (url.isNotEmpty()) getInfo(url)
            else Toast.makeText(this@MainActivity, "Pega una URL", Toast.LENGTH_SHORT).show()
        }

        btnMp4.setOnClickListener {
            val url = urlEdit.text.toString().trim()
            if (url.isNotEmpty()) downloadFromBackend(url, toMp3 = false)
            else Toast.makeText(this@MainActivity, "Pega una URL", Toast.LENGTH_SHORT).show()
        }

        btnMp3.setOnClickListener {
            val url = urlEdit.text.toString().trim()
            if (url.isNotEmpty()) downloadFromBackend(url, toMp3 = true)
            else Toast.makeText(this@MainActivity, "Pega una URL", Toast.LENGTH_SHORT).show()
        }

        // Probar conexión al iniciar
        testConnection()
    }

    private fun testConnection() {
        Log.d("testConnection", "=== INICIANDO DIAGNÓSTICO DE CONEXIÓN ===")
        Log.d("testConnection", "IP Configurada: $BASE_URL")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("testConnection", "Probando conexión a: $BASE_URL")
                val request = Request.Builder()
                    .url("$BASE_URL/")
                    .get()
                    .build()

                Log.d("testConnection", "Request construido: ${request.url}")

                client.newCall(request).execute().use { response ->
                    Log.d("testConnection", "✅ Conexión exitosa!")
                    Log.d("testConnection", "Código HTTP: ${response.code}")
                    Log.d("testConnection", "Headers: ${response.headers}")

                    withContext(Dispatchers.Main) {
                        // Mostrar que está conectado
                        Toast.makeText(this@MainActivity, "✅ Servidor conectado", Toast.LENGTH_SHORT).show()
                        @Suppress("SetTextI18n")
                        infoText.text = "✅ Conectado a $BASE_URL"
                    }
                }
            } catch (e: java.net.ConnectException) {
                Log.e("testConnection", "❌ ERROR DE CONEXIÓN: No se puede alcanzar el servidor")
                Log.e("testConnection", "Detalles: ${e.message}", e)

                withContext(Dispatchers.Main) {
                    @Suppress("SetTextI18n")
                    infoText.text = "❌ Error de conexión\n\nNo se puede conectar a:\n$BASE_URL\n\nVerifica:\n1. Servidor activo\n2. IP correcta\n3. Misma red WiFi"
                    Toast.makeText(this@MainActivity, "❌ No se conecta al servidor", Toast.LENGTH_LONG).show()
                }
            } catch (e: java.net.UnknownHostException) {
                Log.e("testConnection", "❌ ERROR DE HOST: No se puede resolver la IP")
                Log.e("testConnection", "Detalles: ${e.message}", e)

                withContext(Dispatchers.Main) {
                    @Suppress("SetTextI18n")
                    infoText.text = "❌ Error: IP no disponible\n\nNo se puede resolver: $BASE_URL\n\nVerifica la IP con:\nipconfig"
                    Toast.makeText(this@MainActivity, "❌ IP no resuelta", Toast.LENGTH_LONG).show()
                }
            } catch (e: java.net.SocketTimeoutException) {
                Log.e("testConnection", "❌ TIMEOUT: Servidor tardó demasiado en responder")
                Log.e("testConnection", "Detalles: ${e.message}", e)

                withContext(Dispatchers.Main) {
                    @Suppress("SetTextI18n")
                    infoText.text = "⏱️ Timeout\n\nEl servidor tardó demasiado"
                    Toast.makeText(this@MainActivity, "⏱️ Timeout de conexión", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("testConnection", "❌ ERROR INESPERADO: ${e::class.simpleName}")
                Log.e("testConnection", "Mensaje: ${e.message}", e)

                withContext(Dispatchers.Main) {
                    @Suppress("SetTextI18n")
                    infoText.text = "❌ Error: ${e.message ?: "Desconocido"}"
                    Toast.makeText(this@MainActivity, "❌ ${e.message ?: "Error desconocido"}", Toast.LENGTH_LONG).show()
                }
            }

            Log.d("testConnection", "=== FIN DEL DIAGNÓSTICO ===")
        }
    }

    private fun getInfo(url: String) {
        @Suppress("SetTextI18n")
        infoText.text = "Consultando..."
        val formBody = FormBody.Builder()
            .add("url", url)
            .build()

        val request = Request.Builder()
            .url("$BASE_URL/info")
            .post(formBody)
            .build()

        // Ejecutar en background con coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("getInfo", "Enviando solicitud a: $BASE_URL/info")
                client.newCall(request).execute().use { resp ->
                    Log.d("getInfo", "Respuesta recibida: ${resp.code}")
                    val body = resp.body?.string()
                    if (!resp.isSuccessful || body == null) {
                        withContext(Dispatchers.Main) {
                            @Suppress("SetTextI18n")
                            infoText.text = "Error: ${resp.code} - ${resp.message}"
                            Log.e("getInfo", "Error en respuesta: ${resp.message}")
                        }
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        val map = gson.fromJson(body, Map::class.java) as Map<String, Any>
                        val title = map["title"] ?: "Sin título"
                        withContext(Dispatchers.Main) {
                            @Suppress("SetTextI18n")
                            infoText.text = "Título: $title"
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("getInfo", "Excepción: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    val errorMsg = when (e) {
                        is java.net.ConnectException -> "No se puede conectar a $BASE_URL. Verifica que:\n1. El servidor esté activo\n2. La IP sea correcta\n3. Estés en la misma red"
                        is java.net.UnknownHostException -> "No se puede resolver $BASE_URL. Verifica la IP."
                        else -> "Error: ${e.localizedMessage ?: e.message}"
                    }
                    @Suppress("SetTextI18n")
                    infoText.text = errorMsg
                }
            }
        }
    }

    private fun downloadFromBackend(url: String, toMp3: Boolean) {
        progressBar?.progress = 0
        progressBar?.visibility = ProgressBar.VISIBLE
        @Suppress("SetTextI18n")
        progressText?.text = "Preparando..."
        btnMp4.isEnabled = false
        btnMp3.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val formBuilder = FormBody.Builder().add("url", url)
                if (toMp3) formBuilder.add("to_mp3", "true")
                val formBody = formBuilder.build()

                val request = Request.Builder()
                    .url("$BASE_URL/download")
                    .post(formBody)
                    .build()

                Log.d("downloadFromBackend", "Iniciando descarga desde: $BASE_URL/download")
                client.newCall(request).execute().use { response ->
                    Log.d("downloadFromBackend", "Respuesta recibida: ${response.code}")
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string() ?: response.message
                        Log.e("downloadFromBackend", "Error del servidor: $errorBody")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Error ${response.code}: ${response.message}", Toast.LENGTH_LONG).show()
                            @Suppress("SetTextI18n")
                            infoText.text = "Error al descargar: ${response.code}"
                            progressBar?.visibility = ProgressBar.GONE
                            btnMp4.isEnabled = true
                            btnMp3.isEnabled = true
                        }
                        return@use
                    }

                    // Nombre de archivo: intenta obtener de headers o usa timestamp
                    val disposition = response.header("Content-Disposition")
                    val filename = disposition?.let {
                        // intenta extraer filename="..."
                        Regex("filename=\"(.*)\"").find(it)?.groupValues?.get(1)
                    } ?: run {
                        val ext = if (toMp3) "mp3" else "mp4"
                        "download_${System.currentTimeMillis()}.$ext"
                    }

                    Log.d("downloadFromBackend", "Nombre de archivo: $filename")

                    // Carpeta de la app en almacenamiento externo
                    val destDir = getExternalFilesDir(null) // app-specific external dir
                    if (destDir == null) {
                        Log.e("downloadFromBackend", "No hay almacenamiento accesible")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "No hay almacenamiento accesible", Toast.LENGTH_LONG).show()
                        }
                        return@use
                    }
                    val outFile = File(destDir, filename)

                    // Stream y progreso
                    val body = response.body
                    val contentLength = body?.contentLength() ?: -1L
                    Log.d("downloadFromBackend", "Tamaño total: $contentLength bytes")
                    val inputStream: InputStream = body?.byteStream() ?: throw Exception("No body")
                    var output: FileOutputStream? = null
                    try {
                        output = FileOutputStream(outFile)
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        var totalRead = 0L
                        while (true) {
                            bytesRead = inputStream.read(buffer)
                            if (bytesRead == -1) break
                            output.write(buffer, 0, bytesRead)
                            totalRead += bytesRead
                            if (contentLength > 0) {
                                val percent = (100.0 * totalRead / contentLength).toInt()
                                withContext(Dispatchers.Main) {
                                    progressBar?.progress = percent
                                    @Suppress("SetTextI18n")
                                    progressText?.text = "Descargando: $percent%"
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    @Suppress("SetTextI18n")
                                    progressText?.text = "Descargando: ${totalRead / 1024} KB"
                                }
                            }
                        }
                        output.flush()
                        Log.d("downloadFromBackend", "Descarga completada: $totalRead bytes")
                    } finally {
                        inputStream.close()
                        output?.close()
                    }

                    withContext(Dispatchers.Main) {
                        progressBar?.visibility = ProgressBar.GONE

                        // Guardar en galería o descargas según el tipo
                        val savedUri = if (toMp3) {
                            saveAudioToDownloads(outFile)
                        } else {
                            saveVideoToGallery(outFile)
                        }

                        @Suppress("SetTextI18n")
                        progressText?.text = "La descarga se completó"
                        @Suppress("SetTextI18n")
                        infoText.text = ""
                        btnMp4.isEnabled = true
                        btnMp3.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("downloadFromBackend", "Excepción: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        progressBar?.visibility = ProgressBar.GONE
                        val errorMsg = when (e) {
                            is java.net.ConnectException -> "No se puede conectar a $BASE_URL"
                            is java.net.UnknownHostException -> "Error: No se puede resolver $BASE_URL"
                            is java.net.SocketTimeoutException -> "Tiempo de conexión agotado"
                            else -> e.localizedMessage ?: e.message
                        }
                        @Suppress("SetTextI18n")
                        progressText?.text = "Error: $errorMsg"
                        @Suppress("SetTextI18n")
                        infoText.text = ""
                        btnMp4.isEnabled = true
                        btnMp3.isEnabled = true
                    }
            }
        }
    }

    private fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+: Necesita READ_EXTERNAL_STORAGE y WRITE_EXTERNAL_STORAGE
            val permissions = arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_MEDIA_LOCATION
            )
            val permissionsToRequest = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }.toTypedArray()

            if (permissionsToRequest.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsToRequest, 100)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0 - 10: Solicitar WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    100
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show()
            } else {
                // Permisos denegados, sin mostrar mensaje
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Limpiar la información cuando la app va a segundo plano
        clearDownloadInfo()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Limpiar la información cuando la app se cierra
        clearDownloadInfo()
    }

    private fun clearDownloadInfo() {
        progressBar?.visibility = ProgressBar.GONE
        progressText?.text = ""
        infoText.text = ""
    }

    // ...existing code...

    // Guarda video en la galería de Videos
    private fun saveVideoToGallery(file: File): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/DescargadorMom")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)

        uri?.let {
            resolver.openOutputStream(it)?.use { out ->
                file.inputStream().use { input -> input.copyTo(out) }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
        }

        return uri
    }

    // Guarda audio en la carpeta de Descarga
    private fun saveAudioToDownloads(file: File): Uri? {
        // MediaStore.Downloads solo está disponible en API 30+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return null
        }

        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, file.name)
            put(MediaStore.Downloads.MIME_TYPE, "audio/mpeg")

            put(MediaStore.Downloads.RELATIVE_PATH, "Download")
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)

        uri?.let {
            resolver.openOutputStream(it)?.use { out ->
                file.inputStream().use { input -> input.copyTo(out) }
            }
        }

        return uri
    }
}