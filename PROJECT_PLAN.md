# UniStack Project Plan

## 1. Visión del producto

UniStack es una app Android para estudiantes que centraliza la gestión académica y personal del semestre. Permite organizar materias, notas, promedios, tareas, gastos y entregas desde un solo lugar.

El objetivo de UniStack es que el estudiante pueda responder rápidamente preguntas como:

- ¿Cómo voy en mis materias?
- ¿Qué nota necesito para alcanzar mi promedio objetivo?
- ¿Qué tareas debo priorizar?
- ¿En qué estoy gastando durante la semana?
- ¿Qué tengo pendiente por entregar?

UniStack debe sentirse moderna, clara, organizada, fácil de usar y visualmente premium.

---

## 2. Estado actual del proyecto

### Base técnica

- [x] Android project scaffold.
- [x] Kotlin + Jetpack Compose.
- [x] Material 3 / estilo visual UniStack.
- [x] Navegación principal.
- [x] Bottom navigation personalizada.
- [x] DataStore para persistir UserProfile/setup.
- [x] Room para materias y notas.
- [x] Validaciones semánticas básicas.
- [x] Escala de notas dinámica.

### Producto / UX

- [x] HomeScreen visual.
- [x] Onboarding inicial.
- [x] Nombre preferido dinámico.
- [x] Setup académico.
- [x] Eliminación de mock data en runtime.
- [x] Estados pendientes en vez de 0.0.
- [x] Animaciones básicas.
- [x] Branding oficial.
- [x] Launcher icon oficial.
- [x] Header con símbolo oficial.
- [x] Launch animation de marca.

---

## 3. Reglas de trabajo

- No implementar varias features grandes al mismo tiempo.
- Máximo una feature grande activa.
- Los bugs P0 se arreglan antes de nuevas features.
- Los datos mock solo pueden existir en previews o DemoData.
- Cada cambio importante debe compilar con `./gradlew :app:assembleDebug`.
- Cada feature debe mantener la estética visual de UniStack.
- No implementar backend, Firebase, Billing, sincronización, IA o Google Sign-In hasta que el MVP local esté sólido.
- Si aparece una idea nueva, primero va al backlog y no interrumpe la fase actual salvo que sea crítica.
- No mezclar branding, lógica, navegación y persistencia en el mismo PR.
- Cada PR debe tener un objetivo claro y verificable.

---

## 4. Sistema de prioridades

### P0 — Bloqueante

Rompe la app o impide usar una función principal.

Ejemplos:

- La app crashea.
- No se puede navegar.
- No se guardan datos.
- El setup queda bloqueado.

### P1 — Importante

Afecta una función principal o puede confundir al usuario.

Ejemplos:

- Mostrar datos falsos.
- Mostrar 0.0 cuando no hay notas.
- Validaciones incorrectas.
- Cálculos académicos incorrectos.

### P2 — Pulido

Mejora UX, diseño, claridad o comodidad.

Ejemplos:

- Ajustar espaciado.
- Pulir animaciones.
- Mejorar estados vacíos.
- Revisar accesibilidad.

### P3 — Futuro

Idea útil, pero no necesaria para el MVP actual.

Ejemplos:

- Google Sign-In.
- Billing Pro.
- Exportar PDF.
- Sincronización en nube.
- IA.
- Widgets Android.

---

## 5. Roadmap por fases

## Fase 0 — Base del proyecto

Estado: completada.

- [x] Scaffold Android.
- [x] Kotlin + Compose.
- [x] Home visual.
- [x] Onboarding.
- [x] DataStore.
- [x] Room materias/notas.
- [x] Validaciones.
- [x] Escala dinámica de notas.
- [x] Branding oficial.
- [x] Animación inicial de marca.

---

## Fase 1 — Módulo académico sólido

Objetivo: que UniStack sea realmente útil solo con materias y notas.

Pendientes:

- [x] Editar materia.
- [x] Eliminar materia.
- [x] Editar nota.
- [x] Eliminar nota.
- [x] Confirmaciones de eliminación.
- [x] Mejorar detalle de materia.
- [x] Simulador real de nota necesaria.
- [x] Revisar edge cases de porcentajes.
- [x] Revisar comportamiento con materias sin notas.
- [x] Revisar comportamiento con materias evaluadas al 100%.
- [x] Revisar comportamiento cuando la nota necesaria es imposible.
- [x] Revisar comportamiento por escala: 0–5, 0–10, 0–100.

---

## Fase 2 — UX y estabilidad

Objetivo: que la app se sienta estable, fluida y premium.

Pendientes:

- [x] Revisar compactación visual.
- [x] Pulir espaciados.
- [x] Revisar pantallas 360dp–430dp.
- [x] Revisar estados vacíos.
- [x] Revisar accesibilidad básica.
- [x] Revisar modo oscuro si aplica.
- [x] Revisar animaciones y transiciones después de implementar features reales.

---

## Fase 3 — Tareas reales

Objetivo: tareas persistentes con Room.

Pendientes:

