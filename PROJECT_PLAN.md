# UniStack Roadmap

## Estado base

UniStack ya tiene un MVP local funcional con onboarding, perfil, materias, notas, tareas, gastos, branding oficial, animación inicial, preparación Pro, preparación de cuenta/sync y plantillas académicas base.

El foco desde este punto es convertir el MVP local en una beta estable, verificable y lista para crecer sin comprometer la arquitectura ni la experiencia visual.

---

## Principios de trabajo

- Trabajar una fase grande a la vez.
- Corregir bugs bloqueantes antes de agregar nuevas features.
- Mantener los datos mock solo en previews, `DemoData` o entornos explícitamente de muestra.
- Validar cambios relevantes con `./gradlew :app:assembleDebug`.
- No introducir backend, Billing real o sincronización real hasta cerrar la estabilidad beta.
- Mantener el estilo UniStack: limpio, claro, premium, consistente y útil.
- Evitar cambios no relacionados en branding, launcher icon, onboarding o lógica estable.

---

## Prioridades

### P0 - Bloqueante

Rompe la app, impide navegar, impide guardar datos o causa pérdida de información.

### P1 - Importante

Afecta flujos principales, cálculos académicos, persistencia, validaciones o estados que pueden confundir al usuario.

### P2 - Pulido

Mejora UX, accesibilidad, consistencia visual, rendimiento percibido o claridad de estados.

### P3 - Futuro

Ideas útiles que no son necesarias para la beta inicial.

---

## Roadmap activo

## Fase 1 - Estabilización beta

Objetivo: asegurar que el MVP local sea confiable antes de seguir ampliando producto.

- [x] Crear matriz de QA manual para flujos críticos.
- [ ] Probar materias sin notas, con una nota, con varias notas y evaluadas al 100%.
- [ ] Probar tareas vacías, vencidas, completadas y asociadas a materias.
- [ ] Probar gastos vacíos, semanales, editados y eliminados.
- [ ] Revisar cold start, launch animation y navegación inicial.
- [ ] Revisar navegación con bottom bar, back button y gesture back.
- [ ] Revisar toggles de módulos y pantallas deshabilitadas.
- [ ] Revisar pantallas en anchos 360dp, 390dp, 430dp y tablet básica.
- [ ] Revisar accesibilidad básica: tamaños, contraste, labels e interacción táctil.
- [ ] Corregir bugs P0/P1 encontrados durante QA.

## Fase 2 - Pruebas automatizadas

Objetivo: proteger la lógica central contra regresiones.

- [ ] Agregar tests unitarios para cálculos académicos.
- [ ] Cubrir nota necesaria, porcentaje evaluado y promedio actual.
- [ ] Cubrir escalas 0-5, 0-10 y 0-100.
- [ ] Agregar tests para validadores de texto.
- [ ] Agregar tests para utilidades de fecha usadas en tareas y gastos.
- [ ] Agregar tests de repositorios con Room en memoria.
- [ ] Agregar pruebas de migración Room cuando se habiliten schemas.
- [ ] Integrar `./gradlew testDebugUnitTest` como verificación regular.

## Fase 3 - Limpieza arquitectónica

Objetivo: reducir acoplamiento y preparar el proyecto para crecer.

- [ ] Dividir pantallas grandes en secciones/composables privados claros.
- [ ] Revisar `HomeScreen`, `ProfileScreen` y pantallas académicas con mayor tamaño.
- [ ] Centralizar gates de módulos y plan Pro.
- [ ] Revisar creación de ViewModels y dependencia del contenedor de app.
- [ ] Activar exportación de schemas de Room.
- [ ] Revisar modelos de UI para evitar lógica pesada dentro de composables.
- [ ] Unificar componentes repetidos de estados vacíos, errores y confirmaciones.

## Fase 4 - Plantillas académicas v2

Objetivo: convertir las plantillas base en herramientas persistentes y accionables.

