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

---

## 2026-03-24

### Contexto de la sesión

Sesión dedicada íntegramente al backend (`GestionProyectos`). Se implementó la capa de persistencia real con JPA/H2, se incorporó la entidad `Usuario` al dominio y a la base de datos, se enriqueció el Aggregate Root `Tablero` con invariantes de negocio explícitas, y se sincronizó el controlador REST con los últimos contratos.

---

### 1. Persistencia real con JPA (estilo UMULingo)

Se sustituyó `InMemoryTableroRepository` por una pila JPA completa. El dominio permanece puro: ninguna clase bajo `domain/` contiene `@Entity`, `@Id` ni dependencias de Jakarta.

#### Entidades JPA (`adapters/jpa/entity/`)

| Clase | Tabla | Descripción |
|---|---|---|
| `TableroJpaEntity` | `tableros` | Aggregate Root. Cascada a listas, tarjetas completadas e historial. |
| `ListaJpaEntity` | `listas` | `@OneToMany` ordenado con `@OrderColumn`. |
| `TarjetaJpaEntity` | `tarjetas` | Contiene `@Embedded` para Tarea y `@OneToOne` para Checklist. |
| `ChecklistJpaEntity` | `checklists` | `@ElementCollection` para ítems. |
| `TareaJpaEmbeddable` | columnas en `tarjetas` | Mapea los 4 campos del VO `Tarea`. |
| `ItemChecklistJpaEmbeddable` | `checklist_items` | Un registro por ítem. |
| `EtiquetaJpaEmbeddable` | `tarjeta_etiquetas` | Un registro por etiqueta. |
| `TrazaJpaEmbeddable` | `tablero_historial` | Descripcion + timestamp con orden garantizado. |

#### Repositorio JPA y Adaptador

- **`TableroRepositoryJPA`** — extiende `JpaRepository<TableroJpaEntity, String>`. Query derivada: `findByPropietarioEmail(String email)`.
- **`TableroJpaAdapter`** — `@Repository` que implementa el puerto de salida `TableroRepository`. Delega en `TableroRepositoryJPA` y usa el mapper para cruzar la frontera.

#### Mapper (`adapters/mappers/TableroMapper`)

Convierte Dominio ↔ JPA en ambas direcciones. Para reconstruir `Tablero` desde base de datos se usa **reflexión** sobre los campos `private final` (`listas`, `tarjetasCompletadas`, `historial`, `bloqueado`): es la técnica correcta cuando el Aggregate Root no expone un constructor de reconstrucción y el dominio no puede modificarse para añadir anotaciones.

Para `Checklist`, los ítems se reconstituyen accediendo también vía reflexión al `ArrayList` interno para restaurar el estado `completado` de cada ítem (que `agregarItem()` siempre inicializa a `false`).

#### Configuración

- `application.properties` — H2 en modo archivo (`./data/gestion_proyectos`), `ddl-auto=update`. Las tablas se crean automáticamente al arrancar. Consola H2 habilitada en `/h2-console`.
- `BeanConfiguration` — eliminado el bean `InMemoryTableroRepository`; Spring detecta `TableroJpaAdapter` por `@Repository`.
- Fix: el fichero `application.properties` tenía codificación ISO-8859-1 (con caracteres especiales en los comentarios); se reescribió en UTF-8 puro para evitar `MalformedInputException` en el plugin de recursos de Maven.

---

### 2. Entidad Usuario en el dominio y en la persistencia

#### Dominio (`domain/modelo/usuario/Usuario.java`)

Nueva entidad del dominio conforme al Glosario Ubicuo: "Persona identificada por correo electrónico".

- **Identidad**: el propio email (String) actúa como ID — no se necesita UUID aparte.
- **Campos**: `email` (final), `nombre` (mutable via `cambiarNombre()`).
- **Sin setters públicos**, sin anotaciones de persistencia.
- `equals`/`hashCode` por email.

