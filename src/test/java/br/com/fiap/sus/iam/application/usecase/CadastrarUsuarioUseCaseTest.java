package br.com.fiap.sus.iam.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.iam.application.dto.CadastrarUsuarioInput;
import br.com.fiap.sus.iam.application.dto.UsuarioOutput;
import br.com.fiap.sus.iam.application.port.SenhaEncoderPort;
import br.com.fiap.sus.iam.domain.model.Usuario;
import br.com.fiap.sus.iam.domain.repository.UsuarioRepository;
import br.com.fiap.sus.shared.domain.exception.ConflitoException;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cadastro de usuario (HU-02)")
class CadastrarUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository usuarios;
    @Mock
    private SenhaEncoderPort senhas;

    private CadastrarUsuarioUseCase cadastrar;

    @BeforeEach
    void preparar() {
        cadastrar = new CadastrarUsuarioUseCase(usuarios, senhas);
    }

    private CadastrarUsuarioInput entrada(String cpf, String senha, Set<String> roles) {
        return new CadastrarUsuarioInput("Maria Souza", cpf, "maria@sus.gov.br", senha, roles);
    }

    @Test
    @DisplayName("cria usuario ativo com a senha codificada")
    void criaUsuarioComSenhaCodificada() {
        when(usuarios.existePorCpf(any())).thenReturn(false);
        when(usuarios.existePorEmail(any())).thenReturn(false);
        when(senhas.codificar("Senha123")).thenReturn("$2a$10$hash");
        when(usuarios.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        UsuarioOutput saida = cadastrar.executar(entrada("11144477735", "Senha123", Set.of("ATENDENTE")));

        assertThat(saida.ativo()).isTrue();
        assertThat(saida.roles()).containsExactly("ATENDENTE");
        assertThat(saida.cpf()).isEqualTo("111.444.777-35");
    }

    @Test
    @DisplayName("a senha em texto nunca aparece na saida")
    void naoDevolveSenha() {
        when(usuarios.existePorCpf(any())).thenReturn(false);
        when(usuarios.existePorEmail(any())).thenReturn(false);
        when(senhas.codificar(any())).thenReturn("$2a$10$hash");
        when(usuarios.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        UsuarioOutput saida = cadastrar.executar(entrada("11144477735", "Senha123", Set.of("MEDICO")));

        assertThat(saida.toString()).doesNotContain("Senha123").doesNotContain("$2a$10$hash");
    }

    @Test
    @DisplayName("RN-01: CPF duplicado gera conflito")
    void cpfDuplicadoGeraConflito() {
        when(usuarios.existePorCpf(any())).thenReturn(true);

        assertThatThrownBy(() -> cadastrar.executar(entrada("11144477735", "Senha123", Set.of("MEDICO"))))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("CPF");
        verify(usuarios, never()).salvar(any());
    }

    @Test
    @DisplayName("RN-01: e-mail duplicado gera conflito")
    void emailDuplicadoGeraConflito() {
        when(usuarios.existePorCpf(any())).thenReturn(false);
        when(usuarios.existePorEmail(any())).thenReturn(true);

        assertThatThrownBy(() -> cadastrar.executar(entrada("11144477735", "Senha123", Set.of("MEDICO"))))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("e-mail");
        verify(usuarios, never()).salvar(any());
    }

    @Test
    @DisplayName("RN-04: sem perfil informado o cadastro e rejeitado")
    void semPerfilEhRejeitado() {
        assertThatThrownBy(() -> cadastrar.executar(entrada("11144477735", "Senha123", Set.of())))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("perfil");
        verify(usuarios, never()).salvar(any());
    }

    @Test
    @DisplayName("perfil inexistente e rejeitado com a lista de valores aceitos")
    void perfilInexistenteEhRejeitado() {
        assertThatThrownBy(() -> cadastrar.executar(entrada("11144477735", "Senha123", Set.of("ENFERMEIRO"))))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("ADMINISTRADOR");
    }

    @Test
    @DisplayName("RN-02: CPF invalido e rejeitado antes de qualquer consulta")
    void cpfInvalidoEhRejeitado() {
        assertThatThrownBy(() -> cadastrar.executar(entrada("11111111111", "Senha123", Set.of("MEDICO"))))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("CPF");
        verify(usuarios, never()).salvar(any());
    }

    @Test
    @DisplayName("RN-03: senha fraca e rejeitada")
    void senhaFracaEhRejeitada() {
        when(usuarios.existePorCpf(any())).thenReturn(false);
        when(usuarios.existePorEmail(any())).thenReturn(false);

        assertThatThrownBy(() -> cadastrar.executar(entrada("11144477735", "curta", Set.of("MEDICO"))))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("senha");
        verify(usuarios, never()).salvar(any(Usuario.class));
    }
}
