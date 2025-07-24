package enlaceschaco.ar

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.delay
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color
import android.webkit.WebChromeClient
import androidx.activity.addCallback


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var showWebView by remember { mutableStateOf(false) }
            var splashIndex by remember { mutableStateOf(0) }

            val splashLogos = listOf(
                R.drawable.logo1,
                R.drawable.logo2,
                R.drawable.logo3
            )

            // Rota cada 1 seg
            LaunchedEffect(splashIndex, showWebView) {
                while (!showWebView) {
                    delay(5000)
                    splashIndex = (splashIndex + 1) % splashLogos.size
                }
            }

            // Dura 3 seg el splash
            LaunchedEffect(Unit) {
                delay(5000)
                showWebView = true
            }

            if (showWebView) {

                WebViewPage("https://enlaceschacopos.up.railway.app/")
            } else {
                SplashScreen(splashLogos[splashIndex])
            }

        }
        // Bloquea el botón atrás para TODO el Activity
        onBackPressedDispatcher.addCallback(this) {
            // Dejar vacío para bloquear completamente
            // Si quisieras, podés mostrar un Toast aquí
        }
    }

}

@Composable
fun SplashScreen(resId: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001B47)), // Cambia el color por tu color institucional
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlideImage(
                imageModel = { resId },
                modifier = Modifier.size(280.dp) // Más grande que antes
            )
            Spacer(modifier = Modifier.height(32.dp))

        }
    }
}


@Composable
fun WebViewPage(url: String) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                //setLayerType(View.LAYER_TYPE_SOFTWARE, null)

                webViewClient = WebViewClient()
                // ¡Agregá esto!
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage): Boolean {
                        android.util.Log.d("WEBVIEW", "${consoleMessage.message()} -- From line " +
                                "${consoleMessage.lineNumber()} of ${consoleMessage.sourceId()}")
                        return true
                    }
                }
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.userAgentString = "Mozilla/5.0 (Linux; Android 9; Mobile; rv:89.0) Gecko/89.0 Firefox/89.0"

                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.javaScriptCanOpenWindowsAutomatically = true
                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}


