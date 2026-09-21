package br.com.fiap.sus.documentos.infrastructure.persistence.repository;

import br.com.fiap.sus.documentos.infrastructure.persistence.entity.DocumentoMedicoEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.Repository;

public interface DocumentoMedicoJpaRepository
        extends Repository<DocumentoMedicoEntity, UUID>, JpaSpecificationExecutor<DocumentoMedicoEntity> {

    DocumentoMedicoEntity save(DocumentoMedicoEntity entity);

    Optional<DocumentoMedicoEntity> findById(UUID id);

    boolean existsByConsultaId(UUID consultaId);
}
