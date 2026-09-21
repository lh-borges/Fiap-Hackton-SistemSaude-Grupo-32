package br.com.fiap.sus.documentos.presentation.response;

import br.com.fiap.sus.cadastros.api.MedicoResumo;
import br.com.fiap.sus.cadastros.api.PacienteResumo;
import br.com.fiap.sus.documentos.application.dto.DocumentoMedicoOutput;
import br.com.fiap.sus.documentos.domain.enums.SituacaoDocumento;
import br.com.fiap.sus.documentos.domain.enums.TipoDocumento;
import br.com.fiap.sus.documentos.presentation.response.DocumentoMedicoResponse.DocumentoDadosAtendimentoResponse;
import br.com.fiap.sus.documentos.presentation.response.DocumentoMedicoResponse.DocumentoDadosLaudoExameResponse;
import br.com.fiap.sus.resultados.api.ResultadoResumo;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentoMedicoResponseTest {

    @Test
    void mapeiaOutputParaResponse() {
        UUID id = UUID.randomUUID();
        UUID consultaId = UUID.randomUUID();
        UUID exameId = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        UUID medicoId = UUID.randomUUID();
        UUID pacienteUsuarioId = UUID.randomUUID();
        UUID medicoUsuarioId = UUID.randomUUID();
        UUID especialidadeId = UUID.randomUUID();
        Instant data = Instant.parse("2026-09-20T09:00:00Z");
        var output = new DocumentoMedicoOutput(id, TipoDocumento.LAUDO, "Conteudo clinico",
                pacienteId, medicoId, consultaId, exameId, data, "https://arquivo.local/documento.pdf",
                SituacaoDocumento.CANCELADO, "emitido em duplicidade");
        var paciente = new PacienteResumo(pacienteId, pacienteUsuarioId, "Maria Souza",
                "111.444.777-35", "maria.souza@sus.gov.br", true);
        var medico = new MedicoResumo(medicoId, medicoUsuarioId, "Carlos Lima",
                "carlos.lima@sus.gov.br", "123456", "SP", especialidadeId, "Clinica Geral", true);

        DocumentoMedicoResponse response = DocumentoMedicoResponse.de(output, paciente, medico,
                Instant.parse("2026-09-20T12:00:00Z"),
                Instant.parse("2026-09-20T13:30:00Z"),
                "Raio-X de torax",
                new ResultadoResumo(UUID.randomUUID(), exameId, pacienteId, "IMAGEM",
                        Instant.parse("2026-09-21T03:00:14Z"),
                        "Resultado registrado para teste do fluxo de parecer.",
                        "https://exemplo.local/raio-x-torax.pdf",
                        "Raio-X de torax",
                        "Sem alteracoes agudas evidentes."));

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.dataConsulta()).isEqualTo("20/09/2026 09:00");
        assertThat(response.tipoExameNome()).isEqualTo("Raio-X de torax");
        assertThat(response.dataRealizacaoExame()).isEqualTo("20/09/2026 10:30");
        assertThat(response.pacienteNome()).isEqualTo("Maria Souza");
        assertThat(response.pacienteCpf()).isEqualTo("111.444.777-35");
        assertThat(response.medicoNome()).isEqualTo("Carlos Lima");
        assertThat(response.medicoEspecialidade()).isEqualTo("Clinica Geral");
        assertThat(response.medicoCrm()).isEqualTo("123456/SP");
        assertThat(response.tipoDocumento()).isEqualTo(TipoDocumento.LAUDO);
        assertThat(response.dadosEspecificos().tipo()).isEqualTo("LAUDO_EXAME");
        assertThat(response.dadosEspecificos()).isInstanceOf(DocumentoDadosLaudoExameResponse.class);
        var dadosLaudo = (DocumentoDadosLaudoExameResponse) response.dadosEspecificos();
        assertThat(dadosLaudo.dataConsulta()).isEqualTo("20/09/2026 09:00");
        assertThat(dadosLaudo.tipoExameNome()).isEqualTo("Raio-X de torax");
        assertThat(dadosLaudo.dataRealizacaoExame()).isEqualTo("20/09/2026 10:30");
        assertThat(dadosLaudo.tipoResultado()).isEqualTo("IMAGEM");
        assertThat(dadosLaudo.dataResultado()).isEqualTo("21/09/2026 00:00");
        assertThat(dadosLaudo.descricaoResultado()).isEqualTo("Raio-X de torax");
        assertThat(dadosLaudo.laudoResultado()).isEqualTo("Sem alteracoes agudas evidentes.");
        assertThat(dadosLaudo.arquivoResultadoUrl()).isEqualTo("https://exemplo.local/raio-x-torax.pdf");
        assertThat(dadosLaudo.observacaoResultado()).isEqualTo("Resultado registrado para teste do fluxo de parecer.");
        assertThat(response.conteudo()).isEqualTo("Conteudo clinico");
        assertThat(response.arquivoUrl()).isEqualTo("https://arquivo.local/documento.pdf");
        assertThat(response.dataEmissao()).isEqualTo("20/09/2026 06:00");
        assertThat(response.situacao()).isEqualTo(SituacaoDocumento.CANCELADO);
        assertThat(response.motivoCancelamento()).isEqualTo("emitido em duplicidade");
    }

    @Test
    void atestadoNaoRetornaDadosDeExame() {
        UUID id = UUID.randomUUID();
        UUID consultaId = UUID.randomUUID();
        UUID exameId = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        UUID medicoId = UUID.randomUUID();
        var output = new DocumentoMedicoOutput(id, TipoDocumento.ATESTADO, "Texto do atestado",
                pacienteId, medicoId, consultaId, exameId, Instant.parse("2026-09-20T09:00:00Z"),
                null, SituacaoDocumento.EMITIDO, null);

        DocumentoMedicoResponse response = DocumentoMedicoResponse.de(output, null, null,
                Instant.parse("2026-09-20T12:00:00Z"),
                Instant.parse("2026-09-20T13:30:00Z"),
                "Raio-X de torax");

        assertThat(response.tipoDocumento()).isEqualTo(TipoDocumento.ATESTADO);
        assertThat(response.tipoExameNome()).isNull();
        assertThat(response.dataRealizacaoExame()).isNull();
        assertThat(response.dadosEspecificos()).isInstanceOf(DocumentoDadosAtendimentoResponse.class);
        var dadosAtestado = (DocumentoDadosAtendimentoResponse) response.dadosEspecificos();
        assertThat(dadosAtestado.tipo()).isEqualTo("ATESTADO");
        assertThat(dadosAtestado.rotuloConteudo()).isEqualTo("Texto do atestado");
        assertThat(dadosAtestado.dataConsulta()).isEqualTo("20/09/2026 09:00");
    }
}
