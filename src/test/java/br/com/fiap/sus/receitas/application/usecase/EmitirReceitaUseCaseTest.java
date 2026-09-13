package br.com.fiap.sus.receitas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.receitas.application.dto.EmitirReceitaDTO;
import br.com.fiap.sus.receitas.application.dto.ItemReceitaDTO;
import br.com.fiap.sus.receitas.application.dto.ReceitaOutput;
import br.com.fiap.sus.receitas.domain.enums.SituacaoReceita;
import br.com.fiap.sus.receitas.domain.model.Receita;
import br.com.fiap.sus.receitas.domain.repository.ReceitaRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
class EmitirReceitaUseCaseTest {

    @Mock
    private ReceitaRepository receitaRepository;

    @Mock
    private CadastroQuery cadastroQuery;

    @Mock
    private UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    private EmitirReceitaUseCase useCase;

    private final UUID pacienteId = UUID.randomUUID();
    private final UUID medicoId = UUID.randomUUID();
    private final UUID usuarioId = UUID.randomUUID();

    @BeforeEach
    void configurar() {
        useCase = new EmitirReceitaUseCase(receitaRepository, cadastroQuery, usuarioAutenticadoProvider);
    }

    private EmitirReceitaDTO dtoValido() {
        List<ItemReceitaDTO> itens = List.of(
                new ItemReceitaDTO("Paracetamol", "500mg", "8 em 8 horas", "5 dias", null));
        return new EmitirReceitaDTO(pacienteId, null, itens, Instant.now().plus(30, ChronoUnit.DAYS),
                "Observacao");
    }

    @Test
    @DisplayName("medico emite receita para paciente ativo")
    void medicoEmiteParaPacienteAtivo() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Dr. Carlos", Set.of("MEDICO"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.medicoIdDoUsuario(usuarioId)).thenReturn(Optional.of(medicoId));
        when(cadastroQuery.pacienteAtivoExiste(pacienteId)).thenReturn(true);
        when(receitaRepository.salvar(any(Receita.class))).thenAnswer(inv -> inv.getArgument(0));

        ReceitaOutput resultado = useCase.executar(dtoValido());

        assertThat(resultado.medicoId()).isEqualTo(medicoId);
        assertThat(resultado.situacao()).isEqualTo(SituacaoReceita.ATIVA);
        assertThat(resultado.itens()).hasSize(1);
    }

    @Test
    @DisplayName("nao emite se paciente estiver inativo ou nao existir")
    void naoEmiteComPacienteInativo() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Dr. Carlos", Set.of("MEDICO"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.medicoIdDoUsuario(usuarioId)).thenReturn(Optional.of(medicoId));
        when(cadastroQuery.pacienteAtivoExiste(pacienteId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar(dtoValido()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}