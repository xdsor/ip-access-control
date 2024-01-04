package ru.kissp.ipaccesscontrol.appuser.adapter.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.kissp.ipaccesscontrol.appuser.adapter.dto.CreateNewUserRequest;
import ru.kissp.ipaccesscontrol.appuser.port.AppUserPort;
import ru.kissp.ipaccesscontrol.common.config.RouterConfiguration;
import ru.kissp.ipaccesscontrol.ipaccess.adapter.handler.IpAccessHandler;
import ru.kissp.ipaccesscontrol.ipcheck.adapter.IpCheckHandler;
import ru.kissp.ipaccesscontrol.utils.TestDataGenerator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class AppUserHandlerTest {
    @Mock
    private IpCheckHandler ipCheckHandler;
    @Mock
    private IpAccessHandler ipAccessHandler;

    @Mock
    private AppUserPort appUserPort;
    @InjectMocks
    private AppUserHandler appUserHandler;

    private WebTestClient webTestClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        var router = new RouterConfiguration();
        this.webTestClient = WebTestClient.bindToRouterFunction(
                router.route(ipCheckHandler, ipAccessHandler, appUserHandler))
                .configureClient()
                .build();
    }

    @Test
    public void should_handle_create_user_request() throws JsonProcessingException {
        var request = TestDataGenerator.createNewUserRequest();
        var requestBody = objectMapper.writeValueAsString(request);
        when(appUserPort.createUser(any(CreateNewUserRequest.class))).thenReturn(Mono.just(TestDataGenerator.createAppUser()));
        webTestClient.put()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .isEmpty();

        verify(appUserPort).createUser(request);
    }

    @Test
    public void should_handle_activate_user_request() {
        var testUser = TestDataGenerator.createAppUser();
        when(appUserPort.activateUser(any(String.class))).thenReturn(Mono.just(testUser));
        webTestClient.post()
                .uri(String.format("/users/%s/activate", testUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .isEmpty();

        verify(appUserPort).activateUser(testUser.getId());
    }
}
