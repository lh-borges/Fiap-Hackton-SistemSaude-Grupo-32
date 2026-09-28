package br.com.fiap.sus.notificacoes.domain.repository;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class NotificacaoFiltroTest {

    @Test
    void exigeUsuarioDestinatario() {
        assertThatThrownBy(() -> new NotificacaoFiltro(null, true)).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void todasNaoFiltraPorLeitura() {
        var usuario = UUID.randomUUID();
        var filtro = NotificacaoFiltro.todas(usuario);
        assertThat(filtro.usuarioId()).isEqualTo(usuario);
        assertThat(filtro.lida()).isNull();
    }
}
