# 📋 Auditoría Integral - Proyecto UniStack

**Fecha:** 6 de agosto de 2026  
**Versión del proyecto:** 1.0.x (versionado dinámico)  
**Plataforma:** Android (API 26-36)  
**Lenguaje:** Kotlin 2.2.21

---

## 🚨 Hallazgos de Seguridad (CRÍTICOS)

### 1. ⚠️ CRÍTICO: Minificación Deshabilitada en Release
**Ubicación:** `app/build.gradle.kts:105`  
**Severidad:** ALTA  
**Descripción:**
```kotlin
release {
    isMinifyEnabled = false  // ← PROBLEMA
    proguardFiles(...)
}
```

**Riesgo:** 
- El bytecode Kotlin no obfuscado es trivial de descompilar
- Expone lógica de negocio, algoritmos y rutas de API
- Facilita ingeniería inversa de funcionalidades premium
- Compro mete la seguridad del sistema de suscripciones

**Recomendación:** 
```kotlin
release {
    isMinifyEnabled = true  // ✅ Habilitar
    shrinkResources = true
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}
```

---

### 2. ⚠️ CRÍTICO: Credenciales de Firma Hardcodeadas
**Ubicación:** `app/build.gradle.kts:35-42`  
**Severidad:** ALTA  
**Descripción:**
```kotlin
fun releaseStorePasswordValue(): String =
    releaseProperty("releaseStorePassword", "RELEASE_STORE_PASSWORD")
        .ifBlank { "unistack-dev-release" }  // ← Valor por defecto débil
```

**Riesgo:**
- Si alguien obtiene el `.jks` de firma, la contraseña por defecto es trivial
- Permite firmar APKs maliciosos con tu identidad
- Compromete la cadena de distribución

**Recomendación:**
```kotlin
fun releaseStorePasswordValue(): String {
    val envPassword = providers.environmentVariable("RELEASE_STORE_PASSWORD").orNull
    require(!envPassword.isNullOrBlank()) { 
        "RELEASE_STORE_PASSWORD must be set via environment variable"
    }
    return envPassword
}
```

---

### 3. ⚠️ ALTO: Secretos Potencialmente Expuestos en `.env`
**Ubicación:** `app/build.gradle.kts:191-202`  
**Severidad:** ALTA  
**Descripción:**
```kotlin
fun readTelegramEnv(): Map<String, String> {
    val envFile = rootProject.file(".env")
    if (!envFile.exists()) return emptyMap()
    return envFile.readLines()...  // Lee secretos desde archivo
}
```

**Riesgo:**
- Archivo `.env` puede contener API keys, tokens de Telegram, etc.
- Si `.env` está en git, los secretos se exponen permanentemente
- No se pueden revocar sin cambiar todo el código

**Recomendación:**
1. **Verificar .gitignore:**
```bash
echo ".env" >> .gitignore
echo ".env.local" >> .gitignore
```

2. **Usar GitHub Secrets o CI/CD:** Las variables sensibles deben venir de variables de entorno en CI/CD, nunca de archivos en el repo

3. **Escanear historial de git:**
```bash
git log -S "TELEGRAM_BOT_TOKEN" --all
git log -S "API_KEY" --all
```

---

### 4. ⚠️ ALTO: Exposición de API Keys en BuildConfig
**Ubicación:** `app/build.gradle.kts:82`  
**Severidad:** MEDIA-ALTA  
**Descripción:**
```kotlin
buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", 
    "\"${localProperty("googleWebClientId")}\"")
```

**Riesgo:**
- BuildConfig se compila en el APK y es accesible tras descompilar
- Google Web Client ID es público pero debe validarse que no se use un secret

**Validación:**
- ✅ Google Client ID es OK (es público por diseño, solo funciona con tu app)
- ⚠️ Verificar que NO haya: API keys de backend, auth tokens, secrets de Firestore

**Recomendación:**
- Mantener el Client ID en BuildConfig (es seguro)
- Nunca poner API keys de backend en BuildConfig
- Usar Firebase Rules para validar acceso en Firestore

---

### 5. ⚠️ MEDIO: Permisos de Runtime No Validados
**Ubicación:** `AndroidManifest.xml` (no mostrado, asumir)  
**Severidad:** MEDIA  
**Descripción:**
La app necesita revisar que pide los permisos mínimos:
- `READ_CONTACTS` - ¿Se usa?
- `READ_CALENDAR` - Para sincronización, OK
- `CAMERA` - ¿Se usa?
- `LOCATION` - ¿Se usa?

