package br.com.fiap.sus.iam.application.usecase;

import br.com.fiap.sus.iam.application.dto.CadastrarUsuarioInput;
import br.com.fiap.sus.iam.application.dto.UsuarioOutput;
import br.com.fiap.sus.iam.application.port.SenhaEncoderPort;
import br.com.fiap.sus.iam.domain.enums.RoleNome;
import br.com.fiap.sus.iam.domain.model.SenhaEmTexto;
import br.com.fiap.sus.iam.domain.model.Usuario;
import br.com.fiap.sus.iam.domain.repository.UsuarioRepository;
import br.com.fiap.sus.shared.domain.exception.ConflitoException;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import br.com.fiap.sus.shared.domain.vo.Cpf;
import br.com.fiap.sus.shared.domain.vo.Email;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU-02: cadastro de usuario pelo administrador. */
@Service
public class CadastrarUsuarioUseCase {

    private final UsuarioRepository usuarios;
    private final SenhaEncoderPort senhas;

    public CadastrarUsuarioUseCase(UsuarioRepository usuarios, SenhaEncoderPort senhas) {
        this.usuarios = usuarios;
        this.senhas = senhas;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UsuarioOutput executar(CadastrarUsuarioInput input) {
        Cpf cpf = new Cpf(input.cpf());
        Email email = new Email(input.email());

        if (usuarios.existePorCpf(cpf)) {
            throw new ConflitoException("Ja existe um usuario cadastrado com este CPF.");
        }
        if (usuarios.existePorEmail(email)) {
            throw new ConflitoException("Ja existe um usuario cadastrado com este e-mail.");
        }

        SenhaEmTexto senha = new SenhaEmTexto(input.senha());
        Usuario usuario = Usuario.criar(input.nome(), cpf, email, senhas.codificar(senha.valor()),
                converterRoles(input.roles()));
        return UsuarioOutput.de(usuarios.salvar(usuario));
    }

    static Set<RoleNome> converterRoles(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new RegraDeNegocioException("Informe ao menos um perfil para o usuario.");
        }
        Set<RoleNome> convertidas = new LinkedHashSet<>();
        roles.forEach(role -> convertidas.add(RoleNome.de(role)));
        return convertidas;
    }
}
