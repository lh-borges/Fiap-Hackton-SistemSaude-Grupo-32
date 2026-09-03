package br.com.fiap.sus.cadastros.infrastructure.persistence.repository;

import br.com.fiap.sus.cadastros.infrastructure.persistence.entity.MedicoEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MedicoJpaRepository extends JpaRepository<MedicoEntity, UUID>,
        JpaSpecificationExecutor<MedicoEntity> {

    Optional<MedicoEntity> findByUsuarioId(UUID usuarioId);

    boolean existsByCrmAndUfCrm(String crm, String ufCrm);

    boolean existsByUsuarioId(UUID usuarioId);
}
