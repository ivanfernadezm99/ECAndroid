# Instalación del Android SDK para generar el APK

Para generar el APK necesitas tener el Android SDK instalado. Aquí tienes las opciones:

## Opción 1: Instalar Android Studio (Recomendado - Más fácil)

1. **Descarga Android Studio:**
   ```bash
   # Descarga desde: https://developer.android.com/studio
   # O usando wget:
   wget https://redirector.gvt1.com/edgedl/android/studio/ide-zips/2023.3.1.18/android-studio-2023.3.1.18-linux.tar.gz
   ```

2. **Extrae e instala:**
   ```bash
   tar -xzf android-studio-*.tar.gz
   cd android-studio/bin
   ./studio.sh
   ```

3. **Configura el SDK:**
   - Abre Android Studio
   - Ve a: `File > Settings > Appearance & Behavior > System Settings > Android SDK`
   - Instala el SDK Platform Android 34 (o la versión que uses)
   - Instala Android SDK Build-Tools
   - Copia la ruta del "Android SDK Location" (generalmente `~/Android/Sdk`)

4. **Actualiza local.properties:**
   ```bash
   # Edita el archivo local.properties y actualiza la ruta:
   sdk.dir=/home/servidor/Android/Sdk
   ```

## Opción 2: Instalar solo Command Line Tools (Más ligero)

1. **Descarga las command-line tools:**
   ```bash
   cd ~
   wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
   unzip commandlinetools-linux-*_latest.zip
   mkdir -p ~/Android/cmdline-tools
   mv cmdline-tools ~/Android/cmdline-tools/latest
   ```

2. **Configura variables de entorno:**
   ```bash
   export ANDROID_HOME=$HOME/Android
   export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
   export PATH=$PATH:$ANDROID_HOME/platform-tools
   ```

3. **Instala componentes necesarios:**
   ```bash
   sdkmanager --install "platform-tools" "platforms;android-34" "build-tools;34.0.0"
   ```

4. **Actualiza local.properties:**
   ```bash
   echo "sdk.dir=$HOME/Android" > /home/servidor/Descargas/ECAndroid/local.properties
   ```

## Opción 3: Usar Docker (Si tienes Docker instalado)

Puedes usar una imagen de Docker con Android SDK preinstalado:

```bash
docker run --rm -v $(pwd):/project -w /project android/android-build:34 ./gradlew assembleRelease
```

## Verificar instalación

Después de instalar, verifica que funciona:

```bash
cd /home/servidor/Descargas/ECAndroid
./gradlew tasks
```

Si no da errores de SDK, está listo.

## Generar el APK

Una vez configurado el SDK:

```bash
cd /home/servidor/Descargas/ECAndroid
./gradlew assembleRelease
```

El APK estará en: `app/build/outputs/apk/release/app-release.apk`

## Nota sobre Java

Si aún tienes problemas con Java, instala Java 17:

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk
```

Luego actualiza `gradle.properties` o configura `JAVA_HOME`:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

