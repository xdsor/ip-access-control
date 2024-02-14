package ru.kissp.ipaccesscontrol.ipcheck.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.appuser.adapter.handler.AppUserHandler;
import ru.kissp.ipaccesscontrol.common.config.RouterConfiguration;
import ru.kissp.ipaccesscontrol.ipaccess.adapter.handler.IpAccessHandler;
import ru.kissp.ipaccesscontrol.ipcheck.port.IpCheckPort;
import ru.kissp.ipaccesscontrol.security.adapter.AuthenticationHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class IpCheckHandlerTest {
    @InjectMocks
    private IpCheckHandler ipCheckHandler;

    @Mock
    private AppUserHandler appUserHandler;

    @Mock
    private IpAccessHandler ipAccessHandler;

    @Mock
    private AuthenticationHandler authenticationHandler;

    @Mock
    private IpCheckPort ipCheckPort;

    private WebTestClient webTestClient;

    @BeforeEach
    public void setUp() {
        var router = new RouterConfiguration();
        this.webTestClient = WebTestClient.bindToRouterFunction(
                        router.route(ipCheckHandler, ipAccessHandler, appUserHandler, authenticationHandler))
                .configureClient()
                .build();
    }


    @Test
    public void should_successfully_check_if_ip_has_access() {
        when(ipCheckPort.checkIfHasAccess(any()))
                .thenReturn(Mono.just(Boolean.TRUE));

        webTestClient.post()
                .uri("/check")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"ip\": \"192.168.20.58\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("{\"allowed\": true}");

        verify(ipCheckPort).checkIfHasAccess("192.168.20.58");
    }
}
