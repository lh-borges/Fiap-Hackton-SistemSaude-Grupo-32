package br.com.fiap.sus.iam.domain.repository;

import br.com.fiap.sus.iam.domain.enums.RoleNome;
import br.com.fiap.sus.iam.domain.model.Role;
import java.util.List;
import java.util.Optional;

public interface RoleRepository {

    List<Role> listarTodas();

    Optional<Role> porNome(RoleNome nome);
}
