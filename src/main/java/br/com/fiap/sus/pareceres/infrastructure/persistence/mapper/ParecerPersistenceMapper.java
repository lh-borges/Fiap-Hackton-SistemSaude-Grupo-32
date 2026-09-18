package br.com.fiap.sus.pareceres.infrastructure.persistence.mapper;

import br.com.fiap.sus.pareceres.domain.model.ParecerMedico;
import br.com.fiap.sus.pareceres.infrastructure.persistence.entity.ParecerMedicoEntity;
import org.springframework.stereotype.Component;

@Component
public class ParecerPersistenceMapper {
    public ParecerMedicoEntity paraEntidade(ParecerMedico parecer, java.util.UUID criadoPorUsuarioId) {
        return new ParecerMedicoEntity(parecer.getId(), parecer.getResultadoExameId(), parecer.getPacienteId(),
                parecer.getMedicoId(), parecer.getDescricao(), parecer.getDataParecer(), criadoPorUsuarioId);
    }

    public ParecerMedico paraDominio(ParecerMedicoEntity entity) {
        return ParecerMedico.reconstituir(entity.getId(), entity.getResultadoExameId(), entity.getPacienteId(),
                entity.getMedicoId(), entity.getDescricao(), entity.getDataParecer());
    }
}
