package umu.pds.app.domain.ports.output;

import umu.pds.app.domain.modelo.tablero.Tablero;
import umu.pds.app.domain.modelo.shared.TableroId;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: define el contrato de persistencia del Aggregate Root Tablero.
 * El dominio declara esta interfaz; la infraestructura la implementa.
 */
public interface TableroRepository {

    void guardar(Tablero tablero);

    Optional<Tablero> buscarPorId(TableroId id);

    List<Tablero> buscarPorEmail(String email);

    void eliminar(TableroId id);
}
