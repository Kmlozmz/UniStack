# UniStack Product Plan

## Estado actual

Fecha de reevaluación: 2026-05-15.

UniStack ya dejó de ser solo un MVP visual. La app tiene onboarding, perfil, preferencias, módulos activables, materias, notas, simulador, tareas, gastos, plantillas académicas, Room, DataStore, flujo de Pro con BillingClient, auth/backup preparados, QA base verde en dispositivo físico y QA extendido instrumentado. El bloqueo de instalación ADB ya fue superado; el último cierre extendido quedó pendiente porque el teléfono se desconectó antes de repetir el ajuste final de navegación.

La siguiente etapa no debe ser "agregar pantallas". Debe convertir UniStack en una beta útil, coherente y terminada: todo control visible debe hacer algo real, todo dato importante debe persistir, y cada módulo debe ayudar al estudiante a tomar una decisión o completar una acción.

## Norte de producto

UniStack debe sentirse como un centro de mando académico ligero:

- Saber cómo va el semestre.
- Entender qué necesita atención hoy.
- Guardar tareas, notas, trabajos y gastos sin fricción.
- Dar claridad, no ruido.
- Funcionar localmente aunque no haya cuenta, red ni compra Pro (por ahora).
- Escalar hacia backup, exportación y monetización sin romper la experiencia base.

## Principios vigentes

- Local-first: la app debe ser útil sin cuenta ni conexión.
- Cero callejones muertos: ningún botón, toggle, card o CTA debe prometer algo que no responda.
- Persistencia antes que brillo: si el usuario crea algo importante, debe sobrevivir cierre, reinicio y recreación.
- Diseño compacto, premium y académico: menos hero marketing, más información escaneable y acciones claras.
- Pro no debe bloquear el valor básico; debe ampliar límites, automatización, exportación o conveniencia.
- Los datos de demo no viven en código productivo; cualquier muestra debe estar aislada fuera de `main`.
- Cada feature nueva trae su fallback: error claro, estado vacío útil, permisos explicados y prueba mínima.

## Prioridades

### P0 - Bloqueante

Crash, pérdida de datos, navegación rota, guardado imposible, migración dañina, login/compra que deja la app bloqueada.

### P1 - Producto esencial

Flujos principales incompletos, cálculos dudosos, datos no persistidos, CTA visible sin efecto, estados que confunden al usuario.

### P2 - Calidad beta

Accesibilidad, consistencia visual, rendimiento percibido, copy, microinteracciones, pruebas, arquitectura y mantenimiento.

### P3 - Expansión

Ideas potentes para diferenciar la app, pero que no son necesarias para una beta privada estable.

## Matriz de completitud

| Área                | Estado                     | Siguiente mejora útil                                                                                   |
| ------------------- | -------------------------- | ------------------------------------------------------------------------------------------------------- |
| Onboarding          | Funcional y probado        | Permitir editar todos los datos iniciales desde Perfil sin reiniciar setup                              |
| Home                | Funcional con prioridades y semana | Profundizar insights: recomendación de siguiente acción y tendencias |
| Materias y notas    | Funcional                  | Mejorar insights por materia, historial y simulación desde detalle                                      |
| Tareas              | Funcional con filtros y recordatorios locales | Recurrencia simple y agrupación por materia/semana                                      |
| Gastos              | Funcional                  | Presupuesto semanal/mensual, categorías configurables y alertas suaves                                  |
| Trabajos académicos | Funcional con Room         | Mejorar fuentes, APA y posible vista kanban por estado                                                  |
| Perfil              | Funcional con preferencias de notificación | Dividir secciones, editar configuración académica completa y mejorar estados de cuenta/backup |
| Pro                 | Integrado técnicamente     | Entitlement persistente, restaurar compras, límites Free/Pro y copy legal claro                         |
| Backup/sync         | Preparado                  | Definir flujo real: local, cuenta vinculada, backup manual, restore y conflictos                        |
| Exportación         | Funcional localmente       | Endurecer UX de restore, estados de PDF y compartir archivos fuera del portapapeles                     |
| QA                  | Local verde; ADB base verde; ADB extendido pendiente por desconexión del dispositivo | Reconectar teléfono y repetir `connectedDebugAndroidTest` para cerrar el pase extendido |

## Roadmap activo

### Fase 0 - Estabilidad base

Objetivo: cerrar la base antes de crecer.

