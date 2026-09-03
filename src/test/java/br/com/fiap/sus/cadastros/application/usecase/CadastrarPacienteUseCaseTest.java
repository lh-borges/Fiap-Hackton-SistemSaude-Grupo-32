package br.com.fiap.sus.cadastros.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.cadastros.application.dto.CadastrarPacienteInput;
import br.com.fiap.sus.cadastros.application.dto.PacienteOutput;
import br.com.fiap.sus.cadastros.domain.model.Paciente;
import br.com.fiap.sus.cadastros.domain.repository.PacienteRepository;
import br.com.fiap.sus.iam.api.IamQuery;
import br.com.fiap.sus.iam.api.UsuarioResumo;
import br.com.fiap.sus.shared.domain.exception.ConflitoException;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cadastro de paciente (HU-01)")
class CadastrarPacienteUseCaseTest {

    private static final UUID USUARIO = UUID.randomUUID();

    @Mock
    private PacienteRepository pacientes;
    @Mock
    private IamQuery iam;

    private CadastrarPacienteUseCase cadastrar;

    @BeforeEach
    void preparar() {
        cadastrar = new CadastrarPacienteUseCase(pacientes, iam);
    }

    private CadastrarPacienteInput entrada(String cartaoSus) {
        return new CadastrarPacienteInput(USUARIO, cartaoSus, LocalDate.of(1990, 1, 20), "FEMININO", "A+");
    }

    @Test
    @DisplayName("cria o paciente e enriquece a saida com os dados do usuario")
    void criaPaciente() {
        when(iam.usuarioAtivoPossuiRole(USUARIO, "PACIENTE")).thenReturn(true);
        when(pacientes.existePorUsuarioId(USUARIO)).thenReturn(false);
        when(pacientes.existePorCartaoSus(anyString())).thenReturn(false);
        when(pacientes.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));
        when(iam.resumoDoUsuario(USUARIO)).thenReturn(Optional.of(
                new UsuarioResumo(USUARIO, "Maria Souza", "maria@sus.gov.br", true, Set.of("PACIENTE"))));

        PacienteOutput saida = cadastrar.executar(entrada("123456789012345"));

        assertThat(saida.nome()).isEqualTo("Maria Souza");
        assertThat(saida.email()).isEqualTo("maria@sus.gov.br");
        assertThat(saida.cartaoSus()).isEqualTo("123456789012345");
        assertThat(saida.ativo()).isTrue();
    }

    @Test
    @DisplayName("RN-04: usuario sem o perfil PACIENTE e rejeitado")
    void usuarioSemPerfilPacienteEhRejeitado() {
        when(iam.usuarioAtivoPossuiRole(USUARIO, "PACIENTE")).thenReturn(false);

        assertThatThrownBy(() -> cadastrar.executar(entrada("123456789012345")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("PACIENTE");
        verify(pacientes, never()).salvar(any());
    }

    @Test
    @DisplayName("RN-01: usuario que ja tem cadastro de paciente gera conflito")
    void usuarioComCadastroGeraConflito() {
        when(iam.usuarioAtivoPossuiRole(USUARIO, "PACIENTE")).thenReturn(true);
        when(pacientes.existePorUsuarioId(USUARIO)).thenReturn(true);

        assertThatThrownBy(() -> cadastrar.executar(entrada("123456789012345")))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("ja possui cadastro");
        verify(pacientes, never()).salvar(any());
    }

    @Test
    @DisplayName("RN-02: cartao SUS duplicado gera conflito")
    void cartaoSusDuplicadoGeraConflito() {
        when(iam.usuarioAtivoPossuiRole(USUARIO, "PACIENTE")).thenReturn(true);
        when(pacientes.existePorUsuarioId(USUARIO)).thenReturn(false);
        when(pacientes.existePorCartaoSus("123456789012345")).thenReturn(true);

        assertThatThrownBy(() -> cadastrar.executar(entrada("123456789012345")))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("cartao SUS");
        verify(pacientes, never()).salvar(any(Paciente.class));
    }
}
