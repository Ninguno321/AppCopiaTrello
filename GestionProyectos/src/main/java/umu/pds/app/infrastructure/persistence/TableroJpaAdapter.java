package umu.pds.app.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import umu.pds.app.adapters.jpa.repository.TableroRepositoryJPA;
import umu.pds.app.adapters.mappers.TableroMapper;
import umu.pds.app.domain.modelo.shared.TableroId;
import umu.pds.app.domain.modelo.tablero.Tablero;
import umu.pds.app.domain.ports.output.TableroRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de salida: implementa el puerto TableroRepository del dominio
 * delegando en Spring Data JPA y usando el mapper para cruzar la frontera
 * Dominio ↔ Persistencia.
 */
@Repository
public class TableroJpaAdapter implements TableroRepository {

    private final TableroRepositoryJPA repositorioJpa;
    private final TableroMapper mapper;

    public TableroJpaAdapter(TableroRepositoryJPA repositorioJpa, TableroMapper mapper) {
        this.repositorioJpa = repositorioJpa;
        this.mapper = mapper;
    }

    @Override
    public void guardar(Tablero tablero) {
        repositorioJpa.save(mapper.toJpaEntity(tablero));
    }

    @Override
    public Optional<Tablero> buscarPorId(TableroId id) {
        return repositorioJpa.findById(id.toString())
                .map(mapper::toDomain);
    }

    @Override
    public List<Tablero> buscarPorEmail(String email) {
        return repositorioJpa.findByPropietarioEmail(email).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminar(TableroId id) {
        repositorioJpa.deleteById(id.toString());
    }
}