- [x] Matriz QA manual completa.
- [x] Pruebas unitarias de cálculos, validadores y fechas.
- [x] Flujo instrumentado de dispositivo para onboarding, materias, notas, tareas, gastos, perfil, temas, módulos, responsive y reset.
- [x] `testDebugUnitTest`, `assembleDebug`, `assembleDebugAndroidTest`, `lintDebug` y `connectedDebugAndroidTest` verdes.
- [x] Push de cierre QA en `main`.

### Fase 1 - Base técnica para crecer (cerrada localmente)

Objetivo: evitar que nuevas funciones se monten sobre deuda frágil.

- [x] Agregar tests de repositorios con Room en memoria para materias/notas, tareas y gastos.
- [x] Activar exportación de schemas de Room.
- [x] Agregar pruebas de migración Room 1->2, 2->3 y 3->4.
- [x] Separar `AppContainer` en proveedores más testeables o factories explícitas.
- [x] Revisar creación de ViewModels y dependencias para reducir singletons en UI.
- [x] Crear modelos de UI por pantalla cuando los composables estén calculando demasiado.
- [x] Unificar estados vacíos, errores, confirmaciones y loading en componentes comunes.
- [x] Definir una guía corta de navegación: cuándo usar tab, detalle, formulario, diálogo y back.

### Fase 2 - Rediseño práctico de experiencia (cerrada localmente)

Objetivo: que la app se sienta menos como pantallas separadas y más como un sistema diario.

- [x] Reestructurar `HomeScreen` como tablero de "qué hago ahora".
- [x] Agregar una sección de prioridades: materia en riesgo, próxima tarea, gasto semanal y avance de trabajos.
- [x] Hacer que cada card importante tenga una acción directa y una acción secundaria clara.
- [x] Revisar consistencia entre formularios: títulos, guardar, cancelar, errores, fecha y selección de materia.
- [x] Añadir filtros compactos en Tareas: pendientes, vencidas, completadas, por materia.
- [x] Añadir filtros compactos en Gastos: semana, mes, categoría.
- [x] Revisar navegación entre materias, notas y simulador para que el simulador se sienta parte del detalle.
- [x] Dividir responsabilidades de pantallas grandes y extraer piezas comunes de UI.
- [x] Mejorar tablet básica con layouts de dos columnas donde aporte valor real.
- [x] Crear checklist visual de accesibilidad: touch target, contraste, labels, dynamic font y estados seleccionados.

### Fase 3 - Trabajos académicos v2 (cerrada localmente)

Objetivo: convertir "Plantillas" en una función real de planificación y entrega.

- [x] Crear entidad Room para trabajos académicos.
- [x] Crear, editar, eliminar y completar trabajos.
- [x] Asociar trabajos a materias.
- [x] Agregar fecha de entrega, prioridad, estado y porcentaje de avance.
- [x] Persistir checklist por trabajo.
- [x] Permitir duplicar una plantilla como trabajo nuevo.
- [x] Añadir secciones editables: título, tesis/objetivo, esquema, fuentes y notas.
- [x] Mejorar APA como generador estructurado básico, no solo tips.
- [x] Mostrar trabajos próximos en Home y en detalle de materia.
- [x] Agregar exportación de un trabajo a texto compartible.

### Fase 4 - Notificaciones y agenda local (cerrada localmente)

Objetivo: que UniStack ayude antes de que algo se venza.

- [x] Definir preferencias de notificación por módulo.
- [x] Agregar recordatorios locales para tareas.
- [x] Agregar recordatorios locales para trabajos.
- [x] Añadir avisos de tareas vencidas y entregas próximas.
- [x] Evitar duplicados al editar, completar o eliminar.
- [x] Respetar toggles de módulos y permisos del sistema.
- [x] Crear vista "Esta semana" con tareas, trabajos y evaluaciones.
- [x] Mantener calendario mensual simple como P3 hasta validar que la vista semanal cubre el uso diario.

### Fase 5 - Insights académicos y financieros (cerrada localmente)

Objetivo: pasar de registrar datos a interpretar la situación.

- [x] Mostrar riesgo por materia: estable, atención, crítico.
- [x] Explicar por qué una materia está en riesgo: promedio, porcentaje evaluado, nota necesaria.
- [x] Mejorar simulador con escenarios guardables por materia.
- [x] Sugerir próxima acción académica: estudiar, entregar, registrar nota o crear tarea.
- [x] Crear presupuesto semanal y mensual.
- [x] Alertar cuando el gasto semanal supere un umbral definido.
- [x] Mostrar tendencia simple de gastos por categoría.
- [x] Agregar categorías de gastos configurables.
- [x] Mostrar resumen de productividad: tareas completadas, vencidas y próximas.

