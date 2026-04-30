package umu.pds.app.domain.modelo.tablero;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import umu.pds.app.domain.exceptions.TarjetaException;
import umu.pds.app.domain.modelo.shared.ListaId;
import umu.pds.app.domain.modelo.shared.TarjetaId;

import static org.junit.jupiter.api.Assertions.*;

class TarjetaTest {

    private Tarjeta tarjeta;

    @BeforeEach
    void setUp() {
        tarjeta = Tarjeta.nueva("Tarea de prueba");
    }

    // =========================================================
    // Construccion
    // =========================================================

    @Nested
    @DisplayName("Construccion")
    class ConstruccionTest {

        @Test
        @DisplayName("Lanza TarjetaException si el titulo es nulo")
        void crearTarjetaTituloNuloLanzaExcepcion() {
            assertThrows(TarjetaException.class,
                    () -> new Tarjeta(TarjetaId.nuevo(), null));
        }

        @Test
        @DisplayName("Lanza TarjetaException si el id es nulo")
        void crearTarjetaIdNuloLanzaExcepcion() {
            assertThrows(TarjetaException.class,
                    () -> new Tarjeta(null, "Titulo"));
        }
    }

    // =========================================================
    // Etiquetas — ramas huerfanas
    // =========================================================

    @Nested
    @DisplayName("Gestion de Etiquetas")
    class EtiquetasTest {

        @Test
        @DisplayName("Asignar la misma etiqueta dos veces no genera duplicados (rama !contains = false)")
        void asignarEtiquetaDuplicadaNoSeAgrega() {
            Etiqueta etiqueta = Etiqueta.de("Bug", "rojo");

            tarjeta.asignarEtiqueta(etiqueta);
            tarjeta.asignarEtiqueta(etiqueta);

            assertEquals(1, tarjeta.getEtiquetas().size());
        }

        @Test
        @DisplayName("getEtiquetas devuelve lista inmodificable")
        void getEtiquetasInmodificable() {
            assertThrows(UnsupportedOperationException.class,
                    () -> tarjeta.getEtiquetas().add(Etiqueta.de("X", "azul")));
        }

        @Test
        @DisplayName("Lanza TarjetaException si el checklist es nulo")
        void asignarChecklistNuloLanzaExcepcion() {
            assertThrows(TarjetaException.class,
                    () -> tarjeta.asignarChecklist(null));
        }

        @Test
        @DisplayName("Lanza TarjetaException si la tarea es nula")
        void asignarTareaNulaLanzaExcepcion() {
            assertThrows(TarjetaException.class,
                    () -> tarjeta.asignarTarea(null));
        }
    }

    // =========================================================
    // Historial de listas — ramas huerfanas
    // =========================================================

    @Nested
    @DisplayName("Historial de listas")
    class HistorialListasTest {

        @Test
        @DisplayName("Registrar la misma lista dos veces no genera duplicados (rama contains = true)")
        void registrarEnListaMismaListaNoSeDuplica() {
            ListaId id = ListaId.nuevo();

            tarjeta.registrarEnLista(id);
            tarjeta.registrarEnLista(id);

            assertEquals(1, tarjeta.getHistorialListas().size());
        }

        @Test
        @DisplayName("Registrar null no lanza excepcion y el historial permanece vacio (rama null)")
        void registrarEnListaNulaNoFalla() {
            assertDoesNotThrow(() -> tarjeta.registrarEnLista(null));
            assertTrue(tarjeta.getHistorialListas().isEmpty());
        }

        @Test
        @DisplayName("haPasadoPorLista devuelve true tras registrar y false si no se registro")
        void haPasadoPorListaVerdaderoYFalso() {
            ListaId registrada = ListaId.nuevo();
            ListaId noRegistrada = ListaId.nuevo();

            tarjeta.registrarEnLista(registrada);

            assertTrue(tarjeta.haPasadoPorLista(registrada));
            assertFalse(tarjeta.haPasadoPorLista(noRegistrada));
        }
    }
}
