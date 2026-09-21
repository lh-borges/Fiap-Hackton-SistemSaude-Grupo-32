package br.com.fiap.sus.documentos.presentation.response;

import br.com.fiap.sus.cadastros.api.MedicoResumo;
import br.com.fiap.sus.cadastros.api.PacienteResumo;
import br.com.fiap.sus.documentos.application.dto.DocumentoMedicoOutput;
import br.com.fiap.sus.documentos.domain.enums.SituacaoDocumento;
import br.com.fiap.sus.documentos.domain.enums.TipoDocumento;
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
        UUID tipoExameId = UUID.randomUUID();
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
                tipoExameId,
                "Raio-X de torax");

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
        assertThat(response.dadosEspecificos().dataConsulta()).isEqualTo("20/09/2026 09:00");
        assertThat(response.dadosEspecificos().tipoExameNome()).isEqualTo("Raio-X de torax");
        assertThat(response.dadosEspecificos().dataRealizacaoExame()).isEqualTo("20/09/2026 10:30");
        assertThat(response.conteudo()).isEqualTo("Conteudo clinico");
        assertThat(response.arquivoUrl()).isEqualTo("https://arquivo.local/documento.pdf");
        assertThat(response.dataEmissao()).isEqualTo("20/09/2026 06:00");
        assertThat(response.situacao()).isEqualTo(SituacaoDocumento.CANCELADO);
        assertThat(response.motivoCancelamento()).isEqualTo("emitido em duplicidade");
    }
}
