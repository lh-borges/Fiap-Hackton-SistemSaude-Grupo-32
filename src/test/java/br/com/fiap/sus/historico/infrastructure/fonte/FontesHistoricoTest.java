package br.com.fiap.sus.historico.infrastructure.fonte;

import br.com.fiap.sus.consultas.api.ConsultaQuery;
import br.com.fiap.sus.consultas.api.ConsultaResumo;
import br.com.fiap.sus.documentos.api.DocumentoQuery;
import br.com.fiap.sus.documentos.api.DocumentoResumo;
import br.com.fiap.sus.exames.api.ExameQuery;
import br.com.fiap.sus.exames.api.ExameResumo;
import br.com.fiap.sus.exames.api.SolicitacaoExameResumo;
import br.com.fiap.sus.historico.domain.enums.TipoRegistro;
import br.com.fiap.sus.historico.domain.model.Referencia;
import br.com.fiap.sus.historico.domain.model.RegistroHistorico;
import br.com.fiap.sus.pareceres.api.ParecerQuery;
import br.com.fiap.sus.pareceres.api.ParecerResumo;
import br.com.fiap.sus.receitas.api.ReceitaQuery;
import br.com.fiap.sus.receitas.api.ReceitaResumo;
import br.com.fiap.sus.resultados.api.ResultadoQuery;
import br.com.fiap.sus.resultados.api.ResultadoResumo;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Fontes do historico: conversao dos resumos dos modulos em registros (RN-05, RN-06, RF-07)")
class FontesHistoricoTest {
    final UUID paciente = UUID.randomUUID();
    final UUID medico = UUID.randomUUID();
    final Instant t1 = Instant.parse("2026-09-01T10:00:00Z");
    final Instant t2 = Instant.parse("2026-09-05T10:00:00Z");

    @Test
    @DisplayName("consulta: data e o horario; titulo com situacao e marca de remarcada; sem origem")
    void consultas() {
        var query = mock(ConsultaQuery.class);
        var id = UUID.randomUUID();
        when(query.consultasDoPaciente(paciente)).thenReturn(List.of(
                new ConsultaResumo(id, paciente, medico, UUID.randomUUID(), t1, "AGENDADA", true)));

        var registros = new ConsultasFonte(query).registrosDoPaciente(paciente);

        assertThat(registros).singleElement().satisfies(r -> {
            assertThat(r.tipo()).isEqualTo(TipoRegistro.CONSULTA);
            assertThat(r.id()).isEqualTo(id);
            assertThat(r.data()).isEqualTo(t1);
            assertThat(r.titulo()).isEqualTo("Consulta agendada (remarcada)");
            assertThat(r.situacao()).isEqualTo("AGENDADA");
            assertThat(r.medicoId()).isEqualTo(medico);
            assertThat(r.origem()).isNull();
        });
    }

    @Test
    @DisplayName("exames: solicitacao usa criacao e aponta a consulta; exame usa realizacao ou agendamento e aponta a solicitacao")
    void exames() {
        var query = mock(ExameQuery.class);
        var consultaId = UUID.randomUUID();
        var solicitacaoId = UUID.randomUUID();
        when(query.solicitacoesDoPaciente(paciente)).thenReturn(List.of(
                new SolicitacaoExameResumo(solicitacaoId, paciente, medico, UUID.randomUUID(), consultaId, "AGENDADA", t1),
                new SolicitacaoExameResumo(UUID.randomUUID(), paciente, medico, UUID.randomUUID(), null, "PENDENTE", t1)));
        when(query.examesDoPaciente(paciente)).thenReturn(List.of(
                new ExameResumo(UUID.randomUUID(), solicitacaoId, paciente, medico, UUID.randomUUID(), t1, t2, "REALIZADO"),
                new ExameResumo(UUID.randomUUID(), solicitacaoId, paciente, medico, UUID.randomUUID(), t2, null, "AGENDADO")));

        var registros = new ExamesFonte(query).registrosDoPaciente(paciente);

        assertThat(registros).hasSize(4);
        assertThat(registros.get(0).titulo()).isEqualTo("Solicitacao de exame agendada");
        assertThat(registros.get(0).origem()).isEqualTo(new Referencia(TipoRegistro.CONSULTA, consultaId));
        assertThat(registros.get(1).origem()).isNull();
        assertThat(registros.get(2).tipo()).isEqualTo(TipoRegistro.EXAME);
        assertThat(registros.get(2).data()).isEqualTo(t2);
        assertThat(registros.get(2).titulo()).isEqualTo("Exame realizado");
        assertThat(registros.get(2).origem()).isEqualTo(new Referencia(TipoRegistro.SOLICITACAO_EXAME, solicitacaoId));
        assertThat(registros.get(3).data()).isEqualTo(t2);
        assertThat(registros.get(3).medicoId()).isEqualTo(medico);
    }

