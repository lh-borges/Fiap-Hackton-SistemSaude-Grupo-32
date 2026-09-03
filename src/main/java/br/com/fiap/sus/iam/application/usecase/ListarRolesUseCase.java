package br.com.fiap.sus.iam.application.usecase;

import br.com.fiap.sus.iam.domain.model.Role;
import br.com.fiap.sus.iam.domain.repository.RoleRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF-07: catalogo de perfis disponiveis. */
@Service
public class ListarRolesUseCase {

    private final RoleRepository roles;

    public ListarRolesUseCase(RoleRepository roles) {
        this.roles = roles;
    }

    @Transactional(readOnly = true)
    public List<Role> executar() {
        return roles.listarTodas();
    }
}
