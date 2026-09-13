/**
 * Modulo de resultados: dado tecnico produzido pela realizacao de um exame,
 * separado da interpretacao clinica (parecer medico).
 *
 * <p>Depende de cadastros pela porta {@code cadastros::api} (categoria do tipo de
 * exame) e de exames pela porta {@code exames::api} (situacao do exame e tipo de
 * exame da solicitacao de origem). Nao ha FK para as tabelas de outros modulos.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Resultados",
        allowedDependencies = {"shared", "cadastros::api", "exames::api"})
package br.com.fiap.sus.resultados;