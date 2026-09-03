package br.com.fiap.sus.iam.application.usecase;

import br.com.fiap.sus.iam.application.dto.UsuarioOutput;
import br.com.fiap.sus.iam.domain.enums.RoleNome;
import br.com.fiap.sus.iam.domain.model.Usuario;
import br.com.fiap.sus.iam.domain.repository.UsuarioRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU-03 e RN-07: o administrador nao pode remover o proprio perfil ADMINISTRADOR. */
@Service
public class AtribuirRolesUseCase {

    private final UsuarioRepository usuarios;
    private final UsuarioAutenticadoProvider autenticado;

    public AtribuirRolesUseCase(UsuarioRepository usuarios, UsuarioAutenticadoProvider autenticado) {
        this.usuarios = usuarios;
        this.autenticado = autenticado;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UsuarioOutput executar(UUID id, Set<String> roles) {
        Usuario usuario = usuarios.porId(id).orElseThrow(() -> RecursoNaoEncontradoException.de("Usuario"));
        Set<RoleNome> novasRoles = CadastrarUsuarioUseCase.converterRoles(roles);

        boolean alterandoASiMesmo = autenticado.obrigatorio().ehOMesmo(id);
        if (alterandoASiMesmo && !novasRoles.contains(RoleNome.ADMINISTRADOR)) {
            throw new RegraDeNegocioException("Um administrador nao pode remover o proprio perfil ADMINISTRADOR.");
        }
        usuario.substituirRoles(novasRoles);
        return UsuarioOutput.de(usuarios.salvar(usuario));
    }
}
