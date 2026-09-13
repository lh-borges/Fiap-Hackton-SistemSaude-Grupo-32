package br.com.fiap.sus.consultas.application.dto;

import br.com.fiap.sus.consultas.domain.enums.SituacaoConsulta;
import br.com.fiap.sus.consultas.domain.model.Consulta;
import java.time.Instant;
import java.util.UUID;

public record ConsultaOutput(UUID id, UUID pacienteId, UUID medicoId, UUID unidadeSaudeId,
                             Instant dataHora, SituacaoConsulta situacao, String motivo,
                             String observacoes, String motivoCancelamento, boolean remarcada) {

    public static ConsultaOutput de(Consulta consulta) {
        return new ConsultaOutput(consulta.getId(), consulta.getPacienteId(), consulta.getMedicoId(),
                consulta.getUnidadeSaudeId(), consulta.getDataHora(), consulta.getSituacao(),
                consulta.getMotivo(), consulta.getObservacoes(), consulta.getMotivoCancelamento(),
                consulta.isRemarcada());
    }
}