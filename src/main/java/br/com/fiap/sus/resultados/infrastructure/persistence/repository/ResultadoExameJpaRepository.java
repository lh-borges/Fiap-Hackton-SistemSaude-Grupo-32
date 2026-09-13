package br.com.fiap.sus.resultados.infrastructure.persistence.repository;

import br.com.fiap.sus.resultados.infrastructure.persistence.entity.ResultadoExameEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ResultadoExameJpaRepository
        extends JpaRepository<ResultadoExameEntity, UUID>, JpaSpecificationExecutor<ResultadoExameEntity> {

    boolean existsByExameId(UUID exameId);

    Optional<ResultadoExameEntity> findByExameId(UUID exameId);
}