Puerto de salida: **`domain/ports/output/UsuarioRepository`** — `guardar(Usuario)` + `buscarPorEmail(String)`.

#### Actualización de `Tablero`

- Campo `String emailPropietario` sustituido por `Usuario propietario` (final).
- Ambos constructores actualizados.
- Se mantiene `getEmailPropietario()` como método de conveniencia que delega en `propietario.getEmail()` — el controlador REST y los DTOs no necesitaron cambios.
- Añadido `getPropietario()` para acceso al objeto completo.

#### Persistencia JPA del Usuario

| Clase | Descripción |
|---|---|
| `UsuarioJpaEntity` | `@Entity`, tabla `usuarios`, `@Id String email` |
| `UsuarioRepositoryJPA` | `JpaRepository<UsuarioJpaEntity, String>` |
| `UsuarioMapper` | `toDomain` / `toJpaEntity` |
| `UsuarioJpaAdapter` | `@Repository`, implementa `UsuarioRepository` |

`TableroJpaEntity` — sustituido `@Column email_propietario` por `@ManyToOne(eager) UsuarioJpaEntity propietario` con `@JoinColumn(name="propietario_email")`.

#### Lógica find-or-create en `TableroService`

`crearTablero(nombre, email)` ahora:
1. Busca el `Usuario` por email en `UsuarioRepository`.
2. Si no existe, lo crea y lo guarda.
3. Pasa el `Usuario` al constructor de `Tablero`.

---

### 3. Enriquecimiento del Aggregate Root `Tablero`

Se actualizaron los contratos de tres métodos existentes y se añadió un método nuevo, siguiendo la regla de no usar ñ ni tildes en nombres de métodos o variables:

| Método | Cambio |
|---|---|
| `agregarLista(String)` → `agregarLista(Lista)` | El dominio recibe el objeto ya construido; la capa de aplicación hace `Lista.nueva(nombre)`. |
| `agregarTarjeta(ListaId, String)` → `agregarTarjeta(ListaId, Tarjeta) throws TableroException` | Lanza `TableroException` (checked) si el tablero está bloqueado — excepción de dominio en lugar de `IllegalStateException`. |
| `marcarTarjetaCompletada(ListaId, TarjetaId)` → `completarTarjeta(TarjetaId, ListaId)` | Renombrado al término del glosario; orden de parámetros: TarjetaId primero. |

Todos los métodos de mutación ya generaban `Traza` al historial; los nuevos también la generan. Los getters ya devolvían `Collections.unmodifiableList()` — sin cambio.

`TableroService` actualizado: construye `Lista.nueva()` y `Tarjeta.nueva()` en la capa de aplicación antes de pasar al dominio; captura `TableroException` y la envuelve en `IllegalStateException` para que el controlador la mapee a HTTP 409.

---

### 4. Gestión de Etiquetas a través del Aggregate Root

Antes, `TableroService.asignarEtiqueta` accedía directamente a `Lista.buscarTarjeta(...).asignarEtiqueta(...)`, saltándose el Aggregate Root. Se corrigió:

#### `Tarjeta`

- Añadido `agregarEtiqueta(Etiqueta)` — alias semántico del glosario que delega en `asignarEtiqueta`. Se mantiene `asignarEtiqueta` para el mapper JPA (que no puede tocarse).
- `getEtiquetas()` ya devolvía `unmodifiableList` — sin cambio.

#### `Tablero`

Dos nuevos métodos que cierran la frontera del agregado:

```
etiquetarTarjeta(TarjetaId, ListaId, Etiqueta)
    → busca tarjeta via buscarTarjetaEnLista, llama agregarEtiqueta, registra Traza

desetiquetarTarjeta(TarjetaId, ListaId, Etiqueta)
    → busca tarjeta, llama quitarEtiqueta, registra Traza
```

#### Capa de aplicación

