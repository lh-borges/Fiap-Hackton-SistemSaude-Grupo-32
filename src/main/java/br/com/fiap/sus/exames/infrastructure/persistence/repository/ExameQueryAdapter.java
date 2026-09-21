package br.com.fiap.sus.exames.infrastructure.persistence.repository;

import br.com.fiap.sus.exames.api.ExameQuery;
import br.com.fiap.sus.exames.domain.enums.SituacaoExame;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ExameQueryAdapter implements ExameQuery {

    private final ExameJpaRepository exameJpa;
    private final SolicitacaoExameJpaRepository solicitacaoJpa;

    public ExameQueryAdapter(ExameJpaRepository exameJpa, SolicitacaoExameJpaRepository solicitacaoJpa) {
        this.exameJpa = exameJpa;
        this.solicitacaoJpa = solicitacaoJpa;
    }

    @Override
    public boolean exameRealizadoExiste(UUID exameId) {
        return exameJpa.findById(exameId)
                .map(e -> e.getSituacao() == SituacaoExame.REALIZADO)
                .orElse(false);
    }

    @Override
    public Optional<Instant> dataRealizacaoDoExame(UUID exameId) {
        return exameJpa.findById(exameId)
                .map(e -> e.getDataRealizacao());
    }

    @Override
    public Optional<UUID> pacienteIdDoExame(UUID exameId) {
        return exameJpa.findById(exameId)
                .flatMap(exame -> solicitacaoJpa.findById(exame.getSolicitacaoExameId()))
                .map(s -> s.getPacienteId());
    }

    @Override
    public Optional<UUID> tipoExameIdDoExame(UUID exameId) {
        return exameJpa.findById(exameId)
                .flatMap(exame -> solicitacaoJpa.findById(exame.getSolicitacaoExameId()))
                .map(s -> s.getTipoExameId());
    }
}
