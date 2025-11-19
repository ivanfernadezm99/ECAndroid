package enlaceschaco.ar

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val updateMessage: String? = null,
    val forceUpdate: Boolean = false
)

class UpdateManager(private val context: Context) {
    
    companion object {
        private const val TAG = "UpdateManager"
        // URL del endpoint que devuelve la información de actualización
        // Puedes cambiar esto por tu propio servidor
        private const val UPDATE_CHECK_URL = "https://enlaceschacopos.up.railway.app/api/update-check.json"
        
        // O si prefieres usar un archivo estático en tu servidor:
        // private const val UPDATE_CHECK_URL = "https://enlaceschacopos.up.railway.app/update.json"
    }
    
    /**
     * Verifica si hay una actualización disponible
     */
    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val currentVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
            
            Log.d(TAG, "Versión actual: $currentVersionCode")
            
            val url = URL(UPDATE_CHECK_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                
                val latestVersionCode = json.getInt("versionCode")
                val latestVersionName = json.getString("versionName")
                val apkUrl = json.getString("apkUrl")
                val updateMessage = json.optString("updateMessage", null)
                val forceUpdate = json.optBoolean("forceUpdate", false)
                
                Log.d(TAG, "Versión disponible: $latestVersionCode")
                
                if (latestVersionCode > currentVersionCode) {
                    return@withContext UpdateInfo(
                        versionCode = latestVersionCode,
                        versionName = latestVersionName,
                        apkUrl = apkUrl,
                        updateMessage = updateMessage,
                        forceUpdate = forceUpdate
                    )
                }
            } else {
                Log.e(TAG, "Error al verificar actualización: $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar actualización", e)
        }
        null
    }
    
    /**
     * Descarga e instala la actualización
     */
    fun downloadAndInstall(updateInfo: UpdateInfo) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            
            val request = DownloadManager.Request(Uri.parse(updateInfo.apkUrl)).apply {
                setTitle("Actualizando ${context.getString(R.string.app_name)}")
                setDescription("Descargando versión ${updateInfo.versionName}")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "EnlacesChaco-${updateInfo.versionName}.apk")
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }
            
            val downloadId = downloadManager.enqueue(request)
            
            // Registrar un BroadcastReceiver para instalar cuando termine la descarga
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        installApk(downloadManager.getUriForDownloadedFile(downloadId))
                        context.unregisterReceiver(this)
                    }
                }
            }
            
            context.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al descargar actualización", e)
        }
    }
    
    /**
     * Instala el APK descargado
     */
    private fun installApk(apkUri: Uri?) {
        if (apkUri == null) {
            Log.e(TAG, "URI del APK es null")
            return
        }
        
        try {
            val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                flags = flags or Intent.FLAG_GRANT_READ_URI_PERMISSION
                
                // Para Android 8.0+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags = flags or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            }
            
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error al instalar APK", e)
        }
    }
    
    /**
     * Método alternativo usando FileProvider para mejor compatibilidad
     */
    fun downloadAndInstallWithFileProvider(updateInfo: UpdateInfo) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            
            val fileName = "EnlacesChaco-${updateInfo.versionName}.apk"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            
            val request = DownloadManager.Request(Uri.parse(updateInfo.apkUrl)).apply {
                setTitle("Actualizando ${context.getString(R.string.app_name)}")
                setDescription("Descargando versión ${updateInfo.versionName}")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationUri(Uri.fromFile(file))
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }
            
            val downloadId = downloadManager.enqueue(request)
            
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        val downloadedFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
                        if (downloadedFile.exists()) {
                            installApkWithFileProvider(downloadedFile)
                        }
                        context.unregisterReceiver(this)
                    }
                }
            }
            
            context.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al descargar actualización", e)
        }
    }
    
    private fun installApkWithFileProvider(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                flags = flags or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error al instalar APK con FileProvider", e)
        }
    }
}