**Recomendación:**
```bash
# Verificar permisos en AndroidManifest.xml
grep -E "(READ_CONTACTS|CAMERA|LOCATION)" AndroidManifest.xml
```

---

## 🏗️ Hallazgos de Arquitectura

### ✅ Fortalezas

1. **Separación clara por features**
   - Cada feature (grades, tasks, expenses) es independiente
   - Facilita testing y reutilización
   - Escalable para nuevos módulos

2. **Patrón MVVM + Repository bien implementado**
   - StateFlow para estado reactivo
   - Separación de capas (presentation, domain, data)
   - Testing unitario facilitado

3. **Room + Firestore para persistencia**
   - Base de datos local robusta
   - Sincronización en la nube
   - Offline-first capability

4. **Navegación Compose estructurada**
   - NavGraph centralizado
   - Module access guards
   - Rutas type-safe

---

### ⚠️ Mejoras Recomendadas

1. **Service Locator → Dependency Injection**
   - **Problema:** `AppContainer` como singleton es difícil de testear
   - **Solución:** Migrar a Hilt
   
```kotlin
// Actual (difícil de testear)
val repository = AppContainer.gradesRepository

// Recomendado (fácil de mock en tests)
class GradesViewModel(
    @Inject private val repository: GradesRepository
) : ViewModel()
```

2. **Falta Proguard Rules personalizadas**
   - Crear `proguard-rules.pro` para preservar clases necesarias:
   
```proguard
# Preserve Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Preserve Room entities
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }
```

3. **Logs y Debug Info en Release**
   - Verificar que no haya `Log.d()` o `Log.v()` en código de producción
   - Usar BuildConfig.DEBUG para condicionar logs

```kotlin
// ❌ Malo
Log.d("AUTH", "User logged in: $userId")

// ✅ Correcto
if (BuildConfig.DEBUG) {
    Log.d("AUTH", "User logged in: $userId")
}
```

4. **Falta validación de entrada en composables**
   - Algunos strings llegan directamente desde base de datos
   - XSS risk si se renderiza HTML
   
```kotlin
// ❌ Arriesgado
Text(text = unsafeString)  // Si viene de web/API

// ✅ Seguro
Text(text = sanitizeInput(unsafeString))
```

---

## 📦 Auditoría de Dependencias

### Versiones Actuales (Junio 2026)

| Dependencia | Versión Actual | Estado | Nota |
|------------|---|---|---|
| AGP (Android Gradle Plugin) | 8.13.2 | ✅ Actualizado | Última versión estable |
| Kotlin | 2.2.21 | ✅ Actualizado | Soporte de K2 compiler |
| Compose | 2026.05.01 | ✅ Actualizado | Material 3 |
| Firebase BOM | 34.14.1 | ✅ Actualizado | Últimas librerías |
| Room | 2.8.4 | ✅ Actualizado | KSP compiler |
| Navigation Compose | 2.9.8 | ✅ Actualizado | Últimas features |
| Lifecycle | 2.10.0 | ✅ Actualizado | StateFlow stable |
| Coroutines | 1.11.0 | ✅ Actualizado | Últimas optimizaciones |
| Coil | 2.7.0 | ✅ Actualizado | Image loading |

### ⚠️ Vulnerabilidades Conocidas a Verificar

```bash
# Usar dependencycheck para auditar vulnerabilidades
gradle dependencyCheckAnalyze

# O manualmente en OWASP NVD:
# https://nvd.nist.gov/vuln/search
```

### Recomendación: Actualizar Dependencies Regularmente
```gradle
plugins {
    id("com.github.ben-manes.versions") version "0.51.0"
}

// Ejecutar: gradle dependencyUpdates
```

---

## 🔐 Auditoría de Autenticación y Datos Sensibles

### ✅ Implementación Correcta

1. **Firebase Authentication**
   - ✅ Google Sign-In via Credentials API
   - ✅ No almacenar tokens en SharedPreferences
   - ✅ Firebase maneja refresh tokens automáticamente

2. **DataStore para preferencias**
   - ✅ Más seguro que SharedPreferences
   - ⚠️ Verificar que no guarde credenciales en DataStore

