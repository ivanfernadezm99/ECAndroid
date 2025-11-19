# Sistema de Actualizaciones Automáticas

## Configuración

La aplicación ahora incluye un sistema de actualizaciones automáticas (OTA) que verifica si hay una nueva versión disponible al iniciar.

### 1. Configurar el endpoint de actualización

El sistema busca actualizaciones en la URL configurada en `UpdateManager.kt`:

```kotlin
private const val UPDATE_CHECK_URL = "https://enlaceschacopos.up.railway.app/api/update-check.json"
```

Puedes cambiar esta URL para apuntar a tu propio servidor.

### 2. Formato del archivo JSON

El endpoint debe devolver un JSON con el siguiente formato:

```json
{
  "versionCode": 2,
  "versionName": "1.1",
  "apkUrl": "https://tu-servidor.com/downloads/app-release.apk",
  "updateMessage": "Descripción de los cambios en esta versión (opcional)",
  "forceUpdate": false
}
```

**Campos:**
- `versionCode`: Número de versión (debe ser mayor que la versión actual en `build.gradle.kts`)
- `versionName`: Nombre de la versión (ej: "1.1", "2.0")
- `apkUrl`: URL completa donde está alojado el APK
- `updateMessage`: (Opcional) Mensaje que se mostrará al usuario
- `forceUpdate`: (Opcional) Si es `true`, el usuario no puede cancelar la actualización

### 3. Subir el APK

1. Genera el APK con: `./gradlew assembleRelease`
2. El APK estará en: `app/build/outputs/apk/release/app-release.apk`
3. Súbelo a tu servidor (puedes usar Railway, GitHub Releases, o cualquier hosting)
4. Asegúrate de que la URL en `apkUrl` sea accesible públicamente

### 4. Actualizar el archivo JSON

Cada vez que subas una nueva versión:
1. Incrementa `versionCode` en `app/build.gradle.kts`
2. Actualiza `versionName` si es necesario
3. Genera el nuevo APK
4. Súbelo a tu servidor
5. Actualiza el archivo JSON en tu servidor con la nueva información

### 5. Ejemplo de implementación en el servidor

Si usas Railway o un servidor Node.js, puedes crear un endpoint simple:

```javascript
// Ejemplo con Express.js
app.get('/api/update-check.json', (req, res) => {
  res.json({
    versionCode: 2,
    versionName: "1.1",
    apkUrl: "https://enlaceschacopos.up.railway.app/downloads/app-release.apk",
    updateMessage: "Nueva versión con mejoras",
    forceUpdate: false
  });
});
```

### 6. Permisos necesarios

La aplicación ya tiene configurados los permisos necesarios:
- `INTERNET`: Para descargar el APK
- `REQUEST_INSTALL_PACKAGES`: Para instalar APKs (Android 8.0+)
- `WRITE_EXTERNAL_STORAGE`: Para guardar el APK (Android 10 y anteriores)

### 7. Probar el sistema

1. Instala la versión actual (versionCode: 1)
2. Crea un archivo JSON en tu servidor con versionCode: 2
3. Reinicia la app
4. Deberías ver un diálogo de actualización

### Notas importantes

- El sistema verifica actualizaciones cada vez que se abre la app
- Si `forceUpdate` es `true`, el usuario no puede cancelar
- El APK se descarga en segundo plano y se instala automáticamente
- En Android 8.0+, el usuario debe permitir la instalación de apps desde fuentes desconocidas (se solicita automáticamente)

