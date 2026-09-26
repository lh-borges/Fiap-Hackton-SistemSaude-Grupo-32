/**
 * Eventos publicados pelo modulo resultados para consumidores de outros modulos (notificacoes).
 * Contrato em specs/000-plataforma-sus/events.md. Payload sem conteudo clinico: nem laudo, nem itens.
 */
@org.springframework.modulith.NamedInterface("events")
package br.com.fiap.sus.resultados.application.event;
