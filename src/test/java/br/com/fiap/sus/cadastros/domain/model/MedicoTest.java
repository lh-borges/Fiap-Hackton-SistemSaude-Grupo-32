package br.com.fiap.sus.cadastros.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Medico")
class MedicoTest {

    private static final UUID USUARIO = UUID.randomUUID();
    private static final UUID ESPECIALIDADE = UUID.randomUUID();

    @Test
    @DisplayName("normaliza a UF para maiusculas")
    void normalizaUf() {
        Medico medico = Medico.criar(USUARIO, "123456", "sp", ESPECIALIDADE);

        assertThat(medico.getUfCrm()).isEqualTo("SP");
        assertThat(medico.isAtivo()).isTrue();
    }

    @Test
    @DisplayName("rejeita UF inexistente")
    void rejeitaUfInexistente() {
        assertThatThrownBy(() -> Medico.criar(USUARIO, "123456", "XX", ESPECIALIDADE))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("UF");
    }

    @Test
    @DisplayName("rejeita CRM curto demais")
    void rejeitaCrmCurto() {
        assertThatThrownBy(() -> Medico.criar(USUARIO, "12", "SP", ESPECIALIDADE))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("CRM");
    }

    @Test
    @DisplayName("exige especialidade")
    void exigeEspecialidade() {
        assertThatThrownBy(() -> Medico.criar(USUARIO, "123456", "SP", null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("especialidade");
    }

    @Test
    @DisplayName("inativacao e logica")
    void inativacaoEhLogica() {
        Medico medico = Medico.criar(USUARIO, "123456", "SP", ESPECIALIDADE);

        medico.inativar();

        assertThat(medico.isAtivo()).isFalse();
        assertThatThrownBy(medico::inativar).isInstanceOf(RegraDeNegocioException.class);
    }
}
