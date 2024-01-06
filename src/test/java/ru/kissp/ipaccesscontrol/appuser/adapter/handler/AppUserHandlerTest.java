package ru.kissp.ipaccesscontrol.appuser.adapter.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.appuser.adapter.dto.CreateNewUserRequest;
import ru.kissp.ipaccesscontrol.appuser.adapter.dto.UserDto;
import ru.kissp.ipaccesscontrol.appuser.port.AppUserPort;
import ru.kissp.ipaccesscontrol.common.config.RouterConfiguration;
import ru.kissp.ipaccesscontrol.ipaccess.adapter.handler.IpAccessHandler;
import ru.kissp.ipaccesscontrol.ipcheck.adapter.IpCheckHandler;
import ru.kissp.ipaccesscontrol.utils.TestDataGenerator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class AppUserHandlerTest {
    @Mock
    private IpCheckHandler ipCheckHandler;
    @Mock
    private IpAccessHandler ipAccessHandler;

    @Mock
    private AppUserPort appUserPort;

    private AppUserHandler appUserHandler;

    private WebTestClient webTestClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        appUserHandler = new AppUserHandler(
            appUserPort,
            new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator())
        );

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

    @Test
    public void should_handle_update_user_request() {
        var testUser = TestDataGenerator.createAppUser();
        var request = """
            {
                "userComment": "New user comment",
                "name": "New user name",
                "isActive": false
            }
            """;

        when(appUserPort.updateUser(any(), any())).thenReturn(Mono.just(testUser));

        webTestClient.patch()
            .uri(String.format("/users/%s", testUser.getId()))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isNoContent();

        verify(appUserPort).updateUser(any(), eq(testUser.getId()));
    }

    @Test
    public void should_return_bad_request_on_update_user_request_without_request_body() {
        webTestClient.patch()
            .uri(String.format("/users/%s", "testId"))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest();

        verify(appUserPort, never()).updateUser(any(), any());
    }

    @Test
    public void should_return_all_users() {
        when(appUserPort.getAllUsers()).thenReturn(
            Flux.just(
                UserDto.fromDomain(TestDataGenerator.createAppUser()),
                UserDto.fromDomain(TestDataGenerator.createAppUser())
            )
        );

        webTestClient.get()
            .uri("/users")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(UserDto.class)
            .hasSize(2);
    }

    @Test
    public void should_return_empty_users_list() {
        when(appUserPort.getAllUsers()).thenReturn(Flux.empty());

        webTestClient.get()
            .uri("/users")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(UserDto.class)
            .hasSize(0);
    }

    @Test
    public void should_return_user_by_id() {
        when(appUserPort.getUserById("someID")).thenReturn(
            Mono.just(UserDto.fromDomain(TestDataGenerator.createAppUser()))
        );

        webTestClient.get()
            .uri("/users/someID")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserDto.class);
    }

    @Test
    public void should_return_404_if_user_does_not_exist_on_get_user_by_id() {
        when(appUserPort.getUserById("someID")).thenReturn(Mono.empty());

        webTestClient.get()
            .uri("/users/someID")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound();
    }
}