- [ ] Persistir trabajos académicos con Room.
- [ ] Crear, editar y eliminar trabajos académicos.
- [ ] Asociar trabajos a materias.
- [ ] Agregar fecha de entrega y estado de avance.
- [ ] Convertir checklists en elementos marcables persistentes.
- [ ] Mejorar referencias APA con estructura reutilizable.
- [ ] Preparar modelo interno para exportación futura.

## Fase 5 - Notificaciones locales

Objetivo: recordar tareas y entregas sin depender de backend.

- [ ] Definir preferencias de notificación.
- [ ] Agregar recordatorios locales para tareas.
- [ ] Agregar recordatorios para trabajos académicos.
- [ ] Agregar avisos de tareas vencidas.
- [ ] Evitar duplicación de notificaciones al editar o eliminar.
- [ ] Respetar toggles de módulos y preferencias del usuario.

## Fase 6 - Exportación y backup local

Objetivo: permitir que el usuario saque valor de sus datos y los proteja.

- [ ] Exportar resumen académico a PDF.
- [ ] Exportar reporte de notas por materia.
- [ ] Exportar tareas y gastos en formato estructurado.
- [ ] Crear backup local JSON.
- [ ] Crear importación/restauración desde backup local.
- [ ] Validar errores de importación sin corromper datos existentes.

## Fase 7 - Cuenta y sincronización real

Objetivo: pasar de preparación visual/técnica a identidad real con estrategia segura de datos.

- [ ] Configurar OAuth real para Google Sign-In.
- [ ] Definir estrategia de migración de datos locales a cuenta vinculada.
- [ ] Diseñar estado de sync: local, pendiente, sincronizado y error.
- [ ] Resolver conflictos básicos de datos.
- [ ] Agregar estado visible de backup/sync.
- [ ] No bloquear uso local si el usuario no inicia sesión.

## Fase 8 - Pro y monetización real

Objetivo: convertir la preparación Pro en monetización funcional.

- [ ] Definir productos e IDs reales.
- [ ] Integrar BillingClient.
- [ ] Persistir entitlement Pro de forma segura.
- [ ] Implementar restaurar compras.
- [ ] Revisar límites Free vs Pro.
- [ ] Revisar políticas de Play Store antes de release.

## Fase 9 - Beta y release

Objetivo: preparar una entrega instalable y confiable para usuarios reales.

- [ ] Definir versioning.
- [ ] Crear build release firmada.
- [ ] Revisar permisos y política de privacidad.
- [ ] Revisar crash reporting o alternativa mínima de diagnóstico.
- [ ] Preparar descripción de Play Store.
- [ ] Probar instalación limpia y actualización sobre versión previa.
- [ ] Probar en matriz mínima de dispositivos/emuladores.
- [ ] Cerrar bugs P0/P1 antes de publicar beta.

---

## Backlog de ideas

- [ ] Vista calendario.
- [ ] Widgets Android.
- [ ] Categorías de gastos configurables.
- [ ] Insights de rendimiento académico.
- [ ] Archivo por semestre.
- [ ] Adjuntos en materias o trabajos.
- [ ] Importar notas desde CSV.
- [ ] Compartir plantillas académicas.
- [ ] Mejoras de accesibilidad avanzada.
- [ ] Sugerencia de carreras faltantes.

---

## Decisiones vigentes

- Room se usa para datos estructurados locales.
- DataStore se usa para setup, perfil y preferencias.
- El MVP local se estabiliza antes de backend, cloud sync o Billing real.
- El plan Free mantiene límite de materias hasta integrar Billing real.
- El branding oficial no se toca salvo que la tarea sea explícitamente de marca.
- La app debe seguir siendo útil sin cuenta ni conexión.

---

## Definición de beta lista

- No hay bugs P0/P1 conocidos.
- `./gradlew :app:assembleDebug` pasa.
- `./gradlew testDebugUnitTest` pasa cuando la suite esté creada.
- Los flujos principales pasan QA manual.
- No hay datos mock en runtime.
- La información persiste correctamente tras cerrar y abrir la app.
- La navegación no tiene loops, flicker ni pantallas muertas.
- Los placeholders futuros están claramente identificados como no disponibles.
