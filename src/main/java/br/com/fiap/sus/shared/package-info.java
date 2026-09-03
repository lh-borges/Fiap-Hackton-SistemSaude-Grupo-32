/**
 * Tipos compartilhados por todos os modulos: value objects de dominio, excecoes,
 * tratamento de erro HTTP, contexto do usuario autenticado e configuracao do OpenAPI.
 *
 * <p>Modulo aberto: seus pacotes internos podem ser usados pelos demais modulos.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Shared",
        type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package br.com.fiap.sus.shared;
