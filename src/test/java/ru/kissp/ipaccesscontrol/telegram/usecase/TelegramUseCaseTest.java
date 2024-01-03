package ru.kissp.ipaccesscontrol.telegram.usecase;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.kissp.ipaccesscontrol.telegram.port.TelegramApiPort;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class TelegramUseCaseTest {
    @Mock
    private TelegramApiPort telegramApiPort;
    @InjectMocks
    private TelegramUseCase telegramUseCase;

    @Test
    public void should_notify_user_activated() {
        when(telegramApiPort.sendMessage(any(), any())).thenReturn(Mono.just("telegramResponse"));

        StepVerifier.create(telegramUseCase.notifyUserActivated("123456789"))
                .expectNext("telegramResponse")
                .verifyComplete();

        verify(telegramApiPort).sendMessage(eq("123456789"), any());
    }

    @Test
    public void should_notify_user_ip_activated() {
        when(telegramApiPort.sendMessage(any(), any())).thenReturn(Mono.just("telegramResponse"));

        StepVerifier.create(telegramUseCase.notifyUserIpActivated("123456789"))
                .expectNext("telegramResponse")
                .verifyComplete();

        verify(telegramApiPort).sendMessage(eq("123456789"), any());
    }
}
