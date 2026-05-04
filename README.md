# MisTableros.com

> Plataforma de gestión colaborativa de proyectos inspirada en Trello, desarrollada con **Arquitectura Hexagonal (DDD)** en Java y Spring Boot.

## Participantes

| Nombre | Correo electrónico |
|--------|-------------------|
| Alberto Domingo López | a.domingolopez@um.es |
| Andrey Santiago Morales | asantiago.morales@um.es |
| Luis Estival Cantó | luis.estivalc@um.es |

## Descripción

**MisTableros.com** es una aplicación de gestión de proyectos basada en el modelo de tableros Kanban. Permite organizar el trabajo mediante tableros, listas y tarjetas, ofreciendo un conjunto de reglas de negocio que garantiza la integridad y trazabilidad de los proyectos.

El sistema ha sido diseñado siguiendo los principios de **Domain-Driven Design (DDD)** con arquitectura hexagonal, asegurando que la lógica de negocio permanece completamente independiente de la infraestructura.

## Stack Tecnológico

| Tecnología | Versión | Rol |
|------------|---------|-----|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.5 | Framework de aplicación |
| Spring Data JPA / H2 | — | Persistencia (capa de infraestructura) |
| Arquitectura Hexagonal | DDD | Patrón estructural del proyecto |
| JUnit 5 | — | Tests unitarios del dominio |
| Mockito | — | Mocking en tests de servicios de aplicación |
| Cucumber | 7.14 | Tests de aceptación BBDD |
| JaCoCo | 0.8 | Cobertura de código |
| OpenAPI / Swagger | 2.8 | Documentación de la API REST |

## Estructura del Proyecto

```
AppCopiaTrello/
├── GestionProyectos/          # Backend: dominio + aplicación + infraestructura
│   └── src/main/java/umu/pds/app/
│       ├── domain/            # Núcleo de negocio puro (sin Spring, sin JPA)
│       │   ├── modelo/        # Entidades, Value Objects, Aggregate Root
│       │   ├── exceptions/    # Excepciones tipadas del dominio
│       │   └── ports/         # Interfaces de entrada y salida
│       ├── application/       # Casos de uso y servicios de aplicación
│       └── adapters/          # Adaptadores JPA y REST
├── gestion_proyectos_ui/      # Frontend JavaFX (módulo independiente)
└── documentacion/             # Documentación técnica y de requisitos
```
---

## Características Principales

### Gestión de Tableros, Listas y Tarjetas

El núcleo de la aplicación permite crear y organizar el trabajo en tres niveles jerárquicos:

- **Tableros**: raíz del agregado del dominio. Cada tablero tiene un propietario, un nombre y agrupa todas las listas del proyecto.
- **Listas**: columnas de trabajo dentro de un tablero (p. ej., *Por hacer*, *En curso*, *Hecho*). Pueden ser renombradas en cualquier momento.
- **Tarjetas**: unidades de trabajo dentro de una lista. Tienen título, descripción y un conjunto amplio de propiedades opcionales.

### Sistema de prerrequisitos entre Listas

Cada lista puede configurarse para exigir que una tarjeta haya pasado previamente por una o más listas concretas antes de poder ser movida a ella. El sistema valida esta restricción automáticamente en cada operación de movimiento, rechazando transferencias que no cumplan el flujo de trabajo definido.

```
Tablero.configurarListasRequeridas(listaId, [listaA, listaB])
Tablero.moverTarjeta(tarjetaId, origen, destino)  // valida prerrequisitos
```

### Límites de capacidad en Listas

Al crear una lista es posible definir un número máximo de tarjetas (`maxTarjetas`). El dominio impide añadir nuevas tarjetas a una lista que ha alcanzado su límite, aplicando la restricción de *WIP limit* típica de los sistemas Kanban.

### Checklists con seguimiento de completado

Las tarjetas pueden llevar asociado un **Checklist** de ítems verificables. El sistema ofrece:

- Añadir, eliminar y reordenar ítems por índice.
- Marcar y desmarcar cada ítem individualmente.
- Consultar el número de ítems completados (`itemsCompletados()`) y si la checklist está al 100% (`estaCompleto()`).

### Gestión de fechas límite

Las tarjetas aceptan una **Fecha Límite** como Value Object tipado (`FechaLimite`). La fecha es validada en el momento de creación para que no sea pasada, garantizando que solo se asignan plazos vigentes. Existe un método separado (`reconstitute`) para reconstruir fechas desde persistencia sin reejecutar la validación temporal.

### Sistema de etiquetas

Las tarjetas admiten múltiples **Etiquetas** identificadas por nombre y color. El dominio previene etiquetas duplicadas y permite añadir y quitar etiquetas de forma independiente. Las etiquetas son Value Objects inmutables con igualdad por valor.

### Bloqueo de tableros

Un tablero puede ser **bloqueado** explícitamente, lo que impide añadir nuevas tarjetas sin bloquear las operaciones en curso (movimiento, completado, etiquetado). El desbloqueo restaura el comportamiento normal.

### Historial de trazabilidad

Cada operación relevante sobre un tablero (agregar listas, mover tarjetas, completar elementos, renombrar, bloquear) queda registrada automáticamente en un **historial de trazas** (`List<Traza>`). Cada traza contiene una descripción y un timestamp, proporcionando un log completo sin dependencias externas.

### Tareas como valor de dominio

Las tarjetas pueden llevar asociada una **Tarea** (Value Object inmutable) con su propio título, descripción, fecha límite y estado (`PENDIENTE`, `EN_PROGRESO`, `COMPLETADA`). Las transiciones de estado se realizan mediante métodos que devuelven nuevas instancias, preservando la inmutabilidad.

---

## Documentación

- **Créditos y participación**: [`CREDITOS.md`](./CREDITOS.md)
  Detalla la contribución de cada miembro del equipo.

- **Modelo DDD y decisiones de diseño**: [`documentacion/diseño/ModeloDDD.md`](./documentacion/diseño/ModeloDDD.md)
  Documenta el modelo de dominio completo: agregado, entidades, Value Objects, reglas de invarianza y las decisiones de diseño que guían la arquitectura del sistema.

---

## Ejecución

```bash
# Clonar el repositorio
git clone https://github.com/<org>/AppCopiaTrello.git
cd AppCopiaTrello

# Compilar y ejecutar tests
mvn clean verify

# Arrancar el backend
cd GestionProyectos
mvn spring-boot:run
```

La API REST estará disponible en `http://localhost:8080` y la documentación Swagger en `http://localhost:8080/swagger-ui.html`.
