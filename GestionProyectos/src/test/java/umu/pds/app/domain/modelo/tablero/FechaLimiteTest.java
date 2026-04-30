package umu.pds.app.domain.modelo.tablero;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FechaLimiteTest {

    // =========================================================
    // nuevo(LocalDate) — BVA temporal
    // =========================================================

    @Nested
    @DisplayName("BVA nuevo(LocalDate)")
    class NuevoTest {

        @Test
        @DisplayName("Fecha de hoy es valida (isBefore = false, BVA n=0)")
        void fechaHoyEsValida() {
            assertDoesNotThrow(() -> FechaLimite.nuevo(LocalDate.now()));
        }

        @Test
        @DisplayName("Fecha de ayer lanza FechaLimiteException (BVA n-1)")
        void fechaAyerLanzaExcepcion() {
            assertThrows(FechaLimite.FechaLimiteException.class,
                    () -> FechaLimite.nuevo(LocalDate.now().minusDays(1)));
        }

        @Test
        @DisplayName("Fecha de manana es valida (BVA n+1)")
        void fechaMananaEsValida() {
            assertDoesNotThrow(() -> FechaLimite.nuevo(LocalDate.now().plusDays(1)));
        }

        @Test
        @DisplayName("Fecha nula lanza FechaLimiteException")
        void fechaNulaLanzaExcepcion() {
            assertThrows(FechaLimite.FechaLimiteException.class,
                    () -> FechaLimite.nuevo(null));
        }
    }

    // =========================================================
    // de(String) — ramas huerfanas
    // =========================================================

    @Nested
    @DisplayName("de(String)")
    class DeStringTest {

        @Test
        @DisplayName("String nulo lanza FechaLimiteException (rama null)")
        void de_StringNuloLanzaExcepcion() {
            assertThrows(FechaLimite.FechaLimiteException.class,
                    () -> FechaLimite.de(null));
        }

        @Test
        @DisplayName("String malformado lanza FechaLimiteException (rama DateTimeParseException)")
        void de_StringMalformadoLanzaExcepcion() {
            assertThrows(FechaLimite.FechaLimiteException.class,
                    () -> FechaLimite.de("no-es-fecha"));
        }

        @Test
        @DisplayName("String con fecha futura valida crea FechaLimite correctamente")
        void de_StringValidoCreaFechaLimite() {
            String manana = LocalDate.now().plusDays(1).toString();
            FechaLimite resultado = FechaLimite.de(manana);
            assertEquals(LocalDate.now().plusDays(1), resultado.date());
        }
    }

    // =========================================================
    // reconstitute — contrato de persistencia
    // =========================================================

    @Nested
    @DisplayName("reconstitute")
    class ReconstituteTest {

        @Test
        @DisplayName("Permite reconstruir una fecha pasada sin excepcion (contrato de persistencia)")
        void reconstitutePermiteFechaPasada() {
            LocalDate pasado = LocalDate.now().minusDays(30);
            assertDoesNotThrow(() -> FechaLimite.reconstitute(pasado));
        }

        @Test
        @DisplayName("reconstitute con null lanza FechaLimiteException")
        void reconstituteNuloLanzaExcepcion() {
            assertThrows(FechaLimite.FechaLimiteException.class,
                    () -> FechaLimite.reconstitute(null));
        }
    }
}
