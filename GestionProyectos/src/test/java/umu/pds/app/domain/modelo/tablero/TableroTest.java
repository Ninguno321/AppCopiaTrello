package umu.pds.app.domain.modelo.tablero;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import umu.pds.app.domain.exceptions.TableroException;
import umu.pds.app.domain.modelo.shared.ListaId;
import umu.pds.app.domain.modelo.shared.TableroId;
import umu.pds.app.domain.modelo.shared.TarjetaId;
import umu.pds.app.domain.modelo.usuario.Usuario;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TableroTest {

    private Usuario propietario;
    private Tablero tablero;

    @BeforeEach
    void setUp() {
        propietario = new Usuario("owner@test.com");
        tablero = new Tablero("Sprint 1", propietario);
    }

    // =========================================================
    // Construccion
    // =========================================================

    @Nested
    @DisplayName("Construccion")
    class ConstruccionTest {

        @Test
        @DisplayName("Crea tablero con id, nombre y propietario correctos")
        void crearTableroValido() {
            assertNotNull(tablero.getId());
            assertEquals("Sprint 1", tablero.getNombre());
            assertEquals(propietario, tablero.getPropietario());
            assertEquals("owner@test.com", tablero.getEmailPropietario());
            assertFalse(tablero.isBloqueado());
            assertTrue(tablero.getListas().isEmpty());
            assertTrue(tablero.getTarjetasCompletadas().isEmpty());
            assertTrue(tablero.getHistorial().isEmpty());
        }

        @Test
        @DisplayName("Constructor con TableroId explicito conserva el id")
        void crearTableroConIdExplicito() {
            TableroId id = TableroId.nuevo();
            Tablero t = new Tablero(id, "Mi Tablero", propietario);
            assertEquals(id, t.getId());
        }

        @Test
        @DisplayName("Lanza excepcion si id es nulo")
        void crearTableroIdNuloLanzaExcepcion() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Tablero(null, "Nombre", propietario));
        }

        @Test
        @DisplayName("Lanza excepcion si nombre es nulo")
        void crearTableroNombreNuloLanzaExcepcion() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Tablero(null, propietario));
        }

        @Test
        @DisplayName("Lanza excepcion si nombre esta en blanco")
        void crearTableroNombreBlankLanzaExcepcion() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Tablero("  ", propietario));
        }

        @Test
        @DisplayName("Lanza excepcion si propietario es nulo")
        void crearTableroPropietarioNuloLanzaExcepcion() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Tablero("Nombre", null));
        }
    }

    // =========================================================
    // Renombrar
    // =========================================================

    @Nested
    @DisplayName("Renombrar")
    class RenombrarTest {

        @Test
        @DisplayName("Renombrar cambia el nombre del tablero")
        void renombrarCambioNombre() {
            tablero.renombrar("Sprint 2");
            assertEquals("Sprint 2", tablero.getNombre());
        }

        @Test
        @DisplayName("Lanza excepcion si nuevo nombre es nulo")
        void renombrarNuloLanzaExcepcion() {
            assertThrows(IllegalArgumentException.class,
                    () -> tablero.renombrar(null));
        }

        @Test
        @DisplayName("Lanza excepcion si nuevo nombre esta en blanco")
        void renombrarBlankLanzaExcepcion() {
            assertThrows(IllegalArgumentException.class,
                    () -> tablero.renombrar("  "));
        }
    }

    // =========================================================
    // Bloquear / Desbloquear
    // =========================================================

    @Nested
    @DisplayName("Bloquear y Desbloquear")
    class BloquearTest {

        @Test
        @DisplayName("Bloquear cambia el estado a bloqueado y registra traza")
        void bloquearRegistraTraza() {
            tablero.bloquear();
            assertTrue(tablero.isBloqueado());
            assertEquals(1, tablero.getHistorial().size());
            assertTrue(tablero.getHistorial().get(0).descripcion().contains("bloqueado"));
        }

        @Test
        @DisplayName("Desbloquear cambia el estado a no bloqueado y registra traza")
        void desbloquearRegistraTraza() {
            tablero.bloquear();
            tablero.desbloquear();
            assertFalse(tablero.isBloqueado());
            assertEquals(2, tablero.getHistorial().size());
            assertTrue(tablero.getHistorial().get(1).descripcion().contains("desbloqueado"));
        }
    }

    // =========================================================
    // Listas
    // =========================================================

    @Nested
    @DisplayName("Gestion de Listas")
    class ListaTest {

        @Test
        @DisplayName("Agregar lista incrementa el total y registra traza")
        void agregarListaRegistraTraza() {
            Lista lista = Lista.nueva("Por Hacer");
            Lista resultado = tablero.agregarLista(lista);

            assertSame(lista, resultado);
            assertEquals(1, tablero.totalListas());
            assertEquals(1, tablero.getHistorial().size());
            assertTrue(tablero.getHistorial().get(0).descripcion().contains("Por Hacer"));
        }

        @Test
        @DisplayName("Lanza excepcion si lista es nula")
        void agregarListaNulaLanzaExcepcion() {
            assertThrows(IllegalArgumentException.class,
                    () -> tablero.agregarLista(null));
        }

        @Test
        @DisplayName("Eliminar lista existente devuelve true y registra traza")
        void eliminarListaExistente() {
            Lista lista = Lista.nueva("Backlog");
            tablero.agregarLista(lista);

            boolean eliminada = tablero.eliminarLista(lista.getId());

            assertTrue(eliminada);
            assertEquals(0, tablero.totalListas());
            assertEquals(2, tablero.getHistorial().size());
            assertTrue(tablero.getHistorial().get(1).descripcion().contains("eliminada"));
        }

        @Test
        @DisplayName("Eliminar lista inexistente devuelve false y no registra traza")
        void eliminarListaInexistente() {
            boolean eliminada = tablero.eliminarLista(ListaId.nuevo());
            assertFalse(eliminada);
            assertEquals(0, tablero.getHistorial().size());
        }

        @Test
        @DisplayName("buscarLista devuelve Optional con la lista si existe")
        void buscarListaEncontrada() {
            Lista lista = Lista.nueva("En Proceso");
            tablero.agregarLista(lista);
            assertTrue(tablero.buscarLista(lista.getId()).isPresent());
        }

        @Test
        @DisplayName("buscarLista devuelve Optional vacio si no existe")
        void buscarListaNoEncontrada() {
            assertTrue(tablero.buscarLista(ListaId.nuevo()).isEmpty());
        }
    }

    // =========================================================
    // Tarjetas (agregar)
    // =========================================================

    @Nested
    @DisplayName("Agregar Tarjetas")
    class AgregarTarjetaTest {

        @Test
        @DisplayName("Agregar tarjeta en tablero desbloqueado tiene exito y registra traza")
        void agregarTarjetaExito() throws TableroException {
            Lista lista = Lista.nueva("Todo");
            tablero.agregarLista(lista);
            Tarjeta tarjeta = Tarjeta.nueva("Tarea 1");

            Tarjeta resultado = tablero.agregarTarjeta(lista.getId(), tarjeta);

            assertSame(tarjeta, resultado);
            assertEquals(2, tablero.getHistorial().size());
            assertTrue(tablero.getHistorial().get(1).descripcion().contains("Tarea 1"));
        }

        @Test
        @DisplayName("Agregar tarjeta en tablero bloqueado lanza TableroException")
        void agregarTarjetaBloqueadoLanzaExcepcion() {
            Lista lista = Lista.nueva("Todo");
            tablero.agregarLista(lista);
            tablero.bloquear();

            assertThrows(TableroException.class,
                    () -> tablero.agregarTarjeta(lista.getId(), Tarjeta.nueva("Bloqueada")));
        }

        @Test
        @DisplayName("Agregar tarjeta nula lanza excepcion")
        void agregarTarjetaNulaLanzaExcepcion() {
            Lista lista = Lista.nueva("Todo");
            tablero.agregarLista(lista);

            assertThrows(IllegalArgumentException.class,
                    () -> tablero.agregarTarjeta(lista.getId(), null));
        }

        @Test
        @DisplayName("Agregar tarjeta en lista inexistente lanza excepcion")
        void agregarTarjetaListaInexistenteLanzaExcepcion() {
            assertThrows(IllegalArgumentException.class,
                    () -> tablero.agregarTarjeta(ListaId.nuevo(), Tarjeta.nueva("T")));
        }
    }

    // =========================================================
    // Tarjetas (eliminar)
    // =========================================================

    @Nested
    @DisplayName("Eliminar Tarjetas")
    class EliminarTarjetaTest {

        @Test
        @DisplayName("Eliminar tarjeta existente devuelve true y registra traza")
        void eliminarTarjetaExistente() throws TableroException {
            Lista lista = Lista.nueva("Todo");
            tablero.agregarLista(lista);
            Tarjeta tarjeta = Tarjeta.nueva("Borrar esto");
            tablero.agregarTarjeta(lista.getId(), tarjeta);

            boolean eliminada = tablero.eliminarTarjeta(lista.getId(), tarjeta.getId());

            assertTrue(eliminada);
            assertTrue(tablero.getHistorial().get(tablero.getHistorial().size() - 1).descripcion().contains("eliminada"));
        }

        @Test
        @DisplayName("Eliminar tarjeta de lista inexistente devuelve false")
        void eliminarTarjetaListaInexistente() {
            boolean eliminada = tablero.eliminarTarjeta(ListaId.nuevo(), TarjetaId.nuevo());
            assertFalse(eliminada);
        }

        @Test
        @DisplayName("Eliminar tarjeta inexistente en lista existente devuelve false")
        void eliminarTarjetaInexistente() {
            Lista lista = Lista.nueva("Todo");
            tablero.agregarLista(lista);
            boolean eliminada = tablero.eliminarTarjeta(lista.getId(), TarjetaId.nuevo());
            assertFalse(eliminada);
        }
    }

    // =========================================================
    // Mover Tarjetas
    // =========================================================

    @Nested
    @DisplayName("Mover Tarjetas")
    class MoverTarjetaTest {

        @Test
        @DisplayName("Mover tarjeta entre listas registra traza")
        void moverTarjetaExito() throws TableroException {
            Lista origen = Lista.nueva("Todo");
            Lista destino = Lista.nueva("En Proceso");
            tablero.agregarLista(origen);
            tablero.agregarLista(destino);
            Tarjeta tarjeta = Tarjeta.nueva("Movible");
            tablero.agregarTarjeta(origen.getId(), tarjeta);

            tablero.moverTarjeta(tarjeta.getId(), origen.getId(), destino.getId());

            assertEquals(0, origen.getTarjetas().size());
            assertEquals(1, destino.getTarjetas().size());
            assertTrue(tablero.getHistorial().get(tablero.getHistorial().size() - 1)
                    .descripcion().contains("movida"));
        }

        @Test
        @DisplayName("Mover tarjeta permitido aunque tablero este bloqueado")
        void moverTarjetaConTableroBloqueado() throws TableroException {
            Lista origen = Lista.nueva("Todo");
            Lista destino = Lista.nueva("En Proceso");
            tablero.agregarLista(origen);
            tablero.agregarLista(destino);
            Tarjeta tarjeta = Tarjeta.nueva("Movible");
            tablero.agregarTarjeta(origen.getId(), tarjeta);
            tablero.bloquear();

            assertDoesNotThrow(() -> tablero.moverTarjeta(tarjeta.getId(), origen.getId(), destino.getId()));
        }

        @Test
        @DisplayName("Mover tarjeta con lista origen inexistente lanza excepcion")
        void moverTarjetaListaOrigenInexistente() {
            Lista destino = Lista.nueva("En Proceso");
            tablero.agregarLista(destino);

            assertThrows(IllegalArgumentException.class,
                    () -> tablero.moverTarjeta(TarjetaId.nuevo(), ListaId.nuevo(), destino.getId()));
        }

        @Test
        @DisplayName("Mover tarjeta con lista destino inexistente lanza excepcion")
        void moverTarjetaListaDestinoInexistente() throws TableroException {
            Lista origen = Lista.nueva("Todo");
            tablero.agregarLista(origen);
            Tarjeta tarjeta = Tarjeta.nueva("T");
            tablero.agregarTarjeta(origen.getId(), tarjeta);

            assertThrows(IllegalArgumentException.class,
                    () -> tablero.moverTarjeta(tarjeta.getId(), origen.getId(), ListaId.nuevo()));
        }

        @Test
        @DisplayName("Mover tarjeta inexistente en lista origen lanza excepcion")
        void moverTarjetaInexistenteEnOrigen() {
            Lista origen = Lista.nueva("Todo");
            Lista destino = Lista.nueva("Hecho");
            tablero.agregarLista(origen);
            tablero.agregarLista(destino);

            assertThrows(IllegalArgumentException.class,
                    () -> tablero.moverTarjeta(TarjetaId.nuevo(), origen.getId(), destino.getId()));
        }
    }

    // =========================================================
    // Completar Tarjetas
    // =========================================================

    @Nested
    @DisplayName("Completar Tarjetas")
    class CompletarTarjetaTest {

        @Test
        @DisplayName("Completar tarjeta la mueve a tarjetasCompletadas y registra traza")
        void completarTarjetaExito() throws TableroException {
            Lista lista = Lista.nueva("Todo");
            tablero.agregarLista(lista);
            Tarjeta tarjeta = Tarjeta.nueva("Completa");
            tablero.agregarTarjeta(lista.getId(), tarjeta);

            tablero.completarTarjeta(tarjeta.getId(), lista.getId());

            assertEquals(0, lista.getTarjetas().size());
            assertEquals(1, tablero.getTarjetasCompletadas().size());
            assertTrue(tarjeta.estaCompletada());
            assertTrue(tablero.getHistorial().get(tablero.getHistorial().size() - 1)
                    .descripcion().contains("completada"));
        }

        @Test
        @DisplayName("Completar tarjeta con lista inexistente lanza excepcion")
        void completarTarjetaListaInexistente() {
            assertThrows(IllegalArgumentException.class,
                    () -> tablero.completarTarjeta(TarjetaId.nuevo(), ListaId.nuevo()));
        }

        @Test
        @DisplayName("Completar tarjeta inexistente en lista lanza excepcion")
        void completarTarjetaInexistente() {
            Lista lista = Lista.nueva("Todo");
            tablero.agregarLista(lista);

            assertThrows(IllegalArgumentException.class,
                    () -> tablero.completarTarjeta(TarjetaId.nuevo(), lista.getId()));
        }

        @Test
        @DisplayName("Completar tarjeta permitido aunque tablero este bloqueado")
        void completarTarjetaConTableroBloqueado() throws TableroException {
            Lista lista = Lista.nueva("Todo");
            tablero.agregarLista(lista);
            Tarjeta tarjeta = Tarjeta.nueva("Hecha");
            tablero.agregarTarjeta(lista.getId(), tarjeta);
            tablero.bloquear();

            assertDoesNotThrow(() -> tablero.completarTarjeta(tarjeta.getId(), lista.getId()));
        }
    }

    // =========================================================
    // Etiquetas
    // =========================================================

    @Nested
    @DisplayName("Gestion de Etiquetas")
    class EtiquetaTest {

        @Test
        @DisplayName("Etiquetar tarjeta agrega etiqueta y registra traza")
        void etiquetarTarjetaExito() throws TableroException {
            Lista lista = Lista.nueva("Todo");
            tablero.agregarLista(lista);
            Tarjeta tarjeta = Tarjeta.nueva("Con etiqueta");
            tablero.agregarTarjeta(lista.getId(), tarjeta);
            Etiqueta etiqueta = Etiqueta.de("Urgente", "rojo");

            tablero.etiquetarTarjeta(tarjeta.getId(), lista.getId(), etiqueta);

            assertTrue(tarjeta.getEtiquetas().contains(etiqueta));
            assertTrue(tablero.getHistorial().get(tablero.getHistorial().size() - 1)
                    .descripcion().contains("Urgente"));
        }

        @Test
        @DisplayName("Etiquetar tarjeta con etiqueta nula lanza excepcion")
        void etiquetarTarjetaEtiquetaNula() throws TableroException {
            Lista lista = Lista.nueva("Todo");
            tablero.agregarLista(lista);
            Tarjeta tarjeta = Tarjeta.nueva("T");
            tablero.agregarTarjeta(lista.getId(), tarjeta);

            assertThrows(IllegalArgumentException.class,
                    () -> tablero.etiquetarTarjeta(tarjeta.getId(), lista.getId(), null));
        }

        @Test
        @DisplayName("Etiquetar tarjeta en lista inexistente lanza excepcion")
        void etiquetarTarjetaListaInexistente() {
            assertThrows(IllegalArgumentException.class,
                    () -> tablero.etiquetarTarjeta(TarjetaId.nuevo(), ListaId.nuevo(), Etiqueta.de("X", "azul")));
        }

        @Test
        @DisplayName("Desetiquetar tarjeta quita etiqueta y registra traza")
        void desetiquetarTarjetaExito() throws TableroException {
            Lista lista = Lista.nueva("Todo");
            tablero.agregarLista(lista);
            Tarjeta tarjeta = Tarjeta.nueva("Con etiqueta");
            tablero.agregarTarjeta(lista.getId(), tarjeta);
            Etiqueta etiqueta = Etiqueta.de("Urgente", "rojo");
            tablero.etiquetarTarjeta(tarjeta.getId(), lista.getId(), etiqueta);

            tablero.desetiquetarTarjeta(tarjeta.getId(), lista.getId(), etiqueta);

            assertFalse(tarjeta.getEtiquetas().contains(etiqueta));
            assertTrue(tablero.getHistorial().get(tablero.getHistorial().size() - 1)
                    .descripcion().contains("retirada"));
        }

        @Test
        @DisplayName("Desetiquetar tarjeta en lista inexistente lanza excepcion")
        void desetiquetarTarjetaListaInexistente() {
            assertThrows(IllegalArgumentException.class,
                    () -> tablero.desetiquetarTarjeta(TarjetaId.nuevo(), ListaId.nuevo(), Etiqueta.de("X", "verde")));
        }
    }

    // =========================================================
    // Checklist
    // =========================================================

    @Nested
    @DisplayName("Gestion de Checklist")
    class ChecklistTest {

        private Lista lista;
        private Tarjeta tarjeta;

        @BeforeEach
        void setUpChecklist() throws TableroException {
            lista = Lista.nueva("Sprint");
            tablero.agregarLista(lista);
            tarjeta = Tarjeta.nueva("Con checklist");
            tablero.agregarTarjeta(lista.getId(), tarjeta);
        }

        @Test
        @DisplayName("Asignar checklist lo crea y registra traza")
        void asignarChecklistExito() {
            Checklist checklist = tablero.asignarChecklist(lista.getId(), tarjeta.getId(), "QA");

            assertNotNull(checklist);
            assertTrue(tarjeta.tieneChecklist());
            assertTrue(tablero.getHistorial().get(tablero.getHistorial().size() - 1)
                    .descripcion().contains("QA"));
        }

        @Test
        @DisplayName("Asignar checklist en lista inexistente lanza excepcion")
        void asignarChecklistListaInexistente() {
            assertThrows(IllegalArgumentException.class,
                    () -> tablero.asignarChecklist(ListaId.nuevo(), tarjeta.getId(), "QA"));
        }

        @Test
        @DisplayName("Agregar item checklist funciona cuando existe checklist")
        void agregarItemChecklistExito() {
            tablero.asignarChecklist(lista.getId(), tarjeta.getId(), "QA");
            tablero.agregarItemChecklist(lista.getId(), tarjeta.getId(), "Revisar codigo");

            assertEquals(1, tarjeta.getChecklist().get().totalItems());
        }

        @Test
        @DisplayName("Agregar item cuando no hay checklist lanza excepcion")
        void agregarItemSinChecklistLanzaExcepcion() {
            assertThrows(IllegalStateException.class,
                    () -> tablero.agregarItemChecklist(lista.getId(), tarjeta.getId(), "Item"));
        }

        @Test
        @DisplayName("Marcar item checklist lo marca como completado")
        void marcarItemChecklistExito() {
            tablero.asignarChecklist(lista.getId(), tarjeta.getId(), "QA");
            tablero.agregarItemChecklist(lista.getId(), tarjeta.getId(), "Paso 1");

            tablero.marcarItemChecklist(lista.getId(), tarjeta.getId(), 0);

            assertTrue(tarjeta.getChecklist().get().getItems().get(0).completado());
        }

        @Test
        @DisplayName("Marcar item con indice invalido lanza excepcion")
        void marcarItemIndiceInvalidoLanzaExcepcion() {
            tablero.asignarChecklist(lista.getId(), tarjeta.getId(), "QA");

            assertThrows(IllegalArgumentException.class,
                    () -> tablero.marcarItemChecklist(lista.getId(), tarjeta.getId(), 99));
        }

        @Test
        @DisplayName("Marcar item cuando no hay checklist lanza excepcion")
        void marcarItemSinChecklistLanzaExcepcion() {
            assertThrows(IllegalStateException.class,
                    () -> tablero.marcarItemChecklist(lista.getId(), tarjeta.getId(), 0));
        }

        @Test
        @DisplayName("Desmarcar item checklist lo pone como no completado")
        void desmarcarItemChecklistExito() {
            tablero.asignarChecklist(lista.getId(), tarjeta.getId(), "QA");
            tablero.agregarItemChecklist(lista.getId(), tarjeta.getId(), "Paso 1");
            tablero.marcarItemChecklist(lista.getId(), tarjeta.getId(), 0);

            tablero.desmarcarItemChecklist(lista.getId(), tarjeta.getId(), 0);

            assertFalse(tarjeta.getChecklist().get().getItems().get(0).completado());
        }

        @Test
        @DisplayName("Desmarcar item con indice invalido lanza excepcion")
        void desmarcarItemIndiceInvalidoLanzaExcepcion() {
            tablero.asignarChecklist(lista.getId(), tarjeta.getId(), "QA");

            assertThrows(IllegalArgumentException.class,
                    () -> tablero.desmarcarItemChecklist(lista.getId(), tarjeta.getId(), -1));
        }

        @Test
        @DisplayName("Desmarcar item cuando no hay checklist lanza excepcion")
        void desmarcarItemSinChecklistLanzaExcepcion() {
            assertThrows(IllegalStateException.class,
                    () -> tablero.desmarcarItemChecklist(lista.getId(), tarjeta.getId(), 0));
        }
    }

    // =========================================================
    // Colecciones inmodificables
    // =========================================================

    @Nested
    @DisplayName("Colecciones inmodificables")
    class ColeccionesInmodificablesTest {

        @Test
        @DisplayName("getListas devuelve lista inmodificable")
        void getListasInmodificable() {
            List<Lista> listas = tablero.getListas();
            assertThrows(UnsupportedOperationException.class,
                    () -> listas.add(Lista.nueva("Intrusa")));
        }

        @Test
        @DisplayName("getTarjetasCompletadas devuelve lista inmodificable")
        void getTarjetasCompletadasInmodificable() {
            List<Tarjeta> completadas = tablero.getTarjetasCompletadas();
            assertThrows(UnsupportedOperationException.class,
                    () -> completadas.add(Tarjeta.nueva("Intrusa")));
        }

        @Test
        @DisplayName("getHistorial devuelve lista inmodificable")
        void getHistorialInmodificable() {
            List<Traza> historial = tablero.getHistorial();
            assertThrows(UnsupportedOperationException.class,
                    () -> historial.add(Traza.nueva("Intrusa")));
        }
    }

    // =========================================================
    // totalListas
    // =========================================================

    @Test
    @DisplayName("totalListas refleja el numero real de listas")
    void totalListasReflejaNumeroListas() {
        assertEquals(0, tablero.totalListas());
        tablero.agregarLista(Lista.nueva("A"));
        tablero.agregarLista(Lista.nueva("B"));
        assertEquals(2, tablero.totalListas());
    }

    // =========================================================
    // equals / hashCode / toString
    // =========================================================

    @Nested
    @DisplayName("Identidad y representacion")
    class IdentidadTest {

        @Test
        @DisplayName("Dos tableros con el mismo id son iguales")
        void dosTablerosIgualId() {
            TableroId id = TableroId.nuevo();
            Tablero t1 = new Tablero(id, "Uno", propietario);
            Tablero t2 = new Tablero(id, "Dos", propietario);
            assertEquals(t1, t2);
            assertEquals(t1.hashCode(), t2.hashCode());
        }

        @Test
        @DisplayName("Dos tableros con id distinto no son iguales")
        void dosTablerosDistintoId() {
            Tablero otro = new Tablero("Otro", propietario);
            assertNotEquals(tablero, otro);
        }

        @Test
        @DisplayName("Un tablero es igual a si mismo")
        void tableroIgualASiMismo() {
            assertEquals(tablero, tablero);
        }

        @Test
        @DisplayName("Un tablero no es igual a null ni a otro tipo")
        void tableroNoIgualANullNiOtroTipo() {
            assertNotEquals(null, tablero);
            assertNotEquals("string", tablero);
        }

        @Test
        @DisplayName("toString contiene id, nombre y numero de listas")
        void toStringContieneInfo() {
            String str = tablero.toString();
            assertTrue(str.contains("Sprint 1"));
            assertTrue(str.contains("0"));
        }
    }
}
