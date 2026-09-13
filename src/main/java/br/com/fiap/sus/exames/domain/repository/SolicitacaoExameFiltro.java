package br.com.fiap.sus.exames.domain.repository;

import br.com.fiap.sus.exames.domain.enums.SituacaoSolicitacaoExame;
import java.time.Instant;
import java.util.UUID;

public record SolicitacaoExameFiltro(UUID pacienteId, UUID medicoId, UUID tipoExameId,
                                     SituacaoSolicitacaoExame situacao, Instant periodoInicio,
                                     Instant periodoFim) {
}