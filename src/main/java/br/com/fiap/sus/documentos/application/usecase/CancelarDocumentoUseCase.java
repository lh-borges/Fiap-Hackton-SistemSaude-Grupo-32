package br.com.fiap.sus.documentos.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.documentos.application.dto.CancelarDocumentoDTO;
import br.com.fiap.sus.documentos.application.dto.DocumentoMedicoOutput;
import br.com.fiap.sus.documentos.domain.model.DocumentoMedico;
import br.com.fiap.sus.documentos.domain.repository.DocumentoMedicoRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CancelarDocumentoUseCase {

    private final DocumentoMedicoRepository documentoMedicoRepository;
    private final CadastroQuery cadastroQuery;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    public CancelarDocumentoUseCase(
            DocumentoMedicoRepository documentoMedicoRepository,
            CadastroQuery cadastroQuery,
            UsuarioAutenticadoProvider usuarioAutenticadoProvider
    ) {
        this.documentoMedicoRepository = documentoMedicoRepository;
        this.cadastroQuery = cadastroQuery;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
    }

    @PreAuthorize("hasRole('MEDICO')")
    public DocumentoMedicoOutput executar(CancelarDocumentoDTO dto) {
        DocumentoMedico documento = documentoMedicoRepository.buscarPorId(dto.documentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Documento nao encontrado."));

        UsuarioAutenticado usuario = usuarioAutenticadoProvider.obrigatorio();
        UUID medicoId = cadastroQuery.medicoIdDoUsuario(usuario.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario não é um médico cadastrado."));


        if (!documento.getMedicoId().equals(medicoId)) {
            throw new RegraDeNegocioException("Somente o médico autor pode cancelar o documento.");
        }

        DocumentoMedico cancelado = documento.cancelar(dto.motivo());
        return DocumentoMedicoOutput.deDetalhe(documentoMedicoRepository.salvar(cancelado));
    }
}
