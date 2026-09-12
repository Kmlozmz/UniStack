# UniStack development — manual de continuidad

Esta carpeta es el **traspaso completo de UniStack** para un agente de IA (o una persona) que
llegue sin la memoria de las conversaciones anteriores. No documenta el código línea a línea
—para eso está el código, que lleva KDoc con el *porqué* de casi todo—; documenta lo que el
código no puede decir: qué es el producto, cómo se decide, qué está cerrado, qué se probó y se
tiró, y cómo trabaja el desarrollador con su agente.

Escrita el **12 sep 2026**, sobre el estado `1.6.0-alpha.95` de la rama `TDL` (commit `f06d392`).

## Cómo leerla

Marcas que aparecen en todos los archivos:

| Marca | Significa |
|---|---|
| *(sin marca)* | hecho verificado en el código o en el historial de git |
| **DECISIÓN** | lo decidió el desarrollador; no se reabre sin que lo pida |
| **PREFERENCIA** | lo ha dicho o mostrado repetidas veces; guía, no ley |
| **INFERENCIA** | lo deduce el agente del patrón de trabajo; puede estar mal |
| **PROPUESTA** | algo diseñado (normalmente en un artifact) y todavía no construido |
| `PENDING` | trabajo acordado y no hecho |
| `VERIFY` | se recuerda pero no se ha podido comprobar; confirmar antes de apoyarse en ello |
| `UNKNOWN` / `TO DEFINE` | nadie lo ha definido todavía |

## Orden de lectura recomendado para un agente nuevo

1. [`AI_CONTEXT.md`](AI_CONTEXT.md) — diez minutos: qué es, cómo se trabaja, qué reglas mandan.
2. [`CURRENT_STATE.md`](CURRENT_STATE.md) — dónde está el proyecto hoy y qué sigue.
3. [`AI_WORKFLOW.md`](AI_WORKFLOW.md) — cómo trabajar **con este desarrollador**.
4. [`DESIGN_SYSTEM.md`](DESIGN_SYSTEM.md) — cómo tiene que verse y sentirse; qué rechaza.
5. [`DESIGN_DECISIONS.md`](DESIGN_DECISIONS.md) y [`FAILED_APPROACHES.md`](FAILED_APPROACHES.md) — lo que no hay que volver a proponer.
6. [`BUILD.md`](BUILD.md) y [`DEVELOPMENT_WORKFLOW.md`](DEVELOPMENT_WORKFLOW.md) — antes de tocar código.
7. El resto, según la tarea.

## Jerarquía de autoridad

Cuando dos fuentes se contradigan, manda la de arriba:

1. **Lo que el desarrollador diga en la conversación actual.**
2. **El código y el `git log`** del repositorio (`Kmlozmz/UniStack`, rama `TDL`).
3. `CURRENT_STATE.md` y `DESIGN_DECISIONS.md` (esta carpeta), por ser los más recientes.
4. El resto de esta carpeta.
5. Los documentos del repo anteriores a esta carpeta: `CONTEXTO.md` (19 ago 2026),
   `COMPILAR.md`, `PUBLICAR.md` (13 ago 2026). Siguen siendo válidos en lo que no contradiga a
   lo anterior; las contradicciones conocidas están anotadas en `DESIGN_DECISIONS.md` § Conflictos.
6. `design-system/unistack/MASTER.md` del repo: **obsoleto** (generado el 6 ago 2026 con una paleta
   teal que la app nunca usó). No apoyarse en él.

## Archivos

| Archivo | Qué contiene |
|---|---|
| [`AI_CONTEXT.md`](AI_CONTEXT.md) | Resumen denso para un agente: producto, stack, reglas, forma de trabajar |
| [`CURRENT_STATE.md`](CURRENT_STATE.md) | Fotografía al 12 sep 2026: qué funciona, qué se está haciendo, qué sigue |
| [`PROJECT.md`](PROJECT.md) | Qué es UniStack, para quién, qué no es, conceptos y terminología |
| [`PRODUCT.md`](PRODUCT.md) | Funcionalidades por estado (existente / en desarrollo / planeado / idea / descartado), flujos |
| [`ARCHITECTURE.md`](ARCHITECTURE.md) | Capas, paquetes, navegación, estado, datos, notificaciones, actualizaciones |
| [`TECH_STACK.md`](TECH_STACK.md) | Cada tecnología: qué es, dónde, por qué, restricciones |
| [`DESIGN_SYSTEM.md`](DESIGN_SYSTEM.md) | Lenguaje visual real: M3 Expressive, tokens, componentes propios, lo que gusta y lo que no |
| [`DESIGN_DECISIONS.md`](DESIGN_DECISIONS.md) | Registro de decisiones cerradas, con contexto y alternativas; conflictos entre documentos |
| [`ENGINEERING_PRINCIPLES.md`](ENGINEERING_PRINCIPLES.md) | Filosofía de desarrollo: reglas explícitas y preferencias inferidas |
| [`CODING_RULES.md`](CODING_RULES.md) | Convenciones concretas de Kotlin/Compose en este repo |
| [`AI_WORKFLOW.md`](AI_WORKFLOW.md) | Cómo trabaja el desarrollador con un agente: ciclo, reportes, qué preguntar |
| [`DECISION_MAKING.md`](DECISION_MAKING.md) | Cómo se evalúan opciones, con ejemplos reales |
| [`DEVELOPMENT_WORKFLOW.md`](DEVELOPMENT_WORKFLOW.md) | De la tarea al APK: ramas, commits, alphas, publicación |
| [`BUILD.md`](BUILD.md) | Requisitos, comandos, errores conocidos y su solución |
| [`TESTING.md`](TESTING.md) | Qué se prueba, cómo, y qué significa «terminado» |
| [`DATABASE.md`](DATABASE.md) | Room: tablas, migraciones (v21), invariantes, reglas de negocio |
| [`API.md`](API.md) | Las únicas APIs externas: GitHub Releases y Firebase (no conectado) |
| [`ROADMAP.md`](ROADMAP.md) | Completado, en curso, siguiente, aplazado, ideas, descartado |
| [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md) | Problemas conocidos con impacto, causa y estado |
| [`FAILED_APPROACHES.md`](FAILED_APPROACHES.md) | Lo que se intentó y no funcionó, y qué se aprendió |
| [`PREFERENCES.md`](PREFERENCES.md) | Preferencias del desarrollador relevantes para trabajar aquí |
| [`GLOSSARY.md`](GLOSSARY.md) | Términos propios (corte, peldaño, gesto, hero, banco de pruebas…) |
| [`UNISTACK_PITCH_KIT.md`](UNISTACK_PITCH_KIT.md) | Cómo explicar UniStack según el interlocutor (no es documentación técnica) |

## Documentos del repositorio que complementan esta carpeta

- `CHANGELOG.md` — lo que nota el usuario en cada versión publicada. Publicar falla si falta la sección.
- `docs/changelog-historico.md` — versiones anteriores a la 1.4.4.
- `COMPILAR.md` — recetas de compilación paso a paso (se resumen en `BUILD.md`).
- `PUBLICAR.md` — la escalera de versiones y cuándo sube cada peldaño.
- `CONTEXTO.md` — traspaso del 19 ago 2026; parcialmente superado (ver conflictos).
