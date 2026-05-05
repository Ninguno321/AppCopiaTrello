# Modelo de Dominio — Domain-Driven Design (DDD)

**Proyecto:** AppCopiaTrello — Sistema de gestión de trabajo colaborativo a través de tableros de tareas 
**Módulo backend:** `GestionProyectos`  
**Paquete base del dominio:** `umu.pds.app.domain`  

---

## 1. Introducción al Modelo

El dominio modelado es la **gestión colaborativa de proyectos** al estilo Trello: un espacio de trabajo visual organizado en tableros, donde cada tablero contiene listas y cada lista agrupa tarjetas que representan unidades de trabajo. Las tarjetas pueden contener tareas, checklists, etiquetas y fechas límite, y pueden desplazarse entre listas siguiendo unas determinadas reglas de flujo.

---

## 2. Aggregate Root — `Tablero`

### 2.1 Definición

`Tablero` es la **Raíz del Agregado** (*Aggregate Root*) del bounded context principal. Un agregado DDD es un conjunto de objetos del dominio que 
se tratan como una unidad de consistencia, y cuya coherencia se protege mediante reglas del dominio. Toda operación que modifique el estado del agregado debe pasar obligatoriamente por su raíz.

```
Tablero (Aggregate Root)
 ├── Lista  (Entidad)
 │    └── Tarjeta  (Entidad)
 │         ├── Tarea        (Value Object)
 │         ├── Checklist    (Entidad local)
 │         │    └── ItemChecklist  (Value Object)
 │         ├── Etiqueta   (Value Object)
 │         └── FechaLimite  (Value Object)
 └── Traza[]  (Value Object)
```

### 2.2 Justificación

**¿Por qué `Tablero` y no `Lista` o `Tarjeta`?**

1. **Límite de consistencia transaccional.** Las invariantes de negocio más importantes cruzan las fronteras de Lista y Tarjeta. Por ejemplo, la restricción `maxTarjetas` de una lista solo puede hacerse cumplir de forma fiable si el Tablero es el que coordina. La validación de prerrequisitos al mover una tarjeta requiere inspeccionar a la vez la lista origen, la lista destino y el historial de la tarjeta: ninguna entidad interior podría hacerlo sin acceso al contexto completo del tablero.

2. **Control del ciclo de vida.** `Lista` y `Tarjeta` no tienen repositorio propio. Solo se recuperan, crean y eliminan a través del `Tablero`. Esto garantiza que nunca existan en un estado inconsistente con respecto al agregado que las contiene.

3. **Punto único de entrada para mutaciones.** Todos los métodos de escritura son públicos en `Tablero` y privados o internos en las entidades hijas. El exterior no puede invocar directamente `lista.agregarTarjeta(...)` sin pasar por `tablero.agregarTarjeta(listaId, tarjeta)`. Esta cumple con el principio de que la raíz del agregado es el responsable de las reglas de negocio.

4. **Registro centralizado de trazas.** Cada operación relevante registra una `Traza` en `Tablero.historial`. Si los cambios estuvieran dispersos entre las entidades internas, sería imposible mantener un historial coherente.

### 2.3 Método `reconstitute`

La raíz del agregado tiene un método estático `reconstitute(...)` diferenciado del constructor ordinario. A diferencia del constructor normal, `reconstitute` recupera el objeto desde la persistencia preservando el estado exacto almacenado.

---

## 3. Entidades y Value Objects

### 3.1 Clasificación

| Concepto        | Tipo              | Identidad                  | Mutabilidad           |
|-----------------|-------------------|----------------------------|-----------------------|
| `Tablero`       | Entidad / AR      | `TableroId` (UUID)         | Mutable (controlada)  |
| `Lista`         | Entidad           | `ListaId` (UUID)           | Mutable (controlada)  |
| `Tarjeta`       | Entidad           | `TarjetaId` (UUID)         | Mutable (controlada)  |
| `Checklist`     | Entidad local     | `ChecklistId` (UUID)       | Mutable (controlada)  |
| `Usuario`       | Entidad           | email (String)             | Parcialmente mutable  |
| `Tarea`         | Value Object      | Por valor (todos los campos)| Inmutable (record)   |
| `ItemChecklist` | Value Object      | Por valor                  | Inmutable (record)    |
| `Etiqueta`      | Value Object      | Por valor (nombre + color) | Inmutable (record)    |
| `FechaLimite`   | Value Object      | Por valor (LocalDate)      | Inmutable (final class)|
| `Traza`         | Value Object      | Por valor                  | Inmutable (record)    |
| `EstadoTarea`   | Enumerado         | —                          | N/A                   |

### 3.2 Entidades

Las **Entidades** se identifican por una identidad persistente a lo largo del tiempo, independientemente de que cambien sus atributos.

