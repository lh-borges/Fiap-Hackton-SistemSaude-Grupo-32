package br.com.fiap.sus.consultas.presentation.response;

import br.com.fiap.sus.consultas.application.dto.ConsultaOutput;
import br.com.fiap.sus.consultas.domain.enums.SituacaoConsulta;
import java.time.Instant;
import java.util.UUID;

public record ConsultaResponse(UUID id, UUID pacienteId, UUID medicoId, UUID unidadeSaudeId,
                               Instant dataHora, SituacaoConsulta situacao, String motivo,
                               String observacoes, String motivoCancelamento, boolean remarcada) {

    public static ConsultaResponse de(ConsultaOutput output) {
        return new ConsultaResponse(output.id(), output.pacienteId(), output.medicoId(),
                output.unidadeSaudeId(), output.dataHora(), output.situacao(), output.motivo(),
                output.observacoes(), output.motivoCancelamento(), output.remarcada());
    }
}