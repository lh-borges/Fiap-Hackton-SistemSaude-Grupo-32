package br.com.fiap.sus.cadastros.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.sus.cadastros.domain.enums.Sexo;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Paciente")
class PacienteTest {

    private static final UUID USUARIO = UUID.randomUUID();
    private static final LocalDate NASCIMENTO = LocalDate.of(1985, 4, 12);

    @Test
    @DisplayName("nasce ativo e limpa a formatacao do cartao SUS")
    void nasceAtivo() {
        Paciente paciente = Paciente.criar(USUARIO, "123 4567 8901 2345", NASCIMENTO, Sexo.FEMININO, "O+");

        assertThat(paciente.isAtivo()).isTrue();
        assertThat(paciente.getCartaoSus()).isEqualTo("123456789012345");
        assertThat(paciente.getTipoSanguineo()).isEqualTo("O+");
    }

    @Test
    @DisplayName("RN-02: cartao SUS deve ter 15 digitos")
    void cartaoSusPrecisaDe15Digitos() {
        assertThatThrownBy(() -> Paciente.criar(USUARIO, "1234567", NASCIMENTO, Sexo.OUTRO, null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("15 digitos");
    }

    @Test
    @DisplayName("RN-05: data de nascimento futura e rejeitada")
    void dataFuturaEhRejeitada() {
        LocalDate amanha = LocalDate.now().plusDays(1);

        assertThatThrownBy(() -> Paciente.criar(USUARIO, "123456789012345", amanha, Sexo.MASCULINO, null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("nao pode ser futura");
    }

    @Test
    @DisplayName("exige vinculo com um usuario")
    void exigeUsuario() {
        assertThatThrownBy(() -> Paciente.criar(null, "123456789012345", NASCIMENTO, Sexo.OUTRO, null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("usuario");
    }

    @Test
    @DisplayName("tipo sanguineo invalido e rejeitado; ausente e aceito")
    void tipoSanguineo() {
        assertThatThrownBy(() -> Paciente.criar(USUARIO, "123456789012345", NASCIMENTO, Sexo.OUTRO, "XY"))
                .isInstanceOf(RegraDeNegocioException.class);

        assertThat(Paciente.criar(USUARIO, "123456789012345", NASCIMENTO, Sexo.OUTRO, "  ")
                .getTipoSanguineo()).isNull();
    }

    @Test
    @DisplayName("posse: reconhece o usuario dono do cadastro")
    void reconheceODono() {
        Paciente paciente = Paciente.criar(USUARIO, "123456789012345", NASCIMENTO, Sexo.FEMININO, null);

        assertThat(paciente.pertenceAoUsuario(USUARIO)).isTrue();
        assertThat(paciente.pertenceAoUsuario(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("RN-06: inativacao e logica e nao pode repetir")
    void inativacaoEhLogica() {
        Paciente paciente = Paciente.criar(USUARIO, "123456789012345", NASCIMENTO, Sexo.FEMININO, null);

        paciente.inativar();

        assertThat(paciente.isAtivo()).isFalse();
        assertThatThrownBy(paciente::inativar).isInstanceOf(RegraDeNegocioException.class);
    }
}
