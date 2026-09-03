package br.com.fiap.sus.cadastros.infrastructure.persistence.repository;

import br.com.fiap.sus.cadastros.infrastructure.persistence.entity.PacienteEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PacienteJpaRepository extends JpaRepository<PacienteEntity, UUID>,
        JpaSpecificationExecutor<PacienteEntity> {

    Optional<PacienteEntity> findByUsuarioId(UUID usuarioId);

    boolean existsByCartaoSus(String cartaoSus);

    boolean existsByUsuarioId(UUID usuarioId);
}
