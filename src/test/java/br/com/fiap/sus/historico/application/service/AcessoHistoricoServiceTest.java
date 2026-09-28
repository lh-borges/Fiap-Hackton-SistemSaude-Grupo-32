package br.com.fiap.sus.historico.application.service;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.cadastros.api.PacienteResumo;
import br.com.fiap.sus.consultas.api.ConsultaQuery;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Acesso ao historico por perfil (spec 010, secao 4)")
class AcessoHistoricoServiceTest {
    final CadastroQuery cadastros = mock(CadastroQuery.class);
    final ConsultaQuery consultas = mock(ConsultaQuery.class);
    final AcessoHistoricoService service = new AcessoHistoricoService(cadastros, consultas);
    final UUID usuarioId = UUID.randomUUID();
    final UUID pacienteId = UUID.randomUUID();
    final UUID outroPaciente = UUID.randomUUID();
    final UUID medicoId = UUID.randomUUID();

    UsuarioAutenticado usuario(String role) {
        return new UsuarioAutenticado(usuarioId, "Usuario", Set.of(role));
    }

    @Test
    @DisplayName("RN-02: paciente ve o proprio historico e o parametro e ignorado")
    void pacienteIgnoraParametro() {
        when(cadastros.pacienteIdDoUsuario(usuarioId)).thenReturn(Optional.of(pacienteId));

        assertThat(service.pacienteAutorizado(usuario("PACIENTE"), outroPaciente)).isEqualTo(pacienteId);
        assertThat(service.pacienteAutorizado(usuario("PACIENTE"), null)).isEqualTo(pacienteId);
        verifyNoInteractions(consultas);
    }

    @Test
    @DisplayName("EX-04: usuario PACIENTE sem cadastro vinculado responde 404")
    void pacienteSemCadastro() {
        when(cadastros.pacienteIdDoUsuario(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.pacienteAutorizado(usuario("PACIENTE"), null))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("HU-02/RN-03: medico ve paciente com quem tem consulta")
    void medicoAtende() {
        when(cadastros.medicoIdDoUsuario(usuarioId)).thenReturn(Optional.of(medicoId));
        when(consultas.pacientesDoMedico(medicoId)).thenReturn(Set.of(pacienteId));

        assertThat(service.pacienteAutorizado(usuario("MEDICO"), pacienteId)).isEqualTo(pacienteId);
    }

    @Test
    @DisplayName("HU-03/EX-02: medico sem consulta com o paciente recebe recurso inexistente")
    void medicoNaoAtende() {
        when(cadastros.medicoIdDoUsuario(usuarioId)).thenReturn(Optional.of(medicoId));
        when(consultas.pacientesDoMedico(medicoId)).thenReturn(Set.of(pacienteId));

        assertThatThrownBy(() -> service.pacienteAutorizado(usuario("MEDICO"), outroPaciente))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("usuario MEDICO sem cadastro de medico responde 404")
    void medicoSemCadastro() {
        when(cadastros.medicoIdDoUsuario(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.pacienteAutorizado(usuario("MEDICO"), pacienteId))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("EX-01: medico e administrador precisam informar o paciente")
    void parametroObrigatorio() {
        assertThatThrownBy(() -> service.pacienteAutorizado(usuario("MEDICO"), null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.pacienteAutorizado(usuario("ADMINISTRADOR"), null))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(cadastros, consultas);
    }

    @Test
    @DisplayName("administrador ve qualquer paciente existente; inexistente responde 404")
    void administrador() {
        when(cadastros.resumoDoPaciente(pacienteId))
                .thenReturn(Optional.of(new PacienteResumo(pacienteId, UUID.randomUUID(), "Ana", "1", "a@x", true)));
        when(cadastros.resumoDoPaciente(outroPaciente)).thenReturn(Optional.empty());

        assertThat(service.pacienteAutorizado(usuario("ADMINISTRADOR"), pacienteId)).isEqualTo(pacienteId);
        assertThatThrownBy(() -> service.pacienteAutorizado(usuario("ADMINISTRADOR"), outroPaciente))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verifyNoInteractions(consultas);
    }

    @Test
    @DisplayName("EX-03 (defesa em profundidade): perfil sem regra de acesso nao resolve paciente")
    void perfilSemAcesso() {
        assertThatThrownBy(() -> service.pacienteAutorizado(usuario("ATENDENTE"), pacienteId))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
