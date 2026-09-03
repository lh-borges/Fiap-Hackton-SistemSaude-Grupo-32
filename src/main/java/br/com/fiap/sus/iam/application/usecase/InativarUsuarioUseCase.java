package br.com.fiap.sus.iam.application.usecase;

import br.com.fiap.sus.iam.domain.model.Usuario;
import br.com.fiap.sus.iam.domain.repository.UsuarioRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RN-05: inativacao logica. O administrador nao pode inativar a si mesmo. */
@Service
public class InativarUsuarioUseCase {

    private final UsuarioRepository usuarios;
    private final UsuarioAutenticadoProvider autenticado;

    public InativarUsuarioUseCase(UsuarioRepository usuarios, UsuarioAutenticadoProvider autenticado) {
        this.usuarios = usuarios;
        this.autenticado = autenticado;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void executar(UUID id) {
        if (autenticado.obrigatorio().ehOMesmo(id)) {
            throw new RegraDeNegocioException("Um administrador nao pode inativar o proprio usuario.");
        }
        Usuario usuario = usuarios.porId(id).orElseThrow(() -> RecursoNaoEncontradoException.de("Usuario"));
        usuario.inativar();
        usuarios.salvar(usuario);
    }
}
