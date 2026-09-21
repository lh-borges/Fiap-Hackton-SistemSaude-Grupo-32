package br.com.fiap.sus.cadastros.domain.model;

import br.com.fiap.sus.cadastros.domain.enums.CategoriaExame;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogosCadastroTest {

    @Test
    void unidadeNormalizaDadosEInativa() {
        var unidade = UnidadeSaude.criar(" UBS Central ", "12.345-67", "(11) 99999-8888");

        assertThat(unidade.getId()).isNotNull();
        assertThat(unidade.getNome()).isEqualTo("UBS Central");
        assertThat(unidade.getCnes()).isEqualTo("1234567");
        assertThat(unidade.getTelefone()).isEqualTo("11999998888");
        assertThat(unidade.isAtivo()).isTrue();

        unidade.alterarDados("UBS Norte", "");
        assertThat(unidade.getNome()).isEqualTo("UBS Norte");
        assertThat(unidade.getTelefone()).isNull();

        unidade.inativar();
        assertThat(unidade.isAtivo()).isFalse();
        assertThatThrownBy(unidade::inativar).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void unidadeRejeitaDadosInvalidos() {
        assertThatThrownBy(() -> UnidadeSaude.criar("UB", "1234567", null))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> UnidadeSaude.criar("UBS Central", "123", null))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void tipoExameNormalizaDadosEInativa() {
        var tipo = TipoExame.criar(" Raio-X de torax ", CategoriaExame.IMAGEM, " Sem preparo ");

        assertThat(tipo.getId()).isNotNull();
        assertThat(tipo.getNome()).isEqualTo("Raio-X de torax");
        assertThat(tipo.getCategoria()).isEqualTo(CategoriaExame.IMAGEM);
        assertThat(tipo.getPreparo()).isEqualTo("Sem preparo");
        assertThat(tipo.isAtivo()).isTrue();

        tipo.alterarDados("Hemograma completo", "jejum");
        assertThat(tipo.getNome()).isEqualTo("Hemograma completo");
        assertThat(tipo.getPreparo()).isEqualTo("jejum");

        tipo.inativar();
        assertThat(tipo.isAtivo()).isFalse();
        assertThatThrownBy(tipo::inativar).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void tipoExameRejeitaDadosInvalidos() {
        assertThatThrownBy(() -> TipoExame.criar("RX", CategoriaExame.IMAGEM, null))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> TipoExame.criar("Raio-X", null, null))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void especialidadeNormalizaDadosEInativa() {
        var criadaEm = Instant.parse("2026-01-01T00:00:00Z");
        var especialidade = Especialidade.reconstituir(UUID.randomUUID(), " Clinica Geral ",
                " Atendimento inicial ", true, criadaEm, criadaEm);

        assertThat(especialidade.getNome()).isEqualTo("Clinica Geral");
        assertThat(especialidade.getDescricao()).isEqualTo("Atendimento inicial");
        assertThat(especialidade.getCriadoEm()).isEqualTo(criadaEm);

        especialidade.alterarDados("Radiologia", null);
        assertThat(especialidade.getNome()).isEqualTo("Radiologia");
        assertThat(especialidade.getDescricao()).isNull();

        especialidade.inativar();
        assertThat(especialidade.isAtivo()).isFalse();
        assertThatThrownBy(especialidade::inativar).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void especialidadeRejeitaNomeInvalido() {
        assertThatThrownBy(() -> Especialidade.criar("US", null))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}
