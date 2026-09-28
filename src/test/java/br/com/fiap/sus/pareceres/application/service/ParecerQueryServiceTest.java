package br.com.fiap.sus.pareceres.application.service;

import br.com.fiap.sus.pareceres.domain.model.ParecerMedico;
import br.com.fiap.sus.pareceres.domain.repository.ParecerMedicoRepository;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ParecerQueryServiceTest {
    private final ParecerMedicoRepository repository = mock(ParecerMedicoRepository.class);
    private final ParecerQueryService service = new ParecerQueryService(repository);

    @Test
    @DisplayName("feature 010: resumo por paciente sem a descricao do parecer")
    void pareceresDoPaciente() {
        UUID pacienteId = UUID.randomUUID();
        UUID resultadoId = UUID.randomUUID();
        UUID medicoId = UUID.randomUUID();
        var parecer = ParecerMedico.reconstituir(UUID.randomUUID(), resultadoId, pacienteId, medicoId,
                "Descricao clinica confidencial", Instant.parse("2026-09-21T03:00:14Z"));
        when(repository.listarPorPaciente(pacienteId)).thenReturn(List.of(parecer));

        var resumos = service.pareceresDoPaciente(pacienteId);

        assertThat(resumos).singleElement().satisfies(r -> {
            assertThat(r.id()).isEqualTo(parecer.getId());
            assertThat(r.resultadoExameId()).isEqualTo(resultadoId);
            assertThat(r.pacienteId()).isEqualTo(pacienteId);
            assertThat(r.medicoId()).isEqualTo(medicoId);
            assertThat(r.dataParecer()).isEqualTo(Instant.parse("2026-09-21T03:00:14Z"));
            assertThat(r.toString()).doesNotContain("confidencial");
        });
    }

    @Test
    void indicadoresDeParecer() {
        UUID resultadoId = UUID.randomUUID();
        when(repository.existePorResultadoExameId(resultadoId)).thenReturn(true);
        when(repository.resultadosComParecer(Set.of(resultadoId))).thenReturn(Set.of(resultadoId));

        assertThat(service.existeParecerParaResultado(resultadoId)).isTrue();
        assertThat(service.resultadosComParecer(Set.of(resultadoId))).containsExactly(resultadoId);
        assertThat(service.resultadosComParecer(Set.of())).isEmpty();
        verify(repository, never()).resultadosComParecer(Set.of());
    }
}
