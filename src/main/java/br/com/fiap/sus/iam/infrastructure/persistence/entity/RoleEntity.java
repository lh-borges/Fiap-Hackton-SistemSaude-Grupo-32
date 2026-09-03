package br.com.fiap.sus.iam.infrastructure.persistence.entity;

import br.com.fiap.sus.iam.domain.enums.RoleNome;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "iam_role")
public class RoleEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, unique = true)
    private RoleNome nome;

    protected RoleEntity() {
    }

    public UUID getId() {
        return id;
    }

    public RoleNome getNome() {
        return nome;
    }
}
