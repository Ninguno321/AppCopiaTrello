package umu.pds.app.infrastructure.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import umu.pds.app.application.ports.input.GestionTableroUseCase;
import umu.pds.app.domain.modelo.shared.ListaId;
import umu.pds.app.domain.modelo.shared.TableroId;
import umu.pds.app.domain.modelo.shared.TarjetaId;
import umu.pds.app.domain.modelo.tablero.Etiqueta;
import umu.pds.app.domain.modelo.tablero.Lista;
import umu.pds.app.domain.modelo.tablero.Tablero;
import umu.pds.app.domain.modelo.tablero.Tarjeta;
import umu.pds.app.domain.modelo.tablero.Checklist;
import umu.pds.app.infrastructure.rest.dto.AgregarChecklistRequest;
import umu.pds.app.infrastructure.rest.dto.AgregarItemChecklistRequest;
import umu.pds.app.infrastructure.rest.dto.AgregarListaRequest;
import umu.pds.app.infrastructure.rest.dto.AgregarTarjetaRequest;
import umu.pds.app.infrastructure.rest.dto.AsignarEtiquetaRequest;
import umu.pds.app.infrastructure.rest.dto.ChecklistResponse;
import umu.pds.app.infrastructure.rest.dto.CrearTableroRequest;
import umu.pds.app.infrastructure.rest.dto.ListaResponse;
import umu.pds.app.infrastructure.rest.dto.MoverTarjetaRequest;
import umu.pds.app.infrastructure.rest.dto.RenombrarTableroRequest;
import umu.pds.app.infrastructure.rest.dto.TableroResponse;
import umu.pds.app.infrastructure.rest.dto.TarjetaResponse;
import umu.pds.app.infrastructure.rest.dto.TrazaResponse;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${app.server.path}/tableros")
public class TableroController {

    private final GestionTableroUseCase gestionTablero;

    public TableroController(GestionTableroUseCase gestionTablero) {
        this.gestionTablero = gestionTablero;
    }

    // --- Tablero ---

