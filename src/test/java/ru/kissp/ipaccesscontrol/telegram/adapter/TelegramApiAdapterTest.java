package ru.kissp.ipaccesscontrol.telegram.adapter;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TelegramApiAdapterTest {

    private MockWebServer mockWebServer;
    private TelegramApiAdapter telegramApiAdapter;

    @BeforeEach
    public void setUp() {
        mockWebServer = new MockWebServer();
        telegramApiAdapter = new TelegramApiAdapter();
//        ReflectionTestUtils.setField(telegramApiAdapter, "webClient", WebClient.create(mockWebServer.url("/").toString()));
        ReflectionTestUtils.setField(telegramApiAdapter, "apiUrl", mockWebServer.url("").toString());
        ReflectionTestUtils.setField(telegramApiAdapter, "botToken", "secret");
        ReflectionTestUtils.invokeMethod(telegramApiAdapter, "init");
    }

    @AfterEach
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void should_call_sendMessage_method_from_telegram_api() throws InterruptedException {
        var telegramId = "123456";
        var message = "Hello, Telegram!";
        mockWebServer.enqueue(new MockResponse().setBody("{\"ok\":true}").setResponseCode(200));

        Mono<String> result = telegramApiAdapter.sendMessage(telegramId, message);

        StepVerifier.create(result)
                .expectNext("{\"ok\":true}")
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("{\"text\":\"Hello, Telegram!\",\"chat_id\":\"123456\"}",
                recordedRequest.getBody().readString(Charset.defaultCharset()));
        assertTrue(recordedRequest.getPath().endsWith("botsecret/sendMessage"));
    }

    @Test
    public void should_handle_error_on_call_sendMessage_method_from_telegram_api() {
        var telegramId = "123456";
        var message = "Hello, Telegram!";
        mockWebServer.enqueue(new MockResponse().setBody("error").setResponseCode(500));

        Mono<String> result = telegramApiAdapter.sendMessage(telegramId, message);

        StepVerifier.create(result)
                .expectError(WebClientResponseException.InternalServerError.class)
                .verify();
    }
}
