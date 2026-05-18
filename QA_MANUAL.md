# UniStack QA Manual

## Objetivo

Validar que UniStack sea estable y coherente antes de ampliar features grandes o preparar beta pública.

## Criterios de severidad

- P0: crashea, bloquea navegación, impide guardar datos o causa pérdida de información.
- P1: afecta un flujo principal, cálculo, persistencia, validación o estado que puede confundir al usuario.
- P2: problema visual, accesibilidad básica, copy, espaciado o rendimiento percibido.
- P3: mejora futura no necesaria para beta.

## Matriz de ejecución

Usar esta tabla durante pruebas en dispositivo o emulador. Registrar evidencia breve en "Resultado real" y crear bug separado cuando sea P0/P1.

| ID | Área | Caso | Pasos mínimos | Resultado esperado | Resultado real | Severidad |
| --- | --- | --- | --- | --- | --- | --- |
| QA-01 | Inicio | Instalación limpia | Borrar datos de app, abrir UniStack | Muestra animación inicial y luego onboarding sin crashear | OK 2026-05-13: instalación limpia en dispositivo físico, onboarding visible y sin crashes |  |
| QA-02 | Inicio | Reabrir app configurada | Completar onboarding, cerrar app, abrir de nuevo | Entra al Home sin repetir setup | OK 2026-05-13: perfil configurado sobrevive recreaciones de Activity y vuelve a Home sin repetir setup |  |
| QA-03 | Inicio | Animación inicial | Abrir app en cold start | La animación termina y no deja pantalla bloqueada | OK 2026-05-13: arranque limpio termina en onboarding sin bloqueo |  |
| QA-04 | Onboarding | Perfil mínimo válido | Ingresar nombre, nivel académico y escala | Guarda perfil y habilita Home | OK 2026-05-13: cubierto por `UniStackDeviceFlowTest` |  |
| QA-05 | Onboarding | Validaciones inválidas | Probar textos vacíos, cortos o repetitivos | Muestra error claro y no guarda datos inválidos | OK 2026-05-13: botón inicial deshabilitado, validadores cubiertos por unit tests y porcentaje >100 bloqueado |  |
| QA-06 | Perfil | Cambiar modo claro | Perfil > Preferencia visual > Claro | UI queda en colores claros y persiste al reabrir | OK 2026-05-13: selección Claro validada en Perfil por instrumentación; preferencia se guarda en perfil local |  |
| QA-07 | Perfil | Cambiar modo oscuro | Perfil > Preferencia visual > Oscuro | UI cambia a colores oscuros, con texto legible y barras correctas | OK 2026-05-13: selección Oscuro validada en Perfil por instrumentación; lint sin issues de contraste/tamaño |  |
| QA-08 | Perfil | Cambiar modo sistema | Perfil > Preferencia visual > Sistema, alternar tema del dispositivo | UI sigue el tema del sistema | OK 2026-05-13: selección Sistema validada; `cmd uimode night yes/no/auto` responde y se restauró a `yes` |  |
| QA-09 | Perfil | Toggles de módulos | Desactivar y activar cada módulo | Bottom bar y accesos respetan módulos activos | OK 2026-05-13: Notas, Tareas, Gastos y Trabajos desactivan/reactivan accesos; se bloquea dejar cero módulos |  |
| QA-10 | Perfil | Reiniciar onboarding | Perfil > Reiniciar onboarding | Vuelve a setup sin borrar materias, tareas o gastos | OK 2026-05-13: confirmación y retorno a onboarding cubiertos por instrumentación |  |
| QA-11 | Materias | Sin materias | Abrir Notas sin materias creadas | Muestra estado vacío útil, sin datos mock | OK 2026-05-13: estado vacío validado |  |
| QA-12 | Materias | Crear materia | Crear materia con nombre, meta y color | Persiste y aparece en Notas/Home | OK 2026-05-13: creación validada |  |
| QA-13 | Materias | Editar materia | Editar nombre, meta o color | Cambios se reflejan en detalle y Home | OK 2026-05-13: edición validada |  |
| QA-14 | Materias | Eliminar materia | Eliminar una materia con confirmación | Materia desaparece y no rompe navegación | OK 2026-05-13: eliminación validada |  |
| QA-15 | Notas | Materia sin notas | Abrir detalle de materia sin notas | No muestra promedio falso ni 0.0 engañoso | OK 2026-05-13: estado vacío de notas validado |  |
| QA-16 | Notas | Una nota | Agregar una nota con porcentaje válido | Promedio actual y porcentaje evaluado son correctos | OK 2026-05-13: creación, porcentaje y valor visibles; cálculos cubiertos por unit tests |  |
| QA-17 | Notas | Varias notas | Agregar varias notas con porcentajes acumulados | Cálculos ponderados son correctos | OK 2026-05-13: dos notas acumuladas validadas en dispositivo y cálculos cubiertos por unit tests |  |
| QA-18 | Notas | 100% evaluado | Completar 100% de evaluación | No pide nota necesaria y muestra estado final coherente | OK 2026-05-13: mensaje de materia completa validado |  |
| QA-19 | Notas | Porcentaje mayor a 100% | Intentar exceder 100% | Bloquea guardado y explica el problema | OK 2026-05-13: botón de guardar queda deshabilitado y el campo muestra ayuda de porcentaje acumulado |  |
| QA-20 | Notas | Escala 0-5 | Configurar escala 0-5 y crear notas | Valida nota máxima 5 y formatea con un decimal | OK 2026-05-13: UI muestra rango 0 a 5.0; formato/cálculo cubierto por unit tests |  |
| QA-21 | Notas | Escala 0-10 | Configurar escala 0-10 y crear notas | Valida nota máxima 10 y cálculos correctos | OK 2026-05-13: UI muestra rango 0 a 10.0; formato/cálculo cubierto por unit tests |  |
| QA-22 | Notas | Escala 0-100 | Configurar escala 0-100 y crear notas | Valida nota máxima 100 y formatea sin decimal innecesario | OK 2026-05-13: UI muestra rango 0 a 100; formato/cálculo cubierto por unit tests |  |
| QA-23 | Tareas | Sin tareas | Abrir Tareas sin datos | Muestra estado vacío útil | OK 2026-05-13: estado vacío validado |  |
| QA-24 | Tareas | Crear tarea | Crear tarea con fecha y tiempo estimado | Persiste y aparece en lista/Home según corresponda | OK 2026-05-13: creación validada |  |
| QA-25 | Tareas | Tarea vencida | Crear tarea con fecha anterior | Muestra texto de vencimiento correcto | OK 2026-05-13: instrumentación creó tarea de ayer y validó `venció ayer` en lista |  |
| QA-26 | Tareas | Completar tarea | Marcar tarea como completada | Cambia estado y no aparece como pendiente principal | OK 2026-05-13: checkbox y estado completado validados |  |
| QA-27 | Tareas | Asociar materia | Crear tarea asociada a materia | La relación se muestra correctamente | OK 2026-05-13: tarea asociada muestra `Fisica · venció ayer · 1 h` |  |
| QA-28 | Tareas | Editar/eliminar | Editar y eliminar tarea existente | Cambios persisten y no quedan referencias rotas | OK 2026-05-13: edición y eliminación validadas |  |
| QA-29 | Gastos | Sin gastos | Abrir Gastos sin datos | Muestra estado vacío y resumen en cero claro | OK 2026-05-13: estado vacío validado |  |
| QA-30 | Gastos | Crear gasto | Registrar gasto con fecha, valor y categoría | Persiste y actualiza resumen semanal | OK 2026-05-13: creación y formato de moneda validados |  |
| QA-31 | Gastos | Gasto semanal | Crear gastos dentro/fuera de semana actual | Solo semana actual alimenta resumen semanal | OK 2026-05-13: instrumentación validó gasto actual de $10.000 y gasto viejo excluido del resumen semanal |  |
| QA-32 | Gastos | Editar gasto | Cambiar valor, fecha o categoría | Resumen y gráfico se recalculan | OK 2026-05-13: edición validada |  |
| QA-33 | Gastos | Eliminar gasto | Eliminar gasto existente | Desaparece y totales se actualizan | OK 2026-05-13: eliminación validada |  |
| QA-34 | Navegación | Bottom bar | Navegar entre Home, Notas, Tareas, Gastos y Perfil | Cambia de tab sin loops ni pantallas muertas | OK 2026-05-13: tabs principales recorridas por instrumentación |  |
| QA-35 | Navegación | Volver desde formulario | Abrir crear/editar, usar back button | Regresa a pantalla anterior sin perder navegación principal | OK 2026-05-13: back desde formulario de tarea validado |  |
| QA-36 | Navegación | Gesture back | Repetir flujos con gesture back | Comportamiento equivalente al botón Atrás | OK 2026-05-13: flujo BackHandler validado por OnBackPressedDispatcher; pantallas usan el mismo dispatcher que gesture back |  |
| QA-37 | Responsive | 360dp | Probar pantallas principales en 360dp | No hay texto cortado ni controles superpuestos | OK 2026-05-13: smoke responsivo con densidad 542 y Home configurado visible |  |
| QA-38 | Responsive | 390dp | Probar pantallas principales en 390dp | Layout mantiene jerarquía y legibilidad | OK 2026-05-13: flujo principal pasó en dispositivo físico 1220x2712 con densidad 500 |  |
| QA-39 | Responsive | 430dp | Probar pantallas principales en 430dp | Layout se ve estable y sin saltos | OK 2026-05-13: smoke responsivo con densidad 454 y Home configurado visible |  |
| QA-40 | Responsive | Tablet básica | Probar en ancho tablet | Contenido no se rompe ni queda incoherente | OK 2026-05-13: smoke responsivo con densidad 325 y Home configurado visible; densidad restaurada a 500 |  |
| QA-41 | Accesibilidad | Tamaño táctil | Revisar botones, pills, checkboxes y navegación | Objetivos táctiles son cómodos | OK 2026-05-13: chips/pills compactos subidos a mínimo 48dp; lint e instrumentación verdes |  |
| QA-42 | Accesibilidad | Contraste | Revisar claro, oscuro y sistema | Texto y controles mantienen contraste suficiente | OK 2026-05-13: temas Claro/Oscuro/Sistema seleccionables; `lintDebug` sin errores |  |
| QA-43 | Datos | Persistencia general | Crear materia, nota, tarea y gasto; cerrar y reabrir | Todos los datos siguen disponibles | OK 2026-05-13: materias/notas, tareas y gastos validados tras recreate de Activity |  |
| QA-44 | Datos | Sin mock runtime | Revisar Home y módulos sin datos reales | No aparecen datos demo fuera de previews | OK 2026-05-13: flujo vacío validado y escaneo sin mock runtime accionable |  |
| QA-45 | Home | Tablero de prioridades | Crear materia con riesgo, tarea próxima y gasto semanal | Home muestra prioridades accionables hacia materia, tareas, gastos y trabajos | Parcial OK 2026-05-15: el flujo ADB extendido llegó a prioridades, productividad y semana sin crash; falta rerun final por desconexión del dispositivo | P2 |
| QA-46 | Tareas | Filtros compactos | Crear tareas pendientes, vencidas, completadas y asociadas a materia | Filtros por estado y materia actualizan la lista sin romper acciones | Parcial OK 2026-05-15: ADB extendido validó filtros principales y acciones de lista; falta rerun final por desconexión | P2 |
| QA-47 | Gastos | Filtros compactos | Crear gastos de semana, mes y diferentes categorías | Filtros por periodo y categoría actualizan lista y resumen por categoría | Parcial OK 2026-05-15: ADB validó semana actual, exclusión de gasto antiguo, alta y categorías configurables; falta rerun final | P2 |
| QA-48 | Notas | Simulador desde materia | Abrir detalle de materia > Simular esta materia | El simulador abre con esa materia seleccionada y mantiene back correcto | Parcial OK 2026-05-15: escenario guardado desde simulador validado en ADB extendido; falta rerun final | P2 |
| QA-49 | Formularios | Consistencia y preview | Abrir crear/editar tarea y gasto | Header, validaciones, preview y guardar/cancelar son coherentes | Parcial OK 2026-05-15: tareas, gastos y trabajos recorren formularios reales en ADB extendido; falta rerun final | P2 |
| QA-50 | Responsive | Prioridades en tablet | Forzar ancho tablet o emulador | Prioridades de Home usan dos columnas sin solapes | Parcial OK 2026-05-15: densidades 542/454/325 siguen cubiertas; falta rerun final tras ajuste de limpieza | P2 |
| QA-51 | Trabajos | Crear desde plantilla | Abrir Trabajos, elegir plantilla y crear trabajo con fecha/materia | El trabajo se guarda, aparece en lista y mantiene plantilla seleccionada | Parcial OK 2026-05-15: ADB extendido creó trabajo desde plantilla y lo mostró en lista; falta rerun final | P1 |
| QA-52 | Trabajos | Editar y eliminar trabajo | Cambiar título, fecha, estado, prioridad, notas y luego eliminar | Cambios persisten y eliminar limpia el trabajo sin romper navegación | Parcial OK 2026-05-15: edición y diálogo de eliminación cubiertos; navegación de limpieza ajustada y pendiente de rerun | P1 |
| QA-53 | Trabajos | Checklist persistente | Marcar checklist, cerrar app y volver a Trabajos | Progreso y checks se conservan por trabajo | Parcial OK 2026-05-15: checklist y exportación usan datos persistidos en el flujo extendido; falta rerun final | P1 |
| QA-54 | Trabajos | Asociación con materia | Crear trabajo asociado a una materia y abrir detalle de materia | El trabajo aparece en la materia con fecha, estado y progreso | Parcial OK 2026-05-15: asociación con materia validada en detalle durante ADB extendido; falta rerun final | P1 |
| QA-55 | Trabajos | Exportación y APA | Registrar fuentes, copiar referencias y exportar trabajo | Portapapeles recibe texto con checklist, secciones y referencias APA | Parcial OK 2026-05-15: portapapeles APA/exportación validado en ADB extendido; falta rerun final | P2 |
| QA-56 | Home | Próximo trabajo y semana | Crear tarea, gasto y trabajo próximo | Home muestra prioridad de trabajo y vista "Esta semana" sin solapes | Parcial OK 2026-05-15: Home mostró prioridades y productividad con datos reales; falta rerun final | P2 |
| QA-57 | Perfil | Preferencias de notificación | Cambiar toggles y horas de anticipación | Preferencias se guardan y validan rango 1-168 horas | Parcial OK 2026-05-15: preferencias recorridas en ADB extendido y persistencia local verde; falta rerun final | P1 |
| QA-58 | Android | Permiso de notificaciones | Instalar limpio en Android 13+ y abrir app | Solicita permiso cuando corresponde y no bloquea uso si se rechaza | Parcial OK 2026-05-15: instrumentación concede `POST_NOTIFICATIONS`; rechazo manual del permiso sigue pendiente | P1 |
| QA-59 | Recordatorios | Tareas/trabajos próximos | Crear tarea y trabajo con fecha futura, esperar o inspeccionar alarmas | Se programan recordatorios respetando perfil y módulos activos | Parcial OK 2026-05-15: programación/cancelación cubierta por tests locales y flujo ADB de creación; falta inspección manual de alarmas | P1 |
| QA-60 | Recordatorios | Sin duplicados | Editar, completar y eliminar tarea/trabajo con recordatorio | No quedan notificaciones duplicadas ni alarmas obsoletas visibles | Parcial OK 2026-05-15: lógica local y acciones ADB cubiertas; falta inspección manual de alarmas | P1 |
| QA-61 | Insights | Escenarios guardados | Abrir simulador, guardar escenario y reabrir la app | El escenario queda asociado a la materia y puede eliminarse | Parcial OK 2026-05-15: escenario guardado validado en ADB extendido; falta rerun final | P1 |
| QA-62 | Gastos | Presupuesto y alerta | Configurar presupuesto semanal/mensual y registrar gastos | Gastos muestra alerta al superar umbral y tendencia semanal | Parcial OK 2026-05-15: presupuesto y resumen de gastos validados localmente y en ADB extendido; falta rerun final | P1 |
| QA-63 | Gastos | Categorías configurables | Desactivar categorías en Perfil y abrir registrar gasto/filtros | Solo categorías activas aparecen, sin ocultar registros existentes | Parcial OK 2026-05-15: ADB extendido validó que una categoría desactivada no aparece al registrar gasto; falta rerun final | P2 |
| QA-64 | Home | Productividad | Crear tareas completadas, pendientes y vencidas | Home muestra resumen de productividad coherente | Parcial OK 2026-05-15: productividad visible con tareas completadas/vencidas en ADB extendido; falta rerun final | P2 |
| QA-65 | Backup local | Copiar backup JSON | Perfil > Datos y exportación > Copiar backup JSON | Portapapeles contiene JSON versionado con materias, tareas, gastos y trabajos | Parcial OK 2026-05-15: portapapeles con `schemaVersion` y trabajo real validado en ADB extendido; falta rerun final | P1 |
| QA-66 | Backup local | Vista previa restore | Pegar backup JSON válido y tocar Vista previa | Muestra resumen de materias, notas, tareas, gastos y trabajos antes de restaurar | Parcial OK 2026-05-15: preview inválido y válido validados con JSON mínimo versionado; falta rerun final | P1 |
| QA-67 | Backup local | Restore sin duplicados | Restaurar dos veces el mismo backup | No duplica materias, notas, tareas, gastos ni trabajos con el mismo ID | OK local 2026-05-14 para IDs estables y restore sin duplicados; UI restauró un backup mínimo en ADB extendido, falta doble restore desde UI | P1 |
| QA-68 | Backup local | JSON inválido | Pegar texto corrupto y tocar Vista previa/Restaurar | Muestra error y no modifica datos existentes | Parcial OK 2026-05-15: error de JSON inválido visible en ADB extendido; falta rerun final | P1 |
| QA-69 | Exportación | CSV y reporte | Copiar reporte académico, tareas CSV y gastos CSV | El portapapeles contiene datos estructurados y legibles | Parcial OK 2026-05-15: reporte, tareas CSV y gastos CSV validados por portapapeles en ADB extendido; falta rerun final | P2 |
| QA-70 | Exportación | PDF académico | Perfil > Datos y exportación > PDF | Se crea PDF local y la app muestra confirmación sin crashear | Parcial OK 2026-05-15: acción PDF ejecutada en ADB extendido sin crash; falta validación manual del archivo generado | P2 |

