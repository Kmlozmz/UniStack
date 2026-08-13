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
| `0.0.0-sinpublicar.<yyMMddHH>` | `1` | Compilación de trabajo. No es una versión ni se distribuye. |
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

**Hay tres peldaños y nada más: `alpha`, `beta` y la definitiva.** Lo que se compila sin
`-PversionName` no es un peldaño: es un binario de trabajo, no se distribuye y no llega a nadie.

**`alpha`** — para quien tenga el código de alpha. Se espera que algo se rompa. Es también **la
única versión que sale por el bot de Telegram**: beta y definitiva llegan por la app, a quien
corresponda.

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
| Compilación de trabajo → cualquier publicada | ✅ | 1 → 1.000.011 o más |
| `alpha.1` → `alpha.2` → `beta.1` → `rc.1` → `1.0.0` | ✅ | Sube en cada paso |
| `1.0.0` → `1.0.1` → `1.1.0` | ✅ | Sube |
| Publicada → compilación de trabajo | ❌ | 1.000.099 → 1, es bajar |
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

Los tres canales son **públicos distintos, no escalones**: alpha y beta se prueban con gente
distinta y por motivos distintos, así que cada uno tiene su propio código y el de alpha **no**
abre beta.

| Canal | Recibe | Cómo se entra |
|---|---|---|
| **Estable** (por defecto) | Solo `1.0.0`, `1.0.1`, `1.1.0`… | Sin código |
| **Beta** | `-beta.N`, `-rc.N` y las definitivas | Código de beta |
| **Alpha** | `-alpha.N` y las definitivas | Código de alpha |

**La definitiva llega a todos los canales.** Es la salida de cualquier preestreno: sin eso, quien
prueba una alpha se queda anclado en ella para siempre. Un sufijo que la app no reconozca cuenta
como alpha, el círculo más pequeño, para no colarlo donde hay más gente.

Se pueden tener varios códigos a la vez: cada uno suma su canal y se revoca por separado.

**Cuándo se entera la gente.** Un trabajo en segundo plano mira cada 2 horas con la app cerrada,
y al abrirla se consulta si han pasado 45 minutos. **No es instantáneo y se decidió que basta**
(13 ago 2026): avisar en el momento de publicar exige una notificación push, y eso exige Firebase
configurado.

**Beta y Alpha piden un código de acceso.** Aparecen con un candado y, al tocarlos, la app pide
el código. Al canjearlo se guarda su huella, no el código.

**La lista la gestionas tú**, en `canales.json` del repo de publicaciones:

```json
{ "canal": "ALPHA", "huella": "<sha256 del codigo>", "nota": "para quien sea" }
```

- **Dar acceso:** genera un código, añade su huella con el canal que abre. Pídemelo y lo hago.
- **Revocar:** quita esa línea. La app contrasta su huella en cada comprobación de
  actualizaciones, así que al siguiente intento vuelve a Estable sola.
- Un código de `ALPHA` abre también beta: quien acepta lo más inestable no necesita otro permiso
  para lo que está más rodado.
- Si la lista no se puede consultar **no se revoca nada**: quedarse sin cobertura no es lo mismo
  que perder el permiso.

Solo se publican huellas, nunca códigos, así que la lista no sirve para colarse. Lo que sostiene
el sistema es que **el código sea largo**: contra uno corto, la huella se rompe probando. Los que
genero son de 20 caracteres sin letras que se confundan al dictarlas.

**Un acceso que no se puede confirmar caduca a la semana.** Sin ese plazo, quedarse sin conexión
conservaba el canal para siempre y bloquearle el paso a la lista era suficiente para no perderlo
nunca. Un corte normal no revoca nada.

**Aun así no es un candado sobre la instalación, y se decidió que está bien** (13 ago 2026). Los
APK están en un repositorio público y cualquiera con el enlace descarga el que quiera a mano. Lo
que esto controla es **qué ofrece la app**: que nadie acabe en una alpha sin saber dónde se ha
metido. Cerrar la descarga en sí exigiría Firebase App Distribution o un servicio propio que
entregue el archivo contra el código, y se prefirió no montar esa infraestructura. Tampoco hay
defensa contra quien modifique el APK para saltarse la comprobación: eso no se puede resolver
desde dentro de la app.

**Cambiar a un canal de preestreno avisa antes** de lo que implica: que puede fallar y perder
datos, que conviene copia de seguridad, y que no se vuelve atrás sin desinstalar. Bajar a
Estable no pregunta nada.

---

## Cómo pedírmelo

Basta con el peldaño; yo pongo el comando.

| Lo que dices | Lo que ejecuto |
|---|---|
| «publica la alpha 2» | `publishReleaseToGitHub -PversionName=1.0.0-alpha.2` → publica **y** te la manda por Telegram |
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
