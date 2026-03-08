# Historial del Proyecto — GestionProyectos (Trello Clone)

## 2026-03-08

### Arquitectura y estructura de paquetes

Se definió la arquitectura hexagonal (puertos y adaptadores) siguiendo DDD. El proyecto se divide en dos módulos Maven:

- `GestionProyectos` (Spring Boot) — dominio, aplicación e infraestructura.
- `gestion_proyectos_ui` (JavaFX) — adaptador de UI (responsabilidad del compañero).

Estructura de paquetes acordada bajo `umu.pds.app`:

```
domain/
├── modelo/
│   ├── shared/      ← IDs tipados
│   └── tablero/     ← Bounded Context principal
├── ports/
│   ├── input/       ← Puertos de entrada (casos de uso)
│   └── output/      ← Puertos de salida (repositorios)
└── exceptions/

application/
└── services/        ← Implementación de casos de uso

infrastructure/
├── adapters/
│   └── out/
│       └── persistence/   ← JPA (hito futuro)
└── config/
```


### Modelo de dominio creado (Hito 1)

11 clases Java — sin `@Entity`, `@Id` ni ninguna anotación de Spring o JPA.

#### `domain/modelo/shared/` — IDs tipados como `record`

| Clase | Descripción |
|---|---|
| `TableroId` | ID del Aggregate Root |
| `ListaId` | ID de la Entidad Lista |
| `TarjetaId` | ID de la Entidad Tarjeta |
| `ChecklistId` | ID de la Entidad local Checklist |

Todos generan UUID con `nuevo()` y se reconstruyen desde String con `de(String)`. Usar tipos distintos evita mezclas accidentales en tiempo de compilación.

#### `domain/modelo/tablero/` — Bounded Context principal

| Clase | Tipo DDD | Descripción |
|---|---|---|
| `EstadoTarea` | enum | `PENDIENTE`, `EN_PROGRESO`, `COMPLETADA` |
| `Tarea` | Value Object (`record`) | Inmutable. Mutaciones devuelven nueva instancia. |
| `ItemChecklist` | Value Object (`record`) | Ítem de checklist inmutable. |
| `Checklist` | Entidad local | Tiene `ChecklistId`. Vive dentro de `Tarjeta`. |
| `Tarjeta` | Entidad (contenedor) | Puede contener `Tarea` y/o `Checklist` opcionales. |
| `Lista` | Entidad | Constructor y mutaciones **package-private** (frontera del agregado). |
| `Tablero` | **Aggregate Root** | Único punto de entrada para toda mutación del agregado. |

#### Decisiones de diseño destacadas

- **`Lista` con constructor package-private**: nadie fuera del paquete `tablero` puede instanciar listas ni añadirle tarjetas directamente. Solo `Tablero` lo hace.
- **`Tarea` como `record`**: inmutabilidad garantizada por Java; `equals`/`hashCode` por valor de todos los campos sin escribir código extra.
- **Getters devuelven `Collections.unmodifiableList()`**: se expone el contenido pero no se permite mutarlo desde fuera.
- **`Tarjeta.getTarea()` y `getChecklist()` devuelven `Optional<>`**: obliga al llamante a gestionar la ausencia explícitamente.
- **`Tablero.moverTarjeta()`**: operación atómica que valida existencia de listas y tarjeta antes de mover.
