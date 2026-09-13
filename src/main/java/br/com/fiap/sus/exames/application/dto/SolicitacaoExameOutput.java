package br.com.fiap.sus.exames.application.dto;

import br.com.fiap.sus.exames.domain.enums.SituacaoSolicitacaoExame;
import br.com.fiap.sus.exames.domain.model.SolicitacaoExame;
import java.util.UUID;

public record SolicitacaoExameOutput(UUID id, UUID pacienteId, UUID medicoId, UUID tipoExameId,
                                     UUID consultaId, String justificativa,
                                     SituacaoSolicitacaoExame situacao, String motivoCancelamento) {

    public static SolicitacaoExameOutput de(SolicitacaoExame s) {
        return new SolicitacaoExameOutput(s.getId(), s.getPacienteId(), s.getMedicoId(), s.getTipoExameId(),
                s.getConsultaId(), s.getJustificativa(), s.getSituacao(), s.getMotivoCancelamento());
    }
}