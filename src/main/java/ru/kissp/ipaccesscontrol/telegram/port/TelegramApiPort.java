package ru.kissp.ipaccesscontrol.telegram.port;

import reactor.core.publisher.Mono;

public interface TelegramApiPort {
    Mono<String> sendMessage(String telegramId, String message);
}