- [x] Modelo StudentTask.
- [x] TaskEntity.
- [x] TaskDao.
- [x] RoomTasksRepository.
- [x] Crear tarea.
- [x] Editar tarea.
- [x] Eliminar tarea.
- [x] Marcar tarea como completada.
- [x] Asociar tarea a materia opcionalmente.
- [x] Mostrar tareas reales de hoy en Home.
- [x] Mostrar próxima tarea real en Home.

---

## Fase 4 — Gastos reales

Objetivo: gastos persistentes con Room.

Pendientes:

- [x] Modelo Expense.
- [x] ExpenseEntity.
- [x] ExpenseDao.
- [x] RoomExpensesRepository.
- [x] Registrar gasto.
- [x] Editar gasto.
- [x] Eliminar gasto.
- [x] Resumen semanal real.
- [x] Gráfico real por días.
- [x] Resumen por categoría.
- [x] Estados vacíos de gastos.

---

## Fase 5 — Perfil y configuración

Objetivo: que el usuario pueda revisar y ajustar sus datos.

Pendientes:

- [x] ProfileScreen con datos reales.
- [x] Editar nombre preferido.
- [x] Editar escala de notas.
- [x] Editar promedio objetivo.
- [x] Reiniciar onboarding.
- [x] Configuración de módulos.
- [x] Preferencias visuales.

---

## Fase 6 — Monetización preparada

Objetivo: preparar estructura Pro sin pagos reales.

Pendientes:

- [x] UserPlan.
- [x] FeatureGate.
- [x] Límite gratis de 5 materias.
- [x] Pantalla Pro.
- [x] Beneficios Pro.
- [x] Placeholder de upgrade.
- [x] No implementar Billing todavía.

---

## Fase 7 — Login y sincronización futura

Objetivo: preparar identidad de usuario y backup futuro.

Pendientes:

- [x] Google Sign-In preparado.
- [x] Foto de perfil real desde cuenta vinculada.
- [x] Vincular datos a usuario.
- [x] Backup/sync futuro preparado.

---

## Fase 8 — Plantillas académicas

Objetivo: agregar herramientas para entregas académicas.

Pendientes:

- [x] Checklist de trabajos.
- [x] Plantillas de ensayo.
- [x] Formato APA básico.
- [x] Exportar PDF/Word futuro.

## 6. Sprint actual

### Objetivo

Agregar herramientas para entregas académicas.

### Estado

Implementado en `main`.

Pendiente de prueba manual completa en dispositivo/emulador antes de darlo por cerrado a nivel de QA.

### Tareas

- [x] Crear módulo de plantillas académicas.
- [x] Agregar checklist de trabajos.
- [x] Agregar plantillas de ensayo.
- [x] Agregar guía APA básica.
- [x] Agregar placeholder de exportación PDF/Word.
- [x] Conectar módulo Trabajos desde Home.
- [x] Respetar toggle `ACADEMIC_TEMPLATES`.

## 7. Backlog

### Bugs / inconsistencias

- [x] Revisar si persiste algún problema de navegación en bottom nav.
- [x] Cuando estoy agregando materias y quiero volver al inicio presionando en la bottom bar, no pasa nada.
- [x] Revisar si el edge-to-edge compactó demasiado algunas pantallas.
- [x] Revisar posibles casos de porcentaje mayor a 100%.
- [x] Revisar nota necesaria imposible o fuera de rango.

### UX/UI

- [x] Revisar espaciado general.
- [x] Revisar tamaño y peso visual del bottom nav.
- [x] Mejorar respiración visual en pantallas académicas.
- [x] Pulir estados vacíos.
- [x] Revisar accesibilidad básica.

### Features próximas

- [x] Editar/eliminar materias.
- [x] Editar/eliminar notas.
- [x] Tareas reales con Room.
- [x] Gastos reales con Room.
- [x] Perfil funcional.
- [x] Monetización preparada sin Billing.
- [x] Identidad y sync futuro preparados.
- [x] Plantillas académicas base.

### Ideas futuras

- [ ] Google Sign-In real con OAuth.
- [ ] Pro/Billing.
- [ ] Exportar PDF real.
- [x] Plantillas académicas.
- [ ] Notificaciones.
- [ ] Widgets Android.
- [ ] Enviar carreras faltantes como sugerencia futura.

## 8. Convención de commits

- `fix: restore home navigation from bottom bar`
- `fix: show pending state when subject has no grades`
- `ux: improve spacing across academic screens`
- `ux: integrate official UniStack branding`
- `ux: add animated UniStack launch screen`
- `feat: edit and delete academic items`
- `feat: persist tasks with Room`
- `feat: persist expenses with Room`
- `refactor: centralize grading scale logic`

---

## 9. Definición de MVP 1.0

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

---

## 10. Decisiones tomadas

- DataStore se usa para UserProfile/setup.
- Room se usa para datos estructurados como materias, notas, tareas y gastos.
- Mock data solo se permite en previews/DemoData.
- El usuario puede elegir nombre preferido independiente de Google.
- La escala de notas debe ser configurable.
- Primero se construye MVP local, luego login/sync.
- No se implementa monetización real hasta que el producto base sea sólido.
- El branding oficial de UniStack es la opción A / S modular.
- El header mantiene `Uni` oscuro y `Stack` morado.
