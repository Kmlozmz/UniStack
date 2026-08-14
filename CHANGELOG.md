# Cambios

Lo que cambia en cada versión publicada, escrito para quien usa la app.

Cada entrada tiene que decir **qué nota el usuario**, no qué archivos se tocaron. «Se corrigió
el cálculo del promedio» sirve; «cambios en la capa de datos» no. Si algo no se nota en la app,
no va aquí: para eso está el historial de git.

## Cómo se escribe una versión

```
## [1.2.0] — 2026-09-01

**Lo importante:** una o dos frases con lo que de verdad cambia esta versión.

#### Área que se toca
- Una línea por cambio, en presente y desde fuera.
- Otra línea. Sin párrafos y sin contar por qué estaba mal.
```

- **Los destacados van primero.** Quien lee tres líneas se lleva lo que importa; el resto está
  ahí para quien quiera bajar.
- **Se agrupa por área** —Respaldos, Notificaciones, Horario, Notas—, no por «Corregido» y
  «Añadido»: al usuario le importa dónde va a notarlo, no en qué categoría lo clasificamos.
- **Una línea por viñeta.** El motivo, la historia del fallo y lo que se probó viven en el
  mensaje del commit, que es donde alguien los va a buscar de verdad.
- **El encabezado lleva corchetes** (`## [1.2.0] — fecha`): de ahí sacan la sección la
  publicación en GitHub, el aviso de actualización y la pantalla de Novedades.

Al publicar, `publishReleaseToGitHub` copia la sección de esa versión tal cual como notas de la
publicación en GitHub. **Si la sección no existe, la publicación falla**: es a propósito, para
que ninguna versión salga con notas vacías o inventadas.

Lo que se va acumulando entre publicaciones vive en `[Sin publicar]`; al publicar se le pone el
número y la fecha, y se abre una nueva.

---

## [Sin publicar]

#### Ayuda y soporte
- Reportar y sugerir son dos filas con su icono y su descripción, en vez de dos botones donde uno parecía la opción ya elegida.
- El aviso de que el grupo es público sale al escribir, que es cuando importa.

---

## [1.1.0-alpha.1] — 2026-08-14

**Lo importante:** ya se puede reportar un fallo o sugerir algo desde la app, y llega al grupo
de soporte con tu versión y tu teléfono apuntados.

#### Ayuda y soporte
- Dos botones, «Reportar un fallo» y «Sugerir algo», cada uno abre su tema del grupo.
- El mensaje se copia solo: al abrirse Telegram solo hay que pegarlo.
- Se apuntan la versión, el modelo y la versión de Android, que es lo primero que haría falta preguntar.

#### Novedades
- Cada versión se presenta con lo importante primero y viñetas cortas por área.

---

## [1.0.0-beta.1] — 2026-08-13

Primera beta. La lista de lo que lleva la 1.0.0 queda cerrada: a partir de aquí solo entran
arreglos de lo que ya está dentro.

### Corregido

- **Los avisos que apagabas sin permiso no se podían volver a encender.** Encender uno pedía el
  permiso y solo guardaba si lo aceptabas, así que quien lo hubiera denegado podía apagarlos
  todos y ninguno volvía a marcarse. Ahora, sin permiso, no se toca nada: los interruptores
  llegan apagados y lo único que se puede pulsar es el botón de arriba, que es lo que hay que
  resolver primero.
- **Los botones de anticipación no hacían nada.** «1h», «3h», «24h»… se veían y se podían tocar,
  pero ninguno cambiaba la anticipación: al rehacer la pantalla se quedaron sin la parte que
  responde al toque.
- **El botón para activar los avisos no hacía nada.** Cuando Android ya ha dejado de enseñar el
  diálogo del permiso, pedirlo otra vez vuelve con un «no» sin que se vea nada. Ahora, llegado
  ese punto, el botón dice «Ajustes» y abre los del sistema.
- **La app daba por hecho que podía avisar en teléfonos anteriores a Android 13**, aunque
  tuvieras sus notificaciones apagadas en ajustes. Ahora lo comprueba de verdad.
- **Lo que escribías al final de una lista quedaba debajo del teclado.** Pasaba en la caja de
  sugerencia y en las notas rápidas: el contenido ahora sube por encima del teclado.

### Cambiado

- **Novedades enseña solo versiones publicadas, y solo las de tu canal.** Estaba sacando también
  lo que aún no había salido. Cada versión reúne lo suyo desde la anterior de su mismo canal:
  una definitiva recoge todo lo que pasó desde la definitiva anterior —preestrenos incluidos— y
  una beta, desde la beta anterior. Lo no publicado tampoco viaja ya dentro del APK.
- **Antes de restaurar una copia se ve qué cambia.** Decía «v10 · 1 materias · 0 notas» en una
  línea; ese «v10» no significa nada fuera del código y las cifras no decían contra qué se
  comparaban. Ahora salen el nombre del archivo y una tabla de lo que tienes ahora frente a lo
  que quedaría, con lo que baja marcado en rojo. Un archivo que no es una copia lo dice y no
  ofrece restaurar.
- **La pantalla de notificaciones deja de repetirse.** Con la bandeja vacía había tres capas
  diciendo lo mismo; ahora el resumen y los filtros solo salen si hay avisos, y el vacío
  distingue entre «no ha llegado nada» y «este filtro no tiene nada», con acceso directo a
  elegir qué quieres recibir.
- **La caja para enviar sugerencias queda apagada por ahora.** Enviaba abriendo el selector del
  teléfono, así que el mensaje salía hacia donde tú eligieras y a nosotros no nos llegaba nada.
  Vuelve cuando haya un sitio donde se lean. Las preguntas frecuentes siguen igual.

---

## [1.0.0-alpha.5] — 2026-08-13

Quinta alpha. La más grande hasta ahora: respaldos que son archivos de verdad, seis pantallas
nuevas y las notificaciones rehechas.

### Corregido

- **La vista previa del horario esconde clases si el día está repartido.** Enseñaba una ventana
  de siete horas colocada sobre la clase más temprana, así que una clase a la 1:00 dejaba fuera
  todo el resto del día, y quien tuviera algo a las 6:30 y otra cosa a las 15:30 no veía la
  segunda. Ahora enseña las horas que tienen clase y pliega los huecos vacíos con una marca.
- **El selector de color se quedaba pegado al arrastrar el dedo.** El punto no seguía la mano:
  el gesto se cancelaba solo a mitad del arrastre y había que soltar y volver a tocar para
  moverlo un poco más. Ahora sigue el dedo hasta que se levanta.
- **El teclado no se quitaba al tocar fuera de un campo.** En los formularios de materia y de
  nota se quedaba puesto tapando media pantalla. Ahora se cierra al tocar fuera o al desplazar
  con el dedo. Y solo con el dedo: al enfocar un campo del final de una lista, el ajuste que
  hace la app para dejarlo por encima del teclado lo cerraba al instante.
- **Al salir de un formulario, el indicador de la barra inferior pasaba por Inicio.** Cruzaba
  de Inicio a la sección en la que estabas, como si la app hubiera pasado por la pantalla de
  inicio. No cambiaba de sección: era la barra pintando un primer fotograma sin saber todavía
  dónde estaba.
- **El detalle de una clase salía a media pantalla.** El panel abría por la mitad y las
  opciones de abajo —historial, editar, eliminar— quedaban fuera hasta que se arrastraba hacia
  arriba. Ahora abre entero, y rueda si no cabe.

### Cambiado

- **Las copias de seguridad son archivos.** Antes la copia se sacaba copiando un JSON al
  portapapeles y se restauraba pegándolo en un campo de texto: se perdía al copiar cualquier
  otra cosa, no cabía entera y no había dónde guardarla. Ahora la copia se guarda con el
  selector del sistema —donde tú quieras—, se puede compartir, y para restaurar se elige el
  archivo. Antes de restaurar, la app dice qué trae y avisa de que reemplaza lo que tengas.
  Los CSV de tareas y gastos y el PDF de notas también son archivos.
- **La sección de respaldos avisa de que esto es temporal**: por ahora la copia la guardas tú,
  y pronto se podrá vincular la cuenta de Google para que se haga sola. La copia en la nube
  aparece marcada como «Pronto» mientras no esté activa, en vez de fallar al pulsarla.
- **El botón de configuración del perfil dice «Configuración»** en lugar de ser un engranaje
  suelto, y desaparece de Perfil la lista de ajustes que repetía lo que ese botón ya abre.
- **El panel lateral lleva a donde dice.** «Calculadora GPA» abría la lista de materias y
  «Notas rápidas» abría Tareas; ahora cada una abre lo suyo, y Materias y Tareas aparecen con su
  nombre. «Cerrar sesión» desaparece: no cerraba ninguna sesión (la cuenta se desvincula desde
  Perfil).
- **El perfil dice cómo va el semestre**, no solo cuál es tu meta: el promedio de lo evaluado
  con una barra hasta la meta, cuántas materias van aprobando, cuántas en riesgo y cuántas notas
  llevas. Debajo, acceso directo a lo tuyo: académico, notificaciones, módulos, apariencia,
  datos y actualizaciones. El nombre se edita con el lápiz, sin formulario permanente.
- **Las tarjetas del horario se pueden tocar y cuentan el resto.** Los rótulos salían cortados
  («Clases h…», «Esta se…»); ahora son de una palabra y al tocarlas se abre el detalle: las
  clases de hoy una por una, o las horas de la semana repartidas por día.
- **La cabecera del horario dice cuántas clases tienes hoy y cuántas horas de clase lleva la
  semana.** En su sitio había dos tarjetas, «10:30 · Próxima» y «408D · Aula», que repetían lo
  que el panel de «Próxima clase» ya cuenta entero unos centímetros más abajo.
- **«Próxima clase» sube encima de la rejilla y «Horario completo» baja debajo.** Lo que se
  viene a mirar, primero; la salida hacia otra pantalla, cuando ya has visto la semana.
- **Las clases del horario ocupan menos.** Cada bloque se pintaba entero del color de la
  materia y apilaba la hora de inicio, la de fin, el nombre y el aula en una columna estrecha,
  repitiendo lo que el eje de horas ya decía. Ahora el color va en una franja y un fondo suave,
  manda el nombre, y el resto aparece solo si el bloque da de sí.
- **El detalle de una clase enseña sus datos en rejilla:** horario, duración, aula, profesor,
  cada cuántas semanas se repite y el recordatorio, más el estado de asistencia del día a la
  vista. Antes iban en una línea de texto que quedaba vacía si faltaba el aula y el profesor.
- **Volver a tocar el estado de asistencia marcado lo deshace.** Si te equivocabas de botón, no
  había forma de volver a «pendiente».
- **El horario completo respeta el color que elegiste para la materia.** Usaba el color que la
  app deriva del nombre, así que la misma materia salía de dos colores según la pantalla.

### Añadido

- **Cada entrada del panel lateral abre su propia pantalla, a pantalla completa.** La barra
  inferior se aparta mientras estás en ellas, para que se lean como un sitio propio y no como
  una capa encima de Inicio.
- **Calculadora GPA.** Escribe notas con sus créditos y sale el promedio ponderado, sin tocar
  nada de lo registrado. Un botón trae tus materias con lo que llevas evaluado.
- **Notas rápidas.** Un bloc que se guarda solo en este teléfono, para lo que no merece ser una
  tarea: el aula que cambió, el tema del parcial.
- **UniStack AI y Labs tienen pantalla propia** donde se cuenta qué van a hacer y en qué punto
  están, en vez de una fila que no responde.
- **Novedades, Recursos, Ayuda y Acerca de existen de verdad.** Eran filas del panel lateral que
  no llevaban a ninguna parte. Novedades trae el registro de cambios dentro de la app, sin
  conexión; Recursos, enlaces útiles para estudiar; Ayuda, preguntas frecuentes y un formulario
  de sugerencia que se envía por donde tú elijas; Acerca de, la versión y qué hace la app con
  tus datos.
- **El nombre de la app se lee «UniStack»**, con «Uni» del color del texto y «Stack» en morado,
  igual que en la pantalla de bienvenida.
- **Al poner una hora de inicio de madrugada, la app pregunta si es correcta.** Casi siempre es
  un error al girar la rueda —querías las 13:00 y pusiste la 1:00—, así que lo dice y ofrece
  volver. Si la clase es de verdad a esa hora, se confirma y ya.

---

## [1.0.0-alpha.4] — 2026-08-13

Cuarta alpha, con arreglos de lo que salió en la tercera.

### Corregido

- **El código de un canal abría el canal al que perteneciera, sin importar dónde lo escribieras.**
  Metías el de alpha en la casilla de beta y te abría alpha. Ahora cada casilla solo acepta su
  código; si no es el suyo, no abre nada y no dice de quién era.

### Cambiado

- El botón para validar el código dice «Acceder» en lugar de «Canjear».
- El aviso de versión nueva cambia de texto y se puede desplegar para leerlo entero.

---

## [1.0.0-alpha.3] — 2026-08-13

Tercera alpha. Casi todo es la pantalla de actualizaciones: cuándo avisa, qué te ofrece y quién puede recibir cada versión.

### Cambiado

- **Alpha y beta son ahora canales distintos, no escalones.** Cada uno tiene su propio código:
  el de alpha ya no abre beta. La versión definitiva sigue llegando a los tres canales, para que
  nadie se quede anclado en un preestreno.
- Los códigos anteriores dejan de valer, porque abrían los dos canales a la vez.
- **Un acceso que no se puede confirmar durante una semana caduca.** Antes, quedarse sin conexión
  conservaba el canal indefinidamente. Un corte normal no revoca nada.

### Corregido

- Estar en el canal Estable sin ninguna versión estable publicada se anunciaba como un error
  («no se encontró ninguna publicación»), cuando lo que pasa es que no hay nada que instalar.
- **Las notas de la versión se leen como texto, no como código.** Salían con los `###` y los
  asteriscos a la vista, y cada línea del archivo se convertía en una viñeta suelta.
- Los botones «Más tarde» y «Descargar» de esa misma ventana se quedaban sin ancho y se
  recortaban a puntos suspensivos cuando las notas eran largas.

### Añadido

- **La app mira si hay versión nueva cada dos horas aunque esté cerrada**, y al abrirla si han
  pasado 45 minutos. Antes solo miraba al arrancar el proceso y como mucho una vez cada 12 horas,
  así que quien la dejaba en segundo plano no se enteraba nunca.

---

## [1.0.0-alpha.2] — 2026-08-13

Segunda alpha. Sobre todo, el canal por el que recibes las actualizaciones.

### Corregido

- El botón «Permitir instalación» de la ventana de actualización salía cortado.
- La app ya no te manda una notificación de actualización cuando eres tú quien acaba de pulsar
  «Verificar» o de cambiar de canal: solo avisa cuando la comprobación pasa por su cuenta.
- Borrar el APK descargado pide confirmación y avisa cuando ha terminado.
- Cambiar a un canal de preestreno explica antes lo que implica: fallos posibles, copia de
  seguridad recomendada y que no se puede volver atrás sin desinstalar.

### Añadido

- **Puedes elegir qué versiones recibes.** En Ajustes → Actualizaciones: Estable, Beta o Alpha.
  Estable solo recibe versiones terminadas, y es lo que viene puesto.
- **Beta y Alpha se abren con un código** que entrega quien publica la app. Sin él, la app solo
  ofrece versiones estables.

---

## [1.0.0-alpha.1] — 2026-08-13

Primera versión pública. Es un preestreno: está para probarla y avisar de lo que falle.

### Notas y materias

- **Las materias dicen dónde puedes acabar, no una proyección inventada.** En vez de un único
  número que suponía cómo te iría en lo que falta, ahora ves los dos extremos reales: la nota
  mínima que ya tienes asegurada aunque saques cero en todo lo demás, y la máxima que aún
  puedes alcanzar. La meta se dibuja dentro de esa franja, así que se ve de un vistazo si sigue
  a tu alcance.
- **Se corrigió el promedio de la materia.** La pantalla de detalle enseñaba dos cifras
  distintas para lo mismo, una encima de la otra: una nota pequeña en un corte recién empezado
  disparaba la de abajo como si el corte estuviera cerrado.
- **El aviso de la meta ya no se contradice.** Una materia terminada te pedía registrar notas,
  y el estado ámbar de «atención» no aparecía nunca en la escala de 0 a 100.
- **Tú eliges en qué corte vas.** La app daba por hecho que empezabas por el primero y te
  reclamaba el historial de unos cortes que nunca dijiste haber cursado. Ahora lo pregunta, y
  hasta que respondas no supone nada.
- **Un corte se cierra solo al repartir su 100%**: las notas nuevas pasan al siguiente y el
  cerrado baja a «Completados».
- **El tipo de actividad y el corte ya no vienen elegidos de fábrica.** Marcaban «Taller» y
  «Corte 1» sin que nadie los tocara.
- Al elegir «tipo de actividad» ya no se borra el nombre que hubieras escrito.
- El peso de una nota dice cuánto queda libre en el corte («queda 70% por repartir»), en vez de
  recordarte que la suma debe dar 100%.

### Materias y horario

- **Una sola pantalla para crear y editar una materia**, se entre desde Académico o desde
  Horario. Antes eran dos formularios distintos para lo mismo y no coincidían ni en los colores
  ni en las horas por defecto.
- **El profesor, el aula y el horario se ven en Académico.** Se piden al crear la materia y solo
  aparecían en Horario.

### Interfaz

- Las tarjetas de materia caben en la pantalla sin recortar texto, y las listas se pueden
  desplazar aunque el botón flotante ocupe el final.
- El color de una barra ya no significa dos cosas a la vez: su longitud mide cuánto llevas
  evaluado y el color es el de la materia.

### Actualizaciones

- La app busca actualizaciones en su repositorio público y las instala desde dentro.
- Un fallo de red o del servidor ya no se anuncia como «estás al día».
