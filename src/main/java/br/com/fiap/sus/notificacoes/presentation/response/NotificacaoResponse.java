package br.com.fiap.sus.notificacoes.presentation.response;

import br.com.fiap.sus.notificacoes.application.dto.NotificacaoOutput;
import br.com.fiap.sus.notificacoes.domain.enums.TipoNotificacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "NotificacaoResponse", description = "Aviso ao usuario; nunca contem dado clinico")
public record NotificacaoResponse(
        UUID id,
        TipoNotificacao tipo,
        @Schema(example = "Consulta agendada") String titulo,
        @Schema(example = "Sua consulta foi agendada para 30/09/2026 14:00.") String mensagem,
        @Schema(description = "Registro de origem (consulta, exame, resultado, parecer, receita ou documento)")
        UUID referenciaId,
        @Schema(description = "Recurso da API onde o registro de origem pode ser consultado", example = "consultas")
        String recurso,
        boolean lida,
        Instant dataLeitura,
        Instant criadoEm) {

    public static NotificacaoResponse of(NotificacaoOutput o) {
        return new NotificacaoResponse(o.id(), o.tipo(), o.titulo(), o.mensagem(), o.referenciaId(), o.recurso(),
                o.lida(), o.dataLeitura(), o.criadoEm());
    }
}
