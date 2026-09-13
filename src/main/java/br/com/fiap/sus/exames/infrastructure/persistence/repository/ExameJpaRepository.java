package br.com.fiap.sus.exames.infrastructure.persistence.repository;

import br.com.fiap.sus.exames.domain.enums.SituacaoExame;
import br.com.fiap.sus.exames.infrastructure.persistence.entity.ExameEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ExameJpaRepository
        extends JpaRepository<ExameEntity, UUID>, JpaSpecificationExecutor<ExameEntity> {

    Optional<ExameEntity> findBySolicitacaoExameId(UUID solicitacaoExameId);

    boolean existsBySolicitacaoExameIdAndSituacaoNot(UUID solicitacaoExameId, SituacaoExame situacaoExcluida);
}