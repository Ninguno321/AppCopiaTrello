package umu.pds.app.adapters.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umu.pds.app.adapters.jpa.entity.UsuarioJpaEntity;

public interface UsuarioRepositoryJPA extends JpaRepository<UsuarioJpaEntity, String> {
}
