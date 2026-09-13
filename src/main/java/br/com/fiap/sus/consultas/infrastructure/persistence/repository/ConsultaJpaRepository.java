package br.com.fiap.sus.consultas.infrastructure.persistence.repository;

import br.com.fiap.sus.consultas.infrastructure.persistence.entity.ConsultaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ConsultaJpaRepository
        extends JpaRepository<ConsultaEntity, UUID>, JpaSpecificationExecutor<ConsultaEntity> {
}