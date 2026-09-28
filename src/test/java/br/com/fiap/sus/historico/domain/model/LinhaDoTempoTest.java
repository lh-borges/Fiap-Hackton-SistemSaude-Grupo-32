package br.com.fiap.sus.historico.domain.model;

import br.com.fiap.sus.historico.domain.enums.TipoRegistro;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("LinhaDoTempo e HistoricoFiltro")
class LinhaDoTempoTest {
    final Instant d1 = Instant.parse("2026-09-01T10:00:00Z");
    final Instant d2 = Instant.parse("2026-09-02T10:00:00Z");
    final Instant d3 = Instant.parse("2026-09-03T10:00:00Z");
    final RegistroHistorico consulta = registro(TipoRegistro.CONSULTA, d1);
    final RegistroHistorico exame = registro(TipoRegistro.EXAME, d3);
    final RegistroHistorico receita = registro(TipoRegistro.RECEITA, d2);
    final RegistroHistorico documento = registro(TipoRegistro.DOCUMENTO, d2);

    @Test
    @DisplayName("HU-01/RF-02: registros de fontes diferentes saem em uma unica lista, do mais recente ao mais antigo")
    void ordena() {
        var linha = LinhaDoTempo.de(List.of(consulta, documento, exame, receita));

        assertThat(linha.registros()).containsExactly(exame, receita, documento, consulta);
        assertThat(linha.total()).isEqualTo(4);
    }

    @Test
    @DisplayName("HU-04/RF-04: filtra por tipos e por periodo inclusivo")
    void filtra() {
        var linha = LinhaDoTempo.de(List.of(consulta, documento, exame, receita));

        assertThat(linha.filtrar(new HistoricoFiltro(Set.of(TipoRegistro.RECEITA, TipoRegistro.CONSULTA), null, null))
                .registros()).containsExactly(receita, consulta);
        assertThat(linha.filtrar(new HistoricoFiltro(null, d2, d2)).registros()).containsExactly(receita, documento);
        assertThat(linha.filtrar(new HistoricoFiltro(Set.of(), d2, null)).registros()).containsExactly(exame, receita, documento);
        assertThat(linha.filtrar(new HistoricoFiltro(Set.of(), null, d1)).registros()).containsExactly(consulta);
        assertThat(linha.filtrar(HistoricoFiltro.todos()).total()).isEqualTo(4);
    }

    @Test
    @DisplayName("RN-08: periodo invertido e entrada invalida")
    void periodoInvertido() {
        assertThatThrownBy(() -> new HistoricoFiltro(Set.of(), d2, d1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("RF-05: pagina em memoria e informa o total")
    void pagina() {
        var linha = LinhaDoTempo.de(List.of(consulta, documento, exame, receita));

        var primeira = linha.pagina(0, 3);
        assertThat(primeira.conteudo()).containsExactly(exame, receita, documento);
        assertThat(primeira.totalElementos()).isEqualTo(4);
        assertThat(primeira.totalPaginas()).isEqualTo(2);

        assertThat(linha.pagina(1, 3).conteudo()).containsExactly(consulta);
        assertThat(linha.pagina(5, 3).conteudo()).isEmpty();
        assertThat(LinhaDoTempo.de(List.of()).pagina(0, 20).conteudo()).isEmpty();
    }

    @Test
    @DisplayName("RN-08: tamanho de pagina entre 1 e 100")
    void paginaInvalida() {
        var linha = LinhaDoTempo.de(List.of(consulta));
        assertThatThrownBy(() -> linha.pagina(-1, 10)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> linha.pagina(0, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> linha.pagina(0, 101)).isInstanceOf(IllegalArgumentException.class);
    }

    static RegistroHistorico registro(TipoRegistro tipo, Instant data) {
        return new RegistroHistorico(tipo, UUID.randomUUID(), data, "Titulo", "SITUACAO", null, null);
    }
}
