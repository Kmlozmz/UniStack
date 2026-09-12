# KNOWN_ISSUES — problemas conocidos

Prioridad: **Alta** (bloquea publicar o pierde datos) · **Media** (se nota) · **Baja** (deuda).

| # | Problema | Impacto | Causa conocida | Workaround | Solución pendiente | Prioridad |
|---|---|---|---|---|---|---|
| 1 | La landing enlaza al repo privado (`github.com/Kmlozmz/UniStack/releases`) | Nadie sin sesión puede descargar desde la web | Se creó el repo público de releases el 13 ago y no se cambió el enlace | Compartir el enlace de `UniStack-releases` a mano | Cambiar el enlace (él) | Alta |
| 2 | Sin reporte de errores en la app | Los fallos llegan por Telegram si alguien escribe | Nunca se ha construido | Grupo `@unistacksoporte` | En «definir/planear» | Alta (para público) |
| 3 | No hay `CHANGELOG.md` de lo acumulado desde 1.5.10 | No se puede publicar 1.6.0 hasta escribirlo | 95 alphas sin notas | — | Redactarlo antes de la beta | Media |
| 4 | Conectar con Google / copia en la nube no funciona | El botón está al 45 % con «Pronto» | Falta `google-services.json` | Respaldo local JSON | Crear el proyecto de Firebase (él) | Media |
| 5 | `Coral` sirve para Gastos y para error/vencido | No se puede afinar uno sin mover el otro | Token compartido histórico | — | Separar tokens (`SectionColors.expenses` ya existe; migrar usos) | Baja |
| 6 | `heroLabel()` en el hero de Inicio deduce el rótulo por palabras clave | Cambiar una redacción puede cambiar el rótulo | Patrón antiguo | No editar esas frases sin probar | Sacar el rótulo de `HomePriorityAction` del todo | Baja |
| 7 | Rutas `grades`/`tasks` sólo existen como redirección | Código muerto que no se puede borrar | Alarmas antiguas guardan la ruta literal | — | Migrar alarmas, luego borrar | Baja |
| 8 | Notas rápidas y Configuración académica/histórico «no coinciden con M3E» | UI funcional pero él la considera virgen | Se construyeron con tarjetas y rótulos propios | — | Rediseño M3E (en su lista) | Media |
| 9 | Respaldo de notas sin los bytes de los adjuntos | En otro teléfono la nota dice «No está en este teléfono» | Tope de 1 MB en Firestore | Los archivos siguen en el teléfono original | Zip aparte (pendiente de que él lo confirme) | Baja |
| 10 | Un respaldo restaurado con dos `class_sessions` para la misma materia perdería una al editar | Improbable (ninguna ruta crea dos) | `saveSubjectSchedule()` borra sobrantes | — | Validar al restaurar | Baja |
| 11 | Promedio general = media simple de materias | Una materia con 10 % evaluado pesa igual que una terminada | Sin créditos por materia | — | Decidir ponderación | Baja |
| 12 | `check` falla la primera vez (`lintAnalyzeDebugUnitTest`) | Ruido | Entorno | Repetir | — | Baja |
| 13 | La app en inglés no ha sido probada a mano | Puede haber textos que no cuadren en contexto | i18n cerrado el 11 sep | — | Que él la recorra | Media |
| 14 | `Configuración` aparece en el cajón y dentro de Perfil; `Sincronización` solapa con «Datos y respaldos» | Duplicidad de entradas | Crecimiento | — | Auditoría de navegación | Baja |
| 15 | Módulos desactivables no cuadran con las pestañas (Horario no es módulo pero sí pestaña; Trabajos módulo sin pestaña) | Confuso | Diseño antiguo | — | Quitar módulos («no tocar hasta que lo pida») | Baja |
| 16 | Trabajos, UniStack AI y Labs son pantallas «Pronto» fuera de dev/alpha/beta | Prometen algo sin fecha | Por diseño (BuildStage) | — | Definir/planear | Baja |
| 17 | Tests instrumentados (3) sin ejecutar habitualmente | Pueden estar rotos | Sin emulador en el flujo | — | `VERIFY` | Baja |
| 18 | `TasksScreen.kt` 2.223 líneas y `AddTaskScreen.kt` 1.759 | Difícil de mantener | Crecimiento | — | Se parte en el rediseño D | Media |

## Trampas técnicas (no son bugs, pero muerden)

- `UniCard` superpone varios hijos directos → siempre `Column`.
- `GradesRepository.updateSubject()` no toca notas → `clearGrades`.
- El profesor vive en `ClassSession.location` (`"aula•profesor"`), no en `Subject`.
- Debug y release comparten firma; un `dev` (versionCode 1) no entra sobre una alpha.
- `Textos` debe instalarse antes de `super.onCreate()`; en tests, `TextosDePrueba`.
- Una `IOException` de red desde una corrutina del `Application` cierra la app.
- Scripts con `cambiar()` no son idempotentes; heredocs pierden `\`.
- `stringResource` en lambdas no composables no compila.
- `animateFloatAsState` nacido en el valor destino no anima; `scale` dentro de `clip` no se ve;
  tamaños de partículas en px vs dp.
- El artifact publicado se cachea en el navegador local: cache-busting (`?v=`) al revisar.
