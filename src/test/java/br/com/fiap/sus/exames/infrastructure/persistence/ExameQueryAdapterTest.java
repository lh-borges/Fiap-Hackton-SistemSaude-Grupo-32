package br.com.fiap.sus.exames.infrastructure.persistence;

import br.com.fiap.sus.exames.api.ExameResumo;
import br.com.fiap.sus.exames.api.SolicitacaoExameResumo;
import br.com.fiap.sus.exames.domain.enums.SituacaoExame;
import br.com.fiap.sus.exames.domain.enums.SituacaoSolicitacaoExame;
import br.com.fiap.sus.exames.infrastructure.persistence.entity.ExameEntity;
import br.com.fiap.sus.exames.infrastructure.persistence.entity.SolicitacaoExameEntity;
import br.com.fiap.sus.exames.infrastructure.persistence.repository.ExameQueryAdapter;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"}, showSql = false)
@Import(ExameQueryAdapter.class)
@DisplayName("Porta de leitura de exames (H2)")
class ExameQueryAdapterTest {
    @Autowired ExameQueryAdapter adapter;
    @Autowired EntityManager entityManager;
    final UUID paciente = UUID.randomUUID();
    final UUID outroPaciente = UUID.randomUUID();
    final UUID medico = UUID.randomUUID();
    final UUID consulta = UUID.randomUUID();

    UUID solicitacao(UUID pacienteId, Instant criadoEm, SituacaoSolicitacaoExame situacao, UUID consultaId) {
        var id = UUID.randomUUID();
        entityManager.persist(new SolicitacaoExameEntity(id, pacienteId, medico, UUID.randomUUID(), consultaId,
                "justificativa", situacao, null, criadoEm, criadoEm));
        return id;
    }

    UUID exame(UUID solicitacaoId, Instant agendada, Instant realizacao, SituacaoExame situacao) {
        var id = UUID.randomUUID();
        entityManager.persist(new ExameEntity(id, solicitacaoId, UUID.randomUUID(), agendada, realizacao, situacao,
                agendada, agendada));
        return id;
    }

    @Test
    @DisplayName("feature 010: solicitacoes e exames do paciente, via solicitacao, do mais recente ao mais antigo")
    void solicitacoesEExamesDoPaciente() {
        var antiga = solicitacao(paciente, Instant.parse("2026-09-01T10:00:00Z"), SituacaoSolicitacaoExame.REALIZADA, consulta);
        var recente = solicitacao(paciente, Instant.parse("2026-09-03T10:00:00Z"), SituacaoSolicitacaoExame.CANCELADA, null);
        var alheia = solicitacao(outroPaciente, Instant.parse("2026-09-05T10:00:00Z"), SituacaoSolicitacaoExame.PENDENTE, null);
        var realizado = exame(antiga, Instant.parse("2026-09-02T10:00:00Z"), Instant.parse("2026-09-06T10:00:00Z"), SituacaoExame.REALIZADO);
        var cancelado = exame(recente, Instant.parse("2026-09-04T10:00:00Z"), null, SituacaoExame.CANCELADO);
        exame(alheia, Instant.parse("2026-09-07T10:00:00Z"), null, SituacaoExame.AGENDADO);
        entityManager.flush();
        entityManager.clear();

        var solicitacoes = adapter.solicitacoesDoPaciente(paciente);
        assertThat(solicitacoes).extracting(SolicitacaoExameResumo::id).containsExactly(recente, antiga);
        assertThat(solicitacoes.get(1).consultaId()).isEqualTo(consulta);
        assertThat(solicitacoes.get(1).situacao()).isEqualTo("REALIZADA");
        assertThat(solicitacoes.get(1).medicoId()).isEqualTo(medico);

        var exames = adapter.examesDoPaciente(paciente);
        assertThat(exames).extracting(ExameResumo::id).containsExactly(realizado, cancelado);
        assertThat(exames.get(0).pacienteId()).isEqualTo(paciente);
        assertThat(exames.get(0).medicoSolicitanteId()).isEqualTo(medico);
        assertThat(exames.get(0).dataRealizacao()).isEqualTo(Instant.parse("2026-09-06T10:00:00Z"));
        assertThat(exames.get(1).dataRealizacao()).isNull();
        assertThat(exames.get(1).situacao()).isEqualTo("CANCELADO");

        assertThat(adapter.solicitacoesDoPaciente(UUID.randomUUID())).isEmpty();
        assertThat(adapter.examesDoPaciente(UUID.randomUUID())).isEmpty();
    }
}
