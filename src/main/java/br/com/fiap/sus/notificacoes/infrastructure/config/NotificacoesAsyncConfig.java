package br.com.fiap.sus.notificacoes.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * O {@code @ApplicationModuleListener} do Spring Modulith e assincrono ({@code @Async});
 * sem processamento assincrono habilitado ele rodaria na thread da requisicao de origem.
 * Nenhum outro bean do sistema usa {@code @Async}.
 */
@Configuration
@EnableAsync
public class NotificacoesAsyncConfig {
}