    @PostMapping
    public ResponseEntity<TableroResponse> crearTablero(@RequestBody CrearTableroRequest request) {
        Tablero tablero = gestionTablero.crearTablero(request.nombre(), request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(TableroResponse.from(tablero));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableroResponse> obtenerTablero(@PathVariable String id) {
        Tablero tablero = gestionTablero.obtenerTablero(TableroId.de(id));
        return ResponseEntity.ok(TableroResponse.from(tablero));
    }

    @GetMapping
    public ResponseEntity<List<TableroResponse>> obtenerTablerosPorEmail(@RequestParam String email) {
        List<TableroResponse> tableros = gestionTablero.obtenerTablerosPorEmail(email)
                .stream().map(TableroResponse::from).toList();
        return ResponseEntity.ok(tableros);
    }

    @PutMapping("/{id}/nombre")
    public ResponseEntity<Void> renombrarTablero(@PathVariable String id,
                                                  @RequestBody RenombrarTableroRequest request) {
        gestionTablero.renombrarTablero(TableroId.de(id), request.nuevoNombre());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/bloquear")
    public ResponseEntity<Void> bloquearTablero(@PathVariable String id) {
        gestionTablero.bloquearTablero(TableroId.de(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/desbloquear")
    public ResponseEntity<Void> desbloquearTablero(@PathVariable String id) {
        gestionTablero.desbloquearTablero(TableroId.de(id));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<TrazaResponse>> obtenerHistorial(@PathVariable String id) {
        Tablero tablero = gestionTablero.obtenerTablero(TableroId.de(id));
        List<TrazaResponse> historial = tablero.getHistorial()
                .stream().map(TrazaResponse::from).toList();
        return ResponseEntity.ok(historial);
    }

    // --- Listas ---

    @PostMapping("/{id}/listas")
    public ResponseEntity<ListaResponse> agregarLista(@PathVariable String id,
                                                       @RequestBody AgregarListaRequest request) {
        Lista lista = gestionTablero.agregarLista(TableroId.de(id), request.nombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(ListaResponse.from(lista));
    }

    @DeleteMapping("/{id}/listas/{listaId}")
    public ResponseEntity<Void> eliminarLista(@PathVariable String id,
                                               @PathVariable String listaId) {
        gestionTablero.eliminarLista(TableroId.de(id), ListaId.de(listaId));
        return ResponseEntity.noContent().build();
    }

    // --- Tarjetas ---

    @PostMapping("/{id}/listas/{listaId}/tarjetas")
    public ResponseEntity<TarjetaResponse> agregarTarjeta(@PathVariable String id,
                                                            @PathVariable String listaId,
                                                            @RequestBody AgregarTarjetaRequest request) {
        Tarjeta tarjeta = gestionTablero.agregarTarjeta(TableroId.de(id), ListaId.de(listaId), request.titulo());
        return ResponseEntity.status(HttpStatus.CREATED).body(TarjetaResponse.from(tarjeta));
    }

    @DeleteMapping("/{id}/listas/{listaId}/tarjetas/{tarjetaId}")
    public ResponseEntity<Void> eliminarTarjeta(@PathVariable String id,
                                                 @PathVariable String listaId,
                                                 @PathVariable String tarjetaId) {
        gestionTablero.eliminarTarjeta(TableroId.de(id), ListaId.de(listaId), TarjetaId.de(tarjetaId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/tarjetas/mover")
    public ResponseEntity<Void> moverTarjeta(@PathVariable String id,
                                              @RequestBody MoverTarjetaRequest request) {
        gestionTablero.moverTarjeta(
                TableroId.de(id),
                TarjetaId.de(request.tarjetaId()),
                ListaId.de(request.listaOrigenId()),
                ListaId.de(request.listaDestinoId())
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/listas/{listaId}/tarjetas/{tarjetaId}/completar")
    public ResponseEntity<Void> marcarTarjetaCompletada(@PathVariable String id,
                                                         @PathVariable String listaId,
                                                         @PathVariable String tarjetaId) {
        gestionTablero.marcarTarjetaCompletada(TableroId.de(id), ListaId.de(listaId), TarjetaId.de(tarjetaId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/listas/{listaId}/tarjetas/{tarjetaId}/etiquetas")
    public ResponseEntity<Void> asignarEtiqueta(@PathVariable String id,
                                                 @PathVariable String listaId,
                                                 @PathVariable String tarjetaId,
                                                 @RequestBody AsignarEtiquetaRequest request) {
        gestionTablero.asignarEtiqueta(
                TableroId.de(id),
                ListaId.de(listaId),
                TarjetaId.de(tarjetaId),
                Etiqueta.de(request.nombre(), request.color())
        );
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/listas/{listaId}/tarjetas/{tarjetaId}/etiquetas")
    public ResponseEntity<Void> quitarEtiqueta(@PathVariable String id,
                                                @PathVariable String listaId,
                                                @PathVariable String tarjetaId,
                                                @RequestBody AsignarEtiquetaRequest request) {
        gestionTablero.quitarEtiqueta(
                TableroId.de(id),
                ListaId.de(listaId),
                TarjetaId.de(tarjetaId),
                Etiqueta.de(request.nombre(), request.color())
        );
        return ResponseEntity.ok().build();
    }

    // --- Checklist ---

    @PostMapping("/{id}/listas/{listaId}/tarjetas/{tarjetaId}/checklist")
    public ResponseEntity<ChecklistResponse> crearChecklist(@PathVariable String id,
                                                             @PathVariable String listaId,
                                                             @PathVariable String tarjetaId,
                                                             @RequestBody AgregarChecklistRequest request) {
        Checklist checklist = gestionTablero.asignarChecklist(
                TableroId.de(id), ListaId.de(listaId), TarjetaId.de(tarjetaId), request.nombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(ChecklistResponse.from(checklist));
    }

    @PostMapping("/{id}/listas/{listaId}/tarjetas/{tarjetaId}/checklist/items")
    public ResponseEntity<Void> agregarItemChecklist(@PathVariable String id,
                                                      @PathVariable String listaId,
                                                      @PathVariable String tarjetaId,
                                                      @RequestBody AgregarItemChecklistRequest request) {
        gestionTablero.agregarItemChecklist(
                TableroId.de(id), ListaId.de(listaId), TarjetaId.de(tarjetaId), request.descripcion());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/listas/{listaId}/tarjetas/{tarjetaId}/checklist/items/{indice}/marcar")
    public ResponseEntity<Void> marcarItemChecklist(@PathVariable String id,
                                                     @PathVariable String listaId,
                                                     @PathVariable String tarjetaId,
                                                     @PathVariable int indice) {
        gestionTablero.marcarItemChecklist(
                TableroId.de(id), ListaId.de(listaId), TarjetaId.de(tarjetaId), indice);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/listas/{listaId}/tarjetas/{tarjetaId}/checklist/items/{indice}/desmarcar")
    public ResponseEntity<Void> desmarcarItemChecklist(@PathVariable String id,
                                                        @PathVariable String listaId,
                                                        @PathVariable String tarjetaId,
                                                        @PathVariable int indice) {
        gestionTablero.desmarcarItemChecklist(
                TableroId.de(id), ListaId.de(listaId), TarjetaId.de(tarjetaId), indice);
        return ResponseEntity.ok().build();
    }

    // --- Manejo de errores ---

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }
}
