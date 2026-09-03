package br.com.fiap.sus.cadastros.infrastructure.persistence.repository;

import br.com.fiap.sus.cadastros.infrastructure.persistence.entity.UnidadeSaudeEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnidadeSaudeJpaRepository extends JpaRepository<UnidadeSaudeEntity, UUID> {

    boolean existsByCnes(String cnes);

    Page<UnidadeSaudeEntity> findByAtivo(boolean ativo, Pageable paginacao);
}
