package br.com.fiap.sus.notificacoes.infrastructure.persistence.repository;

import br.com.fiap.sus.notificacoes.domain.repository.EventoProcessadoRepository;
import br.com.fiap.sus.notificacoes.infrastructure.persistence.entity.EventoProcessadoEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class EventoProcessadoRepositoryAdapter implements EventoProcessadoRepository {

    private final EventoProcessadoJpaRepository repository;
    private final EntityManager entityManager;

    public EventoProcessadoRepositoryAdapter(EventoProcessadoJpaRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Override
    public boolean jaProcessado(UUID eventoId) {
        return repository.existsById(eventoId);
    }

    /**
     * Persiste e descarrega imediatamente: se outro processamento do mesmo evento chegou
     * antes, a violacao da chave primaria aparece aqui, ainda dentro da transacao do
     * listener, e nao no commit.
     */
    @Override
    @Transactional
    public void registrar(UUID eventoId, String tipoEvento, Instant processadoEm) {
        entityManager.persist(new EventoProcessadoEntity(eventoId, tipoEvento, processadoEm));
        entityManager.flush();
    }
}
