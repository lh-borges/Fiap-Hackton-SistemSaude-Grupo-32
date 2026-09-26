/**
 * Modulo de notificacoes: avisos internos ao usuario sobre fatos do seu atendimento.
 *
 * <p>Nao e chamado por nenhum outro modulo: apenas reage a eventos de dominio (Artigo III).
 * Depende de {@code cadastros::api} para resolver o usuario destinatario a partir de
 * pacienteId/medicoId, e dos pacotes {@code events} dos produtores para enxergar os tipos
 * dos eventos que consome. Nao ha FK para tabelas de outros modulos.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Notificacoes",
        allowedDependencies = {
                "shared", "cadastros::api",
                "consultas::events", "exames::events", "resultados::events",
                "pareceres::events", "receitas::events", "documentos::events"})
package br.com.fiap.sus.notificacoes;
