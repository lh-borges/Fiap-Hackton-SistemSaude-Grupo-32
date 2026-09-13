package br.com.fiap.sus.exames.infrastructure.persistence.repository;

import br.com.fiap.sus.exames.infrastructure.persistence.entity.SolicitacaoExameEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SolicitacaoExameJpaRepository
        extends JpaRepository<SolicitacaoExameEntity, UUID>, JpaSpecificationExecutor<SolicitacaoExameEntity> {
}