- `GestionTableroUseCase` — añadidos `etiquetarTarjeta` y `desetiquetarTarjeta`.
- `TableroService` — `asignarEtiqueta` y `quitarEtiqueta` (legacy, usados por el controlador) delegan en los nuevos métodos. Patrón: obtener → dominio → guardar.

---

### 5. Sincronización del controlador REST

- Endpoints de etiquetas renombrados: `asignarEtiqueta` → `etiquetarTarjeta`, `quitarEtiqueta` → `desetiquetarTarjeta`. Las rutas HTTP no cambian.
- Añadido `@ExceptionHandler(TableroException.class)` → **HTTP 409 Conflict** (regla de negocio violada: tablero bloqueado).
- Mapa de excepciones resultante:

| Excepción | HTTP | Caso típico |
|---|---|---|
| `IllegalArgumentException` | 404 Not Found | ID de tablero/lista/tarjeta no existe |
| `IllegalStateException` | 409 Conflict | Regla de negocio envuelta por el servicio |
| `TableroException` | 409 Conflict | Regla de negocio directa del dominio |

---

## Pendiente para la próxima sesión

### Funcionalidades de la UI aún por conectar

| Feature | Descripción | Estado |
|---|---|---|
| Etiquetas con color | Asignar/quitar etiquetas a tarjeta, mostrar pastillas de color | Pendiente |
| Bloquear/desbloquear tablero | Botón en cabecera, endpoints `etiquetarTarjeta`/`desetiquetarTarjeta` ya operativos | Pendiente |
| Historial del tablero | Panel o modal que muestra las `Traza`s | Pendiente |
| Login / pantalla de usuario | `Usuario` ya existe en dominio y BD; falta flujo de login en la UI | Pendiente |

### Deuda técnica conocida

- Los `target/classes/*.css` y `target/classes/*.fxml` se actualizan manualmente copiando desde `src/`. En un entorno con `mvn compile` activo esto no es problema, pero si Eclipse no reconstruye, los cambios del FXML/CSS no se reflejan en tiempo de ejecución.
- `VentanaTarjeta.fxml` referencia `app-base2.css` (no `app-base.css`). Verificar que el fichero existe en `src/main/resources/.../estilos/`.
- El índice de ítem del checklist es posicional (`int indice`). Si en el futuro se permite reordenar o eliminar ítems, el índice puede quedar desfasado entre UI y backend.
- `InMemoryTableroRepository` ya no se usa pero el fichero sigue en el repositorio — se puede eliminar cuando se confirme que nadie lo referencia. (YA ELIMINADO)

### Próximos pasos sugeridos (por prioridad)

1. **Tests unitarios** del dominio: `Tablero`, `Tarjeta`, `Lista`, `Checklist` — pendiente desde la primera sesión; ahora que el dominio está estabilizado es el momento idóneo.
2. **Etiquetas en la UI**: `ChoiceBox` o `ColorPicker` en el diálogo de tarjeta; pastillas de color en `VentanaTarjeta`; llamar a `POST/DELETE .../etiquetas`.
3. **Bloquear tablero en la UI**: botón toggle en `VentanaPrincipal` o `VentanaTablero`; capturar el 409 que devuelve el backend al intentar agregar tarjeta con tablero bloqueado.
4. **Historial**: botón "Ver historial" que abra un `Alert` o panel lateral con la lista de `TrazaResponse`.
5. **Pantalla de login**: aprovechar que `Usuario` ya existe en dominio y BD; la UI puede solicitar email al arrancar y hacer `GET /tableros?email=...` para cargar los tableros del usuario.

## 2026-03-24 (continuación — tercera sesión)

### Contexto de la sesión
Sesión enfocada en la estabilización del proyecto. Se completaron los requisitos de pruebas de software (testing), se integró el flujo de login real conectando el frontend con el nuevo backend persistente, y se implementó visual y funcionalmente la asignación de etiquetas a las tarjetas.

---

