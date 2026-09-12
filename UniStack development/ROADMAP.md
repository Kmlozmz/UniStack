# ROADMAP — de dónde venimos y qué sigue

La lista de pendientes (**TDL**) la escribe el desarrollador; este archivo la refleja, no la
sustituye. Fechas reales del `git log` y de la memoria. Una idea no es una funcionalidad.

## Completado (hitos, por fecha)

| Cuándo | Qué |
|---|---|
| 1 may 2026 | Primer commit. |
| ~jun–jul | Base de la app: materias, notas, tareas, horario, gastos, onboarding, ajustes, recordatorios (`VERIFY` fechas exactas: historial en español, 94 commits antes de `ce86f91`). |
| 8 ago | Rama `UI-redesign` fusionada; commits pasan a inglés/Conventional. |
| 12–13 ago | Decisiones de datos y distribución: escala borra notas, repo público de releases, escalera de versiones, `1.0.0-alpha.1` publicada. |
| 14–19 ago | Alphas/betas 1.1.0 y 1.2.0; canales eliminados; tanda de movimiento (queda el empuje iOS); barra flotante fuera. |
| 20–21 ago | `versionCode` de tres dígitos; bot recibe todo release; 1.3.x/1.4.4 publicadas. |
| 22–26 ago | Migración a componentes M3 Expressive; **1.5.10 publicada (26 ago)** — última publicación. |
| 27–28 ago | Universidad only; **histórico por semestre** completo (periodos, cortes con fecha, cierre comprobado, materias repetidas); asistencias rediseñadas v1 y sus tres flecos; Notas rápidas tandas 1–4. |
| 29 ago | Notas → Keep (dos vueltas). |
| 2 sep | Pro borrado; Notas y Configuración académica editable cerradas; anotaciones en materias. |
| 3 sep | Gastos cerrado; Apariencia (seis puertas, 28 temas) y Accesibilidad (17 ajustes) implementadas; Movimiento 25 gestos. |
| 10–11 sep | Historial de asistencias v2 (ruedas), celebración y sello, hero de Inicio con clase en curso, Movimiento → 12 → **7 gestos**, aviso post-clase abre asistencias, **i18n real cerrado** (`1.6.0-alpha.95`). |
| 12 sep | Artifact de Tareas (v3) con propuesta **D elegida**; esta carpeta de documentación. |

## Actualmente trabajando

- **Tareas — implementación de D** (hoja + «abrir entera», lista por días con hero y anillo,
  riel y tira, subtareas con tabla nueva, hecha≠entregada, posponer, nota en hoja, buscador tras
  la lupa, «Tareas de esta materia» en el detalle de materia). Ver `CURRENT_STATE.md`.

## Siguiente (en su orden)

1. Que él pruebe la app **en inglés** y las alphas de Tareas.
2. **Definir/planear** (artifact primero, cada uno): recursos → UniStack AI → trabajos (fundir
   en Tareas) → labs → **reporte de errores**.

## Corto plazo (acordado, sin fecha)

- Rediseñar **Apariencia con más opciones** (carrusel M3E, aparcado a propósito).
- **M3E de Notas rápidas** y de **Configuración académica / histórico** («no coincide mucho con M3E»).
- Migrar alarmas antiguas y borrar rutas `grades`/`tasks`; auditoría completa de navegación.
- Separar el token de Gastos del de error.
- Confirmar con él: tachado en notas, respaldo con bytes (zip).

## Medio plazo

- **Publicar 1.6.0** (beta → rc → estable) cuando la TDL actual cierre y las alphas hayan
  aguantado tres días de uso (regla de `PUBLICAR.md`). Escribir `CHANGELOG.md` de todo lo
  acumulado desde 1.5.10 (hoy no hay sección «Sin publicar»: `PENDING`).
- **Landing**: cambiar el enlace de descarga al repo público (bloquea la 1.0.0 «pública»).
- **Términos y privacidad** (con la landing) — aplazado por él.
- **CI/CD GitHub Actions** al preparar v1.0.0 (decidido): tag `v*` → build → release → bot;
  secretos en GitHub; keystore por decidir.

## Largo plazo

- **Conectar con Google**: identidad + respaldo en la nube, cuenta opcional (código listo,
  falta el proyecto de Firebase). Botón al 45 % con «Pronto».
- **Quitar el sistema de módulos** y dejar «ocultar» sin preguntar en el onboarding — «no
  tocar hasta que él lo pida».
- Créditos por materia / promedio ponderado — `TO DEFINE`.

## Ideas (sin decisión)

- Adjuntar media a tareas; cronómetro y «ponerla en el horario» (quedaron fuera de D);
  línea de vida y relacionado en la pantalla entera de tarea (D los deja para «abrir entera»:
  `VERIFY` si entran ahora).
- Rediseñar el flujo de «Añadir materia».
- Pro/planes «quizá en el futuro, debidamente planeado».

## Descartado (no volver a proponer)

Ver `PRODUCT.md` § Descartado y `FAILED_APPROACHES.md`: paywall, canales, alphas públicas,
cierre de descargas, convertir notas, casilla→tarea, subrayado, tipos de nota, barra flotante,
rebote, encabezados que encogen, gesto predictivo, logo por código, franjas de color, variantes
de Movimiento quitadas.
