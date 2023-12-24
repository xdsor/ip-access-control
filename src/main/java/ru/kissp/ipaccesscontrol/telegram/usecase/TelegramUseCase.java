package ru.kissp.ipaccesscontrol.telegram.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.telegram.port.TelegramApiPort;
import ru.kissp.ipaccesscontrol.telegram.port.TelegramPort;

@Component
@RequiredArgsConstructor
public class TelegramUseCase implements TelegramPort {
    private final TelegramApiPort telegramApiPort;

    @Override
    public Mono<String> notifyUserActivated(String telegramId) {
        return telegramApiPort.sendMessage(
                telegramId,
                String.format(
                        "Заявка одобрена.\nПерсональная ссылка для предоставления доступа https://kissp.ru/activate/%s",
                        telegramId
                ));
    }

    @Override
    public Mono<String> notifyUserIpActivated(String telegramId) {
        return telegramApiPort.sendMessage(telegramId, "Доступ предоставлен");
    }
}