### 1. Pruebas Unitarias y Cobertura (Testing)
Se implementaron pruebas unitarias exhaustivas para asegurar la robustez del Bounded Context principal (`Tablero`).

* **Implementación:** Se creó `TableroTest.java` utilizando **JUnit 5**.
* **Enfoque:** Se combinaron pruebas de **caja negra** (validando especificaciones como no añadir tarjetas a tableros bloqueados) y pruebas de **caja blanca** (buscando ejecutar todos los caminos lógicos).
* **Cobertura:** Se configuró el plugin **JaCoCo** (`jacoco-maven-plugin` 0.8.12) en el `pom.xml`.
* **Resultado:** Se generaron 57 tests agrupados mediante `@Nested`, logrando una cobertura completa sobre las reglas de negocio, la gestión de listas, tarjetas, checklists y excepciones (`TableroException`). Se verificó el éxito mediante el reporte HTML generado por Maven (`target/site/jacoco/index.html`).

---

### 2. Integración del Flujo de Login (Frontend - Backend)
Se abordó la deuda técnica generada al hacer que el `Usuario` fuera obligatorio para la persistencia de un `Tablero`.

* **API Client (`TableroApiClient`):** Se añadió el método `obtenerTablerosPorEmail(String email)` que consume el endpoint `GET /tableros?email=...`.
* **Login Visual (`VentanaInicio`):** Se refactorizó la interfaz para actuar exclusivamente como pantalla de inicio de sesión. Ahora solo solicita el `email`.
* **Sincronización:** Al introducir el email, la UI recupera los tableros de ese usuario desde la base de datos H2 y los inyecta en la `VentanaPrincipal` mediante `setDatosUsuario(email, tableros)`.
* **Resolución de Bugs Críticos:**
    * **Jackson Mapping:** Se resolvió un error 500 donde los campos del JSON llegaban nulos al backend. Se aplicó explícitamente la anotación `@JsonProperty` en los `record` DTOs (ej: `CrearTableroRequest`) para asegurar la correcta deserialización.
    * **Entidades Transientes (JPA):** Se solucionó una violación de integridad (`SQL Error: 23502 - EMAIL_PROPIETARIO is null`) en `TableroJpaAdapter`. Al mapear el dominio a JPA, Hibernate no reconocía al usuario instanciado en memoria. Se corrigió inyectando `UsuarioRepositoryJPA` y utilizando `getReferenceById(email)` para asociar el tablero a una entidad gestionada por JPA antes de hacer el `save()`.

---

### 3. Feature: Etiquetas de Colores en la UI
Se completó la funcionalidad visual para categorizar tarjetas.

* **API Client:** Se añadieron los métodos `etiquetarTarjeta` (POST) y `desetiquetarTarjeta` (DELETE).
* **DTO:** Se actualizó `TarjetaDto` para incluir `List<EtiquetaDto> etiquetas` y se creó la clase `EtiquetaDto`.
* **Interfaz de Edición (`VentanaTarjeta`):**
    * Se añadió un contenedor `FlowPane` para mostrar las etiquetas activas.
    * Se implementó un botón "+ Etiqueta" que abre un diálogo (`Dialog`) personalizado. Este diálogo incluye un `TextField` para el nombre y un `ColorPicker` nativo de JavaFX para seleccionar el color (convertido a formato HEX).
* **Visualización (Pastillas):** Las etiquetas se renderizan dinámicamente como `Label`s de JavaFX con CSS inyectado (fondo de color, texto blanco, bordes redondeados).
* **Interacción:** Al hacer clic sobre una "pastilla" de etiqueta en la tarjeta, se ejecuta el borrado asíncrono en el backend y se elimina visualmente al instante.

---

### Estado de Funcionalidades Pendientes (Actualizado)

