package br.com.fiap.sus.notificacoes.infrastructure.persistence;

import br.com.fiap.sus.notificacoes.domain.enums.TipoNotificacao;
import br.com.fiap.sus.notificacoes.domain.model.Notificacao;
import br.com.fiap.sus.notificacoes.domain.repository.NotificacaoFiltro;
import br.com.fiap.sus.notificacoes.infrastructure.persistence.mapper.NotificacaoPersistenceMapper;
import br.com.fiap.sus.notificacoes.infrastructure.persistence.repository.EventoProcessadoRepositoryAdapter;
import br.com.fiap.sus.notificacoes.infrastructure.persistence.repository.NotificacaoRepositoryAdapter;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"}, showSql = false)
@Import({NotificacaoRepositoryAdapter.class, NotificacaoPersistenceMapper.class, EventoProcessadoRepositoryAdapter.class})
@Sql(statements = {"DROP TABLE IF EXISTS not_notificacao", "DROP TABLE IF EXISTS not_evento_processado"})
@Sql("classpath:db/migration/V0082__not_notificacao.sql")
@DisplayName("Persistencia de notificacoes (H2 com a migration real)")
class NotificacaoRepositoryAdapterTest {
    @Autowired NotificacaoRepositoryAdapter notificacoes;
    @Autowired EventoProcessadoRepositoryAdapter eventos;
    @Autowired EntityManager entityManager;
    final UUID usuario = UUID.randomUUID();
    final UUID outroUsuario = UUID.randomUUID();

    Notificacao salvar(UUID dono, UUID evento, Instant criadoEm, boolean lida) {
        var n = Notificacao.reconstituir(UUID.randomUUID(), dono, evento, TipoNotificacao.CONSULTA_AGENDADA,
                "Consulta agendada", "Sua consulta foi agendada.", UUID.randomUUID(), lida,
                lida ? criadoEm.plusSeconds(60) : null, criadoEm, criadoEm);
        notificacoes.salvar(n);
        entityManager.flush();
        entityManager.clear();
        return n;
    }

    @Test
    @DisplayName("RF-03: lista do proprio usuario, da mais recente para a mais antiga, com filtro de leitura")
    void listaOrdenadaEFiltrada() {
        var antiga = salvar(usuario, UUID.randomUUID(), Instant.parse("2026-09-01T10:00:00Z"), true);
        var recente = salvar(usuario, UUID.randomUUID(), Instant.parse("2026-09-03T10:00:00Z"), false);
        var media = salvar(usuario, UUID.randomUUID(), Instant.parse("2026-09-02T10:00:00Z"), false);
        salvar(outroUsuario, UUID.randomUUID(), Instant.parse("2026-09-04T10:00:00Z"), false);

        var todas = notificacoes.listar(NotificacaoFiltro.todas(usuario), 0, 10);
        assertThat(todas.totalElementos()).isEqualTo(3);
        assertThat(todas.conteudo()).extracting(Notificacao::getId)
                .containsExactly(recente.getId(), media.getId(), antiga.getId());

        var naoLidas = notificacoes.listar(new NotificacaoFiltro(usuario, false), 0, 10);
        assertThat(naoLidas.conteudo()).extracting(Notificacao::getId).containsExactly(recente.getId(), media.getId());

        var lidas = notificacoes.listar(new NotificacaoFiltro(usuario, true), 0, 10);
        assertThat(lidas.conteudo()).extracting(Notificacao::getId).containsExactly(antiga.getId());

        var segundaPagina = notificacoes.listar(NotificacaoFiltro.todas(usuario), 1, 2);
        assertThat(segundaPagina.conteudo()).extracting(Notificacao::getId).containsExactly(antiga.getId());
        assertThat(segundaPagina.totalPaginas()).isEqualTo(2);
    }

    @Test
    @DisplayName("RF-05: contagem de nao lidas por usuario")
    void contaNaoLidas() {
        salvar(usuario, UUID.randomUUID(), Instant.now(), false);
        salvar(usuario, UUID.randomUUID(), Instant.now(), false);
        salvar(usuario, UUID.randomUUID(), Instant.now(), true);
        salvar(outroUsuario, UUID.randomUUID(), Instant.now(), false);

        assertThat(notificacoes.contarNaoLidas(usuario)).isEqualTo(2);
        assertThat(notificacoes.contarNaoLidas(outroUsuario)).isEqualTo(1);
        assertThat(notificacoes.contarNaoLidas(UUID.randomUUID())).isZero();
    }

    @Test
    @DisplayName("RN-04: a marcacao de leitura e persistida e o restante permanece imutavel")
    void persisteLeitura() {
        var criada = salvar(usuario, UUID.randomUUID(), Instant.parse("2026-09-01T10:00:00Z"), false);
        var carregada = notificacoes.buscarPorId(criada.getId()).orElseThrow();
        var lidoEm = Instant.parse("2026-09-05T08:00:00Z");
        carregada.marcarComoLida(lidoEm);
        notificacoes.salvar(carregada);
        entityManager.flush();
        entityManager.clear();

        var relida = notificacoes.buscarPorId(criada.getId()).orElseThrow();
        assertThat(relida.isLida()).isTrue();
        assertThat(relida.getDataLeitura()).isEqualTo(lidoEm);
        assertThat(relida.getAtualizadoEm()).isEqualTo(lidoEm);
        assertThat(relida.getCriadoEm()).isEqualTo(criada.getCriadoEm());
        assertThat(relida.getTitulo()).isEqualTo(criada.getTitulo());
        assertThat(notificacoes.contarNaoLidas(usuario)).isZero();
    }

    @Test
    @DisplayName("RN-03: o banco recusa duas notificacoes do mesmo evento para o mesmo usuario")
    void recusaDuplicataDoMesmoEventoParaOMesmoUsuario() {
        var evento = UUID.randomUUID();
        salvar(usuario, evento, Instant.now(), false);
        assertThatThrownBy(() -> salvar(usuario, evento, Instant.now(), false))
                .isInstanceOfAny(DataIntegrityViolationException.class, jakarta.persistence.PersistenceException.class);
    }

    @Test
    @DisplayName("Artigo VI.3: marcador de evento processado e unico")
    void marcadorDeEventoProcessado() {
        var evento = UUID.randomUUID();
        assertThat(eventos.jaProcessado(evento)).isFalse();
        eventos.registrar(evento, "ConsultaAgendadaEvent", Instant.now());
        assertThat(eventos.jaProcessado(evento)).isTrue();
        assertThatThrownBy(() -> eventos.registrar(evento, "ConsultaAgendadaEvent", Instant.now()))
                .isInstanceOfAny(DataIntegrityViolationException.class, jakarta.persistence.PersistenceException.class);
    }
}
