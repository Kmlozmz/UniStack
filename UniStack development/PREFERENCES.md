# PREFERENCES — el desarrollador, en lo que importa para UniStack

Sólo preferencias observadas o dichas. Cada una con la evidencia. Nada inventado.

## Estilo visual

- **Material 3 Expressive de verdad**, con sus piezas nuevas (grupos conectados, wavy, shapes,
  muelles). Rechaza «Material genérico» y lo «virgen/funky».
- **Tema oscuro** como cara principal (fondo `#0A0C11`); juzga los diseños en oscuro.
- **Color que rellena**; contenedores tonales; nada de rails, bordes de color ni sombras.
- **Forma antes que color** para estados (ruedas visto/equis).
- **Textos concretos**: prefiere «te quedan 3 de 4 faltas» a «estado OK»; «no tan genéricos y
  más intuitivos» (11 sep).
- **Muestras en vivo** de cada ajuste; le gusta ver la animación en su tarjeta.
- Le gustan los **artifacts**: «full m3e, colores, organizados, armónicos visualmente»; con
  teléfono real, mandos y varias propuestas; no elige «a ciegas»: quiere ver cada opción.
- Patrones conocidos cuando encajan: Keep para notas; el empuje iOS para navegar.
- Identidad por sección (Horario azul, Gastos rojo) y **el logo es el asset**.

## Código

- Le da igual la forma del código mientras sea coherente con lo que hay y **haga lo que se ve**.
  Lo que sí exige: que un ajuste aplique, que nada quede a medias, que no se pierdan datos.
- Acepta piezas de UI con nombres en español (`HojaDeClase`, `celebracionDelDia`); el dominio
  en inglés. `VERIFY` si prefiere volver a inglés puro.
- Comentarios con el porqué en español (es el estilo de todo el repo).
- **Commits en inglés, Conventional Commits, sin coautoría** — lo repitió tras encontrarlo.
- Quiere reescribir la historia antigua (94 commits) pero **consultándolo antes** (historia publicada).

## UX

- **Honestidad sobre comodidad**: borrar antes que convertir; suelo/techo antes que proyección;
  lo por venir no se edita.
- **Nada interrumpe si no hace falta**: el diálogo «¿lleva nota?» siempre le sobraba; los
  avisos deben llevar a donde se puede actuar.
- **Todo accesible sin cuenta, sin pagar, sin límites.**
- **Deshacer** en lo reversible; doble confirmación escribiendo la palabra en lo irreversible.
- Quiere poder **ocultar** lo que no usa (Gastos), pero sin que el onboarding lo pregunte.

## Comunicación

- Español, tuteo, mensajes cortos con erratas; capturas numeradas; una viñeta por cambio.
- Vetos cortos y definitivos; elogios cortos («me agrada», «está bastante bien», «ya está perfecto»).
- Quiere saber **qué sigue** al cerrar algo («dime qué sigue») y la lista tachada.
- Prefiere que el agente decida lo rutinario y le enseñe el resultado; pregunta sólo si hace falta.
- **Nivel de detalle:** reportes por viñetas con ruta de comprobación; no le interesan los
  detalles internos salvo que expliquen un fallo. En artifacts sí quiere detalle («le falta
  detalle, aunque no mucho»).

## Tolerancia a abstracciones

- Baja. Un módulo, sin capa de casos de uso, sin librerías nuevas por gusto. Centralizar reglas
  sí (calculador, catálogo, proveedor de textos).

## Arquitectura

- Local-first, sin backend, sin cuenta obligatoria. Firebase sólo para identidad+respaldo
  opcional, cuando exista.
- Distribución propia (bot + GitHub Releases); nunca ha hablado de Play Store (`UNKNOWN`).

## Suele rechazar

Rails de color · siluetas raras · pantallas «inventadas» que no copian la app · animaciones sin
texto · efectos pequeños/rápidos · ajustes sin efecto · dos APK por envío · mensajes de bot ·
coautoría en commits · reabrir lo decidido · humo en pitches y artifacts.

## Suele valorar

Que se le entregue entero · que se le diga qué falta y por qué importa · que el artifact se
pueda tocar · que el reporte diga dónde mirar · que se recuerden sus reglas sin repetirlas ·
que se arregle de paso un fallo real encontrado (y se le cuente).
