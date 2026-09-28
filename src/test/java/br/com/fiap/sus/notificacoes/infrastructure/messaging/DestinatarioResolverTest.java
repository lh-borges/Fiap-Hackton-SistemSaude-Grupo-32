package br.com.fiap.sus.notificacoes.infrastructure.messaging;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.cadastros.api.MedicoResumo;
import br.com.fiap.sus.cadastros.api.PacienteResumo;
import br.com.fiap.sus.notificacoes.application.dto.PapelDestinatario;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DestinatarioResolverTest {
    @Mock CadastroQuery cadastros;

    @Test
    void resolvePacienteEMedicoParaOUsuarioVinculado() {
        var paciente = UUID.randomUUID();
        var medico = UUID.randomUUID();
        var usuarioPaciente = UUID.randomUUID();
        var usuarioMedico = UUID.randomUUID();
        when(cadastros.resumoDoPaciente(paciente)).thenReturn(Optional.of(
                new PacienteResumo(paciente, usuarioPaciente, "Ana", "52998224725", "ana@example.org", true)));
        when(cadastros.resumoDoMedico(medico)).thenReturn(Optional.of(
                new MedicoResumo(medico, usuarioMedico, "Dr. Joao", "joao@example.org", "12345", "SP",
                        UUID.randomUUID(), "Clinica Geral", true)));
        var resolver = new DestinatarioResolver(cadastros);

        var p = resolver.paciente(paciente);
        var m = resolver.medico(medico);

        assertThat(p.papel()).isEqualTo(PapelDestinatario.PACIENTE);
        assertThat(p.usuarioId()).isEqualTo(usuarioPaciente);
        assertThat(m.papel()).isEqualTo(PapelDestinatario.MEDICO);
        assertThat(m.usuarioId()).isEqualTo(usuarioMedico);
    }

    @Test
    void semVinculoOuSemIdDevolveDestinatarioNaoResolvido() {
        var desconhecido = UUID.randomUUID();
        when(cadastros.resumoDoPaciente(desconhecido)).thenReturn(Optional.empty());
        var resolver = new DestinatarioResolver(cadastros);

        assertThat(resolver.paciente(desconhecido).resolvido()).isFalse();
        assertThat(resolver.medico(null).resolvido()).isFalse();
        verify(cadastros, never()).resumoDoMedico(any());
    }
}
