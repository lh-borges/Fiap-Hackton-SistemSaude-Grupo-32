package br.com.fiap.sus.documentos.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.consultas.api.ConsultaQuery;
import br.com.fiap.sus.documentos.application.dto.DocumentoMedicoOutput;
import br.com.fiap.sus.documentos.application.dto.EmitirDocumentoDTO;
import br.com.fiap.sus.documentos.application.event.DocumentoEmitidoEvent;
import br.com.fiap.sus.documentos.domain.enums.TipoDocumento;
import br.com.fiap.sus.documentos.domain.model.DocumentoMedico;
import br.com.fiap.sus.documentos.domain.repository.DocumentoMedicoRepository;
import br.com.fiap.sus.exames.api.ExameQuery;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EmitirDocumentoUseCase {

    private final DocumentoMedicoRepository documentoMedicoRepository;
    private final CadastroQuery cadastroQuery;
    private final ConsultaQuery consultaQuery;
    private final ExameQuery exameQuery;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;
    private final ApplicationEventPublisher eventPublisher;

    public EmitirDocumentoUseCase(
            DocumentoMedicoRepository documentoMedicoRepository,
            CadastroQuery cadastroQuery,
            ConsultaQuery consultaQuery,
            ExameQuery exameQuery,
            UsuarioAutenticadoProvider usuarioAutenticadoProvider,
            ApplicationEventPublisher eventPublisher
    ) {
        this.documentoMedicoRepository = documentoMedicoRepository;
        this.cadastroQuery = cadastroQuery;
        this.consultaQuery = consultaQuery;
        this.exameQuery = exameQuery;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
        this.eventPublisher = eventPublisher;
    }

    @PreAuthorize("hasRole('MEDICO')")
    public DocumentoMedicoOutput executar(EmitirDocumentoDTO dto) {
        UsuarioAutenticado usuario = usuarioAutenticadoProvider.obrigatorio();
        UUID medicoId = cadastroQuery.medicoIdDoUsuario(usuario.id())
                .orElseThrow(() -> new RegraDeNegocioException("Usuário não é um médico cadastrado."));

        if (!cadastroQuery.pacienteAtivoExiste(dto.pacienteId())) {
            throw new RecursoNaoEncontradoException("Paciente não encontrado.");
        }

        TipoDocumento tipo = TipoDocumento.de(dto.tipo());

        if (dto.consultaId() != null) {
            UUID pacienteDaConsulta = consultaQuery.pacienteIdDaConsulta(dto.consultaId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada."));
            if (!pacienteDaConsulta.equals(dto.pacienteId())) {
                throw new RegraDeNegocioException("A consulta informada pertence a outro paciente.");
            }
        }

        if (dto.exameId() != null) {
            UUID pacienteDoExame = exameQuery.pacienteIdDoExame(dto.exameId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Exame não encontrado."));
            if (!pacienteDoExame.equals(dto.pacienteId())) {
                throw new RegraDeNegocioException("O exame informado pertence a outro paciente.");
            }
            if (!exameQuery.exameRealizadoExiste(dto.exameId())) {
                throw new RegraDeNegocioException("O exame informado ainda não foi realizado.");
            }
        }

        DocumentoMedico documento = DocumentoMedico.emitir(tipo, dto.conteudo(), dto.pacienteId(), medicoId,
                dto.consultaId(), dto.exameId());
        DocumentoMedico salvo = documentoMedicoRepository.salvar(documento);

        eventPublisher.publishEvent(new DocumentoEmitidoEvent(salvo.getId(), salvo.getPacienteId(),
                salvo.getMedicoId(), salvo.getTipo(), salvo.getDataEmissao()));

        return DocumentoMedicoOutput.deDetalhe(salvo);
    }
}
