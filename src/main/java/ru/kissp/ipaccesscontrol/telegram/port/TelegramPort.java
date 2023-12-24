package ru.kissp.ipaccesscontrol.telegram.port;

import reactor.core.publisher.Mono;

public interface TelegramPort {
    Mono<String> notifyUserActivated(String telegramId);
    Mono<String> notifyUserIpActivated(String telegramId);
}