### Fase 6 - Exportación y backup local (cerrada localmente)

Objetivo: que el usuario pueda llevarse sus datos y recuperarlos.

- [x] Crear backup local JSON versionado.
- [x] Importar/restaurar backup con validación previa.
- [x] Mostrar resumen antes de restaurar: materias, notas, tareas, gastos, trabajos.
- [x] Evitar restauraciones que dupliquen datos sin confirmación.
- [x] Exportar reporte de notas por materia.
- [x] Exportar resumen académico en PDF.
- [x] Exportar tareas y gastos en CSV o JSON estructurado.
- [x] Crear prueba de importación con datos corruptos o incompletos.

### Fase 7 - Cuenta y sincronización real

Estado recomendado: no abrir implementación grande hasta repetir el pase ADB extendido. Si el dispositivo no está disponible, avanzar solo en preparación documental o tickets pequeños sin tocar flujos críticos.

Objetivo: convertir la preparación técnica en una experiencia confiable.

- [ ] Configurar OAuth real para Google Sign-In.
- [ ] Definir estrategia de migración de datos locales a cuenta vinculada.
- [ ] Implementar backup manual a nube con estado visible.
- [ ] Implementar restore desde nube con vista previa.
- [ ] Diseñar estados: local, listo para backup, pendiente, sincronizado, error.
- [ ] Resolver conflictos básicos: conservar local, conservar nube o fusionar.
- [ ] No bloquear uso local si el usuario no inicia sesión.
- [ ] Escribir QA específico para login, logout, backup, restore y error de red.

### Fase 8 - Pro y monetización real

Objetivo: que Pro sea funcional, honesto y aprobable por Play Store.

- [ ] Definir productos e IDs reales.
- [x] Integrar BillingClient.
- [ ] Persistir entitlement Pro de forma segura.
- [ ] Implementar restaurar compras.
- [ ] Definir límites Free vs Pro.
- [ ] Revisar límites actuales: materias, plantillas avanzadas, exportación, backup, insights o widgets.
- [ ] Asegurar que perder Pro degrada con cuidado y no borra datos.
- [ ] Mejorar pantalla Pro con beneficios verificables, estado de compra y errores claros.
- [ ] Revisar políticas de Play Store antes de beta pública.

### Fase 9 - Beta privada y release

Objetivo: preparar una versión instalable para usuarios reales.

- [ ] Definir versioning interno.
- [ ] Crear build release firmada.
- [ ] Documentar permisos, privacidad y manejo de datos.
- [ ] Agregar crash reporting o alternativa mínima de diagnóstico.
- [ ] Preparar descripción corta, screenshots y texto de Play Store.
- [ ] Probar instalación limpia y actualización sobre versión previa.
- [ ] Probar matriz mínima de dispositivos/emuladores.
- [ ] Mantener cero P0/P1 conocidos antes de publicar beta.

## Ideas de diseño

- Home como tablero: arriba una frase de estado real del semestre, luego 3 o 4 bloques accionables.
- Cards menos decorativas y más funcionales: cada card debe responder "qué es", "por qué importa" y "qué hago".
- Densidad adaptable: móvil compacto por defecto, tablet con panel maestro/detalle en Notas y Trabajos.
- Formularios con patrón único: encabezado, campos, ayuda contextual, preview del resultado y acción fija.
- Materias con identidad visual consistente: color, icono/forma, progreso y estado académico.
- Estados vacíos con una sola acción principal, no textos largos.
- Confirmaciones destructivas consistentes: nombre del elemento, consecuencia y acción clara.
- Microinteracciones sobrias: selección, guardado, completar tarea, cambio de módulo y compra/restauración.
- Mejor uso de iconos en acciones repetidas: editar, eliminar, guardar, simular, filtrar, exportar.
- Perfil dividido en bloques: cuenta, apariencia, módulos, academia, datos/backup, Pro y soporte.
- Trabajos académicos como kanban ligero o lista por estado: idea, borrador, revisión, listo, entregado.
- Gastos con barras simples y leyendas escaneables; evitar gráficos vistosos si no ayudan a decidir.

## Ideas de funciones

