# Créditos del Proyecto 

En este fichero se desglosa de forma resumida el reparto del trabajo y la participación de todos los componentes en el desarrollo de la aplicación.

---

## Alberto

Encargado principalmente de la arquitectura de la aplicación y también responsable de la calidad del código. Diseñó y mantuvo la arquitectura hexagonal del backend (GestionProyectos), asegurando la correcta separación entre dominio, aplicación e infraestructura conforme a los principios DDD. Implementó la capa de persistencia completa con JPA/H2 (entidades espejo, mappers y adaptadores de repositorio), así como la API REST del backend. Fue también el encargado del aseguramiento de la calidad, identificando y resolviendo bugs críticos y code smells detectados mediante SonarQube. Además, fue también el encargado de desarrollar una suite de pruebas exhaustiva que abarca tests unitarios con análisis de valores límite (BVA), inyección de dependencias falsas y pruebas de aceptación con Cucumber BDD.

### Evidencias de participación

- **[4199431]**: Implementación persistencia JPA, lógica de dominio en Tablero y actualización API REST (Archivos clave: `TableroJpaEntity.java`, `TableroMapper.java`, `TableroJpaAdapter.java`, `UsuarioJpaEntity.java`).
- **[fc9b292]**: Implementadas nuevas pruebas de software y mejoradas las que había (Archivos clave: `TableroServiceTest.java`, `ChecklistTest.java`, `FechaLimiteTest.java`, `ListaTest.java`, `TarjetaTest.java`, `gestion_tableros.feature`).
- **[604dc7a]**: Fixed bugs críticos detectados con SonarQube (Archivos clave: `TableroService.java`, `Tablero.java`, `TableroTest.java`).
- **[eb1c9da]**: Refactor: limpieza DDD, excepciones de dominio a unchecked y eliminación de firmas throws innecesarias (Archivos clave: `CheckListException.java`, `ListaException.java`, `TableroException.java`, `Checklist.java`, `Tarjeta.java`).
- **[ea18293]**: Correcciones de arquitectura hexagonal — reubicación de adaptadores JPA `adapters.jpa.repository` (Archivos clave: `TableroJpaAdapter.java`, `UsuarioJpaAdapter.java`, `ListaJpaEntity.java`).

---

## Andrey

Andrey fue el responsable de la configuración inicial del proyecto y de las bases del sistema. Creó el repositorio, estableció la estructura del proyecto Maven, configuró la base de datos provisional con H2 y el esquema SQL inicial, e integró OpenAPI/Swagger para la documentación de la API. En la capa de presentación, construyó la estructura base de la interfaz gráfica JavaFX (ventanas, FXML, CSS y navegación), e integró la API de inteligencia artificial conversacional Groq, dotando a la aplicación de un chat contextual por tablero. Asimismo, diseñó y ejecutó las pruebas de carga con Apache JMeter.

### Evidencias de participación

- **[346653a]**: Initial commit — creación del repositorio y estructura inicial del proyecto Maven multi-módulo.
- **[4ed8a50]**: Base de datos y tablas provisionales — configuración de H2 y creación del `schema.sql` inicial (`application.properties`, `schema.sql`).
- **[2a4975d]**: Integración IA — primera integración de la API de Groq, incluyendo el fichero `application-secret.properties` con la clave de API (`ApiGroqTest.java`).
- **[45ced48]**: Chat de la IA agregado, contesta sobre el tablero donde se abrió — integración del chat contextual por tablero en la UI (`VentanaChatController.java`, `chat.fxml`, `VentanaPrincipal.fxml`).
- **[d485104]**: Prueba test de carga con hola Mundo — añadido el plan de pruebas JMeter (`TestCargaBack.jmx`).

---

## Luis

Luis se especializó en el desarrollo de funcionalidades avanzadas de negocio y en la mejora de la experiencia de usuario. Implementó las dos reglas de negocio más complejas del sistema: el límite configurable de tarjetas máximas por lista (`maxTarjetas`) y el sistema de prerrequisitos entre listas, que impone que una tarjeta deba haber transitado por determinadas listas antes de poder avanzar. Desarrolló además la funcionalidad de importación y exportación de tableros mediante ficheros YAML, y los filtros dinámicos de búsqueda en la interfaz gráfica (por etiqueta y por título), con actualización en tiempo real. Contribuyó también a la creación de la vista de tabla y a la corrección de bugs de navegación en la UI.

### Evidencias de participación

- **[84be4f3]**: Añadido número de tarjetas máximas por lista configurable — implementación de la regla `maxTarjetas` en dominio, servicio, API REST y UI (`Lista.java`, `ListaJpaEntity.java`, `TableroService.java`, `AgregarListaRequest.java`, `VentanaListaController.java`).
- **[a1a0ae1]**: Añadido prerequisito a las listas: las tarjetas deben de haber pasado por x listas antes — implementación completa del sistema de prerrequisitos en dominio, persistencia y UI (`Lista.java`, `Tarjeta.java`, `ConfigurarPrerequisitosRequest.java`, `VentanaTarjetaController.java`).
- **[ad1de19]**: Nuevo: exportar tableros a `.yaml` — funcionalidad de exportación de tableros desde la interfaz gráfica (`VentanaPrincipalController.java`).
- **[48bdb4b]**: Filtros funcionando ahora sí a tiempo real y con el título también — filtros dinámicos de tarjetas por etiqueta y título en tiempo real (`VentanaListaController.java`, `VentanaTableroController.java`, `VentanaTarjetaController.java`).
- **[6f0d590]**: Vista de tabla creada — nueva vista alternativa de tablero en formato tabla (`VentanaTablaController.java`, `VentanaTabla.fxml`, `app-board.css`).
