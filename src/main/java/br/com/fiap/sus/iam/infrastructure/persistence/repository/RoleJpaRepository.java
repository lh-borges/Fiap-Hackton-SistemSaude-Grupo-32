package br.com.fiap.sus.iam.infrastructure.persistence.repository;

import br.com.fiap.sus.iam.domain.enums.RoleNome;
import br.com.fiap.sus.iam.infrastructure.persistence.entity.RoleEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, UUID> {

    Optional<RoleEntity> findByNome(RoleNome nome);
}
