package br.com.fiap.sus.cadastros.infrastructure.persistence.mapper;

import br.com.fiap.sus.cadastros.domain.model.Especialidade;
import br.com.fiap.sus.cadastros.domain.model.Medico;
import br.com.fiap.sus.cadastros.domain.model.Paciente;
import br.com.fiap.sus.cadastros.domain.model.TipoExame;
import br.com.fiap.sus.cadastros.domain.model.UnidadeSaude;
import br.com.fiap.sus.cadastros.infrastructure.persistence.entity.EspecialidadeEntity;
import br.com.fiap.sus.cadastros.infrastructure.persistence.entity.MedicoEntity;
import br.com.fiap.sus.cadastros.infrastructure.persistence.entity.PacienteEntity;
import br.com.fiap.sus.cadastros.infrastructure.persistence.entity.TipoExameEntity;
import br.com.fiap.sus.cadastros.infrastructure.persistence.entity.UnidadeSaudeEntity;
import org.springframework.stereotype.Component;

/** Traducao explicita entre dominio e persistencia para os cinco agregados do modulo. */
@Component
public class CadastroPersistenceMapper {

    public Paciente paraDominio(PacienteEntity e) {
        return Paciente.reconstituir(e.getId(), e.getUsuarioId(), e.getCartaoSus(), e.getDataNascimento(),
                e.getSexo(), e.getTipoSanguineo(), e.isAtivo(), e.getCriadoEm(), e.getAtualizadoEm());
    }

    public PacienteEntity paraEntidade(Paciente p) {
        return new PacienteEntity(p.getId(), p.getUsuarioId(), p.getCartaoSus(), p.getDataNascimento(),
                p.getSexo(), p.getTipoSanguineo(), p.isAtivo(), p.getCriadoEm(), p.getAtualizadoEm());
    }

    public Medico paraDominio(MedicoEntity e) {
        return Medico.reconstituir(e.getId(), e.getUsuarioId(), e.getCrm(), e.getUfCrm(),
                e.getEspecialidadeId(), e.isAtivo(), e.getCriadoEm(), e.getAtualizadoEm());
    }

    public MedicoEntity paraEntidade(Medico m) {
        return new MedicoEntity(m.getId(), m.getUsuarioId(), m.getCrm(), m.getUfCrm(),
                m.getEspecialidadeId(), m.isAtivo(), m.getCriadoEm(), m.getAtualizadoEm());
    }

    public Especialidade paraDominio(EspecialidadeEntity e) {
        return Especialidade.reconstituir(e.getId(), e.getNome(), e.getDescricao(), e.isAtivo(),
                e.getCriadoEm(), e.getAtualizadoEm());
    }

    public EspecialidadeEntity paraEntidade(Especialidade especialidade) {
        return new EspecialidadeEntity(especialidade.getId(), especialidade.getNome(),
                especialidade.getDescricao(), especialidade.isAtivo(), especialidade.getCriadoEm(),
                especialidade.getAtualizadoEm());
    }

    public UnidadeSaude paraDominio(UnidadeSaudeEntity e) {
        return UnidadeSaude.reconstituir(e.getId(), e.getNome(), e.getCnes(), e.getTelefone(),
                e.isAtivo(), e.getCriadoEm(), e.getAtualizadoEm());
    }

    public UnidadeSaudeEntity paraEntidade(UnidadeSaude unidade) {
        return new UnidadeSaudeEntity(unidade.getId(), unidade.getNome(), unidade.getCnes(),
                unidade.getTelefone(), unidade.isAtivo(), unidade.getCriadoEm(), unidade.getAtualizadoEm());
    }

    public TipoExame paraDominio(TipoExameEntity e) {
        return TipoExame.reconstituir(e.getId(), e.getNome(), e.getCategoria(), e.getPreparo(),
                e.isAtivo(), e.getCriadoEm(), e.getAtualizadoEm());
    }

    public TipoExameEntity paraEntidade(TipoExame tipoExame) {
        return new TipoExameEntity(tipoExame.getId(), tipoExame.getNome(), tipoExame.getCategoria(),
                tipoExame.getPreparo(), tipoExame.isAtivo(), tipoExame.getCriadoEm(),
                tipoExame.getAtualizadoEm());
    }
}
