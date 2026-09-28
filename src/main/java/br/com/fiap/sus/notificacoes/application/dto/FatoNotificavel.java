package br.com.fiap.sus.notificacoes.application.dto;

import br.com.fiap.sus.notificacoes.domain.enums.TipoNotificacao;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Entrada do processador de fatos: o evento ja traduzido para o vocabulario do modulo.
 *
 * @param eventoId      chave de deduplicacao (envelope do evento)
 * @param tipoEvento    nome simples da classe do evento, para o marcador de processamento
 * @param tipo          tipo da notificacao a gerar
 * @param referenciaId  registro de origem para navegacao (HU-06)
 * @param destinatarios um por usuario a avisar (RN-02)
 * @param parametros    dados nao clinicos usados na mensagem (datas, contagens)
 */
public record FatoNotificavel(UUID eventoId, String tipoEvento, TipoNotificacao tipo, UUID referenciaId,
                              List<Destinatario> destinatarios, Map<String, Object> parametros) {

    public FatoNotificavel {
        destinatarios = destinatarios == null ? List.of() : List.copyOf(destinatarios);
        parametros = parametros == null ? Map.of() : Map.copyOf(parametros);
    }
}
