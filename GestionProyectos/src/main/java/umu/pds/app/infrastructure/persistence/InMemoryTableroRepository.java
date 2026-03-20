package umu.pds.app.infrastructure.persistence;

import umu.pds.app.domain.modelo.shared.TableroId;
import umu.pds.app.domain.modelo.tablero.Tablero;
import umu.pds.app.domain.ports.output.TableroRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Adaptador de salida: implementación en memoria de TableroRepository.
 * Sustituye a la base de datos durante el desarrollo. Los datos se pierden al reiniciar.
 * Cuando llegue el hito de JPA, se creará JpaTableroRepository sin tocar nada más.
 */
public class InMemoryTableroRepository implements TableroRepository {

    private final Map<TableroId, Tablero> almacen = new HashMap<>();

    @Override
    public void guardar(Tablero tablero) {
        almacen.put(tablero.getId(), tablero);
    }

    @Override
    public Optional<Tablero> buscarPorId(TableroId id) {
        return Optional.ofNullable(almacen.get(id));
    }

    @Override
    public List<Tablero> buscarPorEmail(String email) {
        List<Tablero> resultado = new ArrayList<>();
        for (Tablero tablero : almacen.values()) {
            if (tablero.getEmailPropietario().equalsIgnoreCase(email)) {
                resultado.add(tablero);
            }
        }
        return resultado;
    }

    @Override
    public void eliminar(TableroId id) {
        almacen.remove(id);
    }
}