- Vista "Hoy": tareas vencidas, próximas entregas, materia más crítica y gasto de la semana.
- Vista "Semana": agenda académica y financiera en una sola línea temporal.
- Escenarios de nota guardables: "si saco 4.2 en final", "mínimo para aprobar", "meta alta".
- Alertas suaves: "esta materia ya tiene 70% evaluado", "necesitas 3.8 en lo restante".
- Plantillas por tipo: ensayo, informe de laboratorio, reseña, exposición, proyecto final.
- Checklist por tipo de trabajo, editable por el usuario.
- Banco de fuentes por trabajo con APA básico.
- Presupuesto por categoría: comida, transporte, materiales, ocio, otros.
- Metas de gasto semanales y aviso cuando se supera un porcentaje.
- Archivo por semestre para separar materias antiguas sin borrarlas.
- Importar notas desde CSV como función avanzada.
- Widgets Android: próxima tarea, promedio general, gasto semanal.
- Accesos rápidos desde notificación para completar tarea o registrar gasto.
- Modo "semestre nuevo": duplicar configuración, archivar materias y reiniciar tablero.
- Historial de cambios importante: nota editada, materia eliminada, restore aplicado.

## Backlog P3

- Vista calendario mensual.
- Widgets Android.
- Categorías de gastos configurables avanzadas.
- Insights de rendimiento académico por semestre.
- Archivo por semestre.
- Adjuntos en materias, tareas o trabajos.
- Importar notas desde CSV.
- Compartir plantillas académicas.
- Mejoras de accesibilidad avanzada.
- Sugerencia de carreras faltantes.
- Integración con archivos del dispositivo para PDFs y backups.
- Modo enfoque para estudiar una materia.
- Temporizador Pomodoro asociado a tareas.

## Decisiones técnicas vigentes

- Room se usa para datos estructurados locales.
- DataStore se usa para setup, perfil, preferencias y estado ligero del usuario.
- El runtime debe seguir sin datos mock.
- Google/Firebase y BillingClient son integraciones reales, pero siempre con fallback local o error claro.
- El plan Free mantiene valor suficiente; Pro amplía límites, automatización, backup/exportación o insights.
- El branding oficial no se toca salvo que la tarea sea explícitamente de marca.
- Las pantallas grandes deben dividirse por responsabilidad antes de recibir más features.

## Cadencia de trabajo

- Empezar cada bloque con una lectura corta del flujo afectado y su estado en este plan.
- Implementar primero la ruta feliz persistente.
- Añadir errores, estados vacíos y permisos antes de considerar la feature terminada.
- Agregar pruebas unitarias cuando haya lógica pura.
- Agregar pruebas Room o instrumentadas cuando haya persistencia, navegación o integración Android.
- Actualizar `QA_MANUAL.md` cuando aparezca un flujo nuevo que un usuario real pueda tocar.
- Cerrar cada bloque con verificación Gradle proporcional al cambio.

## No objetivos por ahora

- Chat social o comunidad.
- Backend obligatorio para usar funciones básicas.
- IA generativa como dependencia central.
- Rediseño completo de marca sin necesidad.
- Gamificación pesada que distraiga del uso diario.
- Calendario complejo antes de cerrar tareas/trabajos/notificaciones.

## Definición de beta lista

- No hay bugs P0/P1 conocidos.
- `./gradlew :app:assembleDebug` pasa.
- `./gradlew testDebugUnitTest` pasa.
- `./gradlew connectedDebugAndroidTest` pasa en al menos un dispositivo físico.
- Los flujos principales pasan QA manual actualizado.
- No hay datos mock en runtime.
- Room tiene schemas exportados y migraciones probadas.
- La información persiste correctamente tras cerrar y abrir la app.
- La navegación no tiene loops, flicker ni pantallas muertas.
- No hay controles visibles que prometan una acción sin respuesta.
- Cuenta, backup, Pro o integraciones externas muestran estado real, fallback o error claro.

## Siguiente bloque recomendado

Fases 1, 2, 3, 4, 5 y 6 quedan cerradas a nivel local y el pase ADB base ya está verde. El bloqueo por instalación USB quedó resuelto, pero el dispositivo se desconectó antes de repetir el último ajuste de limpieza del test extendido. Antes de abrir Fase 7 conviene cerrar el QA extendido con este orden:

1. Reconectar el Xiaomi y verificar que `adb devices -l` muestre `device`.
2. Ejecutar `./gradlew connectedDebugAndroidTest` sin usar el teléfono durante la corrida.
3. Si pasa, marcar QA-45 a QA-70 como cerrados o dejar solo las salvedades manuales: rechazo de permiso de notificaciones, inspección de alarmas y archivo PDF.
4. Si falla, atender solo el punto reportado por el HTML de instrumentación y repetir.
5. Con el pase extendido verde, iniciar Fase 7 con cuenta y sincronización real.
