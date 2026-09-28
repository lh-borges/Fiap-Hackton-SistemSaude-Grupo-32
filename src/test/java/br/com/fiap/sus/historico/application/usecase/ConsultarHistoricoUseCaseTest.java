package br.com.fiap.sus.historico.application.usecase;

import br.com.fiap.sus.historico.application.service.AcessoHistoricoService;
import br.com.fiap.sus.historico.domain.enums.TipoRegistro;
import br.com.fiap.sus.historico.domain.model.HistoricoFiltro;
import br.com.fiap.sus.historico.domain.model.RegistroHistorico;
import br.com.fiap.sus.historico.domain.port.FonteHistorico;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("ConsultarHistoricoUseCase")
class ConsultarHistoricoUseCaseTest {
    final AcessoHistoricoService acesso = mock(AcessoHistoricoService.class);
    final FonteHistorico fonteA = mock(FonteHistorico.class);
    final FonteHistorico fonteB = mock(FonteHistorico.class);
    final UsuarioAutenticadoProvider usuarios = mock(UsuarioAutenticadoProvider.class);
    final ConsultarHistoricoUseCase useCase = new ConsultarHistoricoUseCase(acesso, List.of(fonteA, fonteB), usuarios);
    final UUID pacienteId = UUID.randomUUID();
    final UsuarioAutenticado usuario = new UsuarioAutenticado(UUID.randomUUID(), "Ana", Set.of("PACIENTE"));
    final RegistroHistorico consulta = registro(TipoRegistro.CONSULTA, "2026-09-01T10:00:00Z");
    final RegistroHistorico exame = registro(TipoRegistro.EXAME, "2026-09-03T10:00:00Z");
    final RegistroHistorico receita = registro(TipoRegistro.RECEITA, "2026-09-02T10:00:00Z");

    @BeforeEach
    void configurar() {
        when(usuarios.obrigatorio()).thenReturn(usuario);
        when(usuarios.atual()).thenReturn(Optional.of(usuario));
        when(acesso.pacienteAutorizado(usuario, null)).thenReturn(pacienteId);
        when(fonteA.registrosDoPaciente(pacienteId)).thenReturn(List.of(consulta, exame));
        when(fonteB.registrosDoPaciente(pacienteId)).thenReturn(List.of(receita));
    }

    @Test
    @DisplayName("RF-01/RF-02: junta todas as fontes do paciente autorizado e ordena")
    void juntaFontes() {
        var pagina = useCase.executar(null, HistoricoFiltro.todos(), 0, 20);

        assertThat(pagina.conteudo()).containsExactly(exame, receita, consulta);
        assertThat(pagina.totalElementos()).isEqualTo(3);
        verify(fonteA).registrosDoPaciente(pacienteId);
        verify(fonteB).registrosDoPaciente(pacienteId);
    }

    @Test
    @DisplayName("RF-04/RF-05: aplica filtro e paginacao; filtro nulo equivale a todos")
    void filtraEPagina() {
        var soReceitas = useCase.executar(null, new HistoricoFiltro(Set.of(TipoRegistro.RECEITA), null, null), 0, 20);
        assertThat(soReceitas.conteudo()).containsExactly(receita);

        var segundaPagina = useCase.executar(null, null, 1, 2);
        assertThat(segundaPagina.conteudo()).containsExactly(consulta);
        assertThat(segundaPagina.totalPaginas()).isEqualTo(2);
    }

    @Test
    @DisplayName("EX-05: paciente sem registros recebe pagina vazia")
    void semRegistros() {
        when(fonteA.registrosDoPaciente(pacienteId)).thenReturn(List.of());
        when(fonteB.registrosDoPaciente(pacienteId)).thenReturn(List.of());

        var pagina = useCase.executar(null, HistoricoFiltro.todos(), 0, 20);

        assertThat(pagina.conteudo()).isEmpty();
        assertThat(pagina.totalElementos()).isZero();
    }

    @Test
    @DisplayName("RF-06: quem decide o paciente e o servico de acesso, nunca as fontes")
    void acessoAntesDasFontes() {
        var solicitado = UUID.randomUUID();
        when(acesso.pacienteAutorizado(usuario, solicitado)).thenThrow(new IllegalArgumentException("x"));

        assertThatThrownBy(() -> useCase.executar(solicitado, null, 0, 20)).isInstanceOf(IllegalArgumentException.class);
        verify(fonteA, never()).registrosDoPaciente(any());
    }

    static RegistroHistorico registro(TipoRegistro tipo, String data) {
        return new RegistroHistorico(tipo, UUID.randomUUID(), Instant.parse(data), "Titulo", "SITUACAO", null, null);
    }
}
