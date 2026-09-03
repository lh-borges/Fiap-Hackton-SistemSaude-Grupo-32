package br.com.fiap.sus.iam.domain.enums;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.util.Arrays;

/** Perfis de autorizacao do sistema (secao 2.1 do relatorio). */
public enum RoleNome {

    ADMINISTRADOR,
    ATENDENTE,
    MEDICO,
    PACIENTE;

    public static RoleNome de(String nome) {
        return Arrays.stream(values())
                .filter(role -> role.name().equalsIgnoreCase(nome == null ? "" : nome.trim()))
                .findFirst()
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Perfil invalido. Valores aceitos: ADMINISTRADOR, ATENDENTE, MEDICO, PACIENTE."));
    }

    /** Autoridade no formato exigido pelo Spring Security (Artigo IV.2). */
    public String autoridade() {
        return "ROLE_" + name();
    }
}
