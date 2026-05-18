# UniStack Navigation Guide

## Principios

- La bottom bar solo contiene destinos raíz de uso frecuente.
- Los formularios siempre vuelven al contexto que los abrió.
- Los detalles de una entidad viven dentro del módulo dueño de esa entidad.
- Si un módulo está desactivado, cualquier ruta protegida vuelve a Inicio.
- Las integraciones externas nunca deben bloquear navegación local.

## Patrones

| Tipo | Uso | Comportamiento |
| --- | --- | --- |
| Tab raíz | Inicio, Materias, Tareas, Gastos | Conserva estado cuando se cambia entre tabs. |
| Detalle | Materia, Pro, Trabajos académicos | Usa back del sistema y botón Volver cuando el contexto puede ser profundo. |
| Formulario | Crear/editar materia, nota, tarea o gasto | Guardar vuelve al contexto anterior; cancelar usa la misma ruta de back. |
| Diálogo | Borrados o decisiones destructivas | Debe nombrar la entidad o consecuencia antes de confirmar. |
| CTA de Home | Prioridad del día | Debe abrir una pantalla accionable, no una explicación. |

## Reglas por módulo

- Materias protege: lista, detalle, notas y simulador.
- Tareas protege: lista, crear y editar.
- Gastos protege: lista, registrar y editar.
- Trabajos protege: plantillas y futuros trabajos persistentes.
- Perfil y Pro siempre son accesibles, aunque el usuario apague módulos.

## Back

- `navigateUp()` es la primera opción.
- Si no hay back stack, volver al root del módulo dueño.
- En rutas protegidas, validar el módulo antes de navegar al fallback.
- Gesture back y botón Volver deben usar el mismo handler.
