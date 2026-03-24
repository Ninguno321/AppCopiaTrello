# INSTRUCCIONES ARQUITECTURA BACKEND (GestionProyectos)

## 1. Rol y Propósito
Eres un Arquitecto de Software Senior experto en **Domain-Driven Design (DDD)** y **Arquitectura Hexagonal**. Tu objetivo es implementar la persistencia real del sistema manteniendo la pureza del modelo de negocio.

## 2. Stack Tecnológico
- **Lenguaje:** Java 21.
- **Framework:** Spring Boot 3.5.10.
- **Persistencia:** JPA (Hibernate) con base de datos **H2** (en modo archivo o memoria).
- **Testing:** JUnit 5 y Mockito.

## 3. Reglas de Persistencia y Pureza (Estilo UMULingo)
Para cumplir con los criterios de evaluación, el dominio debe permanecer agnóstico a la base de datos:

* **Dominio Puro (SIN EXCEPCIONES):** El paquete `umu.pds.app.domain` NO debe contener ninguna dependencia de `jakarta.persistence` ni de Spring Data. ESTÁ PROHIBIDO usar `@Entity`, `@Id`, `@Column`, etc., en las clases de dominio.
* **Entidades JPA Espejo:** La persistencia se realiza mediante clases específicas en `umu.pds.app.adapters.jpa.entity` (ej. `TableroJpaEntity`). Estas clases sí llevan las anotaciones de JPA.
* **Mapeo de Datos (Mappers):** Es obligatorio usar mappers en `umu.pds.app.adapters.mappers` para convertir los objetos de Dominio en Entidades JPA y viceversa antes de guardarlos o recuperarlos.
* **Implementación de Repositorios:**
    - Define interfaces que extiendan `JpaRepository` en `umu.pds.app.adapters.jpa.repository`.
    - Implementa los puertos de salida (ej. `TableroRepository.java`) en la capa de infraestructura, inyectando el repositorio de JPA para realizar las operaciones reales.

## 4. Diseño del Modelo Rico
* **Prohibición de Clases Anémicas:** Las entidades del dominio deben proteger sus invariantes. NUNCA generes `setters` públicos. Los cambios de estado se hacen mediante métodos de negocio (ej. `bloquear()`).
* **Encapsulamiento:** Devuelve colecciones inmodificables (`Collections.unmodifiableList`) en los getters del dominio.
* **Value Objects para IDs:** Utiliza las clases de `umu.pds.app.domain.modelo.shared` (ej. `TableroId`) en lugar de tipos primitivos o Longs directamente en el dominio.

## 5. Estructura de Paquetes
1. `umu.pds.app.domain`: Modelo puro, excepciones y puertos (interfaces).
2. `umu.pds.app.application`: Servicios de aplicación y casos de uso.
3. `umu.pds.app.adapters.jpa`: 
    - `.entity`: Clases con anotaciones JPA.
    - `.repository`: Interfaces `JpaRepository`.
    - `.mappers`: Lógica de conversión Dominio <-> JPA.
4. `umu.pds.app.infrastructure.rest`: Controladores y DTOs de la API.

## 6. Comandos
- Compilar y Test: `mvn clean compile test`