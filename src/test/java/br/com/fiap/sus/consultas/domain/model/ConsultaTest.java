package br.com.fiap.sus.consultas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.sus.consultas.domain.enums.SituacaoConsulta;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConsultaTest {

    private final UUID pacienteId = UUID.randomUUID();
    private final UUID medicoId = UUID.randomUUID();
    private final UUID unidadeSaudeId = UUID.randomUUID();

    // ---------- agendar ----------

    @Test
    void deveAgendarConsultaComDadosValidos() {
        Instant dataFutura = Instant.now().plus(1, ChronoUnit.DAYS);

        Consulta consulta = Consulta.agendar(pacienteId, medicoId, unidadeSaudeId, dataFutura,
                "Consulta de rotina");

        assertThat(consulta.getPacienteId()).isEqualTo(pacienteId);
        assertThat(consulta.getMedicoId()).isEqualTo(medicoId);
        assertThat(consulta.getSituacao()).isEqualTo(SituacaoConsulta.AGENDADA);
        assertThat(consulta.getMotivo()).isEqualTo("Consulta de rotina");
    }

    @Test
    @DisplayName("nao agenda consulta com data no passado")
    void naoDeveAgendarComDataNoPassado() {
        Instant dataPassada = Instant.now().minus(1, ChronoUnit.DAYS);

        assertThatThrownBy(() -> Consulta.agendar(pacienteId, medicoId, unidadeSaudeId, dataPassada,
                "Consulta de rotina"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("futuras");
    }

    @Test
    @DisplayName("nao agenda consulta com motivo vazio")
    void naoDeveAgendarComMotivoVazio() {
        Instant dataFutura = Instant.now().plus(1, ChronoUnit.DAYS);

        assertThatThrownBy(() -> Consulta.agendar(pacienteId, medicoId, unidadeSaudeId, dataFutura, ""))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    // ---------- remarcar ----------

    @Test
    @DisplayName("remarca consulta agendada para nova data futura")
    void deveRemarcarConsultaAgendada() {
        Consulta consulta = consultaComSituacao(SituacaoConsulta.AGENDADA, Instant.now().plus(1, ChronoUnit.DAYS));
        Instant novaData = Instant.now().plus(5, ChronoUnit.DAYS);

        consulta.remarcar(novaData);

        assertThat(consulta.getDataHora()).isEqualTo(novaData);
        assertThat(consulta.isRemarcada()).isTrue();
    }

    @Test
    @DisplayName("nao remarca consulta cancelada")
    void naoDeveRemarcarConsultaCancelada() {
        Consulta consulta = consultaComSituacao(SituacaoConsulta.CANCELADA, Instant.now().minus(1, ChronoUnit.DAYS));
        Instant novaData = Instant.now().plus(5, ChronoUnit.DAYS);

        assertThatThrownBy(() -> consulta.remarcar(novaData))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao remarca consulta ja realizada")
    void naoDeveRemarcarConsultaRealizada() {
        Consulta consulta = consultaComSituacao(SituacaoConsulta.REALIZADA, Instant.now().minus(1, ChronoUnit.DAYS));
        Instant novaData = Instant.now().plus(5, ChronoUnit.DAYS);

        assertThatThrownBy(() -> consulta.remarcar(novaData))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao remarca para data no passado")
    void naoDeveRemarcarParaDataNoPassado() {
        Consulta consulta = consultaComSituacao(SituacaoConsulta.AGENDADA, Instant.now().plus(1, ChronoUnit.DAYS));
        Instant dataPassada = Instant.now().minus(1, ChronoUnit.DAYS);

        assertThatThrownBy(() -> consulta.remarcar(dataPassada))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    // ---------- cancelar ----------

    @Test
    @DisplayName("cancela consulta agendada com motivo")
    void deveCancelarConsultaAgendada() {
        Consulta consulta = consultaComSituacao(SituacaoConsulta.AGENDADA, Instant.now().plus(1, ChronoUnit.DAYS));

        consulta.cancelar("Paciente desistiu");

        assertThat(consulta.getSituacao()).isEqualTo(SituacaoConsulta.CANCELADA);
        assertThat(consulta.getMotivoCancelamento()).isEqualTo("Paciente desistiu");
    }

    @Test
    @DisplayName("nao cancela consulta ja realizada")
    void naoDeveCancelarConsultaRealizada() {
        Consulta consulta = consultaComSituacao(SituacaoConsulta.REALIZADA, Instant.now().minus(1, ChronoUnit.DAYS));

        assertThatThrownBy(() -> consulta.cancelar("Motivo qualquer"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao cancela consulta ja cancelada")
    void naoDeveCancelarConsultaJaCancelada() {
        Consulta consulta = consultaComSituacao(SituacaoConsulta.CANCELADA, Instant.now().minus(1, ChronoUnit.DAYS));

        assertThatThrownBy(() -> consulta.cancelar("Motivo qualquer"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao cancela sem motivo")
    void naoDeveCancelarSemMotivo() {
        Consulta consulta = consultaComSituacao(SituacaoConsulta.AGENDADA, Instant.now().plus(1, ChronoUnit.DAYS));

        assertThatThrownBy(() -> consulta.cancelar(""))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    // ---------- alterarObservacoes ----------

    @Test
    @DisplayName("medico autor altera observacoes mesmo apos consulta realizada")
    void deveAlterarObservacoesComoMedicoAutor() {
        Consulta consulta = consultaComSituacao(SituacaoConsulta.REALIZADA, Instant.now().minus(1, ChronoUnit.DAYS));

        consulta.alterarObservacoes(medicoId, "Observacao complementar");

        assertThat(consulta.getObservacoes()).isEqualTo("Observacao complementar");
    }

    @Test
    @DisplayName("nao altera observacoes se nao for o medico autor")
    void naoDeveAlterarObservacoesComMedicoDiferente() {
        Consulta consulta = consultaComSituacao(SituacaoConsulta.REALIZADA, Instant.now().minus(1, ChronoUnit.DAYS));
        UUID outroMedicoId = UUID.randomUUID();

        assertThatThrownBy(() -> consulta.alterarObservacoes(outroMedicoId, "Observacao"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    // ---------- registrarRealizacao ----------

    @Test
    @DisplayName("medico da consulta registra a realizacao")
    void deveRegistrarRealizacaoPeloMedicoCorreto() {
        Consulta consulta = consultaComSituacao(SituacaoConsulta.AGENDADA, Instant.now().minus(1, ChronoUnit.HOURS));

        consulta.registrarRealizacao(medicoId, "Paciente atendido normalmente");

        assertThat(consulta.getSituacao()).isEqualTo(SituacaoConsulta.REALIZADA);
        assertThat(consulta.getObservacoes()).isEqualTo("Paciente atendido normalmente");
    }

    @Test
    @DisplayName("nao registra realizacao se o medico for diferente")
    void naoDeveRegistrarRealizacaoComMedicoDiferente() {
        Consulta consulta = consultaComSituacao(SituacaoConsulta.AGENDADA, Instant.now().minus(1, ChronoUnit.HOURS));
        UUID outroMedicoId = UUID.randomUUID();

        assertThatThrownBy(() -> consulta.registrarRealizacao(outroMedicoId, "Observacao"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao registra realizacao de consulta cancelada")
    void naoDeveRegistrarRealizacaoDeConsultaCancelada() {
        Consulta consulta = consultaComSituacao(SituacaoConsulta.CANCELADA, Instant.now().minus(1, ChronoUnit.HOURS));

        assertThatThrownBy(() -> consulta.registrarRealizacao(medicoId, "Observacao"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao registra realizacao de consulta futura")
    void naoDeveRegistrarRealizacaoDeConsultaFutura() {
        Consulta consulta = consultaComSituacao(SituacaoConsulta.AGENDADA, Instant.now().plus(1, ChronoUnit.DAYS));

        assertThatThrownBy(() -> consulta.registrarRealizacao(medicoId, "Observacao"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    // ---------- helper ----------

    /** Monta uma consulta ja em um estado especifico, sem precisar simular o fluxo completo. */
    private Consulta consultaComSituacao(SituacaoConsulta situacao, Instant dataHora) {
        Instant agora = Instant.now();
        return Consulta.reconstituir(UUID.randomUUID(), pacienteId, medicoId, unidadeSaudeId, dataHora,
                situacao, "Consulta de rotina", null, null, false, agora, agora);
    }
}