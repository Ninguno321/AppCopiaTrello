# Historias de Usuario

Este documento recoge las Historias de Usuario (HU) que definen los requisitos funcionales y no funcionales del sistema de gestión de trabajo colaborativo.

## REQUISITOS FUNCIONALES

## 1. Gestión de Tableros y Acceso
* **HU-01:** Como usuario, quiero crear un tablero proporcionando mi correo electrónico para obtener una URL única donde organizar mi trabajo.
* **HU-02:** Como usuario, quiero poder compartir la URL de mi tablero con otras personas para que puedan colaborar conmigo en el mismo espacio.
* **HU-03:** Como usuario, quiero poder editar el nombre de un tablero existente desde la barra lateral, para mantener mis proyectos correctamente identificados si cambian de propósito.
* **HU-04:** Como usuario, quiero eliminar un tablero desde la barra lateral, para limpiar mi espacio de trabajo de proyectos finalizados o erróneos.
* **HU-05:** Como usuario, quiero importar un tablero completo desde un archivo YAML local, para agilizar la creación de proyectos utilizando estructuras predefinidas.

## 2. Gestión de Listas
* **HU-06:** Como usuario, quiero crear y modificar listas dentro de mi tablero para definir las diferentes fases o categorías de mi flujo de trabajo.

## 3. Gestión de Tarjetas
* **HU-07:** Como usuario, quiero añadir tarjetas a una lista para asignar tareas o anotar información relevante.
* **HU-08:** Como usuario, quiero poder elegir si mi nueva tarjeta es una "tarea simple" o un "checklist" para adaptar el formato al tipo de trabajo que necesito registrar.
* **HU-09:** Como usuario, quiero asignar etiquetas de colores a las tarjetas para clasificarlas e identificarlas visualmente con facilidad.
* **HU-10:** Como usuario, quiero mover las tarjetas entre las distintas listas del tablero para reflejar su avance o cambio de estado.
* **HU-11:** Como usuario, quiero marcar una tarjeta como completada para que pase a una lista especial de tarjetas completadas del tablero.

## 4. Control y Trazabilidad
* **HU-12:** Como usuario, quiero que el tablero registre automáticamente una historia (traza) de todas las acciones, como por ejemplo cuándo se mueve una tarjeta, para saber qué ha ocurrido en el proyecto.
* **HU-13:** Como administrador/dueño del tablero, quiero poder bloquear temporalmente el tablero para que no se puedan añadir tarjetas nuevas, permitiendo únicamente mover las ya existentes.

## 5. Características Opcionales
* **HU-14:** Como usuario, quiero poder configurar un límite máximo (N) de tarjetas permitidas en una lista, para evitar cuellos de botella en mi flujo de trabajo.
* **HU-15:** Como usuario, quiero definir que una lista exija que las tarjetas hayan pasado por otra(s) lista(s) previamente, para garantizar que se sigue un proceso ordenado antes de avanzar.
* **HU-16:** Como usuario, quiero poder filtrar las tarjetas de mi tablero seleccionando una o varias etiquetas, para visualizar rápidamente solo la información que me interesa en ese momento.

## REQUISITOS NO FUNCIONALES

* **HU-17:** Como arquitecto de software, quiero aplicar Domain-Driven Design (DDD) en el backend, para asegurar un dominio rico, fuertemente encapsulado y tolerante a cambios.
* **HU-18:** Como usuario, quiero que la interfaz responda al instante al realizar acciones, para no sentir lag ni cuelgues mientras la aplicación se comunica con el servidor.
* **HU-19:** Como desarrollador, quiero desacoplar los controladores visuales de JavaFX de las llamadas HTTP, para respetar el Principio de Responsabilidad Única y facilitar futuros tests.
* **HU-20:** Como usuario, quiero que los mensajes de alerta o prompts no oculten la aplicación principal, para mantener mi contexto visual de trabajo.
