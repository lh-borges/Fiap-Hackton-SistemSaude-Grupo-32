package br.com.fiap.sus.consultas.infrastructure.persistence;

import br.com.fiap.sus.consultas.api.ConsultaResumo;
import br.com.fiap.sus.consultas.domain.enums.SituacaoConsulta;
import br.com.fiap.sus.consultas.infrastructure.persistence.entity.ConsultaEntity;
import br.com.fiap.sus.consultas.infrastructure.persistence.repository.ConsultaQueryAdapter;
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
@Import(ConsultaQueryAdapter.class)
@DisplayName("Porta de leitura de consultas (H2)")
class ConsultaQueryAdapterTest {
    @Autowired ConsultaQueryAdapter adapter;
    @Autowired EntityManager entityManager;
    final UUID paciente = UUID.randomUUID();
    final UUID outroPaciente = UUID.randomUUID();
    final UUID medico = UUID.randomUUID();

    UUID salvar(UUID pacienteId, Instant dataHora, SituacaoConsulta situacao, boolean remarcada) {
        var id = UUID.randomUUID();
        entityManager.persist(new ConsultaEntity(id, pacienteId, medico, UUID.randomUUID(), dataHora, situacao,
                "motivo", null, null, remarcada, dataHora, dataHora));
        return id;
    }

    @Test
    @DisplayName("feature 010: consultas do paciente, inclusive canceladas, da mais recente para a mais antiga")
    void consultasDoPaciente() {
        var antiga = salvar(paciente, Instant.parse("2026-09-01T10:00:00Z"), SituacaoConsulta.REALIZADA, false);
        var cancelada = salvar(paciente, Instant.parse("2026-09-03T10:00:00Z"), SituacaoConsulta.CANCELADA, false);
        var recente = salvar(paciente, Instant.parse("2026-09-05T10:00:00Z"), SituacaoConsulta.AGENDADA, true);
        salvar(outroPaciente, Instant.parse("2026-09-04T10:00:00Z"), SituacaoConsulta.AGENDADA, false);
        entityManager.flush();
        entityManager.clear();

        var consultas = adapter.consultasDoPaciente(paciente);

        assertThat(consultas).extracting(ConsultaResumo::id).containsExactly(recente, cancelada, antiga);
        assertThat(consultas.get(0).situacao()).isEqualTo("AGENDADA");
        assertThat(consultas.get(0).remarcada()).isTrue();
        assertThat(consultas.get(0).medicoId()).isEqualTo(medico);
        assertThat(consultas.get(1).situacao()).isEqualTo("CANCELADA");
        assertThat(adapter.consultasDoPaciente(UUID.randomUUID())).isEmpty();
        assertThat(adapter.pacientesDoMedico(medico)).contains(paciente, outroPaciente);
    }
}
