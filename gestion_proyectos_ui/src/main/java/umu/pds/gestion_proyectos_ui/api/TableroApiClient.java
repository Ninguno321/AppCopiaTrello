package umu.pds.gestion_proyectos_ui.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import umu.pds.gestion_proyectos_ui.api.dto.ChecklistDto;
import umu.pds.gestion_proyectos_ui.api.dto.ListaDto;
import umu.pds.gestion_proyectos_ui.api.dto.TableroDto;
import umu.pds.gestion_proyectos_ui.api.dto.TarjetaDto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * Cliente HTTP que comunica la UI con el backend REST.
 * Todos los métodos son síncronos — llamar siempre desde un hilo de fondo (Task de JavaFX).
 */
public class TableroApiClient {

    private static final String BASE_URL = "http://localhost:8080/umu/pds";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TableroApiClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public TableroDto crearTablero(String nombre, String email) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("nombre", nombre, "email", email));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tableros"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            return objectMapper.readValue(response.body(), TableroDto.class);
        }
        throw new RuntimeException("Error al crear tablero (" + response.statusCode() + "): " + response.body());
    }

    public ListaDto agregarLista(String tableroId, String nombre) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("nombre", nombre));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tableros/" + tableroId + "/listas"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            return objectMapper.readValue(response.body(), ListaDto.class);
        }
        throw new RuntimeException("Error al crear lista (" + response.statusCode() + "): " + response.body());
    }

    public TarjetaDto agregarTarjeta(String tableroId, String listaId, String titulo) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("titulo", titulo));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tableros/" + tableroId + "/listas/" + listaId + "/tarjetas"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            return objectMapper.readValue(response.body(), TarjetaDto.class);
        }
        throw new RuntimeException("Error al crear tarjeta (" + response.statusCode() + "): " + response.body());
    }

    public void moverTarjeta(String tableroId, String tarjetaId, String listaOrigenId, String listaDestinoId) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "tarjetaId", tarjetaId,
                "listaOrigenId", listaOrigenId,
                "listaDestinoId", listaDestinoId
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tableros/" + tableroId + "/tarjetas/mover"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error al mover tarjeta (" + response.statusCode() + "): " + response.body());
        }
    }

    public void completarTarjeta(String tableroId, String listaId, String tarjetaId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tableros/" + tableroId + "/listas/" + listaId + "/tarjetas/" + tarjetaId + "/completar"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error al completar tarjeta (" + response.statusCode() + "): " + response.body());
        }
    }

    public ChecklistDto crearChecklist(String tableroId, String listaId, String tarjetaId, String nombre) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("nombre", nombre));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tableros/" + tableroId + "/listas/" + listaId + "/tarjetas/" + tarjetaId + "/checklist"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            return objectMapper.readValue(response.body(), ChecklistDto.class);
        }
        throw new RuntimeException("Error al crear checklist (" + response.statusCode() + "): " + response.body());
    }

    public void agregarItemChecklist(String tableroId, String listaId, String tarjetaId, String descripcion) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("descripcion", descripcion));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tableros/" + tableroId + "/listas/" + listaId + "/tarjetas/" + tarjetaId + "/checklist/items"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error al agregar ítem (" + response.statusCode() + "): " + response.body());
        }
    }

    public void marcarItemChecklist(String tableroId, String listaId, String tarjetaId, int indice) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tableros/" + tableroId + "/listas/" + listaId + "/tarjetas/" + tarjetaId + "/checklist/items/" + indice + "/marcar"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error al marcar ítem (" + response.statusCode() + "): " + response.body());
        }
    }

    public void desmarcarItemChecklist(String tableroId, String listaId, String tarjetaId, int indice) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tableros/" + tableroId + "/listas/" + listaId + "/tarjetas/" + tarjetaId + "/checklist/items/" + indice + "/desmarcar"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error al desmarcar ítem (" + response.statusCode() + "): " + response.body());
        }
    }

    public TableroDto obtenerTablero(String id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tableros/" + id))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), TableroDto.class);
        }
        throw new RuntimeException("Tablero no encontrado (" + response.statusCode() + ")");
    }
}