- **`Tablero`**: identidad `TableroId`. Su `equals`/`hashCode` se basan únicamente en el ID, de acuerdo con el principio DDD de identidad por referencia.
- **`Lista`**: identidad `ListaId`. Puede renombrarse sin perder su identidad, lo cual la distingue de un Value Object.
- **`Tarjeta`**: identidad `TarjetaId`. Mantiene un `historialListas` mutable que registra por qué listas ha transitado.
- **`Checklist`**: entidad *local* (vive exclusivamente dentro de `Tarjeta`). Tiene `ChecklistId` propio pero no tiene repositorio; su ciclo de vida depende completamente de la tarjeta que la contiene.
- **`Usuario`**: identificado por email. No dispone de UUID propio porque el email es suficientemente estable y único como identidad de negocio.

### 3.3 Value Objects

Los **Value Objects** son inmutables y se comparan por el valor de todos sus campos. No tienen identidad propia: dos instancias con los mismos valores son iguales.

- **Identidades tipadas** — `TableroId`, `ListaId`, `TarjetaId`, `ChecklistId`: implementadas como `record` de Java. Envuelven un `UUID` y añaden validación (rechazan `null`), métodos de fábrica (`nuevo()`, `de(String)`) y su propia excepción tipada. Al usar un tipo dedicado en lugar de un `UUID` crudo impide errores de paso de parámetros en tiempo de compilación (p.ej. pasar un `ListaId` donde se espera un `TarjetaId`).

- **`Etiqueta`** — `record(String nombre, String color)`: clasifica visualmente una tarjeta. La igualdad por valor implica que dos etiquetas con el mismo nombre y color son idénticas, lo cual facilita la comprobación de duplicados en `Tarjeta.agregarEtiqueta`.

- **`FechaLimite`** — clase `final` (no `record`, para permitir lógica de validación más rica): encapsula un `LocalDate` y rechaza fechas pasadas en `nuevo(...)`. Expone `reconstitute(...)` como vía alternativa para la carga desde persistencia, evitando esa validación temporal.

- **`Tarea`** — `record` con estado (`EstadoTarea`) y fecha límite opcional. Las operaciones de cambio de estado (`conEstado`, `conDescripcion`, `conFechaLimite`) devuelven **nuevas instancias**, lo que garantiza la inmutabilidad sin renunciar a la expresividad.

- **`ItemChecklist`** — `record(String descripcion, boolean completado)`. Marcar o desmarcar un ítem devuelve una nueva instancia; la lista interna del `Checklist` es reemplazada (`items.set(indice, ...)`), no mutada en el objeto.

- **`Traza`** — `record(String descripcion, LocalDateTime timestamp)`: registro de auditoría generado automáticamente por el Aggregate Root ante cualquier operación relevante. Su inmutabilidad garantiza que el historial no pueda alterarse a posteriori.

---

## 4. Decisiones Clave de Diseño

### 4.1 Modelo de Dominio Rico

Se ha optado por un **modelo de dominio rico** frente al modelo anémico. En un modelo anémico, las clases del dominio son meros contenedores de datos (getters/setters), y la lógica de negocio reside en servicios externos. En nuestro modelo, **las reglas de negocio viven en las propias clases del dominio**.

Ejemplos concretos:

#### 4.1.1 Límite de tarjetas por lista (`maxTarjetas`)

`Lista` almacena la capacidad máxima y la hace cumplir en su propio método `agregarTarjeta`:

```java
// Lista.java
public void agregarTarjeta(Tarjeta tarjeta) {
    if (tarjetas.size() >= maxTarjetas) {
        throw new ListaException(
            "La lista ha alcanzado su máximo de tarjetas (" + maxTarjetas + ")"
        );
    }
    tarjetas.add(tarjeta);
}
```

La invariante es local a la entidad: ningún servicio externo necesita verificarla.

#### 4.1.2 Validación de prerrequisitos al mover tarjetas

Antes de desplazar una tarjeta a una lista destino, la raíz del agregado verifica que la tarjeta ha pasado previamente por todas las listas requeridas:

```java
// Tablero.java
private void validarPrerequisitos(Tarjeta tarjeta, Lista destino) {
    List<ListaId> faltantes = destino.getListasRequeridas().stream()
            .filter(reqId -> !tarjeta.haPasadoPorLista(reqId))
            .collect(Collectors.toList());
    if (!faltantes.isEmpty()) {
        throw new TableroException(
            "La tarjeta '" + tarjeta.getTitulo() + "' no puede moverse a '"
            + destino.getNombre() + "' porque no ha pasado por las listas requeridas: "
            + nombresFaltantes
        );
    }
}
```

Esta lógica involucra tres objetos del dominio (`Tarjeta`, `Lista`, `Tablero`) y solo tiene sentido en el contexto del agregado completo; ubicarla en la raíz del agregado es la decisión correcta.

#### 4.1.3 Bloqueo de tablero

El estado `bloqueado` impide añadir nuevas tarjetas, pero no impide moverlas o completarlas. Esta distinción es una regla de negocio, no una lógica de presentación:

```java
// Tablero.java
public Tarjeta agregarTarjeta(ListaId listaId, Tarjeta tarjeta) {
    if (bloqueado)
        throw new TableroException(
            "El tablero esta bloqueado: no se pueden agregar tarjetas nuevas"
        );
    // ...
}
```

#### 4.1.4 Inmutabilidad de colecciones en getters

