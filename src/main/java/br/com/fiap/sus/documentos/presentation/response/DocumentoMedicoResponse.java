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
import java.util.Objects;
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
        return de(output, null, null);
    }

    public static DocumentoMedicoResponse de(DocumentoMedicoOutput output, PacienteResumo paciente,
                                             MedicoResumo medico) {
        return de(output, paciente, medico, null, null, null);
    }

    public static DocumentoMedicoResponse de(DocumentoMedicoOutput output, PacienteResumo paciente,
                                             MedicoResumo medico, Instant dataConsulta, Instant dataRealizacaoExame,
                                             String tipoExameNome) {
        Objects.requireNonNull(output, "output nao pode ser nulo");

        var datas = DatasDocumento.de(output, dataConsulta, dataRealizacaoExame);
        var pacienteResponse = PacienteResponse.de(paciente);
        var medicoResponse = MedicoResponse.de(medico);
        var exameResponse = ExameResponse.de(output, datas.dataRealizacaoExame(), tipoExameNome);

        return new DocumentoMedicoResponse(
                output.id(),
                datas.dataConsulta(),
                exameResponse.tipoExameNome(),
                exameResponse.dataRealizacaoExame(),
                pacienteResponse.nome(),
                pacienteResponse.cpf(),
                medicoResponse.nome(),
                medicoResponse.especialidade(),
                medicoResponse.crm(),
                output.tipo(),
                output.conteudo(),
                output.arquivoUrl(),
                datas.dataEmissao(),
                output.situacao(),
                output.motivoCancelamento(),
                dadosEspecificos(output, datas, exameResponse)
        );
    }

    private static DocumentoDadosEspecificosResponse dadosEspecificos(DocumentoMedicoOutput output,
                                                                      DatasDocumento datas,
                                                                      ExameResponse exame) {
        if (ehLaudoComExame(output)) {
            return DocumentoDadosEspecificosResponse.laudoExame(
                    datas.dataConsulta(), exame.tipoExameNome(), exame.dataRealizacaoExame());
        }
        return DocumentoDadosEspecificosResponse.atendimento(
                output.tipo().name(), rotuloConteudo(output.tipo()), datas.dataConsulta());
    }

    private static boolean ehLaudoComExame(DocumentoMedicoOutput output) {
        return output.tipo() == TipoDocumento.LAUDO && output.exameId() != null;
    }

    private static String rotuloConteudo(TipoDocumento tipo) {
        return switch (tipo) {
            case ATESTADO -> "Texto do atestado";
            case LAUDO -> "Laudo e conclusao clinica";
            case RELATORIO -> "Relatorio clinico";
            case ENCAMINHAMENTO -> "Justificativa e destino do encaminhamento";
            case DECLARACAO -> "Declaracao";
        };
    }

    private static String formatar(Instant instant) {
        return instant == null ? null : FORMATTER.format(instant);
    }

    private record DatasDocumento(String dataConsulta, String dataRealizacaoExame, String dataEmissao) {
        static DatasDocumento de(DocumentoMedicoOutput output, Instant dataConsulta, Instant dataRealizacaoExame) {
            return new DatasDocumento(formatar(dataConsulta), formatar(dataRealizacaoExame),
                    formatar(output.dataEmissao()));
        }
    }

    private record PacienteResponse(String nome, String cpf) {
        static PacienteResponse de(PacienteResumo paciente) {
            return paciente == null
                    ? new PacienteResponse(null, null)
                    : new PacienteResponse(paciente.nome(), paciente.cpf());
        }
    }

    private record MedicoResponse(String nome, String especialidade, String crm) {
        static MedicoResponse de(MedicoResumo medico) {
            return medico == null
                    ? new MedicoResponse(null, null, null)
                    : new MedicoResponse(medico.nome(), medico.especialidade(), medico.crm() + "/" + medico.ufCrm());
        }
    }

    private record ExameResponse(String tipoExameNome, String dataRealizacaoExame) {
        static ExameResponse de(DocumentoMedicoOutput output, String dataRealizacaoExame, String tipoExameNome) {
            return ehLaudoComExame(output)
                    ? new ExameResponse(tipoExameNome, dataRealizacaoExame)
                    : new ExameResponse(null, null);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public sealed interface DocumentoDadosEspecificosResponse permits DocumentoDadosAtendimentoResponse,
            DocumentoDadosLaudoExameResponse {

        String tipo();

        String rotuloConteudo();

        static DocumentoDadosEspecificosResponse atendimento(String tipo, String rotuloConteudo,
                                                             String dataConsulta) {
            return new DocumentoDadosAtendimentoResponse(tipo, rotuloConteudo, dataConsulta);
        }

        static DocumentoDadosEspecificosResponse laudoExame(String dataConsulta, String tipoExameNome,
                                                            String dataRealizacaoExame) {
            return new DocumentoDadosLaudoExameResponse("LAUDO_EXAME", "Laudo e conclusao clinica",
                    dataConsulta, tipoExameNome, dataRealizacaoExame);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DocumentoDadosAtendimentoResponse(
            String tipo,
            String rotuloConteudo,
            String dataConsulta
    ) implements DocumentoDadosEspecificosResponse {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DocumentoDadosLaudoExameResponse(
            String tipo,
            String rotuloConteudo,
            String dataConsulta,
            String tipoExameNome,
            String dataRealizacaoExame
    ) implements DocumentoDadosEspecificosResponse {
    }
}
