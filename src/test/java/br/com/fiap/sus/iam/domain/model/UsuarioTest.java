package br.com.fiap.sus.iam.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.sus.iam.domain.enums.RoleNome;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import br.com.fiap.sus.shared.domain.vo.Cpf;
import br.com.fiap.sus.shared.domain.vo.Email;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Usuario")
class UsuarioTest {

    private static final Cpf CPF = new Cpf("11144477735");
    private static final Email EMAIL = new Email("maria@sus.gov.br");
    private static final String HASH = "$2a$10$hashficticio";

    private Usuario novoUsuario() {
        return Usuario.criar("Maria Souza", CPF, EMAIL, HASH, Set.of(RoleNome.ATENDENTE));
    }

    @Test
    @DisplayName("nasce ativo, com id e data de criacao")
    void nasceAtivo() {
        Usuario usuario = novoUsuario();

        assertThat(usuario.getId()).isNotNull();
        assertThat(usuario.isAtivo()).isTrue();
        assertThat(usuario.getCriadoEm()).isNotNull();
        assertThat(usuario.getRoles()).containsExactly(RoleNome.ATENDENTE);
    }

    @Test
    @DisplayName("RN-04: exige ao menos um perfil")
    void exigeAoMenosUmPerfil() {
        assertThatThrownBy(() -> Usuario.criar("Maria Souza", CPF, EMAIL, HASH, Set.of()))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("perfil");
    }

    @Test
    @DisplayName("exige nome com ao menos 3 caracteres")
    void exigeNomeMinimo() {
        assertThatThrownBy(() -> Usuario.criar("Ma", CPF, EMAIL, HASH, Set.of(RoleNome.MEDICO)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("nome");
    }

    @Test
    @DisplayName("RN-05: inativacao e logica e nao pode repetir")
    void inativacaoEhLogica() {
        Usuario usuario = novoUsuario();

        usuario.inativar();
        assertThat(usuario.isAtivo()).isFalse();

        assertThatThrownBy(usuario::inativar)
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("ja esta inativo");
    }

    @Test
    @DisplayName("troca de senha guarda o novo hash e atualiza a data")
    void trocaDeSenha() {
        Usuario usuario = novoUsuario();

        usuario.alterarSenha("$2a$10$outrohash");

        assertThat(usuario.getSenhaHash()).isEqualTo("$2a$10$outrohash");
    }

    @Test
    @DisplayName("substituir perfis vazios e rejeitado")
    void substituirPerfisVaziosEhRejeitado() {
        Usuario usuario = novoUsuario();

        assertThatThrownBy(() -> usuario.substituirRoles(Set.of()))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThat(usuario.getRoles()).containsExactly(RoleNome.ATENDENTE);
    }

    @Test
    @DisplayName("a colecao de perfis devolvida e imutavel")
    void perfisSaoImutaveis() {
        Usuario usuario = novoUsuario();

        assertThatThrownBy(() -> usuario.getRoles().add(RoleNome.ADMINISTRADOR))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