| Feature | Descripción | Estado |
|---|---|---|
| Login / pantalla de usuario | Flujo de login por email conectado a la persistencia. | **Completado** |
| Etiquetas con color | Asignar/quitar etiquetas a tarjeta, mostrar pastillas de color. | **Completado** |
| Tests Unitarios | Pruebas unitarias del dominio con JUnit 5 y JaCoCo. | **Completado** |
| Bloquear/desbloquear tablero | Botón en cabecera para bloquear la adición de tarjetas. | Pendiente |
| Historial del tablero | Panel o modal que muestra las `Traza`s. | Pendiente |
| Vista de Calendario | Visualización de tarjetas con fecha de vencimiento en CalendarFX. | **Completado** |
| Fechas de vencimiento (DatePicker) | Asignar fecha de vencimiento a una tarjeta desde la UI. | **Completado** |
| Menú contextual de lista | Opciones "Añadir tarjeta" y "Eliminar lista" desde el botón de opciones. | **Completado** |

---

## 2026-03-30

### Contexto de la sesión

Sesión de integración fullstack. Se completaron cuatro funcionalidades independientes que en conjunto cierran el ciclo de vida de una tarjeta (fechas de vencimiento), mejoran la gestión de listas (menú contextual con eliminación), y añaden una vista alternativa de calendario para la planificación visual.

---

### 1. Menú Contextual en Listas (`ContextMenu`)

Se añadió funcionalidad al botón de opciones de la lista (`btnMenuLista`) en `VentanaListaController`.

**Opciones implementadas:**

| Opción | Comportamiento |
|---|---|
| Añadir tarjeta | Reutiliza el flujo `crearTarjeta()` existente — `ChoiceDialog` de tipo → `TextInputDialog` de título → llamada al backend. |
| Eliminar lista | Muestra un `Alert` de confirmación de tipo `CONFIRMATION`. Si el usuario confirma, ejecuta un `Task<Void>` asíncrono que llama al endpoint `DELETE /tableros/{id}/listas/{listaId}`. |

**Eliminación dinámica del nodo visual:** al recibir HTTP 200, el `HBox` correspondiente a la lista se elimina del `contenedorListas` en el hilo JavaFX (`Platform.runLater`). Si el backend devuelve error, no se modifica la UI y se muestra un `Alert` de tipo `ERROR`.

---

### 2. Soporte de Fechas de Vencimiento (Fullstack — Arquitectura Hexagonal)

Implementación completa de extremo a extremo, respetando en todo momento la separación de capas hexagonal.

#### Backend

**Dominio (`Tarjeta.java`):**
- Añadido campo `LocalDateTime fechaVencimiento` (nullable — `Optional<LocalDateTime>` en el getter).
- Método `asignarFechaVencimiento(LocalDateTime)` añadido a la entidad.

**Persistencia (`TarjetaJpaEntity.java`):**
- Nuevo `@Column` de tipo `LocalDateTime` — mapeado automáticamente por JPA/H2.

**Mapper (`TableroMapper`):**
- `toDomain` y `toJpaEntity` actualizados para trasladar `fechaVencimiento` en ambas direcciones.

**DTO de respuesta (`TarjetaResponse`):**
- Añadido campo `String fechaVencimiento` (serializado como ISO-8601 desde `LocalDateTime.toString()`).
- Factory `from(Tarjeta)` actualizado.

**Nuevo DTO de entrada:**
- `AsignarFechaVencimientoRequest.java` — record con un único campo `String fecha`.

**Puerto de entrada y caso de uso:**
- `GestionTableroUseCase` — nuevo método `asignarFechaVencimiento(TableroId, ListaId, TarjetaId, LocalDateTime)`.
- `TableroService` — implementación con el patrón estándar: obtener → dominio → guardar.

**Nuevo endpoint REST:**

```
PUT /tableros/{id}/listas/{listaId}/tarjetas/{tarjetaId}/vencimiento
Body: { "fecha": "2026-04-15T00:00:00" }
```

#### Frontend

**`TarjetaDto`:** añadido campo `String fechaVencimiento`.

