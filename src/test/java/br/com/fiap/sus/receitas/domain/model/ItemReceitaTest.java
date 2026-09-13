package br.com.fiap.sus.receitas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ItemReceitaTest {

    @Test
    @DisplayName("cria item com dados validos")
    void deveCriarItemComDadosValidos() {
        ItemReceita item = ItemReceita.criar("Paracetamol", "500mg", "8 em 8 horas", "5 dias",
                "Tomar apos as refeicoes");

        assertThat(item.getMedicamento()).isEqualTo("Paracetamol");
        assertThat(item.getDosagem()).isEqualTo("500mg");
        assertThat(item.getOrientacao()).isEqualTo("Tomar apos as refeicoes");
    }

    @Test
    @DisplayName("orientacao e opcional")
    void devePermitirOrientacaoNula() {
        ItemReceita item = ItemReceita.criar("Paracetamol", "500mg", "8 em 8 horas", "5 dias", null);

        assertThat(item.getOrientacao()).isNull();
    }

    @Test
    @DisplayName("nao cria item sem medicamento")
    void naoDeveCriarSemMedicamento() {
        assertThatThrownBy(() -> ItemReceita.criar("", "500mg", "8 em 8 horas", "5 dias", null))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao cria item sem dosagem")
    void naoDeveCriarSemDosagem() {
        assertThatThrownBy(() -> ItemReceita.criar("Paracetamol", "", "8 em 8 horas", "5 dias", null))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao cria item sem frequencia")
    void naoDeveCriarSemFrequencia() {
        assertThatThrownBy(() -> ItemReceita.criar("Paracetamol", "500mg", "", "5 dias", null))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao cria item sem duracao")
    void naoDeveCriarSemDuracao() {
        assertThatThrownBy(() -> ItemReceita.criar("Paracetamol", "500mg", "8 em 8 horas", "", null))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}