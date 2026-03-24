package umu.pds.app.adapters.mappers;

import org.springframework.stereotype.Component;
import umu.pds.app.adapters.jpa.entity.UsuarioJpaEntity;
import umu.pds.app.domain.modelo.usuario.Usuario;

@Component
public class UsuarioMapper {

    public UsuarioJpaEntity toJpaEntity(Usuario usuario) {
        return new UsuarioJpaEntity(usuario.getEmail(), usuario.getNombre());
    }

    public Usuario toDomain(UsuarioJpaEntity entity) {
        return new Usuario(entity.getEmail(), entity.getNombre());
    }
}
