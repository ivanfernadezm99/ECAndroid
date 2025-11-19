package enlaceschaco.ar

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
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
import android.webkit.WebResourceError
import android.webkit.WebResourceResponse
import kotlinx.coroutines.launch
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text as Text3
import android.widget.Toast
import android.content.pm.PackageManager


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var showWebView by remember { mutableStateOf(false) }
            var splashIndex by remember { mutableStateOf(0) }
            var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
            var showUpdateDialog by remember { mutableStateOf(false) }

            val splashLogos = listOf(
                R.drawable.logo1,
                R.drawable.logo2,
                R.drawable.logo3
            )

            // Mostrar versión al iniciar
            LaunchedEffect(Unit) {
                try {
                    val packageInfo = this@MainActivity.packageManager.getPackageInfo(
                        this@MainActivity.packageName, 0
                    )
                    val versionName = packageInfo.versionName
                    val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        packageInfo.longVersionCode.toInt()
                    } else {
                        @Suppress("DEPRECATION")
                        packageInfo.versionCode
                    }
                    
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        Toast.makeText(
                            this@MainActivity,
                            "Versión $versionName (Build $versionCode)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }, 2000) // Mostrar después de 2 segundos
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error obteniendo versión", e)
                }
            }

            // Verificar actualizaciones al iniciar
            LaunchedEffect(Unit) {
                val updateManager = UpdateManager(this@MainActivity)
                val update = updateManager.checkForUpdate()
                if (update != null) {
                    updateInfo = update
                    showUpdateDialog = true
                }
            }

            // Rota cada 1 seg
            LaunchedEffect(splashIndex, showWebView) {
                while (!showWebView) {
                    delay(5000)
                    splashIndex = (splashIndex + 1) % splashLogos.size
                }
            }

            // Dura 5 seg el splash
            LaunchedEffect(Unit) {
                delay(5000)
                showWebView = true
            }
            
            // Diálogo de actualización
            if (showUpdateDialog && updateInfo != null) {
                UpdateDialog(
                    updateInfo = updateInfo!!,
                    onUpdate = {
                        val updateManager = UpdateManager(this@MainActivity)
                        updateManager.downloadAndInstallWithFileProvider(updateInfo!!)
                        showUpdateDialog = false
                    },
                    onDismiss = {
                        if (!updateInfo!!.forceUpdate) {
                            showUpdateDialog = false
                        }
                    },
                    forceUpdate = updateInfo!!.forceUpdate
                )
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
            // Configurar CookieManager para manejar cookies correctamente
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(null, true)
            
            WebView(context).apply {
                // Configurar WebViewClient personalizado para mejor manejo de autenticación y contenido dinámico
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val url = request?.url?.toString()
                        android.util.Log.d("WEBVIEW", "Navegación a: $url")
                        // Permitir que el WebView maneje todas las URLs, incluyendo navegaciones internas
                        if (url != null && url.startsWith("http")) {
                            view?.loadUrl(url)
                        }
                        return true
                    }
                    
                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        // Permitir todas las solicitudes de recursos (CSS, JS, imágenes, etc.)
                        return super.shouldInterceptRequest(view, request)
                    }
                    
                    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        android.util.Log.d("WEBVIEW", "Página iniciada: $url")
                    }
                    
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        android.util.Log.d("WEBVIEW", "Página finalizada: $url")
                        
                        // Asegurar que las cookies se persistan después de cargar la página
                        cookieManager.flush()
                        
                        // Inyectar JavaScript mejorado para SPA y contenido dinámico
                        view?.evaluateJavascript("""
                            (function() {
                                console.log('WebView: Iniciando inyección de JavaScript');
                                
                                // Forzar recarga de eventos
                                if (typeof window.dispatchEvent === 'function') {
                                    window.dispatchEvent(new Event('resize'));
                                    window.dispatchEvent(new Event('load'));
                                    window.dispatchEvent(new Event('DOMContentLoaded'));
                                }
                                
                                // Inicializar contenido si hay funciones
                                if (typeof window.onload === 'function') {
                                    try { window.onload(); } catch(e) { console.log('Error en onload:', e); }
                                }
                                
                                // Manejar React
                                if (window.React && window.ReactDOM) {
                                    console.log('WebView: Detectado React');
                                    var containers = document.querySelectorAll('[data-reactroot], [id^="root"], [id^="app"]');
                                    containers.forEach(function(container) {
                                        if (container && container.innerHTML.trim() === '') {
                                            console.log('WebView: Forzando re-render de React');
                                            var event = new Event('DOMContentLoaded');
                                            document.dispatchEvent(event);
                                        }
                                    });
                                }
                                
                                // Manejar Vue
                                if (window.Vue) {
                                    console.log('WebView: Detectado Vue');
                                    var vueApps = document.querySelectorAll('[data-v-app], [id^="app"]');
                                    vueApps.forEach(function(app) {
                                        if (app && app.innerHTML.trim() === '') {
                                            console.log('WebView: Forzando re-render de Vue');
                                            window.dispatchEvent(new Event('vue-mounted'));
                                        }
                                    });
                                }
                                
                                // Forzar renderizado de elementos ocultos o vacíos
                                setTimeout(function() {
                                    var emptyContainers = document.querySelectorAll('.container, .content, main, [role="main"]');
                                    emptyContainers.forEach(function(container) {
                                        if (container && container.innerHTML.trim() === '') {
                                            console.log('WebView: Contenedor vacío detectado, forzando recarga');
                                            container.style.display = 'block';
                                            container.style.visibility = 'visible';
                                        }
                                    });
                                    
                                    // Forzar clic en elementos de menú si están presentes
                                    var menuItems = document.querySelectorAll('a[href], button, [onclick], .nav-link, .menu-item');
                                    menuItems.forEach(function(item) {
                                        item.addEventListener('click', function(e) {
                                            console.log('WebView: Click detectado en:', item);
                                            // Asegurar que el evento se propaga
                                            if (e.stopPropagation) {
                                                e.stopPropagation();
                                            }
                                        }, true);
                                    });
                                    
                                    // Habilitar todos los enlaces y botones
                                    document.querySelectorAll('a, button').forEach(function(el) {
                                        el.style.pointerEvents = 'auto';
                                        el.style.cursor = 'pointer';
                                    });
                                }, 500);
                                
                                // Re-ejecutar scripts que puedan no haberse ejecutado
                                var scripts = document.querySelectorAll('script[src]');
                                scripts.forEach(function(script) {
                                    if (!script.hasAttribute('data-loaded')) {
                                        var newScript = document.createElement('script');
                                        newScript.src = script.src;
                                        newScript.setAttribute('data-loaded', 'true');
                                        document.head.appendChild(newScript);
                                    }
                                });
                                
                                console.log('WebView: Inyección de JavaScript completada');
                            })();
                        """.trimIndent(), null)
                        
                        // Segunda inyección después de un delay para asegurar que el DOM esté listo
                        view?.postDelayed({
                            view.evaluateJavascript("""
                                (function() {
                                    console.log('WebView: Segunda inyección después de delay');
                                    // Forzar actualización de contenido
                                    if (document.body) {
                                        document.body.style.display = 'block';
                                        var event = new Event('resize');
                                        window.dispatchEvent(event);
                                    }
                                    
                                    // Intentar activar routers de SPA
                                    if (window.history && window.history.pushState) {
                                        var currentPath = window.location.pathname;
                                        window.history.pushState({}, '', currentPath);
                                        window.dispatchEvent(new PopStateEvent('popstate'));
                                    }
                                })();
                            """.trimIndent(), null)
                        }, 1000)
                    }
                    
                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        android.util.Log.e("WEBVIEW", "Error cargando: ${error?.description} - ${request?.url}")
                    }
                    
                    override fun onReceivedHttpError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        errorResponse: WebResourceResponse?
                    ) {
                        super.onReceivedHttpError(view, request, errorResponse)
                        android.util.Log.e("WEBVIEW", "Error HTTP: ${errorResponse?.statusCode} - ${request?.url}")
                    }
                }
                
                // Configurar WebChromeClient para logs y progreso
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage): Boolean {
                        android.util.Log.d("WEBVIEW", "${consoleMessage.message()} -- From line " +
                                "${consoleMessage.lineNumber()} of ${consoleMessage.sourceId()}")
                        return true
                    }
                    
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        android.util.Log.d("WEBVIEW", "Progreso: $newProgress%")
                    }
                }
                
                // Configuraciones completas del WebView para mejor compatibilidad
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.databaseEnabled = true
                
                // Configuraciones de renderizado
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.layoutAlgorithm = android.webkit.WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
                
                // Configuraciones de contenido
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.allowContentAccess = true
                settings.allowFileAccess = true
                settings.allowFileAccessFromFileURLs = true
                settings.allowUniversalAccessFromFileURLs = true
                
                // Configuraciones de JavaScript
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.mediaPlaybackRequiresUserGesture = false
                
                // Habilitar interacciones táctiles
                setOnTouchListener { v, event ->
                    v.performClick()
                    false
                }
                
                // Asegurar que el WebView pueda recibir clics
                isClickable = true
                isFocusable = true
                isFocusableInTouchMode = true
                
                // Configuraciones de zoom
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                
                // User-Agent estándar de Chrome para mejor compatibilidad
                settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                
                // Configuraciones de cache
                settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                
                // Habilitar cookies en el WebView
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this, true)
                
                // Cargar la URL
                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit,
    forceUpdate: Boolean
) {
    AlertDialog(
        onDismissRequest = {
            if (!forceUpdate) {
                onDismiss()
            }
        },
        title = {
            Text3(
                text = "Actualización disponible",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text3(
                    text = "Nueva versión: ${updateInfo.versionName}",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (!updateInfo.updateMessage.isNullOrBlank()) {
                    Text3(
                        text = updateInfo.updateMessage,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Justify
                    )
                }
                if (forceUpdate) {
                    Text3(
                        text = "\nEsta actualización es obligatoria.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onUpdate,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text3("Actualizar ahora")
            }
        },
        dismissButton = if (!forceUpdate) {
            {
                TextButton(onClick = onDismiss) {
                    Text3("Más tarde")
                }
            }
        } else null
    )
}


