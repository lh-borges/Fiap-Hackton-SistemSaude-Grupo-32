package br.com.fiap.sus.iam.infrastructure.persistence.mapper;

import br.com.fiap.sus.iam.domain.enums.RoleNome;
import br.com.fiap.sus.iam.domain.model.Usuario;
import br.com.fiap.sus.iam.infrastructure.persistence.entity.RoleEntity;
import br.com.fiap.sus.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.fiap.sus.shared.domain.vo.Cpf;
import br.com.fiap.sus.shared.domain.vo.Email;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Traducao explicita entre entidade de dominio e entidade JPA (Artigo II.3). */
@Component
public class UsuarioPersistenceMapper {

    public Usuario paraDominio(UsuarioEntity entidade) {
        Set<RoleNome> roles = entidade.getRoles().stream()
                .map(RoleEntity::getNome)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return Usuario.reconstituir(
                entidade.getId(),
                entidade.getNome(),
                new Cpf(entidade.getCpf()),
                new Email(entidade.getEmail()),
                entidade.getSenha(),
                entidade.isAtivo(),
                roles,
                entidade.getCriadoEm(),
                entidade.getAtualizadoEm());
    }

    public UsuarioEntity paraEntidade(Usuario usuario, Map<RoleNome, RoleEntity> rolesPersistidas) {
        Set<RoleEntity> roles = usuario.getRoles().stream()
                .map(rolesPersistidas::get)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new UsuarioEntity(
                usuario.getId(),
                usuario.getNome(),
                usuario.getCpf().valor(),
                usuario.getEmail().valor(),
                usuario.getSenhaHash(),
                usuario.isAtivo(),
                roles,
                usuario.getCriadoEm(),
                usuario.getAtualizadoEm());
    }
}
