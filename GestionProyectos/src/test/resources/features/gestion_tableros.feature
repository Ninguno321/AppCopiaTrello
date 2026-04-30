Feature: Gestión de Tableros

  Background:
    Given un tablero llamado "Sprint 1" creado por "bdd@test.com"

  Scenario: Camino Feliz — agregar lista y tarjeta con exito
    Given se agrega una lista llamada "Backlog" con capacidad 10
    And se agrega una tarjeta llamada "Primera tarea" a la lista "Backlog"
    Then el tablero tiene 1 lista
    And la lista "Backlog" contiene 1 tarjeta

  Scenario: Camino de Error — mover tarjeta sin prerequisito lanza excepcion
    Given se agrega una lista llamada "Pendiente" con capacidad 10
    And se agrega una lista llamada "En Revision" con capacidad 10
    And se agrega una lista llamada "Hecho" con capacidad 10
    And la lista "Hecho" requiere haber pasado por "En Revision"
    And se agrega una tarjeta llamada "Tarea bloqueada" a la lista "Pendiente"
    When se intenta mover la tarjeta "Tarea bloqueada" de "Pendiente" a "Hecho"
    Then se lanza una excepcion de prerequisito no cumplido
