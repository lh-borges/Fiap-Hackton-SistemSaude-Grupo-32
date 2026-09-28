package br.com.fiap.sus.historico.domain.model;

import br.com.fiap.sus.historico.domain.enums.TipoRegistro;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RegistroHistorico")
class RegistroHistoricoTest {
    final Instant agora = Instant.parse("2026-09-28T12:00:00Z");

    @Test
    @DisplayName("RF-03: item completo expoe recurso do tipo e origem opcional")
    void itemCompleto() {
        var origem = new Referencia(TipoRegistro.EXAME, UUID.randomUUID());
        var r = new RegistroHistorico(TipoRegistro.RESULTADO_EXAME, UUID.randomUUID(), agora,
                "Resultado de exame: imagem", "DISPONIVEL", null, origem);

        assertThat(r.recurso()).isEqualTo("resultados-exame");
        assertThat(r.origem()).isEqualTo(origem);
        assertThat(r.medicoId()).isNull();
    }

    @Test
    @DisplayName("tipo, id, data, titulo e situacao sao obrigatorios")
    void obrigatorios() {
        var id = UUID.randomUUID();
        assertThatThrownBy(() -> new RegistroHistorico(null, id, agora, "t", "S", null, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new RegistroHistorico(TipoRegistro.CONSULTA, null, agora, "t", "S", null, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new RegistroHistorico(TipoRegistro.CONSULTA, id, null, "t", "S", null, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new RegistroHistorico(TipoRegistro.CONSULTA, id, agora, " ", "S", null, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Titulo");
        assertThatThrownBy(() -> new RegistroHistorico(TipoRegistro.CONSULTA, id, agora, "x".repeat(151), "S", null, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("150");
        assertThatThrownBy(() -> new RegistroHistorico(TipoRegistro.CONSULTA, id, agora, "t", null, null, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Situacao");
    }

    @Test
    @DisplayName("RN-02/RN-07: ordem e data desc, depois tipo na ordem do atendimento, depois id")
    void ordem() {
        var idMenor = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var idMaior = UUID.fromString("00000000-0000-0000-0000-000000000002");
        var recente = registro(TipoRegistro.DOCUMENTO, idMaior, agora.plusSeconds(10));
        var consultaMesmaHora = registro(TipoRegistro.CONSULTA, idMaior, agora);
        var exameMesmaHora = registro(TipoRegistro.EXAME, idMenor, agora);
        var exameMesmaHoraIdMaior = registro(TipoRegistro.EXAME, idMaior, agora);

        assertThat(RegistroHistorico.ORDEM.compare(recente, consultaMesmaHora)).isNegative();
        assertThat(RegistroHistorico.ORDEM.compare(consultaMesmaHora, exameMesmaHora)).isNegative();
        assertThat(RegistroHistorico.ORDEM.compare(exameMesmaHora, exameMesmaHoraIdMaior)).isNegative();
    }

    @Test
    @DisplayName("Referencia exige tipo e id; opcional devolve null sem id")
    void referencia() {
        assertThat(Referencia.opcional(TipoRegistro.CONSULTA, null)).isNull();
        assertThat(Referencia.opcional(TipoRegistro.CONSULTA, UUID.randomUUID())).isNotNull();
        assertThatThrownBy(() -> new Referencia(null, UUID.randomUUID())).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Referencia(TipoRegistro.CONSULTA, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("todo tipo conhece o recurso REST do modulo dono")
    void tiposComRecurso() {
        for (var tipo : TipoRegistro.values()) {
            assertThat(tipo.recurso()).isNotBlank().doesNotContain("/");
        }
        assertThat(TipoRegistro.SOLICITACAO_EXAME.recurso()).isEqualTo("solicitacoes-exame");
    }

    static RegistroHistorico registro(TipoRegistro tipo, UUID id, Instant data) {
        return new RegistroHistorico(tipo, id, data, "Titulo", "SITUACAO", null, null);
    }
}