Los getters de entidades y de la raíz del agregado devuelven vistas no modificables mediante `Collections.unmodifiableList(...)`. Esto impide que el código externo mute el estado interno del agregado mediante una referencia a la colección:

```java
// Tablero.java
public List<Lista> getListas() {
    return Collections.unmodifiableList(listas);
}
```

Intentar invocar `tablero.getListas().add(...)` lanza `UnsupportedOperationException` en tiempo de ejecución.

#### 4.1.5 Historial de listas recorridas por una tarjeta

`Tarjeta` mantiene un `historialListas` que se actualiza cada vez que la tarjeta se registra en una nueva lista. Este estado interno permite implementar la lógica de prerrequisitos sin consultar una base de datos externa:

```java
// Tarjeta.java
public void registrarEnLista(ListaId listaId) {
    if (listaId != null && !historialListas.contains(listaId)) {
        historialListas.add(listaId);
    }
}

public boolean haPasadoPorLista(ListaId listaId) {
    return historialListas.contains(listaId);
}
```

#### 4.1.6 Immutabilidad de Value Objects mediante `record` y método de copia

`Tarea` (record) implementa sus mutaciones semánticas devolviendo nuevas instancias:

```java
// Tarea.java
public Tarea conEstado(EstadoTarea nuevoEstado) {
    return new Tarea(titulo, descripcion, fechaLimite, nuevoEstado);
}
```

Esto garantiza que compartir una referencia a `Tarea` nunca provoca efectos secundarios inesperados.

---

### 4.2 Arquitectura Hexagonal

Una decisión arquitectónica transversal y relevante en este proyecto es el **aislamiento total del dominio** respecto a cualquier infraestructura técnica.

**Regla estricta:** ninguna clase del paquete `domain/` puede importar ni depender de:
- Anotaciones de Spring Framework (`@Component`, `@Service`, `@Autowired`, etc.)
- Anotaciones de Jakarta Persistence / JPA (`@Entity`, `@Id`, `@Column`, etc.)
- Drivers de base de datos, frameworks HTTP, o cualquier otro adaptador técnico.

Esta regla se aplica siguiendo el patrón de **Arquitectura Hexagonal**:

```
┌─────────────────────────────────────────┐
│              DOMINIO                    │
│  (domain/modelo, domain/ports/input,    │
│   domain/ports/output)                  │
│  — sin Spring, sin JPA, sin HTTP —      │
└──────────────┬──────────────────────────┘
               │  Interfaces (Puertos de salida)
               │  TableroRepository, UsuarioRepository
               ▼
┌─────────────────────────────────────────┐
│           INFRAESTRUCTURA               │
│  (adapters/jpa, adapters/mappers)       │
│  TableroJpaAdapter, TableroMapper…      │
└─────────────────────────────────────────┘
```

**Puertos de entrada** (`domain/ports/input`): el dominio declara interfaces como `GestionTableroUseCase` que definen los casos de uso que la capa de aplicación debe implementar. Al vivir en el dominio, estas interfaces son independientes de cualquier framework.

**Puertos de salida** (`domain/ports/output`): el dominio declara interfaces como `TableroRepository` y `UsuarioRepository`, que especifican el contrato de persistencia sin mencionar ninguna tecnología concreta. Son el único punto de contacto entre el dominio y el mundo exterior.

**Adaptadores JPA** (`adapters/jpa`): la infraestructura implementa esas interfaces. Las entidades JPA (`TableroJpaEntity`, `ListaJpaEntity`, etc.) llevan todas las anotaciones de persistencia y viven fuera del dominio. Los `Mappers` convierten entre el modelo de dominio y el modelo JPA.

**Consecuencias positivas de esta decisión:**

- El dominio puede probarse con tests unitarios en memoria, sin levantar un contexto Spring ni una base de datos.
- Cambiar el motor de persistencia (H2 → PostgreSQL, JPA → MongoDB) no modifica ni una línea del dominio.
- El dominio comunica su semántica con claridad: un `Tablero` expresa conceptos de negocio, no esquemas de tablas.

---

## 5. Resumen del Lenguaje Ubicuo

| Término del negocio | Clase del dominio          | Tipo DDD         |
|---------------------|----------------------------|------------------|
| Tablero             | `Tablero`                  | Aggregate Root   |
| Lista               | `Lista`                    | Entidad          |
| Tarjeta             | `Tarjeta`                  | Entidad          |
| Tarea               | `Tarea`                    | Value Object     |
| Checklist           | `Checklist`                | Entidad local    |
| Ítem de checklist   | `ItemChecklist`            | Value Object     |
| Etiqueta            | `Etiqueta`                 | Value Object     |
| Fecha límite        | `FechaLimite`              | Value Object     |
| Traza / Historial   | `Traza`                    | Value Object     |
| Estado de tarea     | `EstadoTarea`              | Enumerado        |
| Usuario             | `Usuario`                  | Entidad          |
| Prerrequisito       | `Lista.listasRequeridas`   | Regla de negocio |
| Bloquear tablero    | `Tablero.bloqueado`        | Invariante       |
