package br.com.fiap.sus.documentos.application.service;

import br.com.fiap.sus.documentos.domain.enums.SituacaoDocumento;
import br.com.fiap.sus.documentos.domain.enums.TipoDocumento;
import br.com.fiap.sus.documentos.domain.model.DocumentoMedico;
import br.com.fiap.sus.documentos.domain.repository.DocumentoMedicoRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DocumentoQueryServiceTest {
    private final DocumentoMedicoRepository repository = mock(DocumentoMedicoRepository.class);
    private final DocumentoQueryService service = new DocumentoQueryService(repository);

    @Test
    @DisplayName("feature 010: resumo por paciente sem conteudo nem arquivo")
    void documentosDoPaciente() {
        UUID pacienteId = UUID.randomUUID();
        UUID medicoId = UUID.randomUUID();
        UUID consultaId = UUID.randomUUID();
        var emissao = Instant.parse("2026-09-10T12:00:00Z");
        var documento = DocumentoMedico.reconstituir(UUID.randomUUID(), TipoDocumento.ATESTADO,
                "Conteudo confidencial", pacienteId, medicoId, consultaId, null, emissao,
                "http://arquivo/confidencial.pdf", SituacaoDocumento.EMITIDO, null);
        when(repository.listarPorPaciente(pacienteId)).thenReturn(List.of(documento));

        var resumos = service.documentosDoPaciente(pacienteId);

        assertThat(resumos).singleElement().satisfies(r -> {
            assertThat(r.id()).isEqualTo(documento.getId());
            assertThat(r.tipo()).isEqualTo("ATESTADO");
            assertThat(r.pacienteId()).isEqualTo(pacienteId);
            assertThat(r.medicoId()).isEqualTo(medicoId);
            assertThat(r.consultaId()).isEqualTo(consultaId);
            assertThat(r.exameId()).isNull();
            assertThat(r.dataEmissao()).isEqualTo(emissao);
            assertThat(r.situacao()).isEqualTo("EMITIDO");
            assertThat(r.toString()).doesNotContain("confidencial");
        });
    }

    @Test
    void existeDocumentoParaConsulta() {
        UUID consultaId = UUID.randomUUID();
        when(repository.existePorConsultaId(consultaId)).thenReturn(true);

        assertThat(service.existeDocumentoParaConsulta(consultaId)).isTrue();
    }
}
