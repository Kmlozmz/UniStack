# Contexto de trabajo — UniStack

Documento de traspaso entre sesiones. Recoge **decisiones tomadas y su porqué**, no
documentación del código: para saber qué hace algo, léelo; esto es para saber por qué está
así y qué no hay que "arreglar".

Última actualización: 12 ago 2026.

---

## Cómo se trabaja aquí

- **Rama de trabajo:** `test`. `main` recibe lo estable. Cada tanda se compila, se pasan los
  tests y se sube.
- **APK a Telegram siempre.** La task `sendDebugApkToTelegram` va encadenada al
  `assembleDebug`, así que sale sola. No hay que preguntar antes de enviar.
- **Compilar:** el JDK está dentro del repo, en `.toolchains/jdk-17-windows/jdk-17.0.19+10`.
  No hay Java en el PATH del sistema.
  ```
  export JAVA_HOME="$(pwd)/.toolchains/jdk-17-windows/jdk-17.0.19+10"
  ./gradlew testDebugUnitTest assembleDebug
  ```
- **Commits:** en inglés, Conventional Commits, cuerpo explicando el *porqué*. Sin trailer de
  coautoría. El `git log` es el registro más fiel de esta sesión: si falta contexto, está ahí.
- **Al reportar:** decir si cada cambio es **interno** (lógica, refactor, tests) o **palpable**
  (se ve en la app), y en ese caso la ruta concreta para comprobarlo.

---

## Decisiones que parecen fallos y no lo son

**Gastos es rojo a propósito.** Cada sección tiene identidad cromática y esa es la de Gastos.
No es el token de error mal usado. *Pendiente aparte:* `Coral` sirve a la vez de identidad de
Gastos (35 usos) y de error/vencido (23 archivos), así que no se puede afinar uno sin mover el
otro. Separarlos sigue sin hacerse.

**Cambiar la escala de notas borra todas las notas.** Decidido, no accidental. Una nota es un
registro de lo que puso un profesor, no una medida que se pueda reexpresar: convertir 85/100
en 4.3/5 inventa un número que nadie dio, y como se guarda con un decimal la ida y vuelta ya
no devuelve 85. Implementado con doble confirmación, conteo real («23 notas en 4 materias») y
sin aviso cuando no hay notas que perder.

**La barra de navegación tiene dos presentaciones** (Apariencia → Navegación), que son las dos
variantes del spec de toolbars de Material 3:
- *Integrada*: pegada al borde, esquinas rectas, sin contorno. El Scaffold reserva su hueco.
- *Flotante*: separada 8dp, píldora completa, sombra y contorno. **Se superpone al contenido a
  propósito**; el contenido pasa por detrás al desplazarse.

**El indicador activo usa el contenedor del acento, no `secondaryContainer`.** Material pediría
lo segundo, pero aquí `secondary` es un azul con significado propio, no una variante tonal del
primario: seguir la letra del spec dejaría una píldora azul bajo un acento violeta.

**Monet está desactivado por defecto.** Sigue disponible en Apariencia. De serie la app tomaba
el color del fondo de pantalla de cada usuario y no tenía identidad propia.

---

## Reglas que hay que respetar al tocar UI

**Márgenes inferiores.** Dos valores compartidos en `core/design/theme/AppearanceTheme.kt`:
- `scrollBottomRoom` — margen inferior mínimo de cualquier lista o columna desplazable.
- `LocalBottomBarOverlay` — lo que tapa la barra flotante; 0 con la barra acoplada.

Todo elemento **anclado** (FAB, botón pegado abajo) suma `LocalBottomBarOverlay.current`, o la
barra lo tapa para siempre. Todo contenedor **desplazable** usa `scrollBottomRoom` en su
`bottom`, sumado a lo que ya reserve por un botón anclado propio.

**Acciones ancladas al fondo:** usar `Modifier.bottomActionInsets()`, y **dentro** de la
superficie, nunca sobre ella. Por fuera levanta la barra entera y deja una franja transparente
contra el borde por la que se ve pasar el contenido.

**Con targetSdk 36 la ventana ya no se redimensiona al abrir el teclado** — edge-to-edge es
obligatorio y `adjustResize` no se aplica. Cada pantalla se aparta sola.

**Botones de acción:** `SquishyButton` (replica la firma del `Button` de Material) o
`UniStackButton`. Ambos traen el gesto de compresión. El `Button` crudo no.

**Días de la semana:** `DayLabels` en `core/utils`. Miércoles es `X`. No escribir listas de
letras a mano.

