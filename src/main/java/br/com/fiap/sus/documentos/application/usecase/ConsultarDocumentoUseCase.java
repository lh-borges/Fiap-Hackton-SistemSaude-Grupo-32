package br.com.fiap.sus.documentos.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.documentos.application.service.DocumentoLeituraService;
import br.com.fiap.sus.documentos.application.dto.DocumentoMedicoOutput;
import br.com.fiap.sus.documentos.domain.model.DocumentoMedico;
import br.com.fiap.sus.documentos.domain.repository.DocumentoMedicoRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ConsultarDocumentoUseCase {

    private final DocumentoMedicoRepository documentoMedicoRepository;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;
    private final DocumentoLeituraService leitura;

    public ConsultarDocumentoUseCase(
            DocumentoMedicoRepository documentoMedicoRepository,
            CadastroQuery cadastroQuery,
            UsuarioAutenticadoProvider usuarioAutenticadoProvider,
            DocumentoLeituraService leitura
    ) {
        this.documentoMedicoRepository = documentoMedicoRepository;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
        this.leitura = leitura;
    }

    @PreAuthorize("hasAnyRole('MEDICO', 'PACIENTE', 'ADMINISTRADOR')")
    public DocumentoMedicoOutput executar(UUID documentoId) {
        DocumentoMedico documento = documentoMedicoRepository.buscarPorId(documentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Documento não encontrado."));

        leitura.verificarAcesso(documento, usuarioAutenticadoProvider.obrigatorio());

        return DocumentoMedicoOutput.deDetalhe(documento);
    }
}

