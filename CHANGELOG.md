# Cambios

Lo que cambia en cada versión publicada, escrito para quien usa la app.

Cada entrada tiene que decir **qué nota el usuario**, no qué archivos se tocaron. «Se corrigió
el cálculo del promedio» sirve; «cambios en la capa de datos» no. Si algo no se nota en la app,
no va aquí: para eso está el historial de git.

Al publicar, `publishReleaseToGitHub` copia la sección de esa versión tal cual como notas de la
publicación en GitHub. **Si la sección no existe, la publicación falla**: es a propósito, para
que ninguna versión salga con notas vacías o inventadas.

Lo que se va acumulando entre publicaciones vive en `[Sin publicar]`; al publicar se le pone el
número y la fecha, y se abre una nueva.

---

## [Sin publicar]

### Corregido

- El botón «Permitir instalación» de la ventana de actualización salía cortado.
- La app ya no te manda una notificación de actualización cuando eres tú quien acaba de pulsar
  «Verificar» o de cambiar de canal: solo avisa cuando la comprobación pasa por su cuenta.
- Borrar el APK descargado pide confirmación y avisa cuando ha terminado.
- Cambiar a un canal de preestreno explica antes lo que implica: fallos posibles, copia de
  seguridad recomendada y que no se puede volver atrás sin desinstalar.

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