**Sin dato:** constante `NO_DATA` (`—`). Un `0` es un dato («cero pendientes» informa); la raya
es ausencia («no hay promedio sin notas»). No confundirlos.

---

## Trampas conocidas

**`GradesRepository.updateSubject()` no toca las notas**, aunque el `Subject` que recibe las
lleve dentro: viven en otra tabla. Para borrarlas, `clearGrades(subjectId)`. El doble en
memoria se alineó con ese contrato y hay un test que lo fija — antes era más permisivo que
Room, así que el código podía pasar los tests y fallar en producción.

**Rótulos deducidos por palabras clave.** `heroLabel()` en `HomePriorityHero` todavía adivina
el rótulo olfateando su propio mensaje. Ya se corrigió el mismo patrón en las notificaciones
(`SubjectHintKind`) y el comodín del hero ahora sale de `HomePriorityAction`, pero la parte de
palabras clave sigue ahí. Editar la redacción puede cambiar el rótulo sin querer.

**El límite del plan gratis** (`FeatureGate.canCreateSubject`) hay que comprobarlo en **todas**
las rutas que crean materias. Horario lo esquivaba. Hoy `PRO_FEATURES_ENABLED = false`, así que
un fallo ahí es invisible hasta que se active la suscripción.

---

## Repositorios

| | |
|---|---|
| `Kmlozmz/UniStack` | Código. **Privado.** |
| `Kmlozmz/UniStack-landing_page` | Web. **Privado.** |
| `Kmlozmz/UniStack-releases` | Etiquetas y APK. **Público — falta crearlo.** |

Las publicaciones de un repo privado devuelven 404 a quien no ha iniciado sesión, así que ni el
actualizador ni el botón de descarga de la web pueden llegar a ellas. Se descartó incrustar un
token en el APK: se extrae del paquete trivialmente y daría escritura sobre el código.

El código ya apunta al repo nuevo (`githubReleasesSlug` en `app/build.gradle.kts`). Falta:
crear el repo como público, comprobar que el token de `publishReleaseToGitHub` tiene escritura
sobre él, y cambiar el enlace de descarga de la landing.

Hasta que exista, el actualizador mostrará el mensaje del 404 en vez de «Al día». Es correcto:
antes cualquier fallo se convertía en «estás al día».

---

## Pendiente

**Rediseñar la pantalla de materia y unificar las dos duplicadas.** Es lo siguiente y no está
empezado. El usuario dijo «no me gusta como se ve».

Hay dos pantallas para la misma entidad:
- `AddSubjectScreen.kt` — «Agregar materia», desde Académico. Meta, corte, límite de plan,
  paleta completa.
- El diálogo en `CalendarScheduleScreen.kt` (~1130) — «Nueva materia», desde Horario. Solo el
  bloque de clase.
- Y una tercera puerta: «Clase recurrente» en la hoja de agenda.

Ya se unificó lo que no tenía razón de divergir: paleta de colores, valores por defecto del
horario, sitio del botón de guardar y el `FeatureGate` que faltaba. Queda el rediseño visual.

Plan propuesto, sin aprobar:
- Una sola pantalla con dos modos: desde Académico completa; desde Horario con la parte
  académica plegada.
- Tres bloques en vez de ocho tarjetas grises sin jerarquía: *Identidad* (nombre, color,
  profesor), *Cuándo* (días, horas, aula, repetición), *Académico* (meta, corte, recordatorio).
- Sacar el color de la fila diminuta junto a «Profesor»: es lo que identifica la materia en
  toda la app.

**Otros hilos abiertos:** separar el token de Gastos del de error; `Configuración` aparece en el
cajón y dentro de Perfil; `Sincronización` del cajón solapa con «Datos y respaldos»; los módulos
desactivables no cuadran con las pestañas de la barra (Horario no es módulo pero sí pestaña,
Trabajos es módulo y no tiene pestaña).

---

## Informe de auditoría

Análisis inicial de las 14 features y las 20 pantallas, con los hallazgos ordenados por
gravedad: <https://claude.ai/code/artifact/99ce07c0-5e53-4af0-a94a-ddc7803b4044>

Tres afirmaciones de ese informe resultaron **equivocadas** al ir a arreglarlas, y están
corregidas aquí: la rejilla de horario no empieza a las 06:00 fijas (sale de la clase más
temprana), la gráfica vacía de Gastos es un esqueleto deliberado al 38% de opacidad, y
`AddGradeScreen` sí valida la nota contra el máximo y la suma de porcentajes.
