package br.com.fiap.sus.iam.application.port;

import br.com.fiap.sus.iam.domain.model.Usuario;

/** Porta de saida para emissao da credencial de acesso. */
public interface TokenPort {

    TokenGerado gerarPara(Usuario usuario);
}
