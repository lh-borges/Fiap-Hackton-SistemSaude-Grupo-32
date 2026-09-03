package br.com.fiap.sus.iam.application.port;

/** Porta de saida para codificacao de senha. Implementada com BCrypt (Artigo IV.5). */
public interface SenhaEncoderPort {

    String codificar(String senhaEmTexto);

    boolean confere(String senhaEmTexto, String hashArmazenado);
}
