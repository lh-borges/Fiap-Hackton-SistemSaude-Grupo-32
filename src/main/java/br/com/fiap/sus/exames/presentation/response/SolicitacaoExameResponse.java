package br.com.fiap.sus.exames.presentation.response;

import br.com.fiap.sus.exames.application.dto.SolicitacaoExameOutput;
import br.com.fiap.sus.exames.domain.enums.SituacaoSolicitacaoExame;
import java.util.UUID;

public record SolicitacaoExameResponse(UUID id, UUID pacienteId, UUID medicoId, UUID tipoExameId,
                                       UUID consultaId, String justificativa,
                                       SituacaoSolicitacaoExame situacao, String motivoCancelamento) {

    public static SolicitacaoExameResponse de(SolicitacaoExameOutput output) {
        return new SolicitacaoExameResponse(output.id(), output.pacienteId(), output.medicoId(),
                output.tipoExameId(), output.consultaId(), output.justificativa(), output.situacao(),
                output.motivoCancelamento());
    }
}