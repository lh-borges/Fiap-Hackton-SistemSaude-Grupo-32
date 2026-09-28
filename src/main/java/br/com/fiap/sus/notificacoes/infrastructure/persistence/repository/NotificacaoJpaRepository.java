package br.com.fiap.sus.notificacoes.infrastructure.persistence.repository;

import br.com.fiap.sus.notificacoes.infrastructure.persistence.entity.NotificacaoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface NotificacaoJpaRepository extends Repository<NotificacaoEntity, UUID> {

    NotificacaoEntity save(NotificacaoEntity entity);

    Optional<NotificacaoEntity> findById(UUID id);

    Page<NotificacaoEntity> findByUsuarioId(UUID usuarioId, Pageable pageable);

    Page<NotificacaoEntity> findByUsuarioIdAndLida(UUID usuarioId, boolean lida, Pageable pageable);

    long countByUsuarioIdAndLidaFalse(UUID usuarioId);
}
