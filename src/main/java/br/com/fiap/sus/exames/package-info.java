/**
 * Modulo de exames: solicitacao medica e execucao/agendamento do exame,
 * mantidos como registros distintos e rastreaveis.
 *
 * <p>Depende de cadastros apenas pela porta publicada {@code cadastros::api}, para
 * validar paciente, medico, unidade de saude e tipo de exame. Nao ha FK para as
 * tabelas de cadastros.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Exames",
        allowedDependencies = {"shared", "cadastros::api"})
package br.com.fiap.sus.exames;