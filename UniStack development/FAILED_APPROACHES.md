# FAILED_APPROACHES — lo que se probó y no funcionó

Para no repetirlo. Cada entrada: qué, por qué parecía buena idea, qué pasó, qué se aprendió,
qué se usa en su lugar. Fechas del historial y de la memoria.

## Producto y diseño

### Rebote elástico al desplazar, encabezados que encogen, gesto atrás predictivo, barra flotante (17–19 ago 2026)
- **Parecía:** más «Expressive», más iOS/Material moderno.
- **Pasó:** «elimina todo rastro de lo que te pido que remuevas, tanto en opciones como en código».
  `LargeTopAppBar` no encoge un título: hace un fundido entre dos y se veía tosco; el gesto
  predictivo no se apaga con `targetSdk 36`.
- **Queda:** el empuje estilo iOS (que gustó y lo dijo dos veces), comportamiento por defecto
  de Android en scroll y cabeceras, `targetSdk 35`, barra acoplada.

### Fundido dentro del empuje de navegación
- **Pasó:** al cambiar de pestaña a golpes se veían tres pantallas a medio camino unas a través
  de otras. **Ahora:** opacas, sin fundido; cada destino en su capa (`zIndex`).

### Logo dibujado por código con `Canvas` (21 ago)
- **Parecía:** un solo color de marca definido una vez; menos PNG.
- **Pasó:** «Eso sí que no lo apruebo, mantén los assets originales.» **Ahora:** `painterResource`
  del PNG; `Pills` sólo para animar el arranque.

### Hoja de filtro de Notas con cabecera grande y tarjetas propias (2 sep)
- **Pasó:** «no me gusta como quedó, se ve extraño. Solo hazlo como el sheet de la app.»
  **Regla:** copiar la pieza existente.

### Siluetas M3E (trébol, ráfaga) para estados de asistencia (10 sep)
- **Pasó:** «se ve extraño con esas shapes». **Ahora:** ruedas visto/equis/guion/flecha, las
  mismas de marcar.

### Franjas de color al costado de tarjetas (varias veces hasta 11 sep)
- **Pasó:** «AI slop». **Ahora:** contenedores tonales enteros; superficies neutras por elevación.

### Animaciones sin texto y celebración pequeña (11 sep)
- **Pasó:** «a Onda le falta texto, esos círculos solos no van»; «solo unos confetis chiquititos
  y super rápido». **Ahora:** todo momento lleva mensaje; celebración a pantalla completa en dp.

### 25 gestos de Movimiento (3 sep) → 12 → 7 (11 sep)
- **Parecía:** dar control fino. **Pasó:** él quitó gestos en dos rondas; muchos «se guardaban
  pero no se aplicaban». **Regla:** ningún ajuste sin sitio de aplicación; lo que no necesita
  elección «ya se mueve solo».

### Notas rápidas con diseño propio en cinco artifacts (28 ago)
- **Pasó:** al verla construida: «muy funky, no se parece al artifact» y luego «básicamente
  replicar Google Keep». **Aprendido:** cuando existe un patrón que él conoce y funciona,
  replicarlo gana a inventar; y el artifact aprobado se implementa 1:1.

### Tipos de nota al crear, subrayado, casilla → tarea
- Descartados en las vueltas de Keep.

### Diálogo «¿lleva nota?» al marcar cualquier tarea con materia (hasta sep 2026)
- **Pasó:** interrumpe siempre, aunque sea una lectura. **Ahora (D):** el tipo decide; snackbar
  con «No lleva nota».

### Ajustes que se guardaban y no pintaban nada (`surfaceStyle`, `accentIntensity`, `cornerStyle`, alto contraste)
- **Aprendido:** un ajuste sin efecto cuesta más que las variantes que sí lo tienen; se
  arreglaron todos el 3 sep.

## Datos y lógica

### Convertir notas al cambiar de escala
- Rechazado: inventa números; ida y vuelta no cuadra. **Ahora:** borrar con doble confirmación.

### Proyecciones «vas a terminar con X»
- Dos proyecciones distintas en la misma tarjeta. **Ahora:** suelo y techo exactos.

### Asistencia con 120 días fijos hacia atrás
- Materias nuevas con meses de clases «pendientes» inexistentes. **Ahora:** acotado al periodo.

### Comparar nombres de materia para «repetida»
- **Ahora:** `repeatedFromSubjectId` como columna real.

### `updateSubject()` en memoria más permisivo que Room
- El código pasaba los tests y fallaba en producción. **Ahora:** contrato alineado + test.

### Deducir estados por `null` o por texto
- `TargetOutlook` de `null` ambiguo; rótulos por palabras clave. **Ahora:** estados explícitos
  (uno queda: `heroLabel()`).

### Paywall apagado «por si acaso»
- Costaba un permiso e invitaba a gates a medias. **Ahora:** borrado entero.

## Distribución y build

### Publicar alphas en GitHub (hasta 17 ago)
- Quemaba números públicos y confundía canales. **Ahora:** alphas sólo por el bot.

### Canales con códigos y periodo de gracia (hasta 19 ago)
- Repartía descargas ya públicas. **Ahora:** siempre la última release.

### Quitar el peldaño `dev` (13 ago)
- Cada prueba exigía una alpha pública; revertido el mismo día.

### `versionCode` desde el reloj; peldaño de dos dígitos (hasta 20 ago)
- Orden por hora de compilación; 14 alphas con el mismo número. **Ahora:** fórmula de tres dígitos + test.

### Alphas numeradas por delante de la beta (`1.2.0-alpha.N` con `1.1.0-beta.N` fuera)
- El móvil con la alpha no aceptaba ninguna beta. **Regla:** la beta hereda el número de sus alphas.

### Token de GitHub en el APK para leer un repo privado
- Se extrae trivialmente. **Ahora:** repo público sólo de releases.

### Bot con mensajes «cooking…/completed!» y con filtro sólo-alphas
- Tapaban el APK / había que buscarlo a mano. **Ahora:** sólo el APK, todo release.

### Dos APK por envío (definitiva + beta)
- «No es necesario que me envíes 2 apk siempre.» **Ahora:** una, la que toque.

## Herramientas del agente

### Scripts de migración masiva por heredoc de Bash
- Las barras invertidas llegan a medias (`<` → `<`, `\1` → `\x01`) y el reemplazo queda en
  no-op silencioso. **Ahora:** scripts a fichero con el Write tool, `chr(92)`, asserts y
  verificación por resultado.

### Scripts `cambiar()` no idempotentes fallando a medias
- Dejan el árbol a medio migrar. **Ahora:** `git checkout --` de lo tocado, arreglar, relanzar;
  `i18n_recuperar_cadenas.py` para las cadenas.

### `stringResource` dentro de `onClick`
- No compila. **Ahora:** `Textos.get`.

### `Textos.desde(this)` después de `super.onCreate()`
- La app reventaba al abrir (Hilt construye el canal de avisos en un constructor). Lo cazaron
  61 tests de Robolectric. **Ahora:** antes.

### Comprobar artifacts en el navegador integrado sin sesión
- claude.ai pide iniciar sesión. **Ahora:** servir el scratchpad con `python -m http.server`
  (`.claude/launch.json`) y añadir `<meta charset="utf-8">`; cache-busting con `?v=`.
