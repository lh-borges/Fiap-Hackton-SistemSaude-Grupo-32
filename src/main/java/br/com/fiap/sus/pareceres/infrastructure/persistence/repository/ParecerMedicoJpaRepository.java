package br.com.fiap.sus.pareceres.infrastructure.persistence.repository;

import br.com.fiap.sus.pareceres.infrastructure.persistence.entity.ParecerMedicoEntity;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface ParecerMedicoJpaRepository
        extends Repository<ParecerMedicoEntity, UUID>, JpaSpecificationExecutor<ParecerMedicoEntity> {

    @Query("select distinct p.resultadoExameId from ParecerMedicoEntity p where p.resultadoExameId in :ids")
    Set<UUID> resultadosComParecer(@Param("ids") Set<UUID> ids);

    Optional<ParecerMedicoEntity> findById(UUID id);

    boolean existsByResultadoExameId(UUID resultadoExameId);
}
