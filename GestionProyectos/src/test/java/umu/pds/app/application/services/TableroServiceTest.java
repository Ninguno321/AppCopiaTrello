package umu.pds.app.application.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import umu.pds.app.domain.modelo.shared.ListaId;
import umu.pds.app.domain.modelo.shared.TableroId;
import umu.pds.app.domain.modelo.shared.TarjetaId;
import umu.pds.app.domain.modelo.tablero.Lista;
import umu.pds.app.domain.modelo.tablero.Tablero;
import umu.pds.app.domain.modelo.tablero.Tarjeta;
import umu.pds.app.domain.modelo.usuario.Usuario;
import umu.pds.app.domain.ports.output.TableroRepository;
import umu.pds.app.domain.ports.output.UsuarioRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TableroServiceTest {

    @Mock
    private TableroRepository tableroRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private TableroService service;

    // =========================================================
    // crearTablero
    // =========================================================

    @Test
    @DisplayName("Usuario nuevo: guarda en usuarioRepository y en tableroRepository")
    void crearTablero_UsuarioNuevo_LlamaGuardarEnAmbosRepositorios() {
        when(usuarioRepository.buscarPorEmail("nuevo@test.com"))
                .thenReturn(Optional.empty());

        service.crearTablero("Mi Tablero", "nuevo@test.com");

        verify(usuarioRepository).guardar(any(Usuario.class));
        verify(tableroRepository).guardar(any(Tablero.class));
    }

    @Test
    @DisplayName("Usuario existente: no llama a guardar usuario, si llama a guardar tablero")
    void crearTablero_UsuarioExistente_NoLlamaGuardarUsuario() {
        Usuario existente = new Usuario("existente@test.com");
        when(usuarioRepository.buscarPorEmail("existente@test.com"))
                .thenReturn(Optional.of(existente));

        service.crearTablero("Mi Tablero", "existente@test.com");

        verify(usuarioRepository, never()).guardar(any(Usuario.class));
        verify(tableroRepository).guardar(any(Tablero.class));
    }

    // =========================================================
    // obtenerTablero
    // =========================================================

    @Test
    @DisplayName("Tablero no encontrado: lanza IllegalArgumentException")
    void obtenerTablero_NoEncontrado_LanzaExcepcion() {
        TableroId id = TableroId.nuevo();
        when(tableroRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.obtenerTablero(id));
    }

    // =========================================================
    // moverTarjeta
    // =========================================================

    @Test
    @DisplayName("Mover tarjeta en tablero bloqueado sin listas lanza excepcion y no llama guardar")
    void moverTarjeta_TableroBloqueado_NoLlamaGuardar() {
        Usuario propietario = new Usuario("owner@test.com");
        Tablero tablero = new Tablero("Bloqueado", propietario);
        tablero.bloquear();
        when(tableroRepository.buscarPorId(tablero.getId()))
                .thenReturn(Optional.of(tablero));

        // moverTarjeta lanza IllegalArgumentException porque la lista origen no existe
        assertThrows(IllegalArgumentException.class,
                () -> service.moverTarjeta(
                        tablero.getId(),
                        TarjetaId.nuevo(),
                        ListaId.nuevo(),
                        ListaId.nuevo()));

        verify(tableroRepository, never()).guardar(any(Tablero.class));
    }

    @Test
    @DisplayName("agregarTarjeta en tablero bloqueado lanza IllegalStateException y no llama guardar")
    void agregarTarjeta_TableroBloqueado_LanzaIllegalStateException() {
        Usuario propietario = new Usuario("owner@test.com");
        Tablero tablero = new Tablero("Proyecto", propietario);
        Lista lista = Lista.nueva("Backlog", 10);
        tablero.agregarLista(lista);
        tablero.bloquear();
        when(tableroRepository.buscarPorId(tablero.getId()))
                .thenReturn(Optional.of(tablero));

        assertThrows(IllegalStateException.class,
                () -> service.agregarTarjeta(tablero.getId(), lista.getId(), "Nueva tarea"));

        verify(tableroRepository, never()).guardar(any(Tablero.class));
    }

    @Test
    @DisplayName("moverTarjeta con exito llama a tableroRepository.guardar")
    void moverTarjeta_Exito_LlamaGuardarTablero() {
        Usuario propietario = new Usuario("owner@test.com");
        Tablero tablero = new Tablero("Proyecto", propietario);
        Lista lista1 = Lista.nueva("Todo", 10);
        Lista lista2 = Lista.nueva("En Progreso", 10);
        tablero.agregarLista(lista1);
        tablero.agregarLista(lista2);
        Tarjeta tarjeta = Tarjeta.nueva("Tarea movible");
        tablero.agregarTarjeta(lista1.getId(), tarjeta);
        when(tableroRepository.buscarPorId(tablero.getId()))
                .thenReturn(Optional.of(tablero));

        service.moverTarjeta(tablero.getId(), tarjeta.getId(), lista1.getId(), lista2.getId());

        verify(tableroRepository).guardar(any(Tablero.class));
    }
}
