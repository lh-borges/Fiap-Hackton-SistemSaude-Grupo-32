package br.com.fiap.sus.notificacoes.application.usecase;

import br.com.fiap.sus.notificacoes.domain.enums.TipoNotificacao;
import br.com.fiap.sus.notificacoes.domain.model.Notificacao;
import br.com.fiap.sus.notificacoes.domain.repository.NotificacaoFiltro;
import br.com.fiap.sus.notificacoes.domain.repository.NotificacaoRepository;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Casos de uso de leitura e marcacao (HU-02, HU-03, EX-04)")
class LeituraNotificacoesUseCaseTest {
    @Mock NotificacaoRepository repositorio;
    @Mock UsuarioAutenticadoProvider usuarios;
    final UUID usuario = UUID.randomUUID();
    final UUID outroUsuario = UUID.randomUUID();

    @BeforeEach
    void autenticado() {
        lenient().when(usuarios.obrigatorio()).thenReturn(new UsuarioAutenticado(usuario, "Paciente", Set.of("PACIENTE")));
    }

    Notificacao de(UUID dono) {
        return Notificacao.criar(dono, UUID.randomUUID(), TipoNotificacao.RECEITA_EMITIDA, "Receita emitida",
                "Uma nova receita foi emitida para voce.", UUID.randomUUID());
    }

    @Nested
    class Listar {
        ListarMinhasNotificacoesUseCase useCase;

        @BeforeEach
        void criar() { useCase = new ListarMinhasNotificacoesUseCase(repositorio, usuarios); }

        @Test
        void listaSomenteDoUsuarioDoTokenComFiltroDeLeitura() {
            var n = de(usuario);
            when(repositorio.listar(any(), eq(0), eq(20))).thenReturn(PaginaResultado.de(List.of(n), 0, 20, 1));

            var pagina = useCase.executar(false, 0, 20);

            var filtro = ArgumentCaptor.forClass(NotificacaoFiltro.class);
            verify(repositorio).listar(filtro.capture(), eq(0), eq(20));
            assertThat(filtro.getValue().usuarioId()).isEqualTo(usuario);
            assertThat(filtro.getValue().lida()).isFalse();
            assertThat(pagina.totalElementos()).isEqualTo(1);
            var item = pagina.conteudo().get(0);
            assertThat(item.id()).isEqualTo(n.getId());
            assertThat(item.recurso()).isEqualTo("receitas");
            assertThat(item.lida()).isFalse();
        }

        @Test
        void rejeitaPaginacaoInvalida() {
            assertThatThrownBy(() -> useCase.executar(null, -1, 20)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> useCase.executar(null, 0, 0)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> useCase.executar(null, 0, 101)).isInstanceOf(IllegalArgumentException.class);
            verifyNoInteractions(repositorio);
        }
    }

    @Nested
    class Consultar {
        ConsultarMinhaNotificacaoUseCase useCase;

        @BeforeEach
        void criar() { useCase = new ConsultarMinhaNotificacaoUseCase(repositorio, usuarios); }

        @Test
        void devolveAPropria() {
            var n = de(usuario);
            when(repositorio.buscarPorId(n.getId())).thenReturn(Optional.of(n));
            assertThat(useCase.executar(n.getId()).id()).isEqualTo(n.getId());
        }

        @Test
        @DisplayName("HU-03: notificacao de terceiro e tratada como inexistente")
        void deTerceiroEhInexistente() {
            var alheia = de(outroUsuario);
            when(repositorio.buscarPorId(alheia.getId())).thenReturn(Optional.of(alheia));
            assertThatThrownBy(() -> useCase.executar(alheia.getId())).isInstanceOf(RecursoNaoEncontradoException.class);
        }

        @Test
        void inexistenteResponde404() {
            var id = UUID.randomUUID();
            when(repositorio.buscarPorId(id)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> useCase.executar(id)).isInstanceOf(RecursoNaoEncontradoException.class);
        }
    }

    @Nested
    class MarcarComoLida {
        MarcarNotificacaoComoLidaUseCase useCase;

        @BeforeEach
        void criar() { useCase = new MarcarNotificacaoComoLidaUseCase(repositorio, usuarios); }

        @Test
        @DisplayName("RF-04: marca e persiste a leitura")
        void marcaEPersiste() {
            var n = de(usuario);
            when(repositorio.buscarPorId(n.getId())).thenReturn(Optional.of(n));
            when(repositorio.salvar(n)).thenReturn(n);

            var saida = useCase.executar(n.getId());

            assertThat(saida.lida()).isTrue();
            assertThat(saida.dataLeitura()).isNotNull();
            verify(repositorio).salvar(n);
        }

        @Test
        @DisplayName("RN-04: marcar de novo nao regrava nem altera a data")
        void segundaMarcacaoNaoRegrava() {
            var n = de(usuario);
            n.marcarComoLida();
            var dataOriginal = n.getDataLeitura();
            when(repositorio.buscarPorId(n.getId())).thenReturn(Optional.of(n));

            var saida = useCase.executar(n.getId());

            assertThat(saida.dataLeitura()).isEqualTo(dataOriginal);
            verify(repositorio, never()).salvar(any());
        }

        @Test
        @DisplayName("EX-04: marcar notificacao de terceiro responde como inexistente")
        void deTerceiroEhInexistente() {
            var alheia = de(outroUsuario);
            when(repositorio.buscarPorId(alheia.getId())).thenReturn(Optional.of(alheia));
            assertThatThrownBy(() -> useCase.executar(alheia.getId())).isInstanceOf(RecursoNaoEncontradoException.class);
            verify(repositorio, never()).salvar(any());
        }
    }

    @Nested
    class Contar {
        @Test
        @DisplayName("RF-05: conta as nao lidas do usuario do token")
        void contaDoUsuario() {
            when(repositorio.contarNaoLidas(usuario)).thenReturn(4L);
            var useCase = new ContarNaoLidasUseCase(repositorio, usuarios);
            assertThat(useCase.executar().quantidade()).isEqualTo(4);
        }
    }
}
