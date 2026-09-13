package br.com.fiap.sus.exames.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.sus.exames.domain.enums.SituacaoSolicitacaoExame;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SolicitacaoExameTest {

    private final UUID pacienteId = UUID.randomUUID();
    private final UUID medicoId = UUID.randomUUID();
    private final UUID tipoExameId = UUID.randomUUID();
    private final UUID consultaId = UUID.randomUUID();

    // ---------- solicitar ----------

    @Test
    @DisplayName("solicita exame com dados validos, nasce PENDENTE")
    void deveSolicitarExameComDadosValidos() {
        SolicitacaoExame solicitacao = SolicitacaoExame.solicitar(pacienteId, medicoId, tipoExameId,
                consultaId, "Suspeita de anemia");

        assertThat(solicitacao.getPacienteId()).isEqualTo(pacienteId);
        assertThat(solicitacao.getMedicoId()).isEqualTo(medicoId);
        assertThat(solicitacao.getTipoExameId()).isEqualTo(tipoExameId);
        assertThat(solicitacao.getSituacao()).isEqualTo(SituacaoSolicitacaoExame.PENDENTE);
    }

    @Test
    @DisplayName("permite solicitacao sem vinculo com consulta (opcional)")
    void devePermitirSolicitacaoSemConsulta() {
        SolicitacaoExame solicitacao = SolicitacaoExame.solicitar(pacienteId, medicoId, tipoExameId,
                null, "Suspeita de anemia");

        assertThat(solicitacao.getConsultaId()).isNull();
    }

    @Test
    @DisplayName("nao solicita sem paciente")
    void naoDeveSolicitarSemPaciente() {
        assertThatThrownBy(() -> SolicitacaoExame.solicitar(null, medicoId, tipoExameId, consultaId,
                "Justificativa valida"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao solicita com justificativa curta demais")
    void naoDeveSolicitarComJustificativaCurta() {
        assertThatThrownBy(() -> SolicitacaoExame.solicitar(pacienteId, medicoId, tipoExameId, consultaId, "ab"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    // ---------- marcarComoAgendada ----------

    @Test
    @DisplayName("marca como agendada a partir de PENDENTE")
    void deveMarcarComoAgendadaAPartirDePendente() {
        SolicitacaoExame solicitacao = solicitacaoComSituacao(SituacaoSolicitacaoExame.PENDENTE);

        solicitacao.marcarComoAgendada();

        assertThat(solicitacao.getSituacao()).isEqualTo(SituacaoSolicitacaoExame.AGENDADA);
    }

    @Test
    @DisplayName("nao agenda solicitacao que ja esta agendada")
    void naoDeveAgendarSolicitacaoJaAgendada() {
        SolicitacaoExame solicitacao = solicitacaoComSituacao(SituacaoSolicitacaoExame.AGENDADA);

        assertThatThrownBy(solicitacao::marcarComoAgendada)
                .isInstanceOf(RegraDeNegocioException.class);
    }

    // ---------- marcarComoRealizada ----------

    @Test
    @DisplayName("marca como realizada a partir de AGENDADA")
    void deveMarcarComoRealizadaAPartirDeAgendada() {
        SolicitacaoExame solicitacao = solicitacaoComSituacao(SituacaoSolicitacaoExame.AGENDADA);

        solicitacao.marcarComoRealizada();

        assertThat(solicitacao.getSituacao()).isEqualTo(SituacaoSolicitacaoExame.REALIZADA);
    }

    @Test
    @DisplayName("nao marca como realizada se ainda estiver PENDENTE")
    void naoDeveMarcarComoRealizadaSePendente() {
        SolicitacaoExame solicitacao = solicitacaoComSituacao(SituacaoSolicitacaoExame.PENDENTE);

        assertThatThrownBy(solicitacao::marcarComoRealizada)
                .isInstanceOf(RegraDeNegocioException.class);
    }

    // ---------- cancelar ----------

    @Test
    @DisplayName("cancela solicitacao pendente com motivo")
    void deveCancelarSolicitacaoPendente() {
        SolicitacaoExame solicitacao = solicitacaoComSituacao(SituacaoSolicitacaoExame.PENDENTE);

        solicitacao.cancelar("Paciente desistiu do exame");

        assertThat(solicitacao.getSituacao()).isEqualTo(SituacaoSolicitacaoExame.CANCELADA);
        assertThat(solicitacao.getMotivoCancelamento()).isEqualTo("Paciente desistiu do exame");
    }

    @Test
    @DisplayName("nao cancela solicitacao ja agendada diretamente")
    void naoDeveCancelarSolicitacaoJaAgendada() {
        SolicitacaoExame solicitacao = solicitacaoComSituacao(SituacaoSolicitacaoExame.AGENDADA);

        assertThatThrownBy(() -> solicitacao.cancelar("Motivo qualquer"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao cancela sem motivo")
    void naoDeveCancelarSemMotivo() {
        SolicitacaoExame solicitacao = solicitacaoComSituacao(SituacaoSolicitacaoExame.PENDENTE);

        assertThatThrownBy(() -> solicitacao.cancelar(" "))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    // ---------- voltarParaPendente ----------

    @Test
    @DisplayName("volta para pendente a partir de AGENDADA")
    void deveVoltarParaPendenteAPartirDeAgendada() {
        SolicitacaoExame solicitacao = solicitacaoComSituacao(SituacaoSolicitacaoExame.AGENDADA);

        solicitacao.voltarParaPendente();

        assertThat(solicitacao.getSituacao()).isEqualTo(SituacaoSolicitacaoExame.PENDENTE);
    }

    @Test
    @DisplayName("nao volta para pendente se ja estiver realizada")
    void naoDeveVoltarParaPendenteSeRealizada() {
        SolicitacaoExame solicitacao = solicitacaoComSituacao(SituacaoSolicitacaoExame.REALIZADA);

        assertThatThrownBy(solicitacao::voltarParaPendente)
                .isInstanceOf(RegraDeNegocioException.class);
    }

    // ---------- helper ----------

    private SolicitacaoExame solicitacaoComSituacao(SituacaoSolicitacaoExame situacao) {
        Instant agora = Instant.now();
        return SolicitacaoExame.reconstituir(UUID.randomUUID(), pacienteId, medicoId, tipoExameId,
                consultaId, "Justificativa valida", situacao, null, agora, agora);
    }
}