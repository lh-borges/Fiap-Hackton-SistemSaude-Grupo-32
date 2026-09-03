package br.com.fiap.sus.iam.application.usecase;

import br.com.fiap.sus.iam.application.dto.AtualizarUsuarioInput;
import br.com.fiap.sus.iam.application.dto.UsuarioOutput;
import br.com.fiap.sus.iam.domain.model.Usuario;
import br.com.fiap.sus.iam.domain.repository.UsuarioRepository;
import br.com.fiap.sus.shared.domain.exception.ConflitoException;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.domain.vo.Email;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF-02: administrador ou o proprio usuario altera nome e e-mail. CPF nao muda. */
@Service
public class AtualizarUsuarioUseCase {

    private final UsuarioRepository usuarios;
    private final UsuarioAutenticadoProvider autenticado;

    public AtualizarUsuarioUseCase(UsuarioRepository usuarios, UsuarioAutenticadoProvider autenticado) {
        this.usuarios = usuarios;
        this.autenticado = autenticado;
    }

    @Transactional
    public UsuarioOutput executar(UUID id, AtualizarUsuarioInput input) {
        UsuarioAutenticado solicitante = autenticado.obrigatorio();
        if (!solicitante.ehAdministrador() && !solicitante.ehOMesmo(id)) {
            throw RecursoNaoEncontradoException.de("Usuario");
        }
        Usuario usuario = usuarios.porId(id).orElseThrow(() -> RecursoNaoEncontradoException.de("Usuario"));

        Email novoEmail = new Email(input.email());
        if (usuarios.existeOutroComEmail(id, novoEmail)) {
            throw new ConflitoException("Ja existe um usuario cadastrado com este e-mail.");
        }
        usuario.atualizarDados(input.nome(), novoEmail);
        return UsuarioOutput.de(usuarios.salvar(usuario));
    }
}