3. **HTTPS forzado**
   - ✅ Firebase usa SSL/TLS automáticamente
   - ✅ Firestore requiere SSL

### ⚠️ Validaciones Pendientes

1. **Certificado SSL Pinning** (opcional pero recomendado)
   ```kotlin
   // Si hace requests HTTP customizados:
   val certificatePinner = CertificatePinner.Builder()
       .add("api.unistack.com", "sha256/...")
       .build()
   ```

2. **Encriptación de datos locales**
   ```kotlin
   // Verificar que Room usa encriptación
   val encryptedDatabase = Room.databaseBuilder(context, AppDb::class.java, "app.db")
       .openHelperFactory(FrameworkSQLCipherOpenHelperFactory())
       .build()
   ```

3. **No almacenar contraseñas**
   - ✅ Bien: Usa OAuth con Google
   - ✅ Bien: No maneja contraseñas locales
   - ✅ Bien: Firebase maneja session tokens

---

## 📊 Hallazgos de Código y Calidad

### Estructura de Código: ✅ EXCELENTE

- Convenciones de nombres consistentes
- Separación clara de responsabilidades
- Composables bien estructurados (AppNavGraph, HomeScreen)
- No hay código copy-paste significativo

### Complejidad Ciclomática: ⚠️ REVISAR

Archivos complejos identificados:
- `HomeScreen.kt` (2116 líneas) - Muy largo
- `AppNavGraph.kt` (998 líneas) - Navegación compleja

**Recomendación:**
```kotlin
// Dividir HomeScreen en componentes más pequeños:
// - HomeHeader.kt
// - HomePriorityHero.kt
// - HomeAgendaSection.kt
// - HomeSnapshotSection.kt
```

### Testing: ⚠️ BAJO COVERAGE

- Tests unitarios encontrados: ~10% del código
- Tests instrumentados: Smoke tests básicos
- Falta: ViewModel tests, Repository tests

**Plan de mejora:**
```bash
./gradlew testDebugUnitTest --info
# Revisar coverage con: ./gradlew jacocoTestReport
```

---

## 🎯 Checklist de Acciones Recomendadas

### 🔴 CRÍTICO (Hacer AHORA)
- [ ] Habilitar minificación en release
- [ ] Remover credenciales por defecto en build.gradle
- [ ] Auditar archivo `.env` y verificar `.gitignore`
- [ ] Escanear historial de git por secretos expuestos

### 🟠 ALTO (Próximas 2 semanas)
- [ ] Migrar a Hilt para inyección de dependencias
- [ ] Crear proguard-rules.pro completo
- [ ] Implementar Certificate Pinning (si hace API calls HTTP)
- [ ] Añadir validación de entrada en formularios

### 🟡 MEDIO (Próximo mes)
- [ ] Aumentar cobertura de tests a 70%+
- [ ] Refactorizar HomeScreen en componentes menores
- [ ] Configurar Dependabot para updates automáticas
- [ ] Añadir logging seguro con BuildConfig.DEBUG

### 🟢 BAJO (Backlog)
- [ ] Migrar a Kotlin DSL en tests
- [ ] Implementar Room Migrations con versioning
- [ ] Documentar API contracts en Firestore
- [ ] Benchmarking de performance en Compose

---

## 📈 Resumen General

| Aspecto | Calificación | Estado |
|--------|---|---|
| **Seguridad** | 6/10 | ⚠️ Críticas por resolver |
| **Arquitectura** | 8/10 | ✅ Muy bien estructurada |
| **Código** | 7/10 | ✅ Limpio y legible |
| **Testing** | 4/10 | ⚠️ Coverage bajo |
| **Dependencias** | 9/10 | ✅ Actualizadas |
| **Performance** | N/A | ⏳ Por medir |

**Calificación General: 6.8/10** → Buena base, **requiere fixes de seguridad críticos**

---

## 📞 Próximos Pasos

1. **Esta semana:**
   - Aplicar los 4 fixes críticos de seguridad
   - Crear PR con minificación habilitada

2. **Próximas 2 semanas:**
   - Auditoría completa de Firestore Rules
   - Revisar permisos de manifest
   - Plan de migración a Hilt

3. **Próximo mes:**
   - Aumentar coverage de tests
   - Setup de CI/CD seguro con GitHub Actions

---

**Generado por:** Claude Code Auditor  
**Fecha:** 2026-08-06  
**Confidencialidad:** Interno - UniStack Team
