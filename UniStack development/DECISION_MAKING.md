# DECISION_MAKING — cómo se decide en UniStack

## Quién decide qué

- **Producto y diseño: él.** Elige entre propuestas, combina, veta. El agente propone con
  argumentos y acepta el veredicto.
- **Implementación: el agente**, dentro de la forma que la app ya tiene. Lo que decide solo lo
  cuenta en el reporte («decidido por mí sin objeción» queda anotado).
- **Datos y publicación: consulta obligada** si es irreversible o hacia fuera.

## Cómo se evalúa una solución (en este orden)

1. **¿Pierde o inventa datos?** Si sí, no (convertir notas al cambiar de escala → borrar con
   confirmación; proyectar notas → suelo y techo).
2. **¿Es honesto con el usuario?** «Borrar es honesto; convertir parece amable pero falsifica.»
   «Cancelada y reprogramada no gastan falta: es la forma honesta de decir "no fui y no fue
   cosa mía".»
3. **¿Se parece a lo que la app ya hace?** Si hay una pieza, se usa. Si no, se explica por qué
   no sirve la que hay.
4. **¿Hace algo visible?** Un ajuste, una pantalla o una opción que no cambia nada no entra.
5. **¿Es M3 Expressive de verdad?** Contenedor tonal, grupo conectado, indicador ondulado,
   `MaterialShapes`, movimiento por muelles; no «Material genérico».
6. **¿Cabe en esta ronda?** Lo que pide tabla nueva o pantalla nueva se marca y se decide aparte
   (subtareas se aceptaron «en esta ronda» sabiendo que piden tabla).

## Qué se prioriza cuando chocan

| Choque | Gana | Ejemplo real |
|---|---|---|
| UX vs arquitectura | UX, si no cuesta datos | El detalle de tarea como hoja (no pantalla) para no sacar de la lista; la pantalla queda como «abrir entera» |
| Simplicidad vs opciones | Simplicidad | Movimiento de 25 a 7 gestos: «lo demás ya se mueve solo» |
| Spec de Material vs identidad | Identidad, salvo que el spec ya la respete | Indicador de la barra: cuando el secundario dejó de ser azul suelto, se volvió al spec |
| Precisión vs rapidez | Precisión en datos, rapidez en UI | Alarmas exactas; iterar seis alphas en una tarde |
| Lo pedido vs lo «mejor» | Lo pedido | Logo por código era «mejor» (un solo color de marca) y se revirtió |
| Rendimiento | Nunca ha decidido nada | — |

## Cómo se evalúan cambios visuales

- **Con el artifact** primero: teléfono real a escala, tokens reales, todo tocable. Él lo juzga
  ahí («ya sabes cómo me gustan los artifacts»).
- **Con capturas** después: compara el APK con el artifact aprobado. Desviación = fallo.
- Criterios que ha usado: «se ve extraño», «muy funky/virgen», «no coincide con M3E», «AI slop»,
  «le falta texto», «demasiado chiquito y rápido», «toma el color del tema» (cuando debía ser fijo).
- Lo que pasa a la primera suele ser lo que **copia** una pantalla que ya le gustaba (hoja de
  Asistencias, hero de Inicio, MetricCards).

## Cómo se decide entre dos implementaciones

- Se prefiere la que centraliza la regla (un calculador, un catálogo, un proveedor de textos).
- Se prefiere la que no toca lo persistido.
- Se prefiere la que deja test.
- Si empatan, la más corta.

## Cómo se evita la sobreingeniería

- Regla de **«nada a medias por si acaso»**: o se hace entero o se borra (paywall).
- Regla de **«un ajuste sin sitio de aplicación no entra»**.
- No se abren módulos Gradle, capas de casos de uso ni librerías nuevas sin dolor real.
- Los artifacts marcan el alcance en días y qué pide tabla nueva; él elige sabiendo el coste.

## Cómo se tratan las decisiones ya establecidas

- **No se reabren sin que él lo pida.** Está en la memoria del agente y en
  `DESIGN_DECISIONS.md`. Si una tarea nueva las roza, se menciona en una frase («esto está
  decidido así por X; lo mantengo») y se sigue.
- Si él cambia de opinión (Movimiento, dos veces), se aplica y se anota la fecha y el nuevo estado.
- Las decisiones antiguas no se borran de la documentación: explican por qué el código es así.

## Ejemplos reales, resumidos

- **Suelo y techo en vez de proyección** — el detalle enseñaba dos proyecciones distintas en la
  misma tarjeta; se eliminó la suposición y se enseñan los dos extremos exactos.
- **Repo público sólo para releases** — un token en el APK era la alternativa rápida; se
  descartó por seguridad y se creó `UniStack-releases`.
- **Historial de asistencias acotado al periodo** — el «histórico por semestre» se adelantó en
  la lista porque asistencias dependía de él; primero el modelo, luego la pantalla.
- **Notas → Keep** — tras cuatro tandas de artifact propio, al verlo construido pidió replicar
  Keep con capturas; se aplicó y manda sobre el artifact.
- **Selector de gráfico de Gastos en la pantalla** — porque «cambia una tarjeta, no la app».
- **i18n con `Textos`** — la alternativa era `if (isEnglish)` en 432 sitios; se movió todo a
  resources con scripts verificados y tests que leen los XML.
- **Tareas D** — de tres propuestas eligió una combinación y dos decisiones (hoja+abrir entera,
  subtareas ya); el agente lo pidió como tres toggles con vista previa para que no eligiera «a ciegas».
