package br.com.fiap.sus.iam.application.usecase;

import br.com.fiap.sus.iam.application.dto.AutenticacaoOutput;
import br.com.fiap.sus.iam.application.dto.UsuarioOutput;
import br.com.fiap.sus.iam.application.port.SenhaEncoderPort;
import br.com.fiap.sus.iam.application.port.TokenGerado;
import br.com.fiap.sus.iam.application.port.TokenPort;
import br.com.fiap.sus.iam.domain.exception.CredenciaisInvalidasException;
import br.com.fiap.sus.iam.domain.model.Usuario;
import br.com.fiap.sus.iam.domain.repository.UsuarioRepository;
import br.com.fiap.sus.shared.domain.exception.DominioException;
import br.com.fiap.sus.shared.domain.vo.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU-01: autenticacao. Rota publica. */
@Service
public class AutenticarUsuarioUseCase {

    private final UsuarioRepository usuarios;
    private final SenhaEncoderPort senhas;
    private final TokenPort tokens;

    public AutenticarUsuarioUseCase(UsuarioRepository usuarios, SenhaEncoderPort senhas, TokenPort tokens) {
        this.usuarios = usuarios;
        this.senhas = senhas;
        this.tokens = tokens;
    }

    @Transactional(readOnly = true)
    public AutenticacaoOutput executar(String email, String senha) {
        Usuario usuario = buscarPorEmail(email);
        // RN-05 e EX-01: usuario inativo e senha errada devolvem exatamente a mesma resposta.
        if (!usuario.isAtivo() || !senhas.confere(senha, usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }
        TokenGerado gerado = tokens.gerarPara(usuario);
        return AutenticacaoOutput.de(gerado.token(), gerado.expiraEm(), UsuarioOutput.de(usuario));
    }

    private Usuario buscarPorEmail(String email) {
        try {
            return usuarios.porEmail(new Email(email)).orElseThrow(CredenciaisInvalidasException::new);
        } catch (DominioException ex) {
            // E-mail mal formado tambem nao pode diferenciar a resposta.
            throw new CredenciaisInvalidasException();
        }
    }
}
