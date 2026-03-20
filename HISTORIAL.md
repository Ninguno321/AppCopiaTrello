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

---

## 2026-03-20

### Contexto de la sesión

Sesión de conexión UI–backend. Se completó la limpieza y alineación del módulo JavaFX con el lenguaje ubicuo, se completó el dominio con los conceptos que faltaban, y se implementaron las fases 2 y 3 de la arquitectura hexagonal.

---

### 1. Limpieza y renombrado del módulo UI (`gestion_proyectos_ui`)

Se revisaron todos los ficheros FXML y controllers del módulo JavaFX para alinearlos con el lenguaje ubicuo del dominio. Los "Espacios de trabajo" de la UI eran en realidad `Tablero`s.

#### Ficheros renombrados

| Antes | Después |
|---|---|
| `VentanaTarea.fxml` | `VentanaTarjeta.fxml` — modelaba una `Tarjeta`, no una `Tarea` |
| `VistaListaController.java` | `VentanaListaController.java` — consistencia de nomenclatura |

#### Campos y métodos renombrados

**`VentanaPrincipal.fxml` + `VentanaPrincipalController.java`**
- Label `"Espacios de trabajo"` → `"Mis tableros"`
- `btnEspacio1/2` → `btnTablero1/2`
- `btnCrearEspacio` → `btnCrearTablero`
- `lblEspacioActual` → `lblTableroActual`
- `onEspacioSeleccionado()` → `onTableroSeleccionado()`
- `onCrearEspacio()` → `onCrearTablero()`

**`VentanaTablero.fxml` + `VentanaTableroController.java`**
- `HboxDondeVanLasListas` → `contenedorListas`
- `botonCreaLista` → `btnCrearLista`
- Texto del botón `"Cambiar de tablero"` → `"+ Añadir lista"` (estaba mal etiquetado)

**`VentanaLista.fxml` + `VentanaListaController.java`**
- `boton1` → `btnMenuLista`
- `boton2` → `btnAnadirTarjeta`
- `hbox1` → `headerLista`
- `hbox2` → `footerLista`
- `VBox lista` → `contenedorTarjetas`
- Referencia de carga corregida: `VentanaTarea.fxml` → `VentanaTarjeta.fxml`

**`VentanaTarjeta.fxml`**
- `textoTarea` → `tituloTarjeta`

#### Limpieza de `VentanaInicio`

`VentanaInicioController` contenía código del proyecto anterior (gestor de gastos): `ColorPicker`, `Preferences`, y un método `crearNuevaLista()` llamado desde el botón "Launch Board". Todo esto se eliminó.

El controller se reescribió desde cero con la estructura correcta:
- `txtNombreTablero`, `txtEmail`, `btnCrearTablero`
- `onCrearTablero()` con TODOs para la llamada REST

El FXML se actualizó: textos en español, `fx:id` añadidos a los `TextField`, evento `onAction` corregido.

---

### 2. Completar el dominio

#### Nuevos Value Objects

| Clase | Tipo | Descripción |
|---|---|---|
| `Etiqueta` | `record` (VO) | Clasifica una tarjeta. Campos: `nombre` + `color` (String hex). Igualdad por valor. |
| `Traza` | `record` (VO) | Registro inmutable de una acción. Campos: `descripcion` + `timestamp`. Factory method `Traza.nueva(descripcion)` captura `LocalDateTime.now()` automáticamente. |

#### Modificaciones a clases existentes

**`Tarjeta`** — añadido:
- `List<Etiqueta> etiquetas` con `asignarEtiqueta()`, `quitarEtiqueta()`, `getEtiquetas()` (unmodifiable)
- `boolean completada` con `marcarCompletada()` y `estaCompletada()`

**`Tablero`** — añadido:
- `String emailPropietario` — requerido en el constructor (ambas sobrecargas). Razón: HU-01 exige asociar un email al tablero para identificar al propietario.
- `boolean bloqueado` — con `bloquear()`, `desbloquear()`, `isBloqueado()`. Cuando está bloqueado, `agregarTarjeta()` lanza `IllegalStateException`; `moverTarjeta()` sigue permitido.
- `List<Traza> historial` — se añade una `Traza` automáticamente en cada operación relevante: agregar/eliminar lista, agregar/eliminar/mover tarjeta, marcar completada, bloquear/desbloquear.
- `List<Tarjeta> tarjetasCompletadas` — lista especial. `marcarTarjetaCompletada(listaId, tarjetaId)` extrae la tarjeta de su lista, llama a `tarjeta.marcarCompletada()` y la mueve aquí.

#### Corrección de warnings: `serialVersionUID`

Se añadió `private static final long serialVersionUID = 1L;` a las 14 clases de excepción del proyecto (tanto las de `domain/exceptions/` como las inner classes de los records y entidades).

---

### 3. Fase 2 — Puerto de salida

**`domain/ports/output/TableroRepository.java`** — interfaz nueva:

```
guardar(Tablero)
buscarPorId(TableroId) → Optional<Tablero>
buscarPorEmail(String) → List<Tablero>
eliminar(TableroId)
```

Decisión: `buscarPorEmail` devuelve `List` porque un mismo email puede tener múltiples tableros. `buscarPorId` devuelve `Optional` para forzar el manejo explícito del caso "no encontrado".

---

### 4. Fase 3 — Capa de aplicación

**`application/ports/input/GestionTableroUseCase.java`** — interfaz (puerto de entrada):
- Define todas las operaciones que el exterior puede solicitar al sistema.
- La UI y el controller REST dependerán de esta interfaz, nunca de la implementación.

**`application/services/TableroService.java`** — implementa `GestionTableroUseCase`:
- Recibe `TableroRepository` por constructor (inyección de dependencias manual, sin Spring en esta capa).
- Patrón uniforme en cada método: `obtener tablero → llamar método del dominio → guardar → devolver`.
- Sin lógica de negocio propia — toda la lógica vive en el dominio.

---

### Pendiente (próxima sesión) — Fase 4

```
infrastructure/persistence/InMemoryTableroRepository.java
infrastructure/rest/dto/  (CrearTableroRequest, TableroResponse, etc.)
infrastructure/rest/TableroController.java
infrastructure/config/BeanConfiguration.java
```