**`TableroApiClient`:** nuevo método `asignarFechaVencimiento(tableroId, listaId, tarjetaId, fecha)` que serializa la fecha como `String` ISO-8601 truncado a 10 caracteres (`YYYY-MM-DD`) — `fecha.toString().substring(0, 10)` — para evitar discrepancias de formato entre la representación de `LocalDate` y lo que el backend espera.

---

### 3. Vista de Calendario (Integración con CalendarFX)

Se añadió una segunda vista principal, accesible desde la cabecera de `VentanaTablero`, que muestra todas las tarjetas con fecha de vencimiento asignada sobre un calendario mensual interactivo.

#### Dependencia añadida

```xml
<dependency>
    <groupId>com.calendarfx</groupId>
    <artifactId>view</artifactId>
    <version>11.12.7</version>
</dependency>
```

#### Nuevos ficheros

| Fichero | Descripción |
|---|---|
| `VentanaCalendario.fxml` | Layout que contiene el `CalendarView` de CalendarFX. |
| `VentanaCalendarioController.java` | Controlador que recibe el `tableroId`, consulta el backend y puebla el calendario. |

#### Navegación dinámica

En `VentanaTableroController` se añadió un botón "Calendario" en la cabecera. Al pulsarlo se intercambia dinámicamente la vista en el `mainContentPane`:
- Si ya se muestra el calendario → vuelve a la vista de listas (`VentanaTablero.fxml`).
- Si se muestra la vista de listas → carga `VentanaCalendario.fxml`, obtiene el controller y llama a `setTableroId(id)`.

#### Resolución de bug crítico de CalendarFX

CalendarFX genera listeners internos sobre el `CalendarSource` durante la inicialización del `CalendarView`. Si se crea y asigna el `CalendarSource` antes de que el componente esté completamente inicializado (p. ej., en el constructor o en el mismo hilo antes de que JavaFX termine el layout), estos listeners no se registran y el calendario queda en blanco o se rompe al recargar.

**Solución aplicada:**
1. La creación del `CalendarSource` y la asociación al `CalendarView` se movieron al método `initialize()` — garantizando que el componente ya está completamente inicializado por JavaFX.
2. Para añadir entradas de tarjetas se usa `entry.setInterval(LocalDate)` en lugar de `entry.setInterval(LocalDateTime, LocalDateTime)` — la sobrecarga con `LocalDate` usa la zona horaria del sistema y respeta los listeners internos de la librería sin destruirlos al reasignar.

---

### 4. Selector de Fechas (`DatePicker`)

Se añadió un `DatePicker` compacto directamente en `VentanaTarjeta.fxml`, alineado en la misma fila que el botón de etiquetas.

**Interacción:**
- Al cambiar la fecha seleccionada (`setOnAction`), `VentanaTarjetaController` ejecuta un `Task<Void>` asíncrono que llama a `apiClient.asignarFechaVencimiento()`.
- Si el backend responde con éxito, el campo `fechaVencimiento` del `TarjetaDto` local se actualiza para que la vista de calendario pueda reflejarlo al recargar.

**Parseo tolerante al cargar datos desde el backend:**

---

## 2026-04-06

### HU-08 — Lista virtual de Tarjetas Completadas (frontend)

Implementación completa de la historia de usuario "marcar tarjeta como completada" en el frontend, sin modificar el backend. Se aprovecha el campo `tablero.tarjetasCompletadas` que el backend ya devuelve.

#### Estrategia: Lista Virtual

En lugar de añadir una lista real en el backend, se crea un `ListaDto` sintético en el cliente con `id = "ESPECIAL_COMPLETADAS"` y `tarjetas = tablero.tarjetasCompletadas`. Este DTO se pasa al mismo flujo de renderizado que las listas normales, apareciendo siempre al final del tablero.

#### Archivos modificados

