package br.com.fiap.sus.iam.application.usecase;

import br.com.fiap.sus.iam.application.dto.AlterarSenhaInput;
import br.com.fiap.sus.iam.application.port.SenhaEncoderPort;
import br.com.fiap.sus.iam.domain.model.SenhaEmTexto;
import br.com.fiap.sus.iam.domain.model.Usuario;
import br.com.fiap.sus.iam.domain.repository.UsuarioRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RN-03: troca de senha. O proprio usuario precisa informar a senha atual;
 * o administrador pode redefinir a senha de outro usuario sem ela.
 */
@Service
public class AlterarSenhaUseCase {

    private final UsuarioRepository usuarios;
    private final SenhaEncoderPort senhas;
    private final UsuarioAutenticadoProvider autenticado;

    public AlterarSenhaUseCase(UsuarioRepository usuarios, SenhaEncoderPort senhas,
                               UsuarioAutenticadoProvider autenticado) {
        this.usuarios = usuarios;
        this.senhas = senhas;
        this.autenticado = autenticado;
    }

    @Transactional
    public void executar(UUID id, AlterarSenhaInput input) {
        UsuarioAutenticado solicitante = autenticado.obrigatorio();
        boolean ehOProprio = solicitante.ehOMesmo(id);
        if (!solicitante.ehAdministrador() && !ehOProprio) {
            throw RecursoNaoEncontradoException.de("Usuario");
        }
        Usuario usuario = usuarios.porId(id).orElseThrow(() -> RecursoNaoEncontradoException.de("Usuario"));

        if (ehOProprio && !senhas.confere(input.senhaAtual() == null ? "" : input.senhaAtual(),
                usuario.getSenhaHash())) {
            throw new RegraDeNegocioException("A senha atual informada esta incorreta.");
        }
        SenhaEmTexto nova = new SenhaEmTexto(input.novaSenha());
        usuario.alterarSenha(senhas.codificar(nova.valor()));
        usuarios.salvar(usuario);
    }
}
