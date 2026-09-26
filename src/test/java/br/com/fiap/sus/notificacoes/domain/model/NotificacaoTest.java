package br.com.fiap.sus.notificacoes.domain.model;

import br.com.fiap.sus.notificacoes.domain.enums.TipoNotificacao;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Notificacao (dominio)")
class NotificacaoTest {
    private final UUID usuario = UUID.randomUUID();
    private final UUID evento = UUID.randomUUID();
    private final UUID referencia = UUID.randomUUID();

    private Notificacao nova() {
        return Notificacao.criar(usuario, evento, TipoNotificacao.CONSULTA_AGENDADA,
                "  Consulta agendada  ", " Sua consulta foi agendada. ", referencia);
    }

    @Test
    @DisplayName("HU-01: nasce nao lida, com identidade propria e textos normalizados")
    void nasceNaoLida() {
        var antes = Notificacao.agora();
        var n = nova();
        assertThat(n.getId()).isNotNull();
        assertThat(n.getUsuarioId()).isEqualTo(usuario);
        assertThat(n.getEventoId()).isEqualTo(evento);
        assertThat(n.getTipo()).isEqualTo(TipoNotificacao.CONSULTA_AGENDADA);
        assertThat(n.getTitulo()).isEqualTo("Consulta agendada");
        assertThat(n.getMensagem()).isEqualTo("Sua consulta foi agendada.");
        assertThat(n.getReferenciaId()).isEqualTo(referencia);
        assertThat(n.isLida()).isFalse();
        assertThat(n.getDataLeitura()).isNull();
        assertThat(n.getCriadoEm()).isBetween(antes, Instant.now());
        assertThat(n.getCriadoEm().getNano() % 1000).as("precisao de microssegundos").isZero();
        assertThat(n.getAtualizadoEm()).isEqualTo(n.getCriadoEm());
    }

    @Test
    @DisplayName("RN-04: a leitura registra a data uma unica vez e e irreversivel")
    void leituraIrreversivelEIdempotente() {
        var n = nova();
        var primeiraLeitura = Instant.parse("2026-09-25T10:00:00Z");
        assertThat(n.marcarComoLida(primeiraLeitura)).isTrue();
        assertThat(n.isLida()).isTrue();
        assertThat(n.getDataLeitura()).isEqualTo(primeiraLeitura);
        assertThat(n.getAtualizadoEm()).isEqualTo(primeiraLeitura);

        assertThat(n.marcarComoLida(Instant.parse("2026-09-26T10:00:00Z"))).isFalse();
        assertThat(n.getDataLeitura()).isEqualTo(primeiraLeitura);
    }

    @Test
    void leituraExigeInstante() {
        assertThatThrownBy(() -> nova().marcarComoLida(null)).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("HU-03: posse e verificada pelo usuario destinatario")
    void posse() {
        var n = nova();
        assertThat(n.pertenceA(usuario)).isTrue();
        assertThat(n.pertenceA(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("RN-01: exige destinatario, evento e tipo; referencia e opcional")
    void exigeReferenciasObrigatorias() {
        assertThatThrownBy(() -> Notificacao.criar(null, evento, TipoNotificacao.CONSULTA_AGENDADA, "t", "m", null))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> Notificacao.criar(usuario, null, TipoNotificacao.CONSULTA_AGENDADA, "t", "m", null))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> Notificacao.criar(usuario, evento, null, "t", "m", null))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatCode(() -> Notificacao.criar(usuario, evento, TipoNotificacao.PARECER_CRIADO, "t", "m", null))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejeitaTituloEMensagemVazios(String vazio) {
        assertThatThrownBy(() -> Notificacao.criar(usuario, evento, TipoNotificacao.CONSULTA_AGENDADA, vazio, "m", null))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> Notificacao.criar(usuario, evento, TipoNotificacao.CONSULTA_AGENDADA, "t", vazio, null))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void respeitaLimitesDeTamanho() {
        var tituloLongo = "x".repeat(Notificacao.TITULO_MAX + 1);
        var mensagemLonga = "x".repeat(Notificacao.MENSAGEM_MAX + 1);
        assertThatThrownBy(() -> Notificacao.criar(usuario, evento, TipoNotificacao.CONSULTA_AGENDADA, tituloLongo, "m", null))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> Notificacao.criar(usuario, evento, TipoNotificacao.CONSULTA_AGENDADA, "t", mensagemLonga, null))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatCode(() -> Notificacao.criar(usuario, evento, TipoNotificacao.CONSULTA_AGENDADA,
                "x".repeat(Notificacao.TITULO_MAX), "x".repeat(Notificacao.MENSAGEM_MAX), null))
                .doesNotThrowAnyException();
    }

    @Test
    void reconstituicaoPreservaEstadoEValidaCoerenciaDaLeitura() {
        var id = UUID.randomUUID();
        var criado = Instant.parse("2026-09-01T00:00:00Z");
        var lidoEm = Instant.parse("2026-09-02T00:00:00Z");
        var n = Notificacao.reconstituir(id, usuario, evento, TipoNotificacao.RECEITA_EMITIDA, "Receita emitida",
                "Uma nova receita esta disponivel.", referencia, true, lidoEm, criado, lidoEm);
        assertThat(n.getId()).isEqualTo(id);
        assertThat(n.isLida()).isTrue();
        assertThat(n.getDataLeitura()).isEqualTo(lidoEm);
        assertThat(n.getCriadoEm()).isEqualTo(criado);

        assertThatThrownBy(() -> Notificacao.reconstituir(id, usuario, evento, TipoNotificacao.RECEITA_EMITIDA, "t", "m",
                null, true, null, criado, criado)).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> Notificacao.reconstituir(id, usuario, evento, TipoNotificacao.RECEITA_EMITIDA, "t", "m",
                null, false, lidoEm, criado, criado)).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> Notificacao.reconstituir(null, usuario, evento, TipoNotificacao.RECEITA_EMITIDA, "t", "m",
                null, false, null, criado, criado)).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> Notificacao.reconstituir(id, usuario, evento, TipoNotificacao.RECEITA_EMITIDA, "t", "m",
                null, false, null, null, null)).isInstanceOf(RegraDeNegocioException.class);
    }
}