**`VentanaTableroController.java`**
- `cargarDatos()`: ahora itera las listas normales aunque sean `null` (sin early return), y al final siempre construye y renderiza la lista virtual de completadas.
- `recargarVista()` (nuevo): limpia `contenedorListas` y vuelve a llamar a `cargarDatos(tableroDto)` desde el estado en memoria. Lo invocan los controladores hijos tras completar una tarjeta.
- `mostrarLista()` / `mostrarListaConTarjetas()`: propagan `this` a cada `VentanaListaController` mediante el nuevo `setTableroController()`.

**`VentanaListaController.java`**
- `setDatos()`: si `listaId.equals("ESPECIAL_COMPLETADAS")`, oculta y deshabilita `btnAnadirTarjeta`, `btnMenuLista` y `footerLista` (`setVisible/setManaged(false)`), y aplica estilo visual al título (fondo teal, texto blanco).
- `setTableroController()` (nuevo): setter para recibir la referencia al `VentanaTableroController` padre.
- `setOnDragDropped`: bifurca la lógica según la lista destino:
  - Si es `ESPECIAL_COMPLETADAS` → llama a `apiClient.completarTarjeta(tbId, listaOrigen, tId)` y en `setOnSucceeded` invoca `tc.moverATarjetasCompletadas()`.
  - Si es lista normal → comportamiento anterior (`apiClient.moverTarjeta()`).
- `mostrarTarjeta()`: propaga `tableroController` al `VentanaTarjetaController` de cada tarjeta cargada.

**`VentanaTarjetaController.java`**
- `setTableroController()` (nuevo): setter para recibir la referencia al tablero.
- `setDatos()`: si `tarjeta.completada` (tarjetas que ya estaban en la lista especial), deshabilita el drag (`root.setOnDragDetected(null)`) — impide arrastrarlas fuera.
- `moverATarjetasCompletadas()` (renombrado y publicado desde `sincronizarEstadoLocal()`):
  - Elimina la tarjeta de su `ListaDto` de origen en el DTO local.
  - Marca `tarjeta.completada = true` y la añade a `tablero.tarjetasCompletadas` (evitando duplicados).
  - Llama a `tableroController.recargarVista()` para refrescar la vista; si no hay referencia, cae al comportamiento anterior (elimina el nodo del padre directamente).
- `onToggleCompletada()`: ahora llama a `moverATarjetasCompletadas()` en el `setOnSucceeded`.

#### Flujo completo — Checkbox

1. Usuario marca el checkbox → `onToggleCompletada()`.
2. Se llama a `apiClient.completarTarjeta()` de forma asíncrona.
3. Si éxito → `moverATarjetasCompletadas()`: actualiza el DTO + `tableroController.recargarVista()`.
4. La vista se reconstruye desde el DTO actualizado: la tarjeta aparece en la columna "✓ COMPLETADAS".

#### Flujo completo — Drag & Drop

1. Usuario arrastra una tarjeta y la suelta sobre la columna "✓ COMPLETADAS".
2. El nodo se mueve visualmente al contenedor de esa lista.
3. Se llama a `apiClient.completarTarjeta()` de forma asíncrona.
4. Si éxito → `tc.moverATarjetasCompletadas()` → actualiza DTO + `recargarVista()`.
5. La vista se reconstruye correctamente desde el DTO.

#### Restricciones visuales

- La lista especial no tiene botón de añadir tarjeta ni menú de opciones.
- Las tarjetas ya completadas no son arrastrables (drag deshabilitado en `setDatos()`).
- La columna siempre aparece al final del tablero.

Al recibir la fecha del backend (formato ISO con parte de tiempo variable, p. ej. `"2026-04-15T00:00"` o `"2026-04-15T00:00:00"`), se usa `substring(0, 10)` antes de parsear con `LocalDate.parse()` para aislar siempre la parte `YYYY-MM-DD` e impedir `StringIndexOutOfBoundsException` ante respuestas de diferente longitud.