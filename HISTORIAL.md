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

### 5. Fase 4 — Infraestructura REST y persistencia en memoria

#### Adaptador de persistencia

**`infrastructure/persistence/InMemoryTableroRepository.java`**
- `HashMap<TableroId, Tablero>` como almacén en memoria.
- `buscarPorEmail` compara con `equalsIgnoreCase`.
- Registrado como `@Bean` en `BeanConfiguration`.

#### DTOs de la capa REST

11 DTOs bajo `infrastructure/rest/dto/`:

| DTO | Dirección | Descripción |
|---|---|---|
| `CrearTableroRequest` | entrada | `nombre`, `email` |
| `AgregarListaRequest` | entrada | `nombre` |
| `AgregarTarjetaRequest` | entrada | `titulo` |
| `MoverTarjetaRequest` | entrada | `tarjetaId`, `listaOrigenId`, `listaDestinoId` |
| `AsignarEtiquetaRequest` | entrada | `nombre`, `color` |
| `RenombrarTableroRequest` | entrada | `nuevoNombre` |
| `TableroResponse` | salida | id, nombre, email, bloqueado, listas, historial |
| `ListaResponse` | salida | id, nombre, tarjetas |
| `TarjetaResponse` | salida | id, titulo, descripcion, completada, etiquetas, tieneTarea, tieneChecklist, **checklist** |
| `EtiquetaResponse` | salida | nombre, color |
| `TrazaResponse` | salida | descripcion, timestamp |

Todos los response tienen `static from(domainObject)` factory.

#### Controlador REST

**`infrastructure/rest/TableroController.java`** — 13 endpoints bajo `${app.server.path}/tableros`:

```
POST   /tableros                                              → crearTablero
GET    /tableros/{id}                                         → obtenerTablero
GET    /tableros?email=...                                    → obtenerTablerosPorEmail
PUT    /tableros/{id}/nombre                                  → renombrarTablero
POST   /tableros/{id}/bloquear
POST   /tableros/{id}/desbloquear
GET    /tableros/{id}/historial
POST   /tableros/{id}/listas
DELETE /tableros/{id}/listas/{listaId}
POST   /tableros/{id}/listas/{listaId}/tarjetas
DELETE /tableros/{id}/listas/{listaId}/tarjetas/{tarjetaId}
POST   /tableros/{id}/tarjetas/mover
POST   /tableros/{id}/listas/{listaId}/tarjetas/{tarjetaId}/completar
POST   /tableros/{id}/listas/{listaId}/tarjetas/{tarjetaId}/etiquetas
DELETE /tableros/{id}/listas/{listaId}/tarjetas/{tarjetaId}/etiquetas
```

- `@ExceptionHandler`: `IllegalArgumentException` → 404, `IllegalStateException` → 409.
- Mapping usa `${app.server.path}` para aparecer en Swagger UI (`springdoc.pathsToMatch=/umu/pds/**`).

#### Configuración

**`infrastructure/config/BeanConfiguration.java`** — `@Configuration` con:
- `@Bean TableroRepository` → instancia `InMemoryTableroRepository`
- `@Bean GestionTableroUseCase` → instancia `TableroService(tableroRepository)`

#### Fix: arranque de Spring Boot

`ApiGroqTest.java` tenía `@Value("${groq.api.key}")` pero `application-secrets.properties` estaba vacío. Se añadió `groq.api.key=` para evitar el fallo al arrancar.

---

### 6. Conexión UI — Backend (módulo `gestion_proyectos_ui`)

#### Nuevos DTOs en el módulo UI

`api/dto/`: `TableroDto`, `ListaDto`, `TarjetaDto`, `EtiquetaDto` — POJOs con `@JsonIgnoreProperties(ignoreUnknown = true)` y campos públicos.

#### `api/TableroApiClient.java` — cliente HTTP

Usa `java.net.http.HttpClient` + Jackson. `BASE_URL = http://localhost:8080/umu/pds`. Métodos síncronos — se llaman siempre desde un `Task<T>` de JavaFX:

```
crearTablero(nombre, email)          → TableroDto
agregarLista(tableroId, nombre)      → ListaDto
agregarTarjeta(tableroId, listaId, titulo) → TarjetaDto
moverTarjeta(tableroId, tarjetaId, listaOrigenId, listaDestinoId)
obtenerTablero(id)                   → TableroDto
```

#### Flujo de navegación completo

```
VentanaInicio → [crear tablero] → VentanaPrincipal → [crear lista] → VentanaTablero
                                                    → [crear tarjeta] → VentanaLista
```

- `VentanaInicioController`: valida campos, llama `apiClient.crearTablero()` en `Task<TableroDto>`, navega a `VentanaPrincipal` pasando el `TableroDto` al controller con `setTablero(tablero)`.
- `VentanaPrincipalController`: sidebar dinámico (`VBox sidebarTableros`), carga `VentanaTablero.fxml` en `mainContentPane` y llama `controller.setTableroId(id)`.
- `VentanaTableroController`: `crearLista()` con `TextInputDialog` + `Task<ListaDto>`, carga `VentanaLista.fxml` dinámicamente.
- `VentanaListaController`: `crearTarjeta()` con `TextInputDialog` + `Task<TarjetaDto>`, carga `VentanaTarjeta.fxml`.
- `VentanaTarjetaController`: `setDatos(tableroId, listaId, tarjeta)` muestra el título.

Patrón común para pasar IDs entre controllers: `FXMLLoader.getController()` + setter.

#### Drag & drop de tarjetas

- `VentanaTarjetaController`: `root.setOnDragDetected` pone la tarjeta semitransparente (opacidad 0.35), genera imagen inclinada 6° con 85% de opacidad para seguir al cursor, guarda `root` en `scene.setUserData()`.
- `VentanaListaController`: `setOnDragOver` mueve un `HBox placeholder` (borde azul punteado) entre las tarjetas mientras se arrastra. `setOnDragDropped` inserta la tarjeta en la posición del placeholder.
- Conexión con backend: al soltar en lista distinta, extrae `VentanaTarjetaController` del `HBox.getUserData()`, actualiza `listaId` inmediatamente y llama `apiClient.moverTarjeta()` en `Task<Void>`.

---

## 2026-03-20 (continuación — segunda sesión)

### 7. Feature: Marcar tarjeta como completada

**Backend**: endpoint ya existía — `POST /tableros/{id}/listas/{listaId}/tarjetas/{tarjetaId}/completar`.

**Frontend** (`VentanaTarjetaController`):
- Añadido `@FXML CheckBox check`, `@FXML VBox contenedorItems`, `TableroApiClient apiClient`.
- `setDatos()`: si `tarjeta.completada`, marca el check, deshabilita y aplica tachado al título.
- `onCompletar()`: aplica tachado → deshabilita check → llama `apiClient.completarTarjeta()` en background → al éxito, elimina el nodo `root` de su `VBox` padre (la tarjeta desaparece de la lista). Si falla, revierte el estado visual.

**Nota de dominio**: `Tablero.marcarTarjetaCompletada()` saca la tarjeta de su lista y la pone en `tarjetasCompletadas`. Por eso la tarjeta se elimina visualmente de la lista tras completarse.

---

### 8. Feature: Dos tipos de tarjeta (tarea simple / con checklist)

#### Backend — nuevos métodos y endpoints

**`Tablero.java`** — 4 nuevos métodos + helper privado:
```
asignarChecklist(listaId, tarjetaId, nombre)         → Checklist
agregarItemChecklist(listaId, tarjetaId, descripcion)
marcarItemChecklist(listaId, tarjetaId, indice)
desmarcarItemChecklist(listaId, tarjetaId, indice)
buscarTarjetaEnLista(listaId, tarjetaId)             [private]
```

**`GestionTableroUseCase`** y **`TableroService`**: implementan los 4 métodos nuevos.

**Nuevos DTOs**: `ItemChecklistResponse`, `ChecklistResponse`, `AgregarChecklistRequest`, `AgregarItemChecklistRequest`.

