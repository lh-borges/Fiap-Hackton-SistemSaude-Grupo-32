package br.com.fiap.sus.iam.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.iam.application.dto.AutenticacaoOutput;
import br.com.fiap.sus.iam.application.port.SenhaEncoderPort;
import br.com.fiap.sus.iam.application.port.TokenGerado;
import br.com.fiap.sus.iam.application.port.TokenPort;
import br.com.fiap.sus.iam.domain.enums.RoleNome;
import br.com.fiap.sus.iam.domain.exception.CredenciaisInvalidasException;
import br.com.fiap.sus.iam.domain.model.Usuario;
import br.com.fiap.sus.iam.domain.repository.UsuarioRepository;
import br.com.fiap.sus.shared.domain.vo.Cpf;
import br.com.fiap.sus.shared.domain.vo.Email;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Autenticacao (HU-01)")
class AutenticarUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository usuarios;
    @Mock
    private SenhaEncoderPort senhas;
    @Mock
    private TokenPort tokens;

    private AutenticarUsuarioUseCase autenticar;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        autenticar = new AutenticarUsuarioUseCase(usuarios, senhas, tokens);
        usuario = Usuario.criar("Maria Souza", new Cpf("11144477735"), new Email("maria@sus.gov.br"),
                "$2a$10$hash", Set.of(RoleNome.MEDICO));
    }

    @Test
    @DisplayName("emite token quando as credenciais conferem")
    void emiteTokenComCredenciaisValidas() {
        when(usuarios.porEmail(any())).thenReturn(Optional.of(usuario));
        when(senhas.confere("Senha123", "$2a$10$hash")).thenReturn(true);
        Instant expiraEm = Instant.now().plusSeconds(3600);
        when(tokens.gerarPara(usuario)).thenReturn(new TokenGerado("jwt-token", expiraEm));

        AutenticacaoOutput saida = autenticar.executar("maria@sus.gov.br", "Senha123");

        assertThat(saida.token()).isEqualTo("jwt-token");
        assertThat(saida.tipo()).isEqualTo("Bearer");
        assertThat(saida.expiraEm()).isEqualTo(expiraEm);
        assertThat(saida.usuario().roles()).containsExactly("MEDICO");
    }

    @Test
    @DisplayName("EX-01: senha incorreta nao emite token")
    void senhaIncorretaNaoEmiteToken() {
        when(usuarios.porEmail(any())).thenReturn(Optional.of(usuario));
        when(senhas.confere(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> autenticar.executar("maria@sus.gov.br", "errada"))
                .isInstanceOf(CredenciaisInvalidasException.class);
        verify(tokens, never()).gerarPara(any());
    }

    @Test
    @DisplayName("EX-01: e-mail inexistente devolve a mesma mensagem de senha incorreta")
    void emailInexistenteNaoRevelaExistencia() {
        when(usuarios.porEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> autenticar.executar("ninguem@sus.gov.br", "Senha123"))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("E-mail ou senha invalidos.");
    }

    @Test
    @DisplayName("e-mail mal formado tambem nao diferencia a resposta")
    void emailMalFormadoNaoDiferenciaResposta() {
        assertThatThrownBy(() -> autenticar.executar("sem-arroba", "Senha123"))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("E-mail ou senha invalidos.");
    }

    @Test
    @DisplayName("RN-05: usuario inativo nao autentica")
    void usuarioInativoNaoAutentica() {
        usuario.inativar();
        when(usuarios.porEmail(any())).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> autenticar.executar("maria@sus.gov.br", "Senha123"))
                .isInstanceOf(CredenciaisInvalidasException.class);
        verify(tokens, never()).gerarPara(any());
    }
}
