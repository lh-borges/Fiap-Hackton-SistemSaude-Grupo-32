package br.com.fiap.sus.cadastros.application.dto;

import br.com.fiap.sus.cadastros.domain.model.Medico;
import java.util.UUID;

public record MedicoOutput(UUID id, UUID usuarioId, String nome, String email, String crm, String ufCrm,
                           UUID especialidadeId, String especialidade, boolean ativo) {

    public static MedicoOutput de(Medico medico, String nome, String email, String especialidade) {
        return new MedicoOutput(medico.getId(), medico.getUsuarioId(), nome, email, medico.getCrm(),
                medico.getUfCrm(), medico.getEspecialidadeId(), especialidade, medico.isAtivo());
    }
}
