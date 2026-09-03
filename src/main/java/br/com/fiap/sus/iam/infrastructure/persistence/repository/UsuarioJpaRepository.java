package br.com.fiap.sus.iam.infrastructure.persistence.repository;

import br.com.fiap.sus.iam.infrastructure.persistence.entity.UsuarioEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID>,
        JpaSpecificationExecutor<UsuarioEntity> {

    Optional<UsuarioEntity> findByEmail(String email);

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);
}
