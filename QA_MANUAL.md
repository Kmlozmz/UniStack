# UniStack QA Manual

## Objetivo

Validar que el MVP local de UniStack sea estable antes de agregar backend, Billing, sincronización real o features grandes.

## Criterios de severidad

- P0: crashea, bloquea navegación, impide guardar datos o causa pérdida de información.
- P1: afecta un flujo principal, cálculo, persistencia, validación o estado que puede confundir al usuario.
- P2: problema visual, accesibilidad básica, copy, espaciado o rendimiento percibido.
- P3: mejora futura no necesaria para beta.

## Matriz de ejecución

Usar esta tabla durante pruebas en dispositivo o emulador. Registrar evidencia breve en "Resultado real" y crear bug separado cuando sea P0/P1.

| ID | Área | Caso | Pasos mínimos | Resultado esperado | Resultado real | Severidad |
| --- | --- | --- | --- | --- | --- | --- |
| QA-01 | Inicio | Instalación limpia | Borrar datos de app, abrir UniStack | Muestra animación inicial y luego onboarding sin crashear | Pendiente |  |
| QA-02 | Inicio | Reabrir app configurada | Completar onboarding, cerrar app, abrir de nuevo | Entra al Home sin repetir setup | Pendiente |  |
| QA-03 | Inicio | Animación inicial | Abrir app en cold start | La animación termina y no deja pantalla bloqueada | Pendiente |  |
| QA-04 | Onboarding | Perfil mínimo válido | Ingresar nombre, nivel académico y escala | Guarda perfil y habilita Home | Pendiente |  |
| QA-05 | Onboarding | Validaciones inválidas | Probar textos vacíos, cortos o repetitivos | Muestra error claro y no guarda datos inválidos | Pendiente |  |
| QA-06 | Perfil | Cambiar modo claro | Perfil > Preferencia visual > Claro | UI queda en colores claros y persiste al reabrir | Pendiente |  |
| QA-07 | Perfil | Cambiar modo oscuro | Perfil > Preferencia visual > Oscuro | UI cambia a colores oscuros, con texto legible y barras correctas | Pendiente |  |
| QA-08 | Perfil | Cambiar modo sistema | Perfil > Preferencia visual > Sistema, alternar tema del dispositivo | UI sigue el tema del sistema | Pendiente |  |
| QA-09 | Perfil | Toggles de módulos | Desactivar y activar cada módulo | Bottom bar y accesos respetan módulos activos | Pendiente |  |
| QA-10 | Perfil | Reiniciar onboarding | Perfil > Reiniciar onboarding | Vuelve a setup sin borrar materias, tareas o gastos | Pendiente |  |
| QA-11 | Materias | Sin materias | Abrir Notas sin materias creadas | Muestra estado vacío útil, sin datos mock | Pendiente |  |
| QA-12 | Materias | Crear materia | Crear materia con nombre, meta y color | Persiste y aparece en Notas/Home | Pendiente |  |
| QA-13 | Materias | Editar materia | Editar nombre, meta o color | Cambios se reflejan en detalle y Home | Pendiente |  |
| QA-14 | Materias | Eliminar materia | Eliminar una materia con confirmación | Materia desaparece y no rompe navegación | Pendiente |  |
| QA-15 | Notas | Materia sin notas | Abrir detalle de materia sin notas | No muestra promedio falso ni 0.0 engañoso | Pendiente |  |
| QA-16 | Notas | Una nota | Agregar una nota con porcentaje válido | Promedio actual y porcentaje evaluado son correctos | Pendiente |  |
| QA-17 | Notas | Varias notas | Agregar varias notas con porcentajes acumulados | Cálculos ponderados son correctos | Pendiente |  |
| QA-18 | Notas | 100% evaluado | Completar 100% de evaluación | No pide nota necesaria y muestra estado final coherente | Pendiente |  |
| QA-19 | Notas | Porcentaje mayor a 100% | Intentar exceder 100% | Bloquea guardado y explica el problema | Pendiente |  |
| QA-20 | Notas | Escala 0-5 | Configurar escala 0-5 y crear notas | Valida nota máxima 5 y formatea con un decimal | Pendiente |  |
| QA-21 | Notas | Escala 0-10 | Configurar escala 0-10 y crear notas | Valida nota máxima 10 y cálculos correctos | Pendiente |  |
| QA-22 | Notas | Escala 0-100 | Configurar escala 0-100 y crear notas | Valida nota máxima 100 y formatea sin decimal innecesario | Pendiente |  |
| QA-23 | Tareas | Sin tareas | Abrir Tareas sin datos | Muestra estado vacío útil | Pendiente |  |
| QA-24 | Tareas | Crear tarea | Crear tarea con fecha y tiempo estimado | Persiste y aparece en lista/Home según corresponda | Pendiente |  |
| QA-25 | Tareas | Tarea vencida | Crear tarea con fecha anterior | Muestra texto de vencimiento correcto | Pendiente |  |
| QA-26 | Tareas | Completar tarea | Marcar tarea como completada | Cambia estado y no aparece como pendiente principal | Pendiente |  |
| QA-27 | Tareas | Asociar materia | Crear tarea asociada a materia | La relación se muestra correctamente | Pendiente |  |
| QA-28 | Tareas | Editar/eliminar | Editar y eliminar tarea existente | Cambios persisten y no quedan referencias rotas | Pendiente |  |
| QA-29 | Gastos | Sin gastos | Abrir Gastos sin datos | Muestra estado vacío y resumen en cero claro | Pendiente |  |
| QA-30 | Gastos | Crear gasto | Registrar gasto con fecha, valor y categoría | Persiste y actualiza resumen semanal | Pendiente |  |
| QA-31 | Gastos | Gasto semanal | Crear gastos dentro/fuera de semana actual | Solo semana actual alimenta resumen semanal | Pendiente |  |
| QA-32 | Gastos | Editar gasto | Cambiar valor, fecha o categoría | Resumen y gráfico se recalculan | Pendiente |  |
| QA-33 | Gastos | Eliminar gasto | Eliminar gasto existente | Desaparece y totales se actualizan | Pendiente |  |
| QA-34 | Navegación | Bottom bar | Navegar entre Home, Notas, Tareas, Gastos y Perfil | Cambia de tab sin loops ni pantallas muertas | Pendiente |  |
| QA-35 | Navegación | Volver desde formulario | Abrir crear/editar, usar back button | Regresa a pantalla anterior sin perder navegación principal | Pendiente |  |
| QA-36 | Navegación | Gesture back | Repetir flujos con gesture back | Comportamiento equivalente al botón Atrás | Pendiente |  |
| QA-37 | Responsive | 360dp | Probar pantallas principales en 360dp | No hay texto cortado ni controles superpuestos | Pendiente |  |
| QA-38 | Responsive | 390dp | Probar pantallas principales en 390dp | Layout mantiene jerarquía y legibilidad | Pendiente |  |
| QA-39 | Responsive | 430dp | Probar pantallas principales en 430dp | Layout se ve estable y sin saltos | Pendiente |  |
| QA-40 | Responsive | Tablet básica | Probar en ancho tablet | Contenido no se rompe ni queda incoherente | Pendiente |  |
| QA-41 | Accesibilidad | Tamaño táctil | Revisar botones, pills, checkboxes y navegación | Objetivos táctiles son cómodos | Pendiente |  |
| QA-42 | Accesibilidad | Contraste | Revisar claro, oscuro y sistema | Texto y controles mantienen contraste suficiente | Pendiente |  |
| QA-43 | Datos | Persistencia general | Crear materia, nota, tarea y gasto; cerrar y reabrir | Todos los datos siguen disponibles | Pendiente |  |
| QA-44 | Datos | Sin mock runtime | Revisar Home y módulos sin datos reales | No aparecen datos demo fuera de previews | Pendiente |  |

## Comandos de validación

```bash
./gradlew testDebugUnitTest
./gradlew :app:assembleDebug -x sendDebugApkToTelegram
```

## Cierre de Fase 1

- Todos los casos P0/P1 pasan o tienen bug corregido.
- No hay datos mock visibles en runtime.
- La app persiste información tras cerrar y abrir.
- La navegación no presenta loops, flicker bloqueante ni pantallas muertas.
- Claro, oscuro y sistema funcionan desde Perfil.
