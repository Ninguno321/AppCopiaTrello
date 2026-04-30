package umu.pds.app.domain.modelo.tablero;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import umu.pds.app.domain.exceptions.ListaException;
import umu.pds.app.domain.modelo.shared.ListaId;
import umu.pds.app.domain.modelo.shared.TarjetaId;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ListaTest {

    private Lista lista;

    @BeforeEach
    void setUp() {
        lista = Lista.nueva("Por Hacer", 2);
    }

    // =========================================================
    // BVA — maxTarjetas
    // =========================================================

    @Nested
    @DisplayName("BVA maxTarjetas")
    class MaxTarjetasTest {

        @Test
        @DisplayName("Agregar una tarjeta con espacio disponible tiene exito (BVA n-1)")
        void agregarTarjeta_DentroDelLimite_Exito() {
            lista.agregarTarjeta(Tarjeta.nueva("Tarea 1"));

            assertEquals(1, lista.totalTarjetas());
        }

        @Test
        @DisplayName("Agregar la tarjeta que ocupa el ultimo hueco tiene exito (BVA n)")
        void agregarTarjeta_EnElLimiteExacto_Exito() {
            lista.agregarTarjeta(Tarjeta.nueva("Tarea 1"));
            lista.agregarTarjeta(Tarjeta.nueva("Tarea 2"));

            assertEquals(2, lista.totalTarjetas());
        }

        @Test
        @DisplayName("Agregar una tarjeta por encima del limite lanza ListaException (BVA n+1)")
        void agregarTarjeta_SuperandoElLimite_LanzaExcepcion() {
            lista.agregarTarjeta(Tarjeta.nueva("Tarea 1"));
            lista.agregarTarjeta(Tarjeta.nueva("Tarea 2"));

            assertThrows(ListaException.class,
                    () -> lista.agregarTarjeta(Tarjeta.nueva("Tarea 3")));
        }
    }

    // =========================================================
    // Prerequisitos
    // =========================================================

    @Nested
    @DisplayName("Prerequisitos")
    class PrerequisitosTest {

        @Test
        @DisplayName("configurarListasRequeridas sustituye por completo la lista anterior")
        void configurarListasRequeridas_ReemplazaCorrectamente() {
            ListaId idA = ListaId.nuevo();
            lista.configurarListasRequeridas(List.of(idA));
            assertEquals(1, lista.getListasRequeridas().size());
            assertTrue(lista.getListasRequeridas().contains(idA));

            ListaId idB = ListaId.nuevo();
            ListaId idC = ListaId.nuevo();
            lista.configurarListasRequeridas(List.of(idB, idC));

            assertEquals(2, lista.getListasRequeridas().size());
            assertFalse(lista.getListasRequeridas().contains(idA));
            assertTrue(lista.getListasRequeridas().contains(idB));
            assertTrue(lista.getListasRequeridas().contains(idC));
        }

        @Test
        @DisplayName("tienePrerequisitos devuelve false cuando no hay listas requeridas")
        void tienePrerequisitos_RetornaFalso_CuandoListaVacia() {
            assertFalse(lista.tienePrerequisitos());
        }

        @Test
        @DisplayName("tienePrerequisitos devuelve true cuando hay al menos una lista requerida")
        void tienePrerequisitos_RetornaVerdadero_CuandoHayPrerequisitos() {
            lista.configurarListasRequeridas(List.of(ListaId.nuevo()));

            assertTrue(lista.tienePrerequisitos());
        }
    }

    // =========================================================
    // Construccion
    // =========================================================

    @Nested
    @DisplayName("Construccion")
    class ConstruccionTest {

        @Test
        @DisplayName("Lanza ListaException si el nombre es nulo")
        void crearListaNombreNuloLanzaExcepcion() {
            assertThrows(ListaException.class,
                    () -> new Lista(ListaId.nuevo(), null, 5));
        }

        @Test
        @DisplayName("Lanza ListaException si el nombre esta en blanco")
        void crearListaNombreBlankLanzaExcepcion() {
            assertThrows(ListaException.class,
                    () -> new Lista(ListaId.nuevo(), "   ", 5));
        }

        @Test
        @DisplayName("Lanza ListaException si el id es nulo")
        void crearListaIdNuloLanzaExcepcion() {
            assertThrows(ListaException.class,
                    () -> new Lista(null, "Backlog", 5));
        }
    }

    // =========================================================
    // Mutaciones directas
    // =========================================================

    @Nested
    @DisplayName("Mutaciones directas")
    class MutacionesTest {

        @Test
        @DisplayName("renombrar cambia el nombre correctamente")
        void renombrarListaValido() {
            lista.renombrar("En Progreso");

            assertEquals("En Progreso", lista.getNombre());
        }

        @Test
        @DisplayName("renombrar con null lanza ListaException")
        void renombrarListaNuloLanzaExcepcion() {
            assertThrows(ListaException.class,
                    () -> lista.renombrar(null));
        }

        @Test
        @DisplayName("buscarTarjeta devuelve la tarjeta si existe")
        void buscarTarjetaEncontrada() {
            Tarjeta tarjeta = Tarjeta.nueva("Fix bug");
            lista.agregarTarjeta(tarjeta);

            assertTrue(lista.buscarTarjeta(tarjeta.getId()).isPresent());
        }

        @Test
        @DisplayName("buscarTarjeta devuelve Optional vacio si no existe")
        void buscarTarjetaNoEncontrada() {
            assertTrue(lista.buscarTarjeta(TarjetaId.nuevo()).isEmpty());
        }

        @Test
        @DisplayName("eliminarTarjeta devuelve true y reduce el total de tarjetas")
        void eliminarTarjetaExistente() {
            Tarjeta tarjeta = Tarjeta.nueva("A eliminar");
            lista.agregarTarjeta(tarjeta);

            boolean eliminada = lista.eliminarTarjeta(tarjeta.getId());

            assertTrue(eliminada);
            assertEquals(0, lista.totalTarjetas());
        }
    }
}
