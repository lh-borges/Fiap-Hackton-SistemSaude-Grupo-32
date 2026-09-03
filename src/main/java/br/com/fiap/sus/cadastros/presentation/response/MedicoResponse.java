package br.com.fiap.sus.cadastros.presentation.response;

import br.com.fiap.sus.cadastros.application.dto.MedicoOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "Medico")
public record MedicoResponse(UUID id, UUID usuarioId, String nome, String email, String crm, String ufCrm,
                             UUID especialidadeId, String especialidade, boolean ativo) {

    public static MedicoResponse de(MedicoOutput o) {
        return new MedicoResponse(o.id(), o.usuarioId(), o.nome(), o.email(), o.crm(), o.ufCrm(),
                o.especialidadeId(), o.especialidade(), o.ativo());
    }
}
