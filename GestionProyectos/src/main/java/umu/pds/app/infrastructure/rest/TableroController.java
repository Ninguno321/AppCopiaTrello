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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import umu.pds.app.application.commands.PlantillaEtiquetaCommand;
import umu.pds.app.application.commands.PlantillaItemChecklistCommand;
import umu.pds.app.application.commands.PlantillaListaCommand;
import umu.pds.app.application.commands.PlantillaTableroCommand;
import umu.pds.app.application.commands.PlantillaTarjetaCommand;
import umu.pds.app.domain.exceptions.ListaException;
import umu.pds.app.domain.exceptions.PlantillaInvalidaException;
import umu.pds.app.domain.exceptions.TableroException;
import umu.pds.app.domain.exceptions.TarjetaException;
import umu.pds.app.infrastructure.rest.dto.PlantillaEtiquetaDto;
import umu.pds.app.infrastructure.rest.dto.PlantillaItemChecklistDto;
import umu.pds.app.infrastructure.rest.dto.PlantillaListaDto;
import umu.pds.app.infrastructure.rest.dto.PlantillaTableroDto;
import umu.pds.app.infrastructure.rest.dto.PlantillaTarjetaDto;
import umu.pds.app.domain.modelo.shared.ListaId;
import umu.pds.app.domain.modelo.shared.TableroId;
import umu.pds.app.domain.modelo.shared.TarjetaId;
import umu.pds.app.domain.modelo.tablero.Etiqueta;
import umu.pds.app.domain.modelo.tablero.Lista;
import umu.pds.app.domain.modelo.tablero.Tablero;
import umu.pds.app.domain.modelo.tablero.Tarjeta;
import umu.pds.app.domain.ports.input.GestionTableroUseCase;
import umu.pds.app.domain.modelo.tablero.Checklist;
import umu.pds.app.infrastructure.rest.dto.AgregarChecklistRequest;
import umu.pds.app.infrastructure.rest.dto.AsignarFechaVencimientoRequest;
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

import umu.pds.app.infrastructure.rest.dto.ConfigurarPrerequisitosRequest;

import umu.pds.app.domain.modelo.tablero.FechaLimite;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${app.server.path}/tableros")
public class TableroController {

    private final GestionTableroUseCase gestionTablero;
    private final ObjectMapper yamlMapper;

    public TableroController(GestionTableroUseCase gestionTablero) {
        this.gestionTablero = gestionTablero;
        this.yamlMapper = new ObjectMapper(new com.fasterxml.jackson.dataformat.yaml.YAMLFactory());
        this.yamlMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTablero(@PathVariable String id) {
        gestionTablero.eliminarTablero(TableroId.de(id));
        return ResponseEntity.noContent().build();
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
        int maxTarjetas = request.maxTarjetas() != null ? request.maxTarjetas() : 5;
        Lista lista = gestionTablero.agregarLista(TableroId.de(id), request.nombre(), maxTarjetas);
        return ResponseEntity.status(HttpStatus.CREATED).body(ListaResponse.from(lista));
    }

    @DeleteMapping("/{id}/listas/{listaId}")
    public ResponseEntity<Void> eliminarLista(@PathVariable String id,
                                               @PathVariable String listaId) {
        gestionTablero.eliminarLista(TableroId.de(id), ListaId.de(listaId));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/listas/{listaId}/prerequisitos")
    public ResponseEntity<Void> configurarPrerequisitos(@PathVariable String id,
                                                         @PathVariable String listaId,
                                                         @RequestBody ConfigurarPrerequisitosRequest request) {
        List<ListaId> requeridas = request.listasRequeridas().stream()
                .map(ListaId::de)
                .collect(Collectors.toList());
        gestionTablero.configurarListasRequeridas(TableroId.de(id), ListaId.de(listaId), requeridas);
        return ResponseEntity.ok().build();
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
    public ResponseEntity<Void> etiquetarTarjeta(@PathVariable String id,
                                                  @PathVariable String listaId,
                                                  @PathVariable String tarjetaId,
                                                  @RequestBody AsignarEtiquetaRequest request) {
        gestionTablero.etiquetarTarjeta(
                TableroId.de(id),
                ListaId.de(listaId),
                TarjetaId.de(tarjetaId),
                Etiqueta.de(request.nombre(), request.color())
        );
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/listas/{listaId}/tarjetas/{tarjetaId}/etiquetas")
    public ResponseEntity<Void> desetiquetarTarjeta(@PathVariable String id,
                                                     @PathVariable String listaId,
                                                     @PathVariable String tarjetaId,
                                                     @RequestBody AsignarEtiquetaRequest request) {
        gestionTablero.desetiquetarTarjeta(
                TableroId.de(id),
                ListaId.de(listaId),
                TarjetaId.de(tarjetaId),
                Etiqueta.de(request.nombre(), request.color())
        );
        return ResponseEntity.ok().build();
    }

    // --- Checklist ---

    @PutMapping("/{id}/listas/{listaId}/tarjetas/{tarjetaId}/fechaLimite")
    public ResponseEntity<Void> asignarFechaLimite(@PathVariable String id,
                                                     @PathVariable String listaId,
                                                     @PathVariable String tarjetaId,
                                                     @RequestBody AsignarFechaVencimientoRequest request) {
        gestionTablero.asignarFechaLimite(
                TableroId.de(id),
                ListaId.de(listaId),
                TarjetaId.de(tarjetaId),
                FechaLimite.de(request.fecha())
        );
        return ResponseEntity.ok().build();
    }

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

    // --- Importación desde plantilla ---

    @PostMapping("/plantilla")
    public ResponseEntity<TableroResponse> crearDesdePlantilla(
            @RequestBody String yamlContent,
            @RequestParam String email) {
        PlantillaTableroDto dto;
        try {
            dto = yamlMapper.readValue(yamlContent, PlantillaTableroDto.class);
        } catch (JsonProcessingException e) {
            throw new PlantillaInvalidaException("El YAML de la plantilla no es válido: " + e.getOriginalMessage(), e);
        }
        PlantillaTableroCommand command = toCommand(dto);
        Tablero tablero = gestionTablero.crearDesdePlantilla(command, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(TableroResponse.from(tablero));
    }

    private PlantillaTableroCommand toCommand(PlantillaTableroDto dto) {
        List<PlantillaListaCommand> listas = dto.listas() == null ? null :
            dto.listas().stream().map(this::toCommand).toList();
        return new PlantillaTableroCommand(dto.nombre(), listas);
    }

    private PlantillaListaCommand toCommand(PlantillaListaDto dto) {
        List<PlantillaTarjetaCommand> tarjetas = dto.tarjetas() == null ? null :
            dto.tarjetas().stream().map(this::toCommand).toList();
        return new PlantillaListaCommand(dto.nombre(), dto.maxTarjetas(), tarjetas, dto.listasRequeridas());
    }

    private PlantillaTarjetaCommand toCommand(PlantillaTarjetaDto dto) {
        List<PlantillaEtiquetaCommand> etiquetas = dto.etiquetas() == null ? null :
            dto.etiquetas().stream().map(e -> new PlantillaEtiquetaCommand(e.nombre(), e.color())).toList();
        List<PlantillaItemChecklistCommand> items = dto.checklist() == null ? null :
            dto.checklist().stream().map(i -> new PlantillaItemChecklistCommand(i.descripcion(), i.completado())).toList();
        return new PlantillaTarjetaCommand(dto.titulo(), dto.fechaLimite(), etiquetas, items);
    }

    // --- Manejo de errores ---

    @ExceptionHandler(PlantillaInvalidaException.class)
    public ResponseEntity<Map<String, String>> handlePlantillaInvalida(PlantillaInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

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

    /** Datos de entrada inválidos para una tarjeta (título vacío, etc.). */
    @ExceptionHandler(TarjetaException.class)
    public ResponseEntity<Map<String, String>> handleTarjetaException(TarjetaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    /** Datos de entrada inválidos para una lista (nombre vacío, etc.). */
    @ExceptionHandler(ListaException.class)
    public ResponseEntity<Map<String, String>> handleListaException(ListaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    /** Regla de negocio del dominio violada (p.ej. tablero bloqueado). */
    @ExceptionHandler(TableroException.class)
    public ResponseEntity<Map<String, String>> handleTableroException(TableroException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }
}
