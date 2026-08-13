# Guía de publicación — UniStack

Cómo se numera, cuándo se publica cada peldaño y qué reglas impone Android. Para el *porqué*
de las decisiones, `CONTEXTO.md`; esto es el manual de uso.

Última actualización: 13 ago 2026.

---

## La escalera

El `versionCode` sale del nombre de versión (`versionCodeFor()` en `app/build.gradle.kts`), y es
el número que Android mira para decidir qué APK se puede instalar encima de cuál.

| Nombre de versión | `versionCode` | Qué es |
|---|---:|---|
| `0.0.0-dev.<yyMMddHH>` | `1` | Compilación local. Nunca se publica. |
| `1.0.0-alpha.N` | `1_000_010 + N` | Preestreno temprano. |
| `1.0.0-beta.N` | `1_000_030 + N` | Preestreno estable. |
| `1.0.0-rc.N` | `1_000_060 + N` | Candidata a publicación. |
| `1.0.0` | `1_000_099` | Versión pública. |
| `1.0.1` | `1_000_199` | Corrección. |
| `1.1.0` | `1_010_099` | Funciones nuevas. |
| `2.0.0` | `2_000_099` | Cambio que rompe. |

La fórmula: `mayor × 1.000.000 + menor × 10.000 + parche × 100 + peldaño`.

**Topes:** hasta 19 iteraciones por peldaño (`alpha.1` … `alpha.19`), y hasta 99 en menor y en
parche. Pasar de ahí rompe el orden, y ningún test lo va a avisar.

---

## Cuándo se usa cada peldaño

**`dev`** — cada vez que compilas. Sale sola con `assembleDebug` y se va a Telegram. No se
publica nunca en GitHub.

**`alpha`** — cuando quieres probar por el canal real de actualización, tú o dos o tres personas
de confianza. Se espera que algo se rompa. Es el peldaño para «esto ya se puede tocar».

**`beta`** — cuando lo que entra en esa versión ya está cerrado y quieres que gente de fuera la
use de verdad, con sus datos. De aquí en adelante **no entran funciones nuevas**: solo arreglos.
Si se te cuela una función, sube a la siguiente `beta` y dilo en las notas.

**`rc`** — cuando la publicarías tal cual. Solo se toca por un fallo que impida publicarla. Si
una `rc` sobrevive unos días sin quejas, se convierte en la definitiva **con el mismo código**.

**Definitiva (`1.0.0`)** — lo que recibe todo el mundo.

**Parche (`1.0.1`)** — solo arreglos sobre la publicada. Nada nuevo.

**Menor (`1.1.0`)** — funciones nuevas que no rompen nada de lo que ya había.

**Mayor (`2.0.0`)** — algo que rompe. En una app como esta, en la práctica: una migración de
datos que no se puede deshacer, o quitar algo con lo que la gente ya contaba.

---

## Reglas de instalación

Android solo permite instalar un APK encima de otro si se cumplen **las dos**:

1. **La firma coincide.** Aquí siempre coincide: `debug` y `release` usan la misma clave
   (`debug { signingConfig = localRelease }`). Por eso un APK de Telegram y uno publicado se
   sustituyen sin problema.
2. **El `versionCode` no baja.** Igual sí vale (reinstalar), menor no.

De ahí sale todo lo demás:

| De → a | ¿Entra? | Por qué |
|---|:--:|---|
| `dev` → cualquier publicada | ✅ | 1 → 1.000.011 o más |
| `alpha.1` → `alpha.2` → `beta.1` → `rc.1` → `1.0.0` | ✅ | Sube en cada paso |
| `1.0.0` → `1.0.1` → `1.1.0` | ✅ | Sube |
| Publicada → `dev` | ❌ | 1.000.099 → 1, es bajar |
| `1.0.0` → `1.0.0-alpha.2` | ❌ | La alpha va por debajo de su definitiva |
| `1.0.0-alpha.1` reetiquetada | ❌ | Mismo número: quien ya la tenga no la ve como nueva |

Cuando **no** entra, Android dice «aplicación no instalada» sin más explicación. La salida es
desinstalar, **y eso borra los datos locales**.

**El actualizador de la app nunca ofrece una versión anterior**, así que estos casos solo
aparecen si alguien descarga el APK a mano desde GitHub.

---

## Reglas de publicación

**Un número publicado no se reutiliza.** Si hay que rehacer una publicación, sube la iteración
(`alpha.2`), no vuelvas a etiquetar `alpha.1`. Quien instaló la primera tiene ese `versionCode`
y la nueva no le entraría. Solo vale reetiquetar si estás seguro de que **nadie** la instaló.

**Los preestrenos van antes que su definitiva, nunca después.** Si ya publicaste `1.0.0`, no
saques una `1.0.0-beta.2`: para nadie que tenga la `1.0.0` sería una actualización. Lo siguiente
sería `1.0.1-alpha.1` o `1.1.0-alpha.1`.

**El sufijo decide que sea preestreno.** Cualquier nombre con `-algo` se publica marcado como
preestreno en GitHub. Se puede forzar con `-Pprerelease=true|false`, pero entonces la etiqueta y
la marca pueden contradecirse; mejor no.

**Las notas salen de `CHANGELOG.md`,** de la sección de esa versión exacta. Si la sección no
existe, **la publicación falla**: es a propósito, para que ninguna versión salga con notas de
relleno. Cada entrada dice qué nota quien usa la app, no qué archivos se tocaron.

Antes se generaban comparando huellas de archivos y decían cosas como «cambios en la capa de
datos», que no le sirve a nadie.

---

## Quién recibe cada versión

En Ajustes → Actualizaciones hay tres canales, y cada uno es **un suelo de estabilidad**, no un
filtro exclusivo:

| Canal | Recibe |
|---|---|
| **Estable** (por defecto) | Solo versiones sin sufijo: `1.0.0`, `1.0.1`, `1.1.0` |
| **Beta** | Lo anterior, más `-beta.N` y `-rc.N` |
| **Alpha** | Todo, incluidas las `-alpha.N` |

Quien está en Alpha también recibe la definitiva cuando sale: es la versión buena de lo que
estaba probando. Un sufijo que la app no reconozca se trata como lo más inestable, para que no
se cuele en el canal tranquilo.

**Las `dev` no entran en ningún canal**: no se publican en GitHub, salen por Telegram.

**Esto no es un candado.** Los APK están en un repositorio público y cualquiera puede descargar
el que quiera a mano. El canal decide qué te *ofrece* la app, que es el problema real: que nadie
acabe en una alpha sin haberlo pedido. Para restringir de verdad quién puede instalar qué haría
falta que las descargas pasaran por una identidad, y hoy no es así.

---

## Cómo pedírmelo

Basta con el peldaño; yo pongo el comando.

| Lo que dices | Lo que ejecuto |
|---|---|
| «sácame un APK de pruebas» | `./gradlew assembleDebug` → va solo a Telegram |
| «publica la alpha 2» | `publishReleaseToGitHub -PversionName=1.0.0-alpha.2` |
| «pasamos a beta» | `-PversionName=1.0.0-beta.1` |
| «publica la rc» | `-PversionName=1.0.0-rc.1` |
| «saca la 1.0.0» | `-PversionName=1.0.0` — sin sufijo, no es preestreno |
| «publica un parche» | `-PversionName=1.0.1` |

**Lo que necesito saber si no está claro por el contexto:**

- Sobre qué base va: ¿seguimos en la `1.0.0` o esto ya es `1.1.0`?
- Si algo de lo que va dentro merece decirse de otra forma en las notas.
- Si es la definitiva y hay que avisar de algo (una migración de datos, por ejemplo).

**Lo que hago siempre sin que me lo digas:**

1. Paso lo de `[Sin publicar]` del CHANGELOG a la sección de esa versión, con su fecha, y lo
   reescribo en términos de qué se nota en la app.
2. Tests y `verifyDesignTokens` antes de publicar.
3. Compruebo que la firma de release es real y que hay `GITHUB_TOKEN`.
4. Publico, y después **verifico contra la API pública** —sin token, como lo vería un usuario—
   que la publicación aparece, que trae su `.apk` y que el actualizador la encontraría.
5. Te digo el `versionCode` que salió y sobre qué se puede instalar.

---

## Antes de publicar una definitiva

Lo que conviene mirar y no está automatizado:

- ¿La versión anterior lleva unos días publicada sin quejas?
- ¿El enlace de descarga de la landing apunta a `UniStack-releases`? (Hoy **no**: sigue en el
  repo privado, y desde ahí da 404 a quien no ha iniciado sesión.)
- ¿Hay migración de base de datos? Si la hay, pruébala instalando encima de la versión anterior,
  no sobre una instalación limpia.
