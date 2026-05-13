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

- Fecha: 2026-05-13.
- Dispositivo físico: `OBCQDAPVU8AMFAMJ`, modelo `2412DPC0AG`, Android 16.
- Densidad antes/después: `Physical density: 520`, `Override density: 500`.
- `./gradlew testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug`: OK, 87 tasks, 10 s.
- `./gradlew connectedDebugAndroidTest`: OK, 1 test, 49 s.
- Bot Telegram: `getMe` OK para `KmloZzz_bot`; el envío de APK queda como tarea explícita u opt-in para no bloquear QA por red.
- Tema del sistema: `cmd uimode night yes/no/auto` responde; se restauró a `Night mode: yes`.
- Crash log durante instrumentación: sin `FATAL EXCEPTION` de `com.unistack.app`.

## Cierre de Fase 1

- Todos los casos P0/P1 pasan o tienen bug corregido.
- No hay datos mock visibles en runtime.
- La app persiste información tras cerrar y abrir.
- La navegación no presenta loops, flicker bloqueante ni pantallas muertas.
- Claro, oscuro y sistema funcionan desde Perfil.
