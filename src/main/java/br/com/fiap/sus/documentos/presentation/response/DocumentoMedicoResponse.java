package br.com.fiap.sus.documentos.presentation.response;

import br.com.fiap.sus.cadastros.api.MedicoResumo;
import br.com.fiap.sus.cadastros.api.PacienteResumo;
import br.com.fiap.sus.documentos.application.dto.DocumentoMedicoOutput;
import br.com.fiap.sus.documentos.domain.enums.SituacaoDocumento;
import br.com.fiap.sus.documentos.domain.enums.TipoDocumento;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocumentoMedicoResponse(
        UUID id,
        String dataConsulta,
        String tipoExameNome,
        String dataRealizacaoExame,
        String pacienteNome,
        String pacienteCpf,
        String medicoNome,
        String medicoEspecialidade,
        String medicoCrm,
        TipoDocumento tipoDocumento,
        String conteudo,
        String arquivoUrl,
        String dataEmissao,
        SituacaoDocumento situacao,
        String motivoCancelamento,
        DocumentoDadosEspecificosResponse dadosEspecificos
) {
    private static final ZoneId ZONA_SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZONA_SAO_PAULO);

    public static DocumentoMedicoResponse de(DocumentoMedicoOutput output) {
        return new DocumentoMedicoResponse(output.id(), null, null, null,
                null, null, null, null, null, output.tipo(), output.conteudo(), output.arquivoUrl(),
                formatar(output.dataEmissao()), output.situacao(), output.motivoCancelamento(), null);
    }

    public static DocumentoMedicoResponse de(DocumentoMedicoOutput output, PacienteResumo paciente,
                                             MedicoResumo medico) {
        return de(output, paciente, medico, null, null, null, null);
    }

    public static DocumentoMedicoResponse de(DocumentoMedicoOutput output, PacienteResumo paciente,
                                             MedicoResumo medico, Instant dataConsulta, Instant dataRealizacaoExame,
                                             UUID tipoExameId, String tipoExameNome) {
        String dataConsultaFormatada = formatar(dataConsulta);
        String dataRealizacaoExameFormatada = formatar(dataRealizacaoExame);
        return new DocumentoMedicoResponse(output.id(), dataConsultaFormatada,
                tipoExameNome, dataRealizacaoExameFormatada,
                paciente == null ? null : paciente.nome(),
                paciente == null ? null : paciente.cpf(),
                medico == null ? null : medico.nome(),
                medico == null ? null : medico.especialidade(),
                medico == null ? null : medico.crm() + "/" + medico.ufCrm(),
                output.tipo(), output.conteudo(), output.arquivoUrl(), formatar(output.dataEmissao()), output.situacao(),
                output.motivoCancelamento(), dadosEspecificos(output, dataConsultaFormatada, dataRealizacaoExameFormatada,
                tipoExameId, tipoExameNome));
    }

    private static DocumentoDadosEspecificosResponse dadosEspecificos(DocumentoMedicoOutput output,
                                                                      String dataConsulta,
                                                                      String dataRealizacaoExame,
                                                                      UUID tipoExameId,
                                                                      String tipoExameNome) {
        if (output.tipo() == TipoDocumento.LAUDO && output.exameId() != null) {
            return DocumentoDadosEspecificosResponse.laudoExame(dataRealizacaoExame, dataConsulta, tipoExameNome);
        }
        return switch (output.tipo()) {
            case ATESTADO -> DocumentoDadosEspecificosResponse.atendimento("ATESTADO", "Texto do atestado",
                    dataConsulta);
            case RELATORIO -> DocumentoDadosEspecificosResponse.atendimento("RELATORIO", "Relatorio clinico",
                    dataConsulta);
            case ENCAMINHAMENTO -> DocumentoDadosEspecificosResponse.atendimento("ENCAMINHAMENTO",
                    "Justificativa e destino do encaminhamento", dataConsulta);
            case DECLARACAO -> DocumentoDadosEspecificosResponse.atendimento("DECLARACAO", "Declaracao",
                    dataConsulta);
            case LAUDO -> DocumentoDadosEspecificosResponse.atendimento("LAUDO", "Laudo e conclusao clinica",
                    dataConsulta);
        };
    }

    private static String formatar(Instant instant) {
        return instant == null ? null : FORMATTER.format(instant);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DocumentoDadosEspecificosResponse(
            String tipo,
            String rotuloConteudo,
            String dataConsulta,
            String tipoExameNome,
            String dataRealizacaoExame
    ) {
        static DocumentoDadosEspecificosResponse atendimento(String tipo, String rotuloConteudo,
                                                             String dataConsulta) {
            return new DocumentoDadosEspecificosResponse(tipo, rotuloConteudo, dataConsulta, null, null);
        }

        static DocumentoDadosEspecificosResponse laudoExame(String dataRealizacaoExame, String dataConsulta,
                                                            String tipoExameNome) {
            return new DocumentoDadosEspecificosResponse("LAUDO_EXAME", "Laudo e conclusao clinica",
                    dataConsulta, tipoExameNome, dataRealizacaoExame);
        }
    }
}
