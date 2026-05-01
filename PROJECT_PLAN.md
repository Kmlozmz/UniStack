# UniStack Project Plan

## 1. Visión del producto
Explicar brevemente que UniStack es una app Android para estudiantes que permite organizar notas, materias, tareas, gastos y entregas académicas desde un solo lugar.

## 2. Estado actual del proyecto
Marcar como completado lo que ya existe:
- [x] Android project scaffold.
- [x] Kotlin + Jetpack Compose.
- [x] Material 3 / estilo visual UniStack.
- [x] HomeScreen visual.
- [x] Onboarding inicial.
- [x] Nombre preferido dinámico.
- [x] Setup académico.
- [x] DataStore para persistir UserProfile/setup.
- [x] Room para materias y notas.
- [x] Validaciones semánticas básicas.
- [x] Navegación principal.
- [x] Bottom navigation personalizada.
- [x] Eliminación de mock data en runtime.
- [x] Animaciones básicas.
- [x] Escala de notas dinámica.

## 3. Reglas de trabajo
Agregar reglas claras:
- No implementar varias features grandes al mismo tiempo.
- Máximo una feature grande activa.
- Los bugs P0 se arreglan antes de nuevas features.
- Los datos mock solo pueden existir en previews o DemoData.
- Cada cambio importante debe compilar con `./gradlew :app:assembleDebug`.
- Cada feature debe mantener la estética visual de UniStack.
- No implementar backend, Firebase, Billing o Google Sign-In hasta que el MVP local esté sólido.
- Si aparece una idea nueva, va primero a Backlog y no interrumpe la fase actual salvo que sea crítica.

## 4. Sistema de prioridades
Crear clasificación:
- **P0 Bloqueante**: rompe la app o impide usar una función principal.
- **P1 Importante**: confunde al usuario o afecta una función principal.
- **P2 Pulido**: mejora visual, UX o comodidad.
- **P3 Futuro**: idea útil pero no necesaria para el MVP actual.

Incluir ejemplos:
**P0**:
- La app crashea.
- No se puede volver al Home.
- No se guardan datos.

**P1**:
- Mostrar 0.0 cuando no hay notas.
- Mostrar datos fake.
- Validaciones incorrectas.

**P2**:
- Ajustar espaciado.
- Mejorar animaciones.
- Pulir bottom nav.

**P3**:
- Google Sign-In.
- Billing Pro.
- Exportar PDF.
- Sincronización en nube.

## 5. Roadmap por fases

### Fase 0 — Base del proyecto
Debe estar marcada como completada:
- [x] Scaffold Android.
- [x] Kotlin + Compose.
- [x] Home visual.
- [x] Onboarding.
- [x] DataStore.
- [x] Room materias/notas.
- [x] Validaciones.
- [x] Animaciones básicas.

### Fase 1 — Módulo académico sólido
Objetivo: que UniStack sea realmente útil con materias y notas.
Pendientes:
- [x] Corregir estados pendientes: mostrar "--" o "Sin notas" en vez de 0.0.
- [ ] Arreglar bug de bottom nav: Inicio no navega desde Materias.
- [ ] Revisar espaciado visual tras edge-to-edge.
- [ ] Editar materia.
- [ ] Eliminar materia.
- [ ] Editar nota.
- [ ] Eliminar nota.
- [ ] Confirmaciones de eliminación.
- [ ] Mejorar detalle de materia.
- [ ] Simulador real de nota necesaria.
- [ ] Revisar edge cases de porcentajes.

### Fase 2 — UX y estabilidad
Objetivo: que la app se sienta estable, fluida y premium.
Pendientes:
- [ ] Revisar compactación visual.
- [ ] Pulir espaciados.
- [ ] Pulir animaciones.
- [ ] Revisar pantallas 360dp–430dp.
- [ ] Revisar estados vacíos.
- [ ] Revisar modo oscuro si aplica.
- [ ] Mejorar icono/logo.
- [ ] Revisar accesibilidad básica.

### Fase 3 — Tareas reales
Objetivo: tareas persistentes con Room.
Pendientes:
- [ ] Modelo StudentTask.
- [ ] TaskEntity.
- [ ] TaskDao.
- [ ] RoomTasksRepository.
- [ ] Crear tarea.
- [ ] Editar tarea.
- [ ] Eliminar tarea.
- [ ] Marcar tarea como completada.
- [ ] Mostrar tareas reales de hoy en Home.
- [ ] Mostrar próxima tarea real en Home.

### Fase 4 — Gastos reales
Objetivo: gastos persistentes con Room.
Pendientes:
- [ ] Modelo Expense.
- [ ] ExpenseEntity.
- [ ] ExpenseDao.
- [ ] RoomExpensesRepository.
- [ ] Registrar gasto.
- [ ] Editar gasto.
- [ ] Eliminar gasto.
- [ ] Resumen semanal real.
- [ ] Gráfico real por días.
- [ ] Resumen por categoría.

