package umu.pds.app.adapters.mappers;

import org.springframework.stereotype.Component;
import umu.pds.app.adapters.jpa.entity.*;
import umu.pds.app.domain.exceptions.ChecklistException;
import umu.pds.app.domain.modelo.shared.ChecklistId;
import umu.pds.app.domain.modelo.shared.ListaId;
import umu.pds.app.domain.modelo.shared.TableroId;
import umu.pds.app.domain.modelo.shared.TarjetaId;
import umu.pds.app.domain.modelo.tablero.*;
import java.time.LocalDate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Convierte entre el modelo de dominio puro y las entidades JPA.
 * El dominio NUNCA importa Jakarta; toda la traducción vive aquí.
 *
 * La reconstrucción de Tablero utiliza el método estático
 * {@link umu.pds.app.domain.modelo.tablero.Tablero#reconstitute} del Aggregate Root,
 * que reinyecta el estado persistido sin disparar los métodos de negocio
 * (evitando así entradas de historial falsas).
 */
@Component
public class TableroMapper {

    private final UsuarioMapper usuarioMapper;

    public TableroMapper(UsuarioMapper usuarioMapper) {
        this.usuarioMapper = usuarioMapper;
    }

    // -------------------------------------------------------------------------
    // Dominio → JPA
    // -------------------------------------------------------------------------

    public TableroJpaEntity toJpaEntity(Tablero tablero) {
        TableroJpaEntity entity = new TableroJpaEntity();
        entity.setId(tablero.getId().toString());
        entity.setNombre(tablero.getNombre());
        entity.setPropietario(usuarioMapper.toJpaEntity(tablero.getPropietario()));
        entity.setBloqueado(tablero.isBloqueado());

        entity.setListas(tablero.getListas().stream()
                .map(this::toListaJpaEntity)
                .collect(Collectors.toList()));

        entity.setTarjetasCompletadas(tablero.getTarjetasCompletadas().stream()
                .map(this::toTarjetaJpaEntity)
                .collect(Collectors.toList()));

        entity.setHistorial(tablero.getHistorial().stream()
                .map(t -> new TrazaJpaEmbeddable(t.descripcion(), t.timestamp()))
                .collect(Collectors.toList()));

        return entity;
    }

    private ListaJpaEntity toListaJpaEntity(Lista lista) {
        ListaJpaEntity entity = new ListaJpaEntity(lista.getId().toString(), lista.getNombre());
        entity.setTarjetas(lista.getTarjetas().stream()
                .map(this::toTarjetaJpaEntity)
                .collect(Collectors.toList()));
        return entity;
    }

    private TarjetaJpaEntity toTarjetaJpaEntity(Tarjeta tarjeta) {
        TarjetaJpaEntity entity = new TarjetaJpaEntity(
                tarjeta.getId().toString(),
                tarjeta.getTitulo(),
                tarjeta.getDescripcion(),
                tarjeta.estaCompletada()
        );

        tarjeta.getTarea().ifPresent(t -> entity.setTarea(
                new TareaJpaEmbeddable(t.titulo(), t.descripcion(), t.fechaLimite(), t.estado().name())
        ));

        tarjeta.getChecklist().ifPresent(c -> entity.setChecklist(toChecklistJpaEntity(c)));

        entity.setEtiquetas(tarjeta.getEtiquetas().stream()
                .map(e -> new EtiquetaJpaEmbeddable(e.nombre(), e.color()))
                .collect(Collectors.toList()));

        entity.setFechaLimite(tarjeta.getFechaLimite() != null ? tarjeta.getFechaLimite().date() : null);

        return entity;
    }

    private ChecklistJpaEntity toChecklistJpaEntity(Checklist checklist) {
        ChecklistJpaEntity entity = new ChecklistJpaEntity(
                checklist.getId().toString(),
                checklist.getNombre()
        );
        entity.setItems(checklist.getItems().stream()
                .map(i -> new ItemChecklistJpaEmbeddable(i.descripcion(), i.completado()))
                .collect(Collectors.toList()));
        return entity;
    }

    // -------------------------------------------------------------------------
    // JPA → Dominio
    // -------------------------------------------------------------------------

    public Tablero toDomain(TableroJpaEntity entity) {
        List<Lista> listas = entity.getListas().stream()
                .map(this::toListaDomain)
                .collect(Collectors.toList());

        List<Tarjeta> completadas = entity.getTarjetasCompletadas().stream()
                .map(this::toTarjetaDomain)
                .collect(Collectors.toList());

        List<Traza> historial = entity.getHistorial().stream()
                .map(te -> new Traza(te.getDescripcion(), te.getTimestamp()))
                .collect(Collectors.toList());

        return Tablero.reconstitute(
                TableroId.de(entity.getId()),
                entity.getNombre(),
                usuarioMapper.toDomain(entity.getPropietario()),
                entity.isBloqueado(),
                listas,
                completadas,
                historial
        );
    }

    private Lista toListaDomain(ListaJpaEntity entity) {
        Lista lista = new Lista(ListaId.de(entity.getId()), entity.getNombre());
        // agregarTarjeta() es público en Lista, no dispara efectos secundarios
        entity.getTarjetas().forEach(te -> lista.agregarTarjeta(toTarjetaDomain(te)));
        return lista;
    }

    private Tarjeta toTarjetaDomain(TarjetaJpaEntity entity) {
        Tarjeta tarjeta = new Tarjeta(TarjetaId.de(entity.getId()), entity.getTitulo());
        tarjeta.cambiarDescripcion(entity.getDescripcion());

        TareaJpaEmbeddable tareaEmb = entity.getTarea();
        if (tareaEmb != null && tareaEmb.getTitulo() != null) {
            tarjeta.asignarTarea(new Tarea(
                    tareaEmb.getTitulo(),
                    tareaEmb.getDescripcion(),
                    tareaEmb.getFechaLimite(),
                    EstadoTarea.valueOf(tareaEmb.getEstado())
            ));
        }

        if (entity.getChecklist() != null) {
            tarjeta.asignarChecklist(toChecklistDomain(entity.getChecklist()));
        }

        entity.getEtiquetas().forEach(e ->
                tarjeta.asignarEtiqueta(new Etiqueta(e.getNombre(), e.getColor()))
        );

        if (entity.getFechaLimite() != null) {
            tarjeta.asignarFechaLimite(FechaLimite.reconstitute(entity.getFechaLimite()));
        }

        if (entity.isCompletada()) {
            tarjeta.marcarCompletada();
        }

        return tarjeta;
    }

    private Checklist toChecklistDomain(ChecklistJpaEntity entity) {
        try {
            List<ItemChecklist> items = entity.getItems().stream()
                    .map(i -> {
                        ItemChecklist item = ItemChecklist.nuevo(i.getDescripcion());
                        return i.isCompletado() ? item.marcarCompletado() : item;
                    })
                    .collect(Collectors.toList());

            return Checklist.reconstitute(ChecklistId.de(entity.getId()), entity.getNombre(), items);
        } catch (ChecklistException e) {
            throw new IllegalStateException("Error al reconstruir Checklist desde la base de datos", e);
        }
    }
}
