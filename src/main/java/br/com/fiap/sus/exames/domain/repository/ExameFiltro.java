package br.com.fiap.sus.exames.domain.repository;

import br.com.fiap.sus.exames.domain.enums.SituacaoExame;
import java.time.Instant;
import java.util.UUID;

public record ExameFiltro(UUID pacienteId, UUID unidadeSaudeId, SituacaoExame situacao,
                          Instant periodoInicio, Instant periodoFim) {
}