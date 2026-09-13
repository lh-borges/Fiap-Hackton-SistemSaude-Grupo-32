package br.com.fiap.sus.consultas.domain.repository;

import br.com.fiap.sus.consultas.domain.enums.SituacaoConsulta;
import java.time.Instant;
import java.util.UUID;

public record ConsultaFiltro(UUID pacienteId, UUID medicoId, UUID unidadeSaudeId,
                             SituacaoConsulta situacao, Instant periodoInicio, Instant periodoFim) {
}