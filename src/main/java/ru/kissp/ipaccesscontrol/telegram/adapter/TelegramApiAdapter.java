package ru.kissp.ipaccesscontrol.telegram.adapter;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.telegram.port.TelegramApiPort;

@Component
public class TelegramApiAdapter implements TelegramApiPort {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private WebClient webClient;
    @Value("${integration.telegram.url}")
    private String apiUrl;
    @Value("${integration.telegram.token}")
    private String botToken;

    @PostConstruct
    public void init() {
        webClient = WebClient.create(String.format("%s/bot%s", apiUrl, botToken));
    }


    @Override
    public Mono<String> sendMessage(String telegramId, String message) {
        return webClient.post()
                .uri("/sendMessage")
                .bodyValue(new SendTelegramMessageDto(
                        message,
                        telegramId
                ))
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(err -> logger.error("Telegram API error {}", err));
    }
}
