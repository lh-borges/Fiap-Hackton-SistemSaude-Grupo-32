package br.com.fiap.sus.iam.application.usecase;

import br.com.fiap.sus.iam.application.dto.UsuarioOutput;
import br.com.fiap.sus.iam.domain.repository.UsuarioRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-05: administrador ve qualquer usuario; os demais so veem a si mesmos.
 * Consulta a registro de terceiro responde 404, nao 403 (Artigo IV.7).
 */
@Service
public class BuscarUsuarioUseCase {

    private final UsuarioRepository usuarios;
    private final UsuarioAutenticadoProvider autenticado;

    public BuscarUsuarioUseCase(UsuarioRepository usuarios, UsuarioAutenticadoProvider autenticado) {
        this.usuarios = usuarios;
        this.autenticado = autenticado;
    }

    @Transactional(readOnly = true)
    public UsuarioOutput executar(UUID id) {
        UsuarioAutenticado solicitante = autenticado.obrigatorio();
        if (!solicitante.ehAdministrador() && !solicitante.ehOMesmo(id)) {
            throw RecursoNaoEncontradoException.de("Usuario");
        }
        return usuarios.porId(id)
                .map(UsuarioOutput::de)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Usuario"));
    }
}
