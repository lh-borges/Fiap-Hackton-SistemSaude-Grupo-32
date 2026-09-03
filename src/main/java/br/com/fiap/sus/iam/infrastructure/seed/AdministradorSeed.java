package br.com.fiap.sus.iam.infrastructure.seed;

import br.com.fiap.sus.iam.application.port.SenhaEncoderPort;
import br.com.fiap.sus.iam.domain.enums.RoleNome;
import br.com.fiap.sus.iam.domain.model.Usuario;
import br.com.fiap.sus.iam.domain.repository.UsuarioRepository;
import br.com.fiap.sus.shared.domain.vo.Cpf;
import br.com.fiap.sus.shared.domain.vo.Email;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria o administrador inicial na primeira subida, para que a API seja utilizavel
 * imediatamente. Nao vai em migration porque o hash BCrypt precisa ser gerado pela
 * aplicacao. Desligue com SEED_ENABLED=false.
 */
@Component
@ConditionalOnProperty(name = "sus.seed.enabled", havingValue = "true", matchIfMissing = false)
public class AdministradorSeed implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdministradorSeed.class);

    private final UsuarioRepository usuarios;
    private final SenhaEncoderPort senhas;
    private final String nome;
    private final String email;
    private final String senha;
    private final String cpf;

    public AdministradorSeed(UsuarioRepository usuarios, SenhaEncoderPort senhas,
                             @Value("${sus.seed.admin-nome}") String nome,
                             @Value("${sus.seed.admin-email}") String email,
                             @Value("${sus.seed.admin-senha}") String senha,
                             @Value("${sus.seed.admin-cpf}") String cpf) {
        this.usuarios = usuarios;
        this.senhas = senhas;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.cpf = cpf;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Email emailAdmin = new Email(email);
        if (usuarios.existePorEmail(emailAdmin)) {
            return;
        }
        usuarios.salvar(Usuario.criar(nome, new Cpf(cpf), emailAdmin, senhas.codificar(senha),
                Set.of(RoleNome.ADMINISTRADOR)));
        log.info("Administrador inicial criado. Troque a senha padrao antes de qualquer uso real.");
    }
}
