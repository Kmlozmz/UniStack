# AI_CONTEXT — lo mínimo que un agente necesita saber

Léelo entero antes de tocar nada. Cada punto remite al archivo con el detalle.

## Qué es UniStack

App Android nativa (Kotlin + Jetpack Compose + Material 3 Expressive) para **estudiantes
universitarios**: notas por materia con «suelo y techo» respecto a una meta, tareas y entregas,
horario con asistencia y tope de faltas, periodos académicos (semestres) con histórico, notas
rápidas tipo Keep, gastos, calculadora de promedio, recordatorios locales. Funciona **sin
cuenta y sin conexión**; los datos viven en el teléfono (Room). Español primero; inglés
completo desde el 11 sep 2026. **Uso libre, sin planes de pago** (DECISIÓN, 2 sep 2026).

Un solo desarrollador (GitHub `Kmlozmz`), que además es usuario diario de la app en su propio
semestre y valida cada compilación desde su móvil. Contexto colombiano (INFERENCIA: moneda COP
por defecto, «corte», «parcial», zona `America/Bogota` en los tests).

Detalle: [`PROJECT.md`](PROJECT.md), [`PRODUCT.md`](PRODUCT.md).

## Stack, en una línea cada uno

Kotlin 2.2.21 · Compose BOM 2026.05.01 · **material3 1.5.0-alpha15 clavado a propósito** ·
Hilt 2.56.2 · Room 2.8.4 (base `unistack.db`, **versión 21**, 12 tablas) · DataStore
Preferences · WorkManager · Navigation Compose · Firebase Auth/Firestore **referenciados pero no
conectados** (no hay `google-services.json`) · Robolectric + JUnit4 (534 tests) · AGP 8.13.2,
Gradle 8.14.5, JDK 21, `minSdk 26`, `targetSdk 35` (a propósito), `compileSdk 36`.
Un solo módulo Gradle (`:app`), organizado por paquetes `feature_*` + `core`.

Detalle: [`TECH_STACK.md`](TECH_STACK.md), [`ARCHITECTURE.md`](ARCHITECTURE.md), [`DATABASE.md`](DATABASE.md).

## Cómo se trabaja (reglas duras)

1. **Todo APK de release recién compilado va al bot de Telegram del desarrollador, sin
   preguntar.** El comando del día a día:
   `./gradlew sendAlpha "-PversionName=1.6.0-alpha.N"` (comillas obligatorias en PowerShell).
   Siguiente número al escribir esto: **`1.6.1-alpha.5`** (la serie pasó a 1.6.1 el 13 sep 2026). → [`BUILD.md`](BUILD.md)
2. **Commits en inglés, Conventional Commits, cuerpo que explica el porqué, y NUNCA un trailer
   `Co-Authored-By` ni «Generated with…».** El desarrollador lo pidió y tuvo que repetirlo.
   Escribir el mensaje a un fichero y usar `git commit -F`. → [`DEVELOPMENT_WORKFLOW.md`](DEVELOPMENT_WORKFLOW.md)
3. **Al reportar, decir si cada cambio es interno o palpable, y para los palpables la ruta exacta
   para comprobarlo en el teléfono** («Ajustes → Apariencia → … debería pasar X»). → [`AI_WORKFLOW.md`](AI_WORKFLOW.md)
4. **Rediseñar es copiar la pieza que la app ya tiene**, no inventar una. Antes de escribir una
   hoja, una fila o un selector: buscar en `core/design/components/` y en una pantalla parecida.
   → [`DESIGN_SYSTEM.md`](DESIGN_SYSTEM.md)
5. **Los diseños se deciden primero en un artifact interactivo** (HTML con el teléfono a la
   izquierda y los mandos a la derecha, tokens de la app, todo tocable) y después se
   implementan 1:1. Él aprueba por artifact y luego compara con capturas. → [`DECISION_MAKING.md`](DECISION_MAKING.md)
6. **Textos de usuario en `strings.xml` (es + `values-en`)**. Código no-Compose los pide por
   `Textos.get(R.string.x)`; nunca `if (isEnglish) "…" else "…"`. Comentarios del código en
   español, con el porqué. → [`CODING_RULES.md`](CODING_RULES.md)
7. **No reabrir lo decidido.** La lista está en [`DESIGN_DECISIONS.md`](DESIGN_DECISIONS.md) y
   [`FAILED_APPROACHES.md`](FAILED_APPROACHES.md). Lo más repetido: nada de franjas de color al
   costado de las tarjetas («AI slop»), nada de rebote al desplazar, nada de gesto atrás
   predictivo, el logo no se dibuja por código, Gastos es rojo a propósito, cambiar la escala de
   notas borra las notas a propósito.
8. **La lista de pendientes (TDL) la escribe él.** No se inventan puntos ni se reordena; al
   cerrar uno, se le enseña la lista con lo hecho tachado. → [`CURRENT_STATE.md`](CURRENT_STATE.md)
9. **Rama de trabajo `TDL`**; al terminar una tanda: push de `TDL`, fast-forward de `main` y push.
10. **Lo a medio hacer sólo se ve en dev/alpha/beta** (`BuildStage.allowsUnfinished`); rc y
    estable lo llevan apagado con etiqueta «Pronto».

## Cómo comunica y cómo hay que responderle

- Escribe en español, corto, con capturas numeradas («Imagen 1: …») y viñetas por pantalla.
  Espera respuesta en español, directa, sin preámbulos ni disculpas.
- «Procede», «continúa», «no pares hasta tenerlo todo cubierto» = hazlo entero sin volver a
  preguntar; pregunta sólo si dos lecturas llevan a trabajo materialmente distinto.
- Sus vetos son cortos y definitivos («eso sí que no lo apruebo», «se va», «no me gusta como
  quedó, se ve extraño»). No se discuten: se deshacen y se anota la regla detrás.
- Valora que se le diga lo que **no** está en su lista y por qué importa (ej. reporte de
  errores), pero no que se haga sin pedirlo.
- Prefiere que el agente decida lo rutinario solo y le enseñe el resultado a que le pregunte.

Detalle: [`AI_WORKFLOW.md`](AI_WORKFLOW.md), [`PREFERENCES.md`](PREFERENCES.md).

## Dónde está el proyecto hoy (12 sep 2026)

- Última publicación: **1.5.10** (26 ago 2026). Desde entonces, 95 alphas sin publicar con
  Notas rápidas, Gastos, histórico de semestres, asistencias, Movimiento e i18n.
- Cerrado hace nada: **Accesibilidad e idiomas** (i18n real, `Textos`).
- En curso: **rediseño de Tareas**, propuesta D elegida en el artifact
  `https://claude.ai/code/artifact/9e646565-dfd7-4a7b-a22c-7ce3bf18c1a1` (hoja + «abrir entera»,
  lista por días, subtareas en esta ronda). Empieza la implementación.
- Después: fase de **definir/planear** (recursos, UniStack AI, trabajos, labs, reporte de errores).

Detalle: [`CURRENT_STATE.md`](CURRENT_STATE.md), [`ROADMAP.md`](ROADMAP.md).

## Trampas que ya costaron tiempo

- `material3` va clavado en `1.5.0-alpha15`; **todos** los componentes M3E que se necesitan ya
  están ahí. No tocar la cadena de compilación para «poder usar» un componente.
- `UniCard` mete el contenido en un `Box`: varios hijos directos se pintan unos encima de
  otros. Siempre una `Column` dentro.
- `GradesRepository.updateSubject()` no toca las notas (tabla aparte): `clearGrades(subjectId)`.
- Compose: `stringResource` sólo en contexto composable; dentro de `onClick` y corrutinas,
  `Textos.get`. `Textos.desde(this)` va **antes** de `super.onCreate()` en la `Application`.
- Scripts Python por heredoc de Bash pierden las barras invertidas; escribir el script a fichero.
- Dos compilaciones a la vez rompen (`Couldn't delete R.jar`); `./gradlew --stop` y repetir.
- `check` a veces falla en la primera ejecución por lint del entorno y pasa al repetir.

Detalle: [`FAILED_APPROACHES.md`](FAILED_APPROACHES.md), [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md).
