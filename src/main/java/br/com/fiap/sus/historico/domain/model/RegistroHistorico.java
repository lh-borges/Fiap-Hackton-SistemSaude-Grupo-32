package br.com.fiap.sus.historico.domain.model;

import br.com.fiap.sus.historico.domain.enums.TipoRegistro;
import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

/**
 * Item da linha do tempo. Projecao imutavel de um registro de outro modulo; nao e persistido (RN-01)
 * e nao carrega conteudo clinico (RN-06).
 */
public record RegistroHistorico(
        TipoRegistro tipo,
        UUID id,
        Instant data,
        String titulo,
        String situacao,
        UUID medicoId,
        Referencia origem
) {
    public static final int TITULO_MAXIMO = 150;

    /** Mais recente primeiro; empate por tipo (ordem natural do atendimento) e depois por id (RN-02, RN-07). */
    public static final Comparator<RegistroHistorico> ORDEM = Comparator
            .comparing(RegistroHistorico::data, Comparator.reverseOrder())
            .thenComparing(RegistroHistorico::tipo)
            .thenComparing(RegistroHistorico::id);

    public RegistroHistorico {
        Objects.requireNonNull(tipo, "Tipo do registro e obrigatorio.");
        Objects.requireNonNull(id, "Id do registro e obrigatorio.");
        Objects.requireNonNull(data, "Data do registro e obrigatoria.");
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Titulo do registro e obrigatorio.");
        }
        if (titulo.length() > TITULO_MAXIMO) {
            throw new IllegalArgumentException("Titulo do registro excede " + TITULO_MAXIMO + " caracteres.");
        }
        if (situacao == null || situacao.isBlank()) {
            throw new IllegalArgumentException("Situacao do registro e obrigatoria.");
        }
    }

    public String recurso() {
        return tipo.recurso();
    }
}
