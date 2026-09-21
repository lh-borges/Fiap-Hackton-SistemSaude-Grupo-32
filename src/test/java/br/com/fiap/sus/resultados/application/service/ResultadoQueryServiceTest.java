package br.com.fiap.sus.resultados.application.service;

import br.com.fiap.sus.resultados.domain.enums.TipoResultado;
import br.com.fiap.sus.resultados.domain.model.ResultadoExame;
import br.com.fiap.sus.resultados.domain.repository.ResultadoExameRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResultadoQueryServiceTest {

    private final ResultadoExameRepository repository = mock(ResultadoExameRepository.class);
    private final ResultadoQueryService service = new ResultadoQueryService(repository);

    @Test
    void retornaPacienteDoResultado() {
        UUID resultadoId = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        var resultado = resultado(resultadoId, UUID.randomUUID(), pacienteId);
        when(repository.buscarPorId(resultadoId)).thenReturn(Optional.of(resultado));

        assertThat(service.pacienteIdDoResultado(resultadoId)).contains(pacienteId);
    }

    @Test
    void retornaResumoDoResultadoPeloExame() {
        UUID resultadoId = UUID.randomUUID();
        UUID exameId = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        var resultado = resultado(resultadoId, exameId, pacienteId);
        when(repository.buscarPorExameId(exameId)).thenReturn(Optional.of(resultado));

        var resumo = service.resultadoDoExame(exameId);

        assertThat(resumo).isPresent();
        assertThat(resumo.get().id()).isEqualTo(resultadoId);
        assertThat(resumo.get().exameId()).isEqualTo(exameId);
        assertThat(resumo.get().pacienteId()).isEqualTo(pacienteId);
        assertThat(resumo.get().tipoResultado()).isEqualTo("IMAGEM");
        assertThat(resumo.get().dataResultado()).isEqualTo(Instant.parse("2026-09-21T03:00:14Z"));
        assertThat(resumo.get().observacao()).isEqualTo("Resultado registrado para teste do fluxo de parecer.");
        assertThat(resumo.get().arquivoUrl()).isEqualTo("https://exemplo.local/raio-x-torax.pdf");
        assertThat(resumo.get().descricao()).isEqualTo("Raio-X de torax");
        assertThat(resumo.get().laudo()).isEqualTo("Sem alteracoes agudas evidentes.");
    }

    @Test
    void retornaVazioQuandoResultadoDoExameNaoExiste() {
        UUID exameId = UUID.randomUUID();
        when(repository.buscarPorExameId(exameId)).thenReturn(Optional.empty());

        assertThat(service.resultadoDoExame(exameId)).isEmpty();
    }

    private ResultadoExame resultado(UUID resultadoId, UUID exameId, UUID pacienteId) {
        return ResultadoExame.reconstituir(
                resultadoId,
                exameId,
                pacienteId,
                TipoResultado.IMAGEM,
                Instant.parse("2026-09-21T03:00:14Z"),
                "Resultado registrado para teste do fluxo de parecer.",
                "https://exemplo.local/raio-x-torax.pdf",
                "Raio-X de torax",
                "Sem alteracoes agudas evidentes.",
                List.of()
        );
    }
}
