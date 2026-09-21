package br.com.fiap.sus.documentos.application.service;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.cadastros.api.MedicoResumo;
import br.com.fiap.sus.cadastros.api.PacienteResumo;
import br.com.fiap.sus.consultas.api.ConsultaQuery;
import br.com.fiap.sus.documentos.application.dto.DocumentoMedicoOutput;
import br.com.fiap.sus.documentos.domain.enums.TipoDocumento;
import br.com.fiap.sus.exames.api.ExameQuery;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class DocumentoPdfService {
    private static final DateTimeFormatter DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("America/Sao_Paulo"));

    private final CadastroQuery cadastros;
    private final ConsultaQuery consultas;
    private final ExameQuery exames;

    public DocumentoPdfService(CadastroQuery cadastros, ConsultaQuery consultas, ExameQuery exames) {
        this.cadastros = cadastros;
        this.consultas = consultas;
        this.exames = exames;
    }

    public byte[] gerar(DocumentoMedicoOutput documento) {
        try (InputStream template = new ClassPathResource("reports/documento-medico.jrxml").getInputStream()) {
            JasperReport report = JasperCompileManager.compileReport(template);
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("titulo", documento.tipo().tituloPdf());
            parametros.put("tipo", documento.tipo().name());
            parametros.put("secaoEspecifica", documento.tipo().secaoPdf());
            parametros.put("rotuloConteudo", documento.tipo().rotuloConteudoPdf());
            parametros.put("situacao", documento.situacao().name());
            parametros.put("dataEmissao", DATA.format(documento.dataEmissao()));
            parametros.put("pacienteNome", nomePaciente(documento));
            parametros.put("pacienteCpf", cpfPaciente(documento));
            parametros.put("medicoNome", nomeMedico(documento));
            parametros.put("medicoEspecialidade", especialidadeMedico(documento));
            parametros.put("medicoCrm", crmMedico(documento));
            parametros.put("consultaId", documento.consultaId() == null ? "-" : documento.consultaId().toString());
            parametros.put("dataConsulta", dataConsulta(documento));
            parametros.put("exameId", documento.exameId() == null ? "-" : documento.exameId().toString());
            parametros.put("tipoExameNome", nomeTipoExame(documento));
            parametros.put("dataRealizacaoExame", dataRealizacaoExame(documento));
            parametros.put("exibirDadosExame", documento.tipo() == TipoDocumento.LAUDO && documento.exameId() != null);
            parametros.put("conteudo", documento.conteudo());
            parametros.put("motivoCancelamento",
                    documento.motivoCancelamento() == null ? "" : documento.motivoCancelamento());
            var print = JasperFillManager.fillReport(report, parametros, new JREmptyDataSource(1));
            return JasperExportManager.exportReportToPdf(print);
        } catch (Exception ex) {
            throw new RegraDeNegocioException("Nao foi possivel gerar o PDF do documento.");
        }
    }

    private String nomePaciente(DocumentoMedicoOutput documento) {
        return cadastros.resumoDoPaciente(documento.pacienteId())
                .map(PacienteResumo::nome)
                .filter(nome -> !nome.isBlank())
                .orElse(documento.pacienteId().toString());
    }

    private String cpfPaciente(DocumentoMedicoOutput documento) {
        return cadastros.resumoDoPaciente(documento.pacienteId())
                .map(PacienteResumo::cpf)
                .filter(cpf -> !cpf.isBlank())
                .orElse("-");
    }

    private String nomeMedico(DocumentoMedicoOutput documento) {
        return cadastros.resumoDoMedico(documento.medicoId())
                .map(MedicoResumo::nome)
                .filter(nome -> !nome.isBlank())
                .orElse(documento.medicoId().toString());
    }

    private String crmMedico(DocumentoMedicoOutput documento) {
        return cadastros.resumoDoMedico(documento.medicoId())
                .map(medico -> medico.crm() + "/" + medico.ufCrm())
                .filter(crm -> !crm.isBlank())
                .orElse("-");
    }

    private String especialidadeMedico(DocumentoMedicoOutput documento) {
        return cadastros.resumoDoMedico(documento.medicoId())
                .map(MedicoResumo::especialidade)
                .filter(especialidade -> !especialidade.isBlank())
                .orElse("-");
    }

    private String dataConsulta(DocumentoMedicoOutput documento) {
        if (documento.consultaId() == null) {
            return "-";
        }
        return consultas.dataHoraDaConsulta(documento.consultaId())
                .map(DATA::format)
                .orElse("-");
    }

    private String dataRealizacaoExame(DocumentoMedicoOutput documento) {
        if (documento.exameId() == null) {
            return "-";
        }
        return exames.dataRealizacaoDoExame(documento.exameId())
                .map(DATA::format)
                .orElse("-");
    }

    private String nomeTipoExame(DocumentoMedicoOutput documento) {
        if (documento.exameId() == null) {
            return "-";
        }
        return exames.tipoExameIdDoExame(documento.exameId())
                .flatMap(cadastros::nomeDoTipoExame)
                .filter(nome -> !nome.isBlank())
                .orElse("-");
    }
}
