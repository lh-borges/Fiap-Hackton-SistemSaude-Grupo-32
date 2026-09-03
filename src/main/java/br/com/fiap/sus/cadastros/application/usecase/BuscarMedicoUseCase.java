package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.application.dto.MedicoOutput;
import br.com.fiap.sus.cadastros.domain.model.Medico;
import br.com.fiap.sus.cadastros.domain.repository.EspecialidadeRepository;
import br.com.fiap.sus.cadastros.domain.repository.MedicoRepository;
import br.com.fiap.sus.iam.api.IamQuery;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Dados do medico sao visiveis a qualquer usuario autenticado (o paciente precisa escolher). */
@Service
public class BuscarMedicoUseCase {

    private final MedicoRepository medicos;
    private final EspecialidadeRepository especialidades;
    private final IamQuery iam;

    public BuscarMedicoUseCase(MedicoRepository medicos, EspecialidadeRepository especialidades,
                               IamQuery iam) {
        this.medicos = medicos;
        this.especialidades = especialidades;
        this.iam = iam;
    }

    @Transactional(readOnly = true)
    public MedicoOutput executar(UUID id) {
        Medico medico = medicos.porId(id).orElseThrow(() -> RecursoNaoEncontradoException.de("Medico"));
        return montar(medico);
    }

    MedicoOutput montar(Medico medico) {
        var usuario = iam.resumoDoUsuario(medico.getUsuarioId()).orElse(null);
        String especialidade = especialidades.porId(medico.getEspecialidadeId())
                .map(br.com.fiap.sus.cadastros.domain.model.Especialidade::getNome)
                .orElse(null);
        return MedicoOutput.de(medico,
                usuario == null ? null : usuario.nome(),
                usuario == null ? null : usuario.email(),
                especialidade);
    }
}
