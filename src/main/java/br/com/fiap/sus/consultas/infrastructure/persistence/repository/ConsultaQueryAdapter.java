package br.com.fiap.sus.consultas.infrastructure.persistence.repository;

import br.com.fiap.sus.consultas.api.ConsultaQuery;
import br.com.fiap.sus.consultas.domain.enums.SituacaoConsulta;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class ConsultaQueryAdapter implements ConsultaQuery {
    private final ConsultaJpaRepository repository;

    public ConsultaQueryAdapter(ConsultaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Set<UUID> pacientesDoMedico(UUID medicoId) {
        return repository.pacientesDoMedico(medicoId, SituacaoConsulta.CANCELADA);
    }

    @Override
    public Optional<UUID> pacienteIdDaConsulta(UUID consultaId) {
        return repository.pacienteIdDaConsulta(consultaId);
    }

    @Override
    public Optional<Instant> dataHoraDaConsulta(UUID consultaId) {
        return repository.dataHoraDaConsulta(consultaId);
    }
}