### Fase 5 — Perfil y configuración
Pendientes:
- [ ] ProfileScreen con datos reales.
- [ ] Editar nombre preferido.
- [ ] Editar escala de notas.
- [ ] Editar promedio objetivo.
- [ ] Reiniciar onboarding.
- [ ] Configuración de módulos.
- [ ] Preferencias visuales.

### Fase 6 — Monetización preparada
Pendientes:
- [ ] UserPlan.
- [ ] FeatureGate.
- [ ] Límite gratis de 5 materias.
- [ ] Pantalla Pro.
- [ ] Beneficios Pro.
- [ ] Placeholder de upgrade.
- [ ] No implementar Billing todavía.

### Fase 7 — Login y sincronización futura
Pendientes:
- [ ] Google Sign-In.
- [ ] Foto de perfil real.
- [ ] Vincular datos a usuario.
- [ ] Backup/sync futuro.

### Fase 8 — Plantillas académicas
Pendientes:
- [ ] Checklist de trabajos.
- [ ] Plantillas de ensayo.
- [ ] Formato APA básico.
- [ ] Exportar PDF/Word futuro.

## 6. Backlog actual
Dividir en:

### Bugs actuales
- Bottom nav: desde Materias, Inicio no navega.
- SubjectDetail muestra 0.0 cuando no hay notas.
- Revisar si el edge-to-edge compactó demasiado algunas pantallas.

### UX/UI
- Revisar espaciado general.
- Revisar tamaño y peso visual del bottom nav.
- Mejorar respiración visual en pantallas académicas.
- Pulir animaciones.
- Mejorar icono/logo.

### Features próximas
- Editar/eliminar materias.
- Editar/eliminar notas.
- Tareas reales con Room.
- Gastos reales con Room.
- Perfil funcional.

### Ideas futuras
- Google Sign-In.
- Pro/Billing.
- Exportar PDF.
- Plantillas académicas.
- Notificaciones.
- Widgets Android.
- Enviar carreras faltantes como sugerencia futura.

## 7. Flujo de trabajo recomendado
Explicar:

Para bugfix:
- crear rama `fix/nombre-del-bug`
- hacer cambio pequeño
- ejecutar `./gradlew :app:assembleDebug`
- commit
- PR

Para feature:
- crear rama `feat/nombre-feature`
- no mezclar con otros cambios
- ejecutar build
- PR

Para UX:
- crear rama `ux/nombre-ajuste`
- revisar visualmente en 360dp–430dp
- build
- PR

## 8. Convención de ramas
Agregar ejemplos:
- `fix/bottom-nav-home`
- `fix/pending-grade-states`
- `ux/spacing-polish`
- `feat/edit-delete-grades`
- `feat/tasks-room`
- `feat/expenses-room`
- `refactor/academic-state-models`

## 9. Convención de commits
Agregar ejemplos:
- `fix: restore home navigation from bottom bar`
- `fix: show pending state when subject has no grades`
- `ux: improve spacing across academic screens`
- `feat: add subject and grade editing`
- `feat: persist tasks with Room`
- `refactor: centralize grading scale logic`

## 10. Definición de MVP 1.0
El MVP 1.0 debe incluir:
- Onboarding persistente.
- Perfil local.
- Materias reales.
- Notas reales.
- Promedio real.
- Nota necesaria real.
- Escala de notas dinámica.
- Editar/eliminar materias y notas.
- Tareas básicas reales.
- Gastos básicos reales.
- Estados vacíos correctos.
- Persistencia local.
- UI pulida.
- Sin datos mock en runtime.

No incluir todavía:
- Google Sign-In.
- Firebase.
- Billing real.
- Exportar PDF.
- Sincronización en la nube.
- IA.
- Notificaciones inteligentes.

## 11. Decisiones tomadas
Agregar:
- DataStore se usa para UserProfile/setup.
- Room se usa para datos estructurados como materias, notas, tareas y gastos.
- Mock data solo se permite en previews/DemoData.
- El usuario puede elegir nombre preferido independiente de Google.
- La escala de notas debe ser configurable.
- Primero se construye MVP local, luego login/sync.
- No se implementa monetización real hasta que el producto base sea sólido.

## 12. Próximas tareas inmediatas
Orden:
1. Arreglar bottom nav: Inicio no navega desde Materias.
2. Corregir estados pendientes: no mostrar 0.0 cuando no hay notas.
3. Revisar espaciado visual tras edge-to-edge.
4. Implementar editar/eliminar materias y notas.
