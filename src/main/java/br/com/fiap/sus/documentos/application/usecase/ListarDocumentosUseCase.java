package br.com.fiap.sus.documentos.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.documentos.application.service.DocumentoLeituraService;
import br.com.fiap.sus.documentos.application.dto.DocumentoMedicoOutput;
import br.com.fiap.sus.documentos.domain.repository.DocumentoMedicoFiltro;
import br.com.fiap.sus.documentos.domain.repository.DocumentoMedicoRepository;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class ListarDocumentosUseCase {

    private final DocumentoMedicoRepository documentoMedicoRepository;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;
    private final DocumentoLeituraService leitura;

    public ListarDocumentosUseCase(
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
    public PaginaResultado<DocumentoMedicoOutput> executar(DocumentoMedicoFiltro filtro, int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 100) {
            throw new IllegalArgumentException("Pagina deve ser positiva ou zero e tamanho entre 1 e 100.");
        }
        if (filtro.periodoInicio() != null && filtro.periodoFim() != null
                && filtro.periodoInicio().isAfter(filtro.periodoFim())) {
            throw new IllegalArgumentException("O inicio do periodo deve ser anterior ou igual ao fim.");
        }

        DocumentoMedicoFiltro filtroEfetivo = leitura.restringir(filtro, usuarioAutenticadoProvider.obrigatorio());
        return documentoMedicoRepository.listar(filtroEfetivo, pagina, tamanho)
                .mapear(DocumentoMedicoOutput::deResumo);
    }
}
