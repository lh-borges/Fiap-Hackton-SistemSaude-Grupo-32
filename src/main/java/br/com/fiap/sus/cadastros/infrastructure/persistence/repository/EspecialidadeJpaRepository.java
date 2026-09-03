package br.com.fiap.sus.cadastros.infrastructure.persistence.repository;

import br.com.fiap.sus.cadastros.infrastructure.persistence.entity.EspecialidadeEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecialidadeJpaRepository extends JpaRepository<EspecialidadeEntity, UUID> {

    boolean existsByNomeIgnoreCase(String nome);

    Page<EspecialidadeEntity> findByAtivo(boolean ativo, Pageable paginacao);
}
