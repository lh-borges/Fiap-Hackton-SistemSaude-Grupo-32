package br.com.fiap.sus.cadastros.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.cadastros.application.dto.PacienteOutput;
import br.com.fiap.sus.cadastros.domain.enums.Sexo;
import br.com.fiap.sus.cadastros.domain.model.Paciente;
import br.com.fiap.sus.cadastros.domain.repository.PacienteRepository;
import br.com.fiap.sus.iam.api.IamQuery;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
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
@DisplayName("Consulta de paciente e posse do dado (HU-04, Artigo IV.4)")
class BuscarPacienteUseCaseTest {

    private static final UUID USUARIO_DONO = UUID.randomUUID();
    private static final UUID OUTRO_USUARIO = UUID.randomUUID();

    @Mock
    private PacienteRepository pacientes;
    @Mock
    private IamQuery iam;
    @Mock
    private UsuarioAutenticadoProvider autenticado;

    private BuscarPacienteUseCase buscar;
    private Paciente paciente;

    @BeforeEach
    void preparar() {
        buscar = new BuscarPacienteUseCase(pacientes, iam, autenticado);
        paciente = Paciente.criar(USUARIO_DONO, "123456789012345", LocalDate.of(1990, 1, 20),
                Sexo.FEMININO, "A+");
    }

    private void autenticadoComo(UUID usuarioId, String... roles) {
        when(autenticado.obrigatorio()).thenReturn(new UsuarioAutenticado(usuarioId, "Fulano", Set.of(roles)));
    }

    @Test
    @DisplayName("o paciente enxerga o proprio cadastro")
    void pacienteVeOProprioCadastro() {
        autenticadoComo(USUARIO_DONO, "PACIENTE");
        when(pacientes.porId(paciente.getId())).thenReturn(Optional.of(paciente));
        when(iam.resumoDoUsuario(any())).thenReturn(Optional.empty());

        PacienteOutput saida = buscar.executar(paciente.getId());

        assertThat(saida.id()).isEqualTo(paciente.getId());
    }

    @Test
    @DisplayName("cadastro de terceiro responde como inexistente, nao como proibido")
    void cadastroDeTerceiroRespondeComoInexistente() {
        autenticadoComo(OUTRO_USUARIO, "PACIENTE");
        when(pacientes.porId(paciente.getId())).thenReturn(Optional.of(paciente));

        assertThatThrownBy(() -> buscar.executar(paciente.getId()))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("nao encontrado");
    }

    @Test
    @DisplayName("atendente enxerga o cadastro de qualquer paciente")
    void atendenteVeQualquerCadastro() {
        autenticadoComo(OUTRO_USUARIO, "ATENDENTE");
        when(pacientes.porId(paciente.getId())).thenReturn(Optional.of(paciente));
        when(iam.resumoDoUsuario(any())).thenReturn(Optional.empty());

        assertThat(buscar.executar(paciente.getId()).id()).isEqualTo(paciente.getId());
    }

    @Test
    @DisplayName("medico enxerga o cadastro de qualquer paciente")
    void medicoVeQualquerCadastro() {
        autenticadoComo(OUTRO_USUARIO, "MEDICO");
        when(pacientes.porId(paciente.getId())).thenReturn(Optional.of(paciente));
        when(iam.resumoDoUsuario(any())).thenReturn(Optional.empty());

        assertThat(buscar.executar(paciente.getId()).id()).isEqualTo(paciente.getId());
    }

    @Test
    @DisplayName("paciente inexistente responde 404")
    void pacienteInexistente() {
        autenticadoComo(OUTRO_USUARIO, "ADMINISTRADOR");
        UUID id = UUID.randomUUID();
        when(pacientes.porId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> buscar.executar(id)).isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