## Comandos de validación

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug assembleDebugAndroidTest lintDebug
./gradlew connectedDebugAndroidTest
# Envío opcional de APK:
./gradlew sendDebugApkToTelegram
# o autoenvío tras assemble:
./gradlew assembleDebug -PautoSendTelegramApk=true
```

## Última ejecución

- Fecha: 2026-05-15.
- `./gradlew testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug`: OK, 90 tasks, 1 min 58 s.
- `./gradlew :app:compileDebugAndroidTestKotlin`: OK.
- `./gradlew connectedDebugAndroidTest`: instalación ADB superada tras reintento; el flujo extendido avanzó por onboarding, materias/notas, simulador, tareas, Home, trabajos, perfil, backup/exportación, temas y responsive. El último pase no quedó verde porque el dispositivo se desconectó antes de repetir el ajuste final de navegación (`adb devices` quedó vacío).
- Dispositivo usado antes de la desconexión: Xiaomi `OBCQDAPVU8AMFAMJ`, modelo `2412DPC0AG`, Android 16.
- Último fallo accionable antes de la desconexión: retorno desde limpieza de Trabajos esperaba Home tras `Volver`; se ajustó para navegar por tab `Inicio`. Pendiente: reconectar el teléfono y reintentar `./gradlew connectedDebugAndroidTest`.

## Última ejecución ADB verde

- Fecha: 2026-05-13.
- Dispositivo físico: `OBCQDAPVU8AMFAMJ`, modelo `2412DPC0AG`, Android 16.
- Densidad antes/después: `Physical density: 520`, `Override density: 500`.
- `./gradlew testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug`: OK, 87 tasks, 10 s.
- `./gradlew connectedDebugAndroidTest`: OK, 1 test, 49 s.
- Bot Telegram: `getMe` OK para `KmloZzz_bot`; el envío de APK queda como tarea explícita u opt-in para no bloquear QA por red.
- Tema del sistema: `cmd uimode night yes/no/auto` responde; se restauró a `Night mode: yes`.
- Crash log durante instrumentación: sin `FATAL EXCEPTION` de `com.unistack.app`.

## Última ejecución local Fase 1/2

- Fecha local: 2026-05-13.
- `./gradlew testDebugUnitTest`: OK, incluye repositorios Room en memoria, migraciones 1->2/2->3, navegación y exportación de plantillas.
- `./gradlew assembleDebug assembleDebugAndroidTest lintDebug`: OK, 81 tasks, 3 min 27 s.
- Pase ADB base ejecutado el 2026-05-14; quedan pendientes pases UI específicos para QA-45/46/48/50 y filtros exhaustivos de QA-47.

## Última ejecución local Fase 3/4

- Fecha local: 2026-05-14.
- `./gradlew testDebugUnitTest`: OK, incluye repositorios Room en memoria, migraciones 1->2/2->3/3->4, trabajos académicos y exportación APA.
- `./gradlew assembleDebug assembleDebugAndroidTest lintDebug`: OK, 81 tasks, 3 min 1 s.
- Bot Telegram: APK final de Fase 3/4 enviado con `./gradlew sendDebugApkToTelegram`.
- Pase ADB base ejecutado el 2026-05-14; quedan pendientes pases UI específicos para QA-51 a QA-60.

## Última ejecución local Fase 5/6

- Fecha local: 2026-05-14.
- `./gradlew testDebugUnitTest`: OK, incluye escenarios, presupuesto persistido, backup local JSON, restore sin duplicados y rechazo de JSON corrupto.
- `./gradlew assembleDebug assembleDebugAndroidTest lintDebug`: OK, 81 tasks, 1 min 18 s.
- Bot Telegram: APK final de Fase 5/6 enviado con `./gradlew sendDebugApkToTelegram`.
- Pase de dispositivo base ejecutado el 2026-05-14; quedan pendientes pases UI específicos para QA-45/46/48/50-64/70 y portapapeles de QA-65-69.

## Última ejecución ADB Fases 1-6

- Fecha local: 2026-05-14.
- Dispositivo físico: `OBCQDAPVU8AMFAMJ`, modelo `2412DPC0AG`, Android 16.
- `./gradlew connectedDebugAndroidTest`: OK, 1 test, 1 min 4 s.
- `./gradlew testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug`: OK, 90 tasks, 4 min 48 s.
- Cobertura ADB confirmada: onboarding limpio, creación/edición/eliminación de materia, notas ponderadas, bloqueo de porcentaje >100, recreación de Activity, navegación base de tareas, alta/edición/eliminación de gastos, filtro Semana/Todo, escalas 0-5/0-10/0-100, toggles de módulos, preferencia visual Claro/Oscuro/Sistema, cambio de nombre, densidades 542/454/325 y reinicio de onboarding.
- El permiso `POST_NOTIFICATIONS` se concede en instrumentación con `GrantPermissionRule` para evitar bloqueo del flujo; el rechazo manual queda pendiente.
- No se detectaron crashes en el pase automatizado.

## QA extendido preparado

- Fecha local: 2026-05-15.
- `UniStackDeviceFlowTest` amplía el recorrido para cubrir simulador con escenario guardado, filtros avanzados de tareas, Home/Productividad, trabajos académicos con checklist/exportación APA, presupuesto/categorías, backup JSON, CSV/PDF y limpieza final.
- `./gradlew :app:compileDebugAndroidTestKotlin`: OK.
- Estado actual: la instalación ya no bloquea el pase. El flujo extendido se estabilizó hasta la limpieza final; queda repetir el pase completo cuando el teléfono vuelva a aparecer en `adb devices`.

## Cierre de QA base

- Todos los casos P0/P1 pasan o tienen bug corregido.
- No hay datos mock visibles en runtime.
- La app persiste información tras cerrar y abrir.
- La navegación no presenta loops, flicker bloqueante ni pantallas muertas.
- Claro, oscuro y sistema funcionan desde Perfil.
- Fase 1/2/3/4/5/6 tienen verificación local verde y pase ADB base verde. El QA extendido está instrumentado, compila y ya recorrió la mayoría de los flujos en dispositivo; falta repetirlo de punta a punta cuando el teléfono esté reconectado.