    @Test
    @DisplayName("resultado: laudo e descricao nao aparecem; aponta o exame")
    void resultados() {
        var query = mock(ResultadoQuery.class);
        var exameId = UUID.randomUUID();
        when(query.resultadosDoPaciente(paciente)).thenReturn(List.of(new ResultadoResumo(UUID.randomUUID(), exameId,
                paciente, "LABORATORIAL", t2, "obs secreta", "http://arq", "descricao secreta", "laudo secreto")));

        var r = new ResultadosFonte(query).registrosDoPaciente(paciente).get(0);

        assertThat(r.tipo()).isEqualTo(TipoRegistro.RESULTADO_EXAME);
        assertThat(r.titulo()).isEqualTo("Resultado de exame: laboratorial");
        assertThat(r.situacao()).isEqualTo("DISPONIVEL");
        assertThat(r.data()).isEqualTo(t2);
        assertThat(r.origem()).isEqualTo(new Referencia(TipoRegistro.EXAME, exameId));
        assertThat(r.toString()).doesNotContain("secret").doesNotContain("http://arq");
    }

    @Test
    @DisplayName("parecer: aponta o resultado analisado; situacao fixa")
    void pareceres() {
        var query = mock(ParecerQuery.class);
        var resultadoId = UUID.randomUUID();
        when(query.pareceresDoPaciente(paciente)).thenReturn(List.of(
                new ParecerResumo(UUID.randomUUID(), resultadoId, paciente, medico, t1)));

        var r = new PareceresFonte(query).registrosDoPaciente(paciente).get(0);

        assertThat(r.tipo()).isEqualTo(TipoRegistro.PARECER);
        assertThat(r.titulo()).isEqualTo("Parecer medico emitido");
        assertThat(r.situacao()).isEqualTo("EMITIDO");
        assertThat(r.medicoId()).isEqualTo(medico);
        assertThat(r.origem()).isEqualTo(new Referencia(TipoRegistro.RESULTADO_EXAME, resultadoId));
    }

    @Test
    @DisplayName("receita: renovacao aponta a receita de origem; emissao nao tem origem")
    void receitas() {
        var query = mock(ReceitaQuery.class);
        var origemId = UUID.randomUUID();
        when(query.receitasDoPaciente(paciente)).thenReturn(List.of(
                new ReceitaResumo(UUID.randomUUID(), paciente, medico, null, t2, t2.plusSeconds(86400), "ATIVA", origemId),
                new ReceitaResumo(origemId, paciente, medico, null, t1, t2, "RENOVADA", null)));

        var registros = new ReceitasFonte(query).registrosDoPaciente(paciente);

        assertThat(registros.get(0).titulo()).isEqualTo("Receita renovada, ativa");
        assertThat(registros.get(0).origem()).isEqualTo(new Referencia(TipoRegistro.RECEITA, origemId));
        assertThat(registros.get(1).titulo()).isEqualTo("Receita renovada");
        assertThat(registros.get(1).situacao()).isEqualTo("RENOVADA");
        assertThat(registros.get(1).origem()).isNull();
    }

    @Test
    @DisplayName("documento: titulo pelo tipo; aponta a consulta, senao o exame; cancelado continua no historico (RN-04)")
    void documentos() {
        var query = mock(DocumentoQuery.class);
        var consultaId = UUID.randomUUID();
        var exameId = UUID.randomUUID();
        when(query.documentosDoPaciente(paciente)).thenReturn(List.of(
                new DocumentoResumo(UUID.randomUUID(), "ATESTADO", paciente, medico, consultaId, null, t1, "EMITIDO"),
                new DocumentoResumo(UUID.randomUUID(), "LAUDO", paciente, medico, null, exameId, t2, "CANCELADO"),
                new DocumentoResumo(UUID.randomUUID(), "DECLARACAO", paciente, medico, null, null, t2, "EMITIDO")));

        var registros = new DocumentosFonte(query).registrosDoPaciente(paciente);

        assertThat(registros).extracting(RegistroHistorico::titulo)
                .containsExactly("Documento: atestado", "Documento: laudo", "Documento: declaracao");
        assertThat(registros.get(0).origem()).isEqualTo(new Referencia(TipoRegistro.CONSULTA, consultaId));
        assertThat(registros.get(1).origem()).isEqualTo(new Referencia(TipoRegistro.EXAME, exameId));
        assertThat(registros.get(1).situacao()).isEqualTo("CANCELADO");
        assertThat(registros.get(2).origem()).isNull();
    }
}
