package br.com.fiap.sus.exames.presentation.response;

import br.com.fiap.sus.exames.application.dto.ExameOutput;
import br.com.fiap.sus.exames.domain.enums.SituacaoExame;
import java.time.Instant;
import java.util.UUID;

public record ExameResponse(UUID id, UUID solicitacaoExameId, UUID unidadeSaudeId, Instant dataAgendada,
                            Instant dataRealizacao, SituacaoExame situacao) {

    public static ExameResponse de(ExameOutput output) {
        return new ExameResponse(output.id(), output.solicitacaoExameId(), output.unidadeSaudeId(),
                output.dataAgendada(), output.dataRealizacao(), output.situacao());
    }
}