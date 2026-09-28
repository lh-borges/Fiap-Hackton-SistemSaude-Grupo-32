package br.com.fiap.sus.exames.infrastructure.persistence.repository;

import br.com.fiap.sus.exames.api.ExameQuery;
import br.com.fiap.sus.exames.api.ExameResumo;
import br.com.fiap.sus.exames.api.SolicitacaoExameResumo;
import br.com.fiap.sus.exames.infrastructure.persistence.entity.SolicitacaoExameEntity;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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

    @Override
    public Optional<UUID> medicoSolicitanteIdDoExame(UUID exameId) {
        return exameJpa.findById(exameId)
                .flatMap(exame -> solicitacaoJpa.findById(exame.getSolicitacaoExameId()))
                .map(s -> s.getMedicoId());
    }

    @Override
    public List<SolicitacaoExameResumo> solicitacoesDoPaciente(UUID pacienteId) {
        return solicitacaoJpa.findByPacienteIdOrderByCriadoEmDesc(pacienteId).stream()
                .map(s -> new SolicitacaoExameResumo(s.getId(), s.getPacienteId(), s.getMedicoId(),
                        s.getTipoExameId(), s.getConsultaId(), s.getSituacao().name(), s.getCriadoEm()))
                .toList();
    }

    @Override
    public List<ExameResumo> examesDoPaciente(UUID pacienteId) {
        Map<UUID, SolicitacaoExameEntity> solicitacoes = solicitacaoJpa
                .findByPacienteIdOrderByCriadoEmDesc(pacienteId).stream()
                .collect(Collectors.toMap(SolicitacaoExameEntity::getId, Function.identity()));
        if (solicitacoes.isEmpty()) {
            return List.of();
        }
        return exameJpa.findBySolicitacaoExameIdIn(solicitacoes.keySet()).stream()
                .map(e -> {
                    var solicitacao = solicitacoes.get(e.getSolicitacaoExameId());
                    return new ExameResumo(e.getId(), e.getSolicitacaoExameId(), solicitacao.getPacienteId(),
                            solicitacao.getMedicoId(), e.getUnidadeSaudeId(), e.getDataAgendada(),
                            e.getDataRealizacao(), e.getSituacao().name());
                })
                .sorted(Comparator.comparing(
                        (ExameResumo r) -> r.dataRealizacao() != null ? r.dataRealizacao() : r.dataAgendada(),
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }
}
