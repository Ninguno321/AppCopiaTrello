# Glosario de Lenguaje Ubicuo

Este documento define los conceptos centrales del dominio de nuestra aplicación de gestión de proyectos, asegurando un vocabulario común entre el equipo de desarrollo.

| CONCEPTO | DESCRIPCIÓN | TIPO EN DDD | JUSTIFICACIÓN |
| **Tablero** | Espacio principal de trabajo colaborativo. | Entidad (Raíz de Agregado) | Tiene una identidad única a través de su URL. Es la entidad central que coordina el bloqueo y mantiene la historia de las acciones. |
| **Lista** | Contenedor ordenado para varias tarjetas. | Entidad | Pertenece a un tablero en el que se pueden añadir listas. Contiene tarjetas y necesita una identidad para que el sistema sepa entre qué listas se mueven. |
| **Tarjeta** | Anotación de información o trabajo. | Entidad | Sirven para asignar tareas o anotar información. Su estado cambia al marcarse como completadas o moverse. Requieren un ciclo de vida propio. Actúan como el contenedor principal. |
| **Tarea** | Trabajo simple a ser realizado. | Objeto de valor | Es el contenido de un tipo especifico de tarjeta. No tiene sentido ni identidad fuera de la tarjeta que la contiene. |
| **Checklist** | Lista de elementos a verificar. Entidad (Local al agregado) | Un tipo de tarjeta contiene checklists. Como sus elementos internos se irán marcando/desmarcando con el tiempo, su estado interno cambia, por lo que actúa como entidad dentro del agregado de la tarjeta. |
| **Ítem de Checklist** | Elemento verificable de una checklist. | Entidad (Local al agregado) | Es cada una de las tareas dentro de un Checklist. Tiene estado propio (completado/no completado) que cambia en el tiempo, pero no tiene sentido fuera de su Checklist. |
| **Etiqueta** | Clasificador de tarjetas con color. | Objeto de Valor | Las tarjetas tienen etiquetas de diferentes colores para poder identificarlas fácilmente. No necesitan un ID propio; si dos etiquetas son rojas y dicen lo mismo, son intercambiables. |
| **Usuario** | Persona identificada por correo electrónico. | Entidad | Un usuario puede crear un tablero simplemente indicando su dirección de correo electrónico. Conserva su identidad para la gestión de permisos y la compartición de URLs. |
| **Traza** | Registro histórico de una acción. | Objeto de Valor | El sistema debe registrar la historia de todas las acciones de los usuarios, como el movimiento de las tarjetas. Es un hecho inmutable que ocurrió en el pasado. |