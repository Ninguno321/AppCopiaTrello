package umu.pds.app.domain.modelo.tablero;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import umu.pds.app.domain.exceptions.ChecklistException;
import umu.pds.app.domain.exceptions.ChecklistIndiceException;

import static org.junit.jupiter.api.Assertions.*;

class ChecklistTest {

    private Checklist checklist;

    @BeforeEach
    void setUp() {
        checklist = Checklist.nuevo("Verificacion");
    }

    // =========================================================
    // Construccion
    // =========================================================

    @Nested
    @DisplayName("Construccion")
    class ConstruccionTest {

        @Test
        @DisplayName("Lanza ChecklistException si el nombre es nulo")
        void crearChecklistNombreNuloLanzaExcepcion() {
            assertThrows(ChecklistException.class,
                    () -> Checklist.nuevo(null));
        }

        @Test
        @DisplayName("Lanza ChecklistException si el nombre esta en blanco")
        void crearChecklistNombreBlankLanzaExcepcion() {
            assertThrows(ChecklistException.class,
                    () -> Checklist.nuevo("   "));
        }
    }

    // =========================================================
    // estaCompleto — tres ramas del &&
    // =========================================================

    @Nested
    @DisplayName("estaCompleto")
    class EstaCompletoTest {

        @Test
        @DisplayName("Lista vacia devuelve false (rama !isEmpty() = false)")
        void estaCompletoConListaVacia_retornaFalse() {
            assertFalse(checklist.estaCompleto());
        }

        @Test
        @DisplayName("Items completados parcialmente devuelve false (rama count != size)")
        void estaCompletoConItemsParciales_retornaFalse() {
            checklist.agregarItem("Paso 1");
            checklist.agregarItem("Paso 2");
            checklist.marcarItem(0);

            assertFalse(checklist.estaCompleto());
        }

        @Test
        @DisplayName("Todos los items completados devuelve true")
        void estaCompletoConTodosCompletados_retornaTrue() {
            checklist.agregarItem("Paso 1");
            checklist.agregarItem("Paso 2");
            checklist.marcarItem(0);
            checklist.marcarItem(1);

            assertTrue(checklist.estaCompleto());
        }
    }

    // =========================================================
    // BVA — validarIndice
    // =========================================================

    @Nested
    @DisplayName("BVA validarIndice")
    class IndiceTest {

        @Test
        @DisplayName("Marcar el indice size-1 (ultimo valido) tiene exito (BVA n)")
        void marcarItemIndiceExactoUltimo() {
            checklist.agregarItem("A");
            checklist.agregarItem("B");
            checklist.agregarItem("C");

            assertDoesNotThrow(() -> checklist.marcarItem(2));
            assertTrue(checklist.getItems().get(2).completado());
        }

        @Test
        @DisplayName("Marcar el indice size lanza ChecklistIndiceException (BVA n+1)")
        void marcarItemIndiceSizeLanzaExcepcion() {
            checklist.agregarItem("A");
            checklist.agregarItem("B");
            checklist.agregarItem("C");

            assertThrows(ChecklistIndiceException.class,
                    () -> checklist.marcarItem(3));
        }

        @Test
        @DisplayName("Marcar el indice 0 con un solo item tiene exito (BVA limite inferior)")
        void marcarItemIndiceInferiorValido() {
            checklist.agregarItem("Unico");

            assertDoesNotThrow(() -> checklist.marcarItem(0));
            assertTrue(checklist.getItems().get(0).completado());
        }
    }
}
