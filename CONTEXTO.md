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

## El formulario de materia

Hay **una sola pantalla** para crear y editar materias, `SubjectFormScreen.kt`, con dos modos.
Antes eran dos formularios distintos para la misma entidad y divergían en todo lo que nadie
sincronizaba a mano.

- `ACADEMIC` — desde Académico. El bloque académico llega abierto y el horario se puede apagar.
- `SCHEDULE` — desde Horario. El bloque académico llega plegado y el horario no se puede apagar:
  es justo lo que se venía a crear.

Tres bloques, en este orden: **Identidad** (nombre, color, profesor), **Cuándo** (días, horas,
aula, repetición) y **Académico** (meta, corte, recordatorio). Solo el de identidad va teñido
con el color de la materia; los otros dos son neutros. Esa es la jerarquía: antes eran ocho
tarjetas grises del mismo tono.

**Plegar esconde los controles, nunca la información.** Cada bloque plegado resume su contenido
en la cabecera. Y el bloque académico se despliega solo si la meta bloquea el guardado: un botón
apagado sin motivo visible es peor que un bloque abierto de más.

**Las rutas de Horario no dependen del módulo de notas.** `moduleForRoute()` mapea `add_subject`
y `edit_subject` a `GRADES`, pero deja fuera a propósito `add_subject_from_schedule` y
`edit_subject_from_schedule`. Horario es pestaña fija aunque Académico esté apagado; atarlas
convertiría «Añadir clase» en un botón que lleva a Inicio. Hay un test que lo fija.

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
un fallo ahí es invisible hasta que se active la suscripción. Ahora hay una sola ruta y la
comprobación vive en el formulario, pero la regla sigue en pie para cualquier puerta nueva.

**El profesor no es de la materia, es del bloque de clase.** `Subject` no tiene ese campo: se
guarda dentro de `ClassSession.location`, con el formato `"aula•profesor"`. Por eso el formulario
avisa cuando hay profesor escrito y el horario apagado — sin clase no hay dónde guardarlo. El
campo aparece en *Identidad* porque es donde el usuario lo busca, no donde vive el dato. Para
tener profesor sin horario habría que añadirlo a `Subject`.

**Una materia tiene como mucho un bloque de clase.** `saveSubjectSchedule()` borra los sobrantes
(`existing.drop(1)`). Ninguna ruta puede crear un segundo, así que hoy el invariante se cumple,
pero un respaldo restaurado con dos sesiones para la misma materia perdería una al editarla.

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

**Probar el formulario unificado en el móvil.** Está compilado y con los tests en verde, pero
las tres puertas de Horario —«Agregar clase», «Añadir clase» del día vacío y «Clase recurrente»
de la hoja de agenda— no se han recorrido a mano.

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
