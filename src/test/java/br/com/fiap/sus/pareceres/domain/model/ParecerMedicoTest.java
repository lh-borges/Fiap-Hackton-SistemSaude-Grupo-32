package br.com.fiap.sus.pareceres.domain.model;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

class ParecerMedicoTest {
    private final UUID resultado = UUID.randomUUID();
    private final UUID paciente = UUID.randomUUID();
    private final UUID medico = UUID.randomUUID();

    @Test
    void emiteComReferenciasDataEIdentidadeProprias() {
        var antes = Instant.now();
        var parecer = ParecerMedico.emitir(resultado, paciente, medico, "  Interpretacao do resultado.  ");
        assertThat(parecer.getId()).isNotNull();
        assertThat(parecer.getResultadoExameId()).isEqualTo(resultado);
        assertThat(parecer.getPacienteId()).isEqualTo(paciente);
        assertThat(parecer.getMedicoId()).isEqualTo(medico);
        assertThat(parecer.getDescricao()).isEqualTo("Interpretacao do resultado.");
        assertThat(parecer.getDataParecer()).isBetween(antes, Instant.now());
    }

    @Test
    void segundaOpiniaoERetificacaoGeramNovosPareceres() {
        var original = ParecerMedico.emitir(resultado, paciente, medico, "Interpretacao original.");
        var segundaOpiniao = ParecerMedico.emitir(resultado, paciente, UUID.randomUUID(), "Segunda interpretacao.");
        var retificacao = ParecerMedico.emitir(resultado, paciente, medico, "Interpretacao retificada.");
        assertThat(original.getId()).isNotEqualTo(segundaOpiniao.getId()).isNotEqualTo(retificacao.getId());
        assertThat(original.getDescricao()).isEqualTo("Interpretacao original.");
        assertThat(segundaOpiniao.getResultadoExameId()).isEqualTo(original.getResultadoExameId());
    }

    @Test
    void exigeTodasAsReferencias() {
        assertThatThrownBy(() -> ParecerMedico.emitir(null, paciente, medico, "Descricao valida"))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> ParecerMedico.emitir(resultado, null, medico, "Descricao valida"))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> ParecerMedico.emitir(resultado, paciente, null, "Descricao valida"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "123456789", " 123456789 "})
    void rejeitaDescricaoCurta(String descricao) {
        assertThatThrownBy(() -> ParecerMedico.emitir(resultado, paciente, medico, descricao))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void reconstituicaoExigeChavePrimariaEData() {
        assertThatThrownBy(() -> ParecerMedico.reconstituir(null, resultado, paciente, medico, "Descricao valida", Instant.now()))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> ParecerMedico.reconstituir(UUID.randomUUID(), resultado, paciente, medico, "Descricao valida", null))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void respeitaLimitesDeDescricao() {
        assertThat(ParecerMedico.emitir(resultado, paciente, medico, "a".repeat(10)).getDescricao()).hasSize(10);
        assertThat(ParecerMedico.emitir(resultado, paciente, medico, "a".repeat(5000)).getDescricao()).hasSize(5000);
        assertThatThrownBy(() -> ParecerMedico.emitir(resultado, paciente, medico, "a".repeat(5001)))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}
