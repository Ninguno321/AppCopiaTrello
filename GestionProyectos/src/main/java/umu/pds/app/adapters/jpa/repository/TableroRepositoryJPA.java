package umu.pds.app.adapters.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umu.pds.app.adapters.jpa.entity.TableroJpaEntity;

import java.util.List;

public interface TableroRepositoryJPA extends JpaRepository<TableroJpaEntity, String> {

    List<TableroJpaEntity> findByPropietarioEmail(String email);
}
