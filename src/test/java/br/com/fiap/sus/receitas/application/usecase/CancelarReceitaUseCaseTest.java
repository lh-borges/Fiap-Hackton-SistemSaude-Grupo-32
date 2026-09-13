package br.com.fiap.sus.receitas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.receitas.application.dto.CancelarReceitaDTO;
import br.com.fiap.sus.receitas.application.dto.ReceitaOutput;
import br.com.fiap.sus.receitas.domain.enums.SituacaoReceita;
import br.com.fiap.sus.receitas.domain.model.ItemReceita;
import br.com.fiap.sus.receitas.domain.model.Receita;
import br.com.fiap.sus.receitas.domain.repository.ReceitaRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CancelarReceitaUseCaseTest {

    @Mock
    private ReceitaRepository receitaRepository;

    @Mock
    private CadastroQuery cadastroQuery;

    @Mock
    private UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    private CancelarReceitaUseCase useCase;

    private final UUID medicoId = UUID.randomUUID();
    private final UUID usuarioId = UUID.randomUUID();
    private final UUID receitaId = UUID.randomUUID();
    private final List<ItemReceita> itens = List.of(
            ItemReceita.criar("Paracetamol", "500mg", "8 em 8 horas", "5 dias", null));

    @BeforeEach
    void configurar() {
        useCase = new CancelarReceitaUseCase(receitaRepository, cadastroQuery, usuarioAutenticadoProvider);
    }

    private Receita receitaAtivaDoMedico(UUID medicoAutor) {
        return Receita.reconstituir(receitaId, UUID.randomUUID(), medicoAutor, null, itens,
                Instant.now().minus(1, ChronoUnit.DAYS), Instant.now().plus(20, ChronoUnit.DAYS),
                "Observacao", SituacaoReceita.ATIVA, null, null);
    }

    @Test
    @DisplayName("medico autor cancela a propria receita")
    void medicoAutorCancela() {
        when(receitaRepository.buscarPorId(receitaId)).thenReturn(Optional.of(receitaAtivaDoMedico(medicoId)));
        when(receitaRepository.salvar(any(Receita.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cadastroQuery.medicoIdDoUsuario(usuarioId)).thenReturn(Optional.of(medicoId));
        var usuario = new br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado(usuarioId,
                "Dr. Carlos", java.util.Set.of("MEDICO"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);

        ReceitaOutput resultado = useCase.executar(new CancelarReceitaDTO(receitaId, "Reacao adversa"));

        assertThat(resultado.situacao()).isEqualTo(SituacaoReceita.CANCELADA);
    }

    @Test
    @DisplayName("nao cancela se o medico autenticado nao for o autor")
    void naoCancelaSeNaoForAutor() {
        UUID medicoDiferente = UUID.randomUUID();
        when(receitaRepository.buscarPorId(receitaId)).thenReturn(Optional.of(receitaAtivaDoMedico(medicoDiferente)));
        when(cadastroQuery.medicoIdDoUsuario(usuarioId)).thenReturn(Optional.of(medicoId));
        var usuario = new br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado(usuarioId,
                "Dr. Carlos", java.util.Set.of("MEDICO"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);

        assertThatThrownBy(() -> useCase.executar(new CancelarReceitaDTO(receitaId, "Motivo")))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("lanca excecao se a receita nao existir")
    void lancaExcecaoSeReceitaNaoExiste() {
        when(receitaRepository.buscarPorId(receitaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(new CancelarReceitaDTO(receitaId, "Motivo")))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}