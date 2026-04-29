Feature: Hola Mundo API

  Scenario: Llamar al endpoint pagina
    When el cliente llama al "/umu/pds/public/holaMundo"
    Then la respuesta es "Hola mundo"


#  Scenario: Llamar al endpoint holaMundo
#    When el cliente llama al "/umu/pds/public/holaMundo"
 #   Then la respuesta es "holaMundoParking"

    