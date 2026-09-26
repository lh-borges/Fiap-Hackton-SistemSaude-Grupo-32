package br.com.fiap.sus.notificacoes.infrastructure.persistence.repository;

import br.com.fiap.sus.notificacoes.infrastructure.persistence.entity.EventoProcessadoEntity;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface EventoProcessadoJpaRepository extends Repository<EventoProcessadoEntity, UUID> {

    boolean existsById(UUID eventoId);
}
