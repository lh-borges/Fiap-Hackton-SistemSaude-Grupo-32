package br.com.fiap.sus.exames.application.dto;

import br.com.fiap.sus.exames.domain.enums.SituacaoExame;
import br.com.fiap.sus.exames.domain.model.Exame;
import java.time.Instant;
import java.util.UUID;

public record ExameOutput(UUID id, UUID solicitacaoExameId, UUID unidadeSaudeId, Instant dataAgendada,
                          Instant dataRealizacao, SituacaoExame situacao) {

    public static ExameOutput de(Exame e) {
        return new ExameOutput(e.getId(), e.getSolicitacaoExameId(), e.getUnidadeSaudeId(),
                e.getDataAgendada(), e.getDataRealizacao(), e.getSituacao());
    }
}