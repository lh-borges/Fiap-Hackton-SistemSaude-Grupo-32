package br.com.fiap.sus.receitas.application.service;

import br.com.fiap.sus.receitas.domain.enums.SituacaoReceita;
import br.com.fiap.sus.receitas.domain.model.ItemReceita;
import br.com.fiap.sus.receitas.domain.model.Receita;
import br.com.fiap.sus.receitas.domain.repository.ReceitaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReceitaQueryServiceTest {
    private final ReceitaRepository repository = mock(ReceitaRepository.class);
    private final ReceitaQueryService service = new ReceitaQueryService(repository);

    @Test
    @DisplayName("feature 010: resumo por paciente sem itens nem observacao")
    void receitasDoPaciente() {
        UUID pacienteId = UUID.randomUUID();
        UUID medicoId = UUID.randomUUID();
        UUID origemId = UUID.randomUUID();
        var emissao = Instant.parse("2026-09-10T12:00:00Z");
        var validade = Instant.parse("2026-10-10T12:00:00Z");
        var item = ItemReceita.criar("Medicamento Confidencial", "10mg", "1x ao dia", "7 dias", null);
        var receita = Receita.reconstituir(UUID.randomUUID(), pacienteId, medicoId, null, List.of(item), emissao,
                validade, "observacao confidencial", SituacaoReceita.ATIVA, origemId, null);
        when(repository.listarPorPaciente(pacienteId)).thenReturn(List.of(receita));

        var resumos = service.receitasDoPaciente(pacienteId);

        assertThat(resumos).singleElement().satisfies(r -> {
            assertThat(r.id()).isEqualTo(receita.getId());
            assertThat(r.pacienteId()).isEqualTo(pacienteId);
            assertThat(r.medicoId()).isEqualTo(medicoId);
            assertThat(r.consultaId()).isNull();
            assertThat(r.dataEmissao()).isEqualTo(emissao);
            assertThat(r.validade()).isEqualTo(validade);
            assertThat(r.situacao()).isEqualTo("ATIVA");
            assertThat(r.receitaOrigemId()).isEqualTo(origemId);
            assertThat(r.toString()).doesNotContain("Confidencial").doesNotContain("confidencial");
        });
    }

    @Test
    void pacienteSemReceitas() {
        UUID pacienteId = UUID.randomUUID();
        when(repository.listarPorPaciente(pacienteId)).thenReturn(List.of());

        assertThat(service.receitasDoPaciente(pacienteId)).isEmpty();
    }
}
