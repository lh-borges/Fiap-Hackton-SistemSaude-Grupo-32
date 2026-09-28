package br.com.fiap.sus.notificacoes.application.mensagem;

import br.com.fiap.sus.notificacoes.application.dto.PapelDestinatario;
import br.com.fiap.sus.notificacoes.domain.enums.TipoNotificacao;
import br.com.fiap.sus.notificacoes.domain.model.Notificacao;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Catalogo de mensagens (RN-05)")
class CatalogoMensagensTest {
    final CatalogoMensagens catalogo = new CatalogoMensagens();

    /** Valores que jamais podem aparecer numa mensagem, mesmo que alguem os passe como parametro. */
    static final String SENTINELA = "DADO-CLINICO-SENTINELA";

    Map<String, Object> parametrosCompletos() {
        var p = new HashMap<String, Object>();
        p.put(CatalogoMensagens.DATA_HORA, Instant.parse("2026-09-30T17:00:00Z"));
        p.put(CatalogoMensagens.DATA_HORA_ANTERIOR, Instant.parse("2026-09-30T17:00:00Z"));
        p.put(CatalogoMensagens.DATA_HORA_NOVA, Instant.parse("2026-10-02T12:30:00Z"));
        p.put(CatalogoMensagens.DATA_AGENDADA, Instant.parse("2026-10-05T11:00:00Z"));
        p.put(CatalogoMensagens.VALIDADE, Instant.parse("2026-12-31T02:59:59Z"));
        p.put(CatalogoMensagens.QUANTIDADE_ITENS, 3);
        // parametros clinicos que um produtor descuidado poderia enviar: sao ignorados
        p.put("laudo", SENTINELA);
        p.put("resultado", SENTINELA);
        p.put("diagnostico", SENTINELA);
        p.put("medicamento", SENTINELA);
        p.put("descricao", SENTINELA);
        return p;
    }

    @ParameterizedTest
    @EnumSource(TipoNotificacao.class)
    @DisplayName("todo tipo tem titulo e mensagem para paciente e medico, dentro dos limites e sem dado clinico")
    void todoTipoTemMensagemSegura(TipoNotificacao tipo) {
        for (PapelDestinatario papel : PapelDestinatario.values()) {
            var m = catalogo.montar(tipo, papel, parametrosCompletos());
            assertThat(m.titulo()).as("%s/%s titulo", tipo, papel).isNotBlank().hasSizeLessThanOrEqualTo(Notificacao.TITULO_MAX);
            assertThat(m.texto()).as("%s/%s texto", tipo, papel).isNotBlank().hasSizeLessThanOrEqualTo(Notificacao.MENSAGEM_MAX);
            assertThat(m.titulo() + m.texto()).doesNotContain(SENTINELA).doesNotContain("{").doesNotContain("null");
        }
    }

    @Test
    @DisplayName("datas sao apresentadas no fuso de Sao Paulo")
    void formataDatasNoFusoDeSaoPaulo() {
        var paciente = catalogo.montar(TipoNotificacao.CONSULTA_REMARCADA, PapelDestinatario.PACIENTE, parametrosCompletos());
        assertThat(paciente.texto()).isEqualTo("Sua consulta de 30/09/2026 14:00 foi remarcada para 02/10/2026 09:30.");

        var receita = catalogo.montar(TipoNotificacao.RECEITA_EMITIDA, PapelDestinatario.PACIENTE, parametrosCompletos());
        assertThat(receita.texto()).contains("30/12/2026");
    }

    @Test
    void aceitaDataPuraEDegradaQuandoFaltaParametro() {
        var comLocalDate = catalogo.montar(TipoNotificacao.RECEITA_EMITIDA, PapelDestinatario.PACIENTE,
                Map.of(CatalogoMensagens.VALIDADE, LocalDate.of(2026, 12, 31)));
        assertThat(comLocalDate.texto()).contains("31/12/2026");

        var semParametro = catalogo.montar(TipoNotificacao.CONSULTA_AGENDADA, PapelDestinatario.PACIENTE, Map.of());
        assertThat(semParametro.texto()).isEqualTo("Sua consulta foi agendada para data a confirmar.");
    }

    @Test
    @DisplayName("paciente e medico recebem textos distintos nos fatos de consulta e resultado")
    void textosPorPapel() {
        var p = parametrosCompletos();
        for (TipoNotificacao tipo : new TipoNotificacao[]{TipoNotificacao.CONSULTA_AGENDADA,
                TipoNotificacao.CONSULTA_REMARCADA, TipoNotificacao.CONSULTA_CANCELADA, TipoNotificacao.RESULTADO_DISPONIVEL}) {
            assertThat(catalogo.montar(tipo, PapelDestinatario.PACIENTE, p).texto())
                    .as(tipo.name())
                    .isNotEqualTo(catalogo.montar(tipo, PapelDestinatario.MEDICO, p).texto());
        }
    }
}
