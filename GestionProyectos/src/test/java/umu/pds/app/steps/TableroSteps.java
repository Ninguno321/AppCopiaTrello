package umu.pds.app.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import umu.pds.app.domain.exceptions.TableroException;
import umu.pds.app.domain.modelo.shared.ListaId;
import umu.pds.app.domain.modelo.shared.TableroId;
import umu.pds.app.domain.modelo.shared.TarjetaId;
import umu.pds.app.domain.modelo.tablero.Lista;
import umu.pds.app.domain.modelo.tablero.Tablero;
import umu.pds.app.domain.modelo.tablero.Tarjeta;
import umu.pds.app.domain.ports.input.GestionTableroUseCase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TableroSteps {

    @Autowired
    private GestionTableroUseCase gestionTablero;

    private TableroId tableroId;
    private final Map<String, ListaId> listaPorNombre = new HashMap<>();
    private final Map<String, TarjetaId> tarjetaPorTitulo = new HashMap<>();
    private Exception excepcionCapturada;

    @Given("un tablero llamado {string} creado por {string}")
    public void unTableroLlamadoCreadoPor(String nombre, String email) {
        Tablero tablero = gestionTablero.crearTablero(nombre, email);
        tableroId = tablero.getId();
    }

    @Given("se agrega una lista llamada {string} con capacidad {int}")
    public void seAgregaUnaListaLlamadaConCapacidad(String nombre, int capacidad) {
        Lista lista = gestionTablero.agregarLista(tableroId, nombre, capacidad);
        listaPorNombre.put(nombre, lista.getId());
    }

    @Given("se agrega una tarjeta llamada {string} a la lista {string}")
    public void seAgregaUnaTarjetaLlamadaALaLista(String titulo, String nombreLista) {
        ListaId listaId = listaPorNombre.get(nombreLista);
        Tarjeta tarjeta = gestionTablero.agregarTarjeta(tableroId, listaId, titulo);
        tarjetaPorTitulo.put(titulo, tarjeta.getId());
    }

    @Given("la lista {string} requiere haber pasado por {string}")
    public void laListaRequiereHaberPasadoPor(String destino, String requerida) {
        ListaId listaDestinoId = listaPorNombre.get(destino);
        ListaId listaRequeridaId = listaPorNombre.get(requerida);
        gestionTablero.configurarListasRequeridas(tableroId, listaDestinoId, List.of(listaRequeridaId));
    }

    @When("se intenta mover la tarjeta {string} de {string} a {string}")
    public void seIntentaMoverLaTarjetaDe(String titulo, String origen, String destino) {
        TarjetaId tarjetaId = tarjetaPorTitulo.get(titulo);
        ListaId origenId = listaPorNombre.get(origen);
        ListaId destinoId = listaPorNombre.get(destino);
        try {
            gestionTablero.moverTarjeta(tableroId, tarjetaId, origenId, destinoId);
        } catch (Exception e) {
            excepcionCapturada = e;
        }
    }

    @Then("el tablero tiene {int} lista")
    public void elTableroTieneLista(int cantidad) {
        Tablero tablero = gestionTablero.obtenerTablero(tableroId);
        assertEquals(cantidad, tablero.getListas().size());
    }

    @Then("la lista {string} contiene {int} tarjeta")
    public void laListaContieneTarjeta(String nombreLista, int cantidad) {
        Tablero tablero = gestionTablero.obtenerTablero(tableroId);
        ListaId listaId = listaPorNombre.get(nombreLista);
        Lista lista = tablero.getListas().stream()
                .filter(l -> l.getId().equals(listaId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Lista no encontrada: " + nombreLista));
        assertEquals(cantidad, lista.getTarjetas().size());
    }

    @Then("se lanza una excepcion de prerequisito no cumplido")
    public void seLanzaUnaExcepcionDePrerequisito() {
        assertNotNull(excepcionCapturada, "Se esperaba una excepcion pero no se lanzo ninguna");
        assertInstanceOf(TableroException.class, excepcionCapturada);
    }
}
