/**
 * Eventos publicados pelo modulo receitas para consumidores de outros modulos (notificacoes).
 * Contrato em specs/000-plataforma-sus/events.md. Payload sem medicamento nem posologia.
 */
@org.springframework.modulith.NamedInterface("events")
package br.com.fiap.sus.receitas.application.event;