**`TarjetaResponse`**: añadido campo `ChecklistResponse checklist` (null si no tiene).

**`TableroController`** — 4 nuevos endpoints:
```
POST /tableros/{id}/listas/{listaId}/tarjetas/{tarjetaId}/checklist
POST /tableros/{id}/listas/{listaId}/tarjetas/{tarjetaId}/checklist/items
POST /tableros/{id}/listas/{listaId}/tarjetas/{tarjetaId}/checklist/items/{i}/marcar
POST /tableros/{id}/listas/{listaId}/tarjetas/{tarjetaId}/checklist/items/{i}/desmarcar
```

#### Frontend — nuevos DTOs y lógica

**Nuevos DTOs UI**: `ItemChecklistDto`, `ChecklistDto`. `TarjetaDto` actualizado con campo `ChecklistDto checklist`.

**`TableroApiClient`** — 5 nuevos métodos:
```
completarTarjeta(tableroId, listaId, tarjetaId)
crearChecklist(tableroId, listaId, tarjetaId, nombre)  → ChecklistDto
agregarItemChecklist(tableroId, listaId, tarjetaId, descripcion)
marcarItemChecklist(tableroId, listaId, tarjetaId, indice)
desmarcarItemChecklist(tableroId, listaId, tarjetaId, indice)
```

**`VentanaListaController.crearTarjeta()`**: flujo nuevo:
1. `ChoiceDialog` → "Tarea simple" / "Con checklist"
2. `TextInputDialog` para el título
3. Si checklist: bucle de `TextInputDialog` para ítems (termina con vacío)
4. `Task<TarjetaDto>`: crea tarjeta → si checklist, crea checklist y añade ítems → construye DTO local con ítems para no re-fetchear

**`VentanaTarjeta.fxml`**: reestructurado — el `CheckBox` (completar) y un `VBox` interior que contiene `tituloTarjeta` + `VBox fx:id="contenedorItems"` (oculto por defecto, visible si hay checklist).

**`VentanaTarjetaController`**: si `tarjeta.tieneChecklist && tarjeta.checklist != null`, hace `contenedorItems` visible y genera un `CheckBox` por ítem. Cada CheckBox llama a `marcar/desmarcar` en backend al cambiar de estado. Si la llamada falla, revierte el estado del CheckBox.

---

## Pendiente para la próxima sesión

### Funcionalidades de la UI aún por conectar

| Feature | Descripción | Estado |
|---|---|---|
| Etiquetas con color | Asignar/quitar etiquetas a tarjeta, mostrar pastillas de color | Pendiente |
| Bloquear/desbloquear tablero | Botón en cabecera, endpoint ya existe | Pendiente |
| Historial del tablero | Panel o modal que muestra las `Traza`s | Pendiente |

### Deuda técnica conocida

- Los `target/classes/*.css` y `target/classes/*.fxml` se actualizan manualmente copiando desde `src/`. En un entorno con `mvn compile` activo esto no es problema, pero si Eclipse no reconstruye, los cambios del FXML/CSS no se reflejan en tiempo de ejecución.
- `VentanaTarjeta.fxml` referencia `app-base2.css` (no `app-base.css`). Verificar que el fichero existe en `src/main/resources/.../estilos/`.
- El índice de ítem del checklist es posicional (`int indice`). Si en el futuro se permite reordenar o eliminar ítems, el índice puede quedar desfasado entre UI y backend.

### Próximos pasos sugeridos (por prioridad)

1. **Etiquetas**: añadir `ChoiceBox` o `ColorPicker` en el diálogo de tarjeta; mostrar pastillas en `VentanaTarjeta`
2. **Bloquear tablero**: botón en `VentanaPrincipal` o `VentanaTablero`, toggle con `apiClient.bloquearTablero()` / `desbloquearTablero()`
3. **Historial**: botón "Ver historial" que abra un `Alert` o panel lateral con la lista de `TrazaResponse`
4. **Tests unitarios** del dominio (pendiente desde la primera sesión)

