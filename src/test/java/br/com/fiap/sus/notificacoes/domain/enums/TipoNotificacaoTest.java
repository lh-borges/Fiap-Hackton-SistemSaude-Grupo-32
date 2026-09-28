package br.com.fiap.sus.notificacoes.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class TipoNotificacaoTest {

    @Test
    @DisplayName("RF-08: os dez fatos do atendimento tem um tipo")
    void cobreOsDezFatos() {
        assertThat(TipoNotificacao.values()).hasSize(10);
    }

    @Test
    @DisplayName("HU-06: todo tipo aponta para um recurso existente da API")
    void todoTipoApontaParaUmRecurso() {
        for (TipoNotificacao tipo : TipoNotificacao.values()) {
            assertThat(tipo.getRecurso()).as(tipo.name()).isNotBlank().doesNotContain("/");
        }
        assertThat(TipoNotificacao.CONSULTA_CANCELADA.getRecurso()).isEqualTo("consultas");
        assertThat(TipoNotificacao.RESULTADO_DISPONIVEL.getRecurso()).isEqualTo("resultados-exame");
        assertThat(TipoNotificacao.EXAME_SOLICITADO.getRecurso()).isEqualTo("solicitacoes-exame");
    }
}
