# Cambios

Lo que cambia en cada versión publicada, escrito para quien usa la app.

Cada entrada tiene que decir **qué nota el usuario**, no qué archivos se tocaron. «Se corrigió
el cálculo del promedio» sirve; «cambios en la capa de datos» no. Si algo no se nota en la app,
no va aquí: para eso está el historial de git.

Lo anterior a esta versión está en `docs/changelog-historico.md`, fuera del APK. La app solo
enseña la versión que tienes puesta.

## [Sin publicar]

**Lo importante:** la app entera pasa a los componentes de Material 3 Expressive. Cambian la
barra de abajo, los selectores de fecha y hora, los interruptores, las barras de progreso, los
menús y las listas de ajustes. Y tres de esos cambios ya se eligen desde Apariencia.

### Añadido

- **Puedes elegir en Apariencia** si la barra de abajo lleva texto, si las barras de progreso de
  tus estudios van rectas u onduladas, y si el interruptor lleva un icono dentro. Están en
  «Detalles de la interfaz», con un interruptor de muestra al lado para ver cómo queda.
- **Gastos entre dos fechas.** Además de «Todo», «Semana» y «Mes», ahora hay «Periodo»: eliges
  el desde y el hasta en un calendario. Sirve para un corte del semestre o un viaje, que no
  empiezan un lunes ni caben en un mes.
- **Los botones de icono dicen qué hacen.** Mantén pulsado cualquiera y sale su nombre.
- **La hora se escribe.** Al poner la hora límite de una tarea o la de una clase, se abre el
  teclado numérico en vez del reloj. El reloj sigue ahí, en el botón de abajo a la izquierda.
- **El buscador se vacía con una cruz**, sin borrar letra a letra.

- **Mantén pulsada una materia y se marca.** Con varias marcadas sale una barra flotante abajo:
  cuántas llevas, marcar todas las de la lista, quitar la selección y eliminarlas de una vez.
- **El filtro de categoría de Gastos se quita con una cruz**, sin abrir la hoja para buscar
  «Todas».
- **Volver a tocar el tramo de Gastos que ya está puesto lo quita** y deja «Todo».

### Cambiado

- **La barra de abajo es la de Material.** Mismo sitio y mismos iconos, pero la pastilla del
  seleccionado, su animación y la altura ya no están calculadas a mano.
- **El calendario para elegir fecha es el de Material.** El que había estaba dibujado a mano,
  uno en Gastos y otro en Tareas, y para saltar de agosto a diciembre había que pulsar la
  flecha cuatro veces. El nuevo salta de año y deja escribir la fecha.
- **Los interruptores llevan un icono dentro.** Un visto encendidos y un aspa apagados, en toda
  la app. Antes el único indicio era el color, que no todo el mundo distingue igual.
- **Las barras y anillos de progreso de tus estudios son rectos** por defecto. La onda se queda
  para las descargas.
- **Eliminar está separado del resto en los menús**, debajo de una línea y en rojo.
- **Esperar y avanzar ya no se ven igual.** Cuando la app no sabe cuánto falta sale una forma
  que cambia, y no una barra que parecía atascada.
- **Los filtros de Materias van con contorno, no rellenos**, para distinguirlos del selector de
  Materias/Tareas que tienen justo encima.
- Las filas de ajustes, los selectores de una opción entre varias y los dos buscadores se
  comportan igual en toda la app.

- **El historial de Gastos va agrupado por día.** Cada día lleva su fecha una sola vez y el
  total de ese día al lado, en vez de repetir la fecha en cada gasto.
- **«Escríbenos» explica cómo se envía antes de salir de la app.** El botón dice «Enviar»,
  y el que abre Telegram aparece después, con las instrucciones delante.

- **El filtro de categoría de Gastos es un desplegable.** La flechita que lleva al lado ahora
  significa lo que parece: se abre una lista corta debajo en vez de otra pantalla. Elegir
  categoría pasa de tres toques a dos, y administrar cuáles aparecen al registrar sigue estando
  al final del menú.

### Corregido

- **El selector de fecha se dibujaba a franjas** al cambiar entre calendario y teclado: la
  ventana cambiaba de alto y Android la redibujaba por tramos. Ahora reserva el alto desde el
  principio.
- Se quitó el filtro de periodo a medida de Gastos: daba más problemas que utilidad.

- **La onda gris del toque se salía de los bordes** al mantener pulsado. Pasaba en las materias,
  la foto de perfil, dos filas de Ayuda, las cabeceras de formulario y el aviso de actualización.
- **El calendario y el reloj salían en inglés.** La app va en español entera, también en lo que
  dibuja Material.
- **El selector de dos fechas se metía debajo del reloj del móvil.**
- Volver a tocar el tramo de Gastos ya no hace un «deseleccionar» que en realidad elegía «Todo».

- **El selector de motivo de «Escríbenos» vuelve a ser el de siempre**, con su rebote al marcar.
- **La barra de selección salía como un círculo negro enorme** en mitad de la lista. Ahora es una
  pastilla que se ajusta a lo que lleva dentro.
- **El calendario para elegir fecha se salía del diálogo**: le sobraba un título que no cabía.
- **Los filtros de Gastos ya no salen cortados** en «Tod», «Sem», «Mes». El tramo va en su fila y
  el periodo a medida baja con los chips, donde cabe decir las fechas.
- **La hoja de Categorías tenía dos rejillas idénticas** —una filtraba, otra encendía categorías—
  sin nada que dijera cuál era cuál. Ahora es una sola lista con dos modos arriba.

- **La cabecera de Académico ya no salta al cambiar de pestaña ni al abrir la lupa.** El
  buscador va debajo del texto de apoyo en vez de sustituirlo, y Académico, Horario y Gastos
  usan la misma cabecera: mismo tamaño, mismo espacio y mismos márgenes.
- **La sección elegida de la barra de abajo sube y crece un poco** al tocarla, así que se ve
  cuál es sin depender solo del color.
- **Las filas de ajustes son piezas sueltas**, cada una con su forma: la primera redondea
  arriba, la última abajo. Antes iban todas dentro de una misma tarjeta.

- El anillo de porcentaje evaluado de una materia se dibujaba dos veces, una encima de otra.
- El mismo botón de «mas opciónes» medía distinto en cada pantalla: había cinco tamaños
  repartidos por la app.

---

## [1.4.4] — 2026-08-21

Primera versión estable de UniStack. Esto es lo que puedes hacer con ella.

**Tus materias y tus notas.** Registra cada corte con lo que vale y la app calcula tu promedio
con lo que ya está evaluado, sin inventar notas que todavía no existen. Cada materia enseña su
suelo y su techo —con cuánto acabarías sacando 0 en lo que falta y con cuánto sacándolo todo— y
tu meta marcada dentro de esa franja: si queda fuera, lo sabes a tiempo.

**Tu horario.** Las clases con sus días y sus horas, la semana entera de un vistazo y la
próxima clase siempre a mano.

**Tus tareas.** Entregas y pendientes con fecha, ordenadas por lo que corre más prisa, y avisos
para que no se te pase ninguna.

**Tus gastos.** Registros rápidos, presupuesto de la semana y en qué se te va.

**Una calculadora de notas** con tres cuentas: cuánto llevas en una materia repartiendo el peso
de cada corte, cómo queda el semestre con todas tus materias, y qué nota necesitas sacar en lo
que falta para llegar a donde quieres.

**Notas rápidas** para lo que no quieras olvidar y **recursos** para buscar, estudiar y citar.

**Y una copia de todo cuando quieras.** Expórtala, llévatela a otro teléfono y deja la app
como estaba.
