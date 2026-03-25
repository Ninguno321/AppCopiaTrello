package umu.pds.app.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import umu.pds.app.adapters.jpa.repository.UsuarioRepositoryJPA;
import umu.pds.app.adapters.mappers.UsuarioMapper;
import umu.pds.app.domain.modelo.usuario.Usuario;
import umu.pds.app.domain.ports.output.UsuarioRepository;

import java.util.Optional;

/**
 * Adaptador de salida: implementa el puerto UsuarioRepository del dominio
 * delegando en Spring Data JPA.
 */
@Repository
public class UsuarioJpaAdapter implements UsuarioRepository {

    private final UsuarioRepositoryJPA repositorioJpa;
    private final UsuarioMapper mapper;

    public UsuarioJpaAdapter(UsuarioRepositoryJPA repositorioJpa, UsuarioMapper mapper) {
        this.repositorioJpa = repositorioJpa;
        this.mapper = mapper;
    }

    @Override
    public void guardar(Usuario usuario) {
        repositorioJpa.save(mapper.toJpaEntity(usuario));
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return repositorioJpa.findById(email).map(mapper::toDomain);
    }
}
