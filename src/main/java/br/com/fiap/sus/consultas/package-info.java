/**
 * Modulo de consultas: agendamento, remarcacao, cancelamento e realizacao de
 * consultas entre paciente e medico em uma unidade de saude.
 *
 * <p>Depende de cadastros apenas pela porta publicada {@code cadastros::api}, para
 * validar existencia/atividade de paciente, medico e unidade de saude, e para
 * resolver o usuario autenticado em pacienteId/medicoId. Nao ha FK para as
 * tabelas de cadastros.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Consultas",
        allowedDependencies = {"shared", "cadastros::api"})
package br.com.fiap.sus.consultas;