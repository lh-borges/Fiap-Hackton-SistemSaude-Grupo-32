package br.com.fiap.sus.documentos.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.cadastros.api.MedicoResumo;
import br.com.fiap.sus.cadastros.api.PacienteResumo;
import br.com.fiap.sus.consultas.api.ConsultaQuery;
import br.com.fiap.sus.documentos.application.dto.DocumentoMedicoOutput;
import br.com.fiap.sus.documentos.domain.enums.SituacaoDocumento;
import br.com.fiap.sus.documentos.domain.enums.TipoDocumento;
import br.com.fiap.sus.exames.api.ExameQuery;
import br.com.fiap.sus.resultados.api.ResultadoQuery;
import br.com.fiap.sus.resultados.api.ResultadoResumo;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DocumentoPdfServiceTest {

    private final CadastroQuery cadastros = mock(CadastroQuery.class);
    private final ConsultaQuery consultas = mock(ConsultaQuery.class);
    private final ExameQuery exames = mock(ExameQuery.class);
    private final ResultadoQuery resultados = mock(ResultadoQuery.class);
    private final DocumentoPdfService service = new DocumentoPdfService(cadastros, consultas, exames, resultados);

    @Test
    void geraPdfComNomeDoPacienteEDoMedico() throws Exception {
        UUID pacienteId = UUID.fromString("c5c12660-77bd-470f-bab8-000000000001");
        UUID medicoId = UUID.fromString("2ca73356-c586-4016-83c0-000000000002");
        UUID pacienteUsuarioId = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
        UUID medicoUsuarioId = UUID.fromString("00000000-0000-0000-0000-0000000000a2");
        UUID especialidadeId = UUID.fromString("00000000-0000-0000-0000-0000000000e1");
        UUID consultaId = UUID.fromString("00000000-0000-0000-0000-0000000000f1");

        when(cadastros.resumoDoPaciente(pacienteId))
                .thenReturn(Optional.of(new PacienteResumo(pacienteId, pacienteUsuarioId,
                        "Maria Souza", "111.444.777-35", "maria.souza@sus.gov.br", true)));
        when(cadastros.resumoDoMedico(medicoId))
                .thenReturn(Optional.of(new MedicoResumo(medicoId, medicoUsuarioId,
                        "Carlos Lima", "carlos.lima@sus.gov.br", "123456", "SP",
                        especialidadeId, "Clinica Geral", true)));
        when(consultas.dataHoraDaConsulta(consultaId))
                .thenReturn(Optional.of(Instant.parse("2026-09-20T13:30:00Z")));

        var documento = new DocumentoMedicoOutput(
                UUID.fromString("00000000-0000-0000-0000-0000000000d1"),
                TipoDocumento.ATESTADO,
                "Atesto, para os devidos fins, que o paciente necessita de acompanhamento clinico.",
                pacienteId,
                medicoId,
                consultaId,
                null,
                Instant.parse("2026-09-20T23:55:00Z"),
                null,
                SituacaoDocumento.EMITIDO,
                null
        );

        byte[] pdf = service.gerar(documento);

        String texto = textoDoPdf(pdf);
        assertThat(texto).contains("Atestado Medico", "Informa", "Atestado:",
                "Paciente: Maria Souza", "CPF: 111.444.777-35",
                "Medico: Carlos Lima", "CRM: 123456/SP", "Especialidade: Clinica Geral",
                "Data da consulta: 20/09/2026 10:30", "Situacao: EMITIDO");
        assertThat(texto).doesNotContain(pacienteId.toString(), medicoId.toString());
    }

    @ParameterizedTest
    @EnumSource(TipoDocumento.class)
    void geraPdfComCamposEspecificosDoTipo(TipoDocumento tipo) throws Exception {
        UUID pacienteId = UUID.randomUUID();
        UUID medicoId = UUID.randomUUID();

        when(cadastros.resumoDoPaciente(pacienteId))
                .thenReturn(Optional.of(new PacienteResumo(pacienteId, UUID.randomUUID(),
                        "Maria Souza", "111.444.777-35", "maria.souza@sus.gov.br", true)));
        when(cadastros.resumoDoMedico(medicoId))
                .thenReturn(Optional.of(new MedicoResumo(medicoId, UUID.randomUUID(),
                        "Carlos Lima", "carlos.lima@sus.gov.br", "123456", "SP",
                        UUID.randomUUID(), "Clinica Geral", true)));

        var documento = new DocumentoMedicoOutput(
                UUID.randomUUID(),
                tipo,
                "Conteudo especifico para emissao do documento medico.",
                pacienteId,
                medicoId,
                null,
                null,
                Instant.parse("2026-09-20T23:55:00Z"),
                null,
                SituacaoDocumento.EMITIDO,
                null
        );

        String texto = textoDoPdf(service.gerar(documento));

        assertThat(texto).contains(tipo.tituloPdf(), tipo.secaoPdf(),
                tipo.rotuloConteudoPdf() + ":", "Conteudo especifico para emissao do documento medico.");
    }

    @Test
    void geraPdfDeLaudoComDadosDoExame() throws Exception {
        UUID pacienteId = UUID.randomUUID();
        UUID medicoId = UUID.randomUUID();
        UUID exameId = UUID.randomUUID();
        UUID tipoExameId = UUID.randomUUID();

        when(cadastros.resumoDoPaciente(pacienteId))
                .thenReturn(Optional.of(new PacienteResumo(pacienteId, UUID.randomUUID(),
                        "Maria Souza", "111.444.777-35", "maria.souza@sus.gov.br", true)));
        when(cadastros.resumoDoMedico(medicoId))
                .thenReturn(Optional.of(new MedicoResumo(medicoId, UUID.randomUUID(),
                        "Carlos Lima", "carlos.lima@sus.gov.br", "123456", "SP",
                        UUID.randomUUID(), "Radiologia", true)));
        when(exames.dataRealizacaoDoExame(exameId))
                .thenReturn(Optional.of(Instant.parse("2026-09-20T12:00:00Z")));
        when(exames.tipoExameIdDoExame(exameId)).thenReturn(Optional.of(tipoExameId));
        when(cadastros.nomeDoTipoExame(tipoExameId)).thenReturn(Optional.of("Raio-X de torax"));
        when(resultados.resultadoDoExame(exameId)).thenReturn(Optional.of(new ResultadoResumo(
                UUID.randomUUID(),
                exameId,
                pacienteId,
                "IMAGEM",
                Instant.parse("2026-09-21T03:00:14Z"),
                "Resultado registrado para teste do fluxo de parecer.",
                "https://exemplo.local/raio-x-torax.pdf",
                "Raio-X de torax",
                "Sem alteracoes agudas evidentes."
        )));

        var documento = new DocumentoMedicoOutput(
                UUID.randomUUID(),
                TipoDocumento.LAUDO,
                "Laudo medico emitido com base no exame realizado.",
                pacienteId,
                medicoId,
                null,
                exameId,
                Instant.parse("2026-09-20T23:55:00Z"),
                null,
                SituacaoDocumento.EMITIDO,
                null
        );

        String texto = textoDoPdf(service.gerar(documento));

        assertThat(texto).contains("Laudo M", "Informa", "Laudo e conclus",
                "Exame: " + exameId, "Tipo de exame: Raio-X de torax",
                "Data de realizacao do exame: 20/09/2026 09:00",
                "Tipo do resultado: IMAGEM",
                "Data do resultado: 21/09/2026 00:00",
                "Descricao: Raio-X de torax",
                "Laudo do resultado: Sem alteracoes agudas evidentes.",
                "Observacao: Resultado registrado para teste do fluxo de parecer.",
                "Arquivo do resultado: https://exemplo.local/raio-x-torax.pdf");
    }

    private String textoDoPdf(byte[] pdf) throws Exception {
        PdfReader reader = new PdfReader(pdf);
        try {
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            StringBuilder texto = new StringBuilder();
            for (int pagina = 1; pagina <= reader.getNumberOfPages(); pagina++) {
                texto.append(extractor.getTextFromPage(pagina));
            }
            return texto.toString();
        } finally {
            reader.close();
        }
    }
}
