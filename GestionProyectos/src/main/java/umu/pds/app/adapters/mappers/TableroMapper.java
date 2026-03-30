package umu.pds.app.adapters.mappers;

import org.springframework.stereotype.Component;
import umu.pds.app.adapters.jpa.entity.*;
import umu.pds.app.domain.exceptions.CheckListException;
import umu.pds.app.domain.modelo.shared.ChecklistId;
import umu.pds.app.domain.modelo.shared.ListaId;
import umu.pds.app.domain.modelo.shared.TableroId;
import umu.pds.app.domain.modelo.shared.TarjetaId;
import umu.pds.app.domain.modelo.tablero.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Convierte entre el modelo de dominio puro y las entidades JPA.
 * El dominio NUNCA importa Jakarta; toda la traducción vive aquí.
 *
 * La reconstrucción de Tablero usa reflexión para inyectar datos en los
 * campos privados sin disparar los métodos de negocio (que generarían
 * entradas de historial falsas). Es la técnica estándar cuando el Aggregate
 * Root no expone un constructor de reconstrucción y el dominio no puede
 * modificarse.
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

        entity.setFechaVencimiento(tarjeta.getFechaVencimiento());

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
        Tablero tablero = new Tablero(
                TableroId.de(entity.getId()),
                entity.getNombre(),
                usuarioMapper.toDomain(entity.getPropietario())
        );

        try {
            // Restaurar listas (sin pasar por agregarLista() que escribiría historial)
            Field listasField = Tablero.class.getDeclaredField("listas");
            listasField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Lista> listas = (List<Lista>) listasField.get(tablero);
            entity.getListas().forEach(le -> listas.add(toListaDomain(le)));

            // Restaurar tarjetas completadas
            Field completadasField = Tablero.class.getDeclaredField("tarjetasCompletadas");
            completadasField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Tarjeta> completadas = (List<Tarjeta>) completadasField.get(tablero);
            entity.getTarjetasCompletadas().forEach(te -> completadas.add(toTarjetaDomain(te)));

            // Restaurar historial real
            Field historialField = Tablero.class.getDeclaredField("historial");
            historialField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Traza> historial = (List<Traza>) historialField.get(tablero);
            entity.getHistorial().forEach(te ->
                    historial.add(new Traza(te.getDescripcion(), te.getTimestamp()))
            );

            // Restaurar estado de bloqueo
            Field bloqueadoField = Tablero.class.getDeclaredField("bloqueado");
            bloqueadoField.setAccessible(true);
            bloqueadoField.setBoolean(tablero, entity.isBloqueado());

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Error al reconstruir Tablero desde la base de datos", e);
        }

        return tablero;
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

        if (entity.getFechaVencimiento() != null) {
            tarjeta.asignarFechaVencimiento(entity.getFechaVencimiento());
        }

        if (entity.isCompletada()) {
            tarjeta.marcarCompletada();
        }

        return tarjeta;
    }

    private Checklist toChecklistDomain(ChecklistJpaEntity entity) {
        try {
            Checklist checklist = new Checklist(ChecklistId.de(entity.getId()), entity.getNombre());

            // Accedemos al ArrayList interno para poblar ítems con su estado real
            // (agregarItem() siempre crea ítems como no-completados)
            Field itemsField = Checklist.class.getDeclaredField("items");
            itemsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<ItemChecklist> items = (List<ItemChecklist>) itemsField.get(checklist);

            entity.getItems().forEach(i -> {
                ItemChecklist item = ItemChecklist.nuevo(i.getDescripcion());
                items.add(i.isCompletado() ? item.marcarCompletado() : item);
            });

            return checklist;
        } catch (CheckListException | NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Error al reconstruir Checklist desde la base de datos", e);
        }
    }
}
