package br.com.fiap.sus.receitas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.receitas.application.dto.ReceitaOutput;
import br.com.fiap.sus.receitas.application.dto.RenovarReceitaDTO;
import br.com.fiap.sus.receitas.domain.enums.SituacaoReceita;
import br.com.fiap.sus.receitas.domain.model.ItemReceita;
import br.com.fiap.sus.receitas.domain.model.Receita;
import br.com.fiap.sus.receitas.domain.repository.ReceitaRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
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
class RenovarReceitaUseCaseTest {

    @Mock
    private ReceitaRepository receitaRepository;

    @Mock
    private CadastroQuery cadastroQuery;

    @Mock
    private UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    private RenovarReceitaUseCase useCase;

    private final UUID pacienteId = UUID.randomUUID();
    private final UUID medicoId = UUID.randomUUID();
    private final UUID usuarioId = UUID.randomUUID();
    private final UUID receitaOrigemId = UUID.randomUUID();
    private final List<ItemReceita> itens = List.of(
            ItemReceita.criar("Paracetamol", "500mg", "8 em 8 horas", "5 dias", null));

    @BeforeEach
    void configurar() {
        useCase = new RenovarReceitaUseCase(receitaRepository, cadastroQuery, usuarioAutenticadoProvider);
    }

    /** Programa o medico autenticado (chamado antes de tudo dentro do usecase). */
    private void mockarMedicoAtivo() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Dr. Carlos", Set.of("MEDICO"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.medicoIdDoUsuario(usuarioId)).thenReturn(Optional.of(medicoId));
        when(cadastroQuery.medicoAtivoExiste(medicoId)).thenReturn(true);
    }

    private Receita receitaAtiva() {
        return Receita.reconstituir(receitaOrigemId, pacienteId, medicoId, null, itens,
                Instant.now().minus(10, ChronoUnit.DAYS), Instant.now().plus(20, ChronoUnit.DAYS),
                "Observacao", SituacaoReceita.ATIVA, null, null);
    }

    @Test
    @DisplayName("medico ativo renova receita ativa e nao vencida")
    void medicoAtivoRenovaReceitaAtiva() {
        mockarMedicoAtivo();
        when(receitaRepository.buscarPorId(receitaOrigemId)).thenReturn(Optional.of(receitaAtiva()));
        when(receitaRepository.salvar(any(Receita.class))).thenAnswer(inv -> inv.getArgument(0));

        ReceitaOutput resultado = useCase.executar(
                new RenovarReceitaDTO(receitaOrigemId, null, Instant.now().plus(30, ChronoUnit.DAYS), "Renovacao"));

        assertThat(resultado.situacao()).isEqualTo(SituacaoReceita.ATIVA);
        assertThat(resultado.receitaOrigemId()).isEqualTo(receitaOrigemId);
    }

    @Test
    @DisplayName("nao renova se o medico autenticado estiver inativo")
    void naoRenovaSeMedicoInativo() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Dr. Carlos", Set.of("MEDICO"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.medicoIdDoUsuario(usuarioId)).thenReturn(Optional.of(medicoId));
        when(cadastroQuery.medicoAtivoExiste(medicoId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar(
                new RenovarReceitaDTO(receitaOrigemId, null, Instant.now().plus(30, ChronoUnit.DAYS), "Renovacao")))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("lanca excecao se a receita original nao existir")
    void lancaExcecaoSeReceitaNaoExiste() {
        mockarMedicoAtivo();
        when(receitaRepository.buscarPorId(receitaOrigemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(
                new RenovarReceitaDTO(receitaOrigemId, null, Instant.now().plus(30, ChronoUnit.DAYS), "Renovacao")))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("nao renova receita vencida")
    void naoRenovaReceitaVencida() {
        mockarMedicoAtivo();
        Receita receitaVencida = Receita.reconstituir(receitaOrigemId, pacienteId, medicoId, null, itens,
                Instant.now().minus(60, ChronoUnit.DAYS), Instant.now().minus(1, ChronoUnit.DAYS),
                "Observacao", SituacaoReceita.ATIVA, null, null);
        when(receitaRepository.buscarPorId(receitaOrigemId)).thenReturn(Optional.of(receitaVencida));

        assertThatThrownBy(() -> useCase.executar(
                new RenovarReceitaDTO(receitaOrigemId, null, Instant.now().plus(30, ChronoUnit.DAYS), "Renovacao")))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}