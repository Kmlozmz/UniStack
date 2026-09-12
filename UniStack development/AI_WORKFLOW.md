# AI_WORKFLOW — cómo trabaja el desarrollador con un agente

Extraído de meses de sesiones (may–sep 2026). Es el archivo que hace posible que otro agente
trabaje «como el anterior». Todo lo de aquí es **observado**; lo que dijo él literalmente va
entre comillas.

## El ciclo real

1. **Él manda un punto de su lista (la TDL) o una tanda de capturas numeradas** con una línea por
   captura: «Imagen 1: … Imagen 2: …». A veces varias tareas en un mensaje, con «No pares hasta
   tenerlo todo cubierto».
2. **Si es un cambio de producto grande → artifact primero.** Un HTML interactivo (teléfono 390×844
   con los tokens del tema oscuro a la izquierda, panel de mandos y razones a la derecha, varias
   propuestas cuando aplique). Él lo abre, toca, y responde con una elección, una combinación
   («haz una combinación de A y B pero con el anillo de C») o correcciones. **Nunca se implementa
   un rediseño grande sin ese paso.**
3. **Implementar entero**, compilar, correr tests, **enviar la alpha al bot sin preguntar**,
   commitear, empujar `TDL` y `main`.
4. **Reportar** en español: qué se hizo, y por cada cambio si es **interno o palpable** y **la
   ruta para comprobarlo**. Corto, por viñetas, sin preámbulos.
5. Él prueba en el móvil y vuelve con capturas. Se itera hasta «ya está perfecto» / «dime qué
   sigue». Entonces se le enseña **la lista con lo hecho tachado** y se pasa al siguiente punto.

## Cuánto contexto analizar antes de tocar

- **Siempre leer la pantalla/feature entera afectada** y la pieza equivalente en otra feature
  (cómo lo hace Horario, cómo lo hace Notas). Él conoce su app: nota cuando algo no se parece.
- Buscar primero en `core/design/components/` y en los KDoc: «los KDoc explican cuándo va cada
  uno; esa regla manda sobre lo que a mí me parezca mejor».
- Revisar `DESIGN_DECISIONS.md` / `FAILED_APPROACHES.md` antes de proponer: proponer algo ya
  vetado gasta confianza.
- Para datos: leer entidad, DAO, repositorio, respaldo y tests antes de cambiar un esquema.

## Cuándo preguntar y cuándo decidir solo

**Decidir solo (y contarlo en el reporte):**
- Elecciones rutinarias de implementación, nombres, dónde va una pieza, qué componente usar.
- Detalles de diseño que el artifact no cubrió, si siguen la forma de la app (él suele aceptarlos:
  «decidido por mí sin objeción» aparece en la memoria).
- Arreglar de paso un fallo real encontrado (se reporta como «de paso salió un fallo real»).

**Preguntar (con opciones, no abierto):**
- Cuando dos lecturas del pedido llevan a trabajo materialmente distinto.
- Cuando algo toca datos de usuario de forma irreversible o publica hacia fuera (reescribir
  historia de git publicada, publicar release, borrar).
- Cuando contradice una decisión anterior suya: señalarlo en una frase y esperar.

**No preguntar nunca:**
- Si enviar el APK al bot. Si crear los archivos que pidió. Si seguir con el siguiente punto
  cuando dijo «procede».

## Cómo quiere que se ejecuten los cambios

- **Entero, no por partes**: si pide cinco cosas, las cinco. Si algo queda fuera, decir qué y por qué.
- **Sin ampliar el alcance por cuenta propia**: lo que él no pidió se sugiere, no se hace
  (excepto arreglos de fallos reales encontrados en el camino).
- **Sin reabrir lo decidido**: sus vetos son definitivos («eso sí que no lo apruebo», «se va»).
- **1:1 con el artifact aprobado**: la desviación es un fallo, no una mejora («no se parece
  mucho al artifact que aprobé» fue su crítica a Notas).
- **Copiar la pieza que existe**: ver `DESIGN_SYSTEM.md` § 6.

## Cómo verificar

- `./gradlew compileDebugKotlin` durante el trabajo; `./gradlew testDebugUnitTest` antes de la
  alpha; `check` cuando toque tokens (puede fallar la primera vez por lint del entorno).
- Para artifacts: servirlos en local (`python -m http.server` en el scratchpad) y probar cada
  flujo con clics reales o JS antes de publicar; añadir `<meta charset="utf-8">` para el preview
  local.
- **La verificación final es su teléfono**; el reporte debe decirle exactamente qué mirar.

## Cómo manejar errores

- Un script de migración que falla a medias: `git checkout -- <ficheros>`, arreglar, relanzar
  (los scripts con `cambiar()` no son idempotentes).
- Compilación rota por dos gradle a la vez: `./gradlew --stop`.
- Un test rojo tras un cambio de texto: casi siempre falta `TextosDePrueba.instalar()` o la clave
  en un XML.
- Decirle los fallos tal cual, con la salida; nunca «debería funcionar».

## Cómo presentar resultados

- Español. Directo. Viñetas cortas. Negrita en lo que tiene que mirar.
- Por cambio: **interno** / **palpable + ruta** («Horario → Ponerse al día → marca una como
  asistida y otra como falta; deben salir verde y rojo con cualquier tema»).
- Al final: qué queda pendiente de él (probar X en inglés, elegir Y), y qué sigue.
- Con el estilo «Explanatory» activo, un bloque `✶ Insight` breve con algo específico del
  código (una trampa, un porqué), no teoría general.
- Cuando cierra un punto de la TDL: la lista entera con lo hecho tachado.

## Tareas grandes vs pequeñas

- **Grandes** (rediseño, i18n, histórico): artifact → plan por bloques → cada bloque compila,
  tests, commit propio → alpha al final de la tanda (no por bloque) → reporte agrupado. Guardar
  scripts de migración masiva en el scratchpad con asserts.
- **Pequeñas** (una captura): cambio, compilar, alpha, commit, reporte de tres líneas.

## Debugging e investigación de código existente

- `grep`/lectura antes que suposición; los KDoc suelen contener el porqué del estado actual.
- Cuando algo «no se ve», sospechar de las trampas conocidas: `UniCard` con varios hijos,
  `animateFloatAsState` nacido en el destino, `size` coercionado por el padre, escala dentro de
  un `clip`, `stringResource` en lambda, `Textos` sin proveedor.
- Reproducir con datos: banco de pruebas y notas de ejemplo existen para eso («le da pereza
  crearlas a mano»).

## Cómo evitar romper lo existente

- Tests en verde + revisar respaldo/restauración si se tocó el modelo (`LocalJsonBackupRepository`
  y Firebase lo serializan a mano: campo nuevo = campo en los dos).
- No renombrar lo persistido. No cambiar valores por defecto de preferencias sin migrar la clave
  (`screenTransitionStyle` nació nueva por eso).
- Recordar que las alarmas guardan rutas literales: no borrar rutas.

## Preferencias de interacción (resumen)

- Tutea, escribe rápido y con erratas; no le importa la forma, le importa que se haga.
- Le molesta repetir una instrucción (el trailer de coautoría, el bot): las reglas de memoria
  existen para eso.
- Aprecia que el agente le diga qué falta y qué es bloqueante para el público, pero decide él.
- No quiere «humo»: en artifacts y reportes, lo que hay y lo que falta, con `PENDING` explícito.
