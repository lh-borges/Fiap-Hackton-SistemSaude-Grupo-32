package br.com.fiap.sus.receitas.infrastructure.persistence.repository;

import br.com.fiap.sus.receitas.infrastructure.persistence.entity.ReceitaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReceitaJpaRepository
        extends JpaRepository<ReceitaEntity, UUID>, JpaSpecificationExecutor<ReceitaEntity> {
}