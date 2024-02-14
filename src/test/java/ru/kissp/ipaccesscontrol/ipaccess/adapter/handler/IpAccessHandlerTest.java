package ru.kissp.ipaccesscontrol.ipaccess.adapter.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import ru.kissp.ipaccesscontrol.appuser.adapter.handler.AppUserHandler;
import ru.kissp.ipaccesscontrol.appuser.usecase.exceptions.UserIsNotActivatedException;
import ru.kissp.ipaccesscontrol.appuser.usecase.exceptions.UserNotFoundException;
import ru.kissp.ipaccesscontrol.common.config.RouterConfiguration;
import ru.kissp.ipaccesscontrol.ipaccess.adapter.dto.IpAccessDto;
import ru.kissp.ipaccesscontrol.ipaccess.port.IpAccessPort;
import ru.kissp.ipaccesscontrol.ipaccess.usecase.exceptions.IpAccessNotFoundException;
import ru.kissp.ipaccesscontrol.ipaccess.usecase.exceptions.UserHasActiveIpException;
import ru.kissp.ipaccesscontrol.ipcheck.adapter.IpCheckHandler;
import ru.kissp.ipaccesscontrol.security.adapter.AuthenticationHandler;
import ru.kissp.ipaccesscontrol.utils.TestDataGenerator;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class IpAccessHandlerTest {
    @Mock
    private IpCheckHandler ipCheckHandler;

    @Mock
    private IpAccessPort ipAccessPort;
    @Mock
    private AppUserHandler appUserHandler;

    @Mock
    private AuthenticationHandler authenticationHandler;

    private WebTestClient webTestClient;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    public void setUp() {
        IpAccessHandler ipAccessHandler = new IpAccessHandler(
                ipAccessPort,
                new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator()),
                new IpAccessExceptionHandler()
        );

        var router = new RouterConfiguration();
        this.webTestClient = WebTestClient.bindToRouterFunction(
                        router.route(ipCheckHandler, ipAccessHandler, appUserHandler, authenticationHandler))
                .configureClient()
                .build();
    }

    @Test
    public void should_handle_request_for_getting_all_accesses() throws JsonProcessingException {
        var issuedFor = TestDataGenerator.createAppUser();
        var ipAccess = TestDataGenerator.createIpAccess(issuedFor.getId());
        var expectedResponse = List.of(
                new IpAccessDto(
                        ipAccess.getId(),
                        ipAccess.getIp(),
                        ipAccess.getIsActive(),
                        ipAccess.getCreatedAt(),
                        new IpAccessDto.IpAccessIssuedFor(
                                issuedFor.getId(),
                                issuedFor.getTelegramId(),
                                issuedFor.getName(),
                                issuedFor.getIsActive()
                        )
                )
        );

        when(ipAccessPort.getAllIpAccess()).thenReturn(Flux.fromIterable(expectedResponse));

        webTestClient.get()
                .uri("/ip")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json(objectMapper.writeValueAsString(expectedResponse));

        verify(ipAccessPort).getAllIpAccess();
    }

    @Test
    public void should_return_empty_response_on_getting_all_ip_accesses() {

        when(ipAccessPort.getAllIpAccess()).thenReturn(Flux.empty());
        webTestClient.get()
                .uri("/ip")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        verify(ipAccessPort).getAllIpAccess();
    }

    @Test
    public void should_handle_add_new_ip_request() {
        var request = """
                {
                    "userTelegramId": 123456,
                    "ipAddress": "192.168.20.58"
                }
                """;

        when(ipAccessPort.createNewIpAccess(any()))
                .thenReturn(Mono.just(TestDataGenerator.createIpAccess("test")));

        webTestClient.put()
                .uri("/ip")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated();

        verify(ipAccessPort).createNewIpAccess(any());
    }

    @Test
    public void should_return_bad_request_on_add_new_ip_request() {
        var request = """
                {
                    "somethingStrange": 123456,
                    "ipAddress": "192.168.20.58"
                }
                """;

        webTestClient.put()
                .uri("/ip")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

        verify(ipAccessPort, never()).createNewIpAccess(any());
    }

    @Test
    public void should_return_bad_request_on_bad_ip_address_on_add_new_ip_request() {
        var request = """
                {
                    "userTelegramId": 123456,
                    "ipAddress": "192.1268.20.58"
                }
                """;

        webTestClient.put()
                .uri("/ip")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

        verify(ipAccessPort, never()).createNewIpAccess(any());
    }

    @Test
    public void should_return_500_if_user_not_found() {
        var request = """
                {
                    "userTelegramId": 123456,
                    "ipAddress": "192.168.20.58"
                }
                """;

        when(ipAccessPort.createNewIpAccess(any())).thenReturn(
                Mono.error(new UserNotFoundException())
        );

        webTestClient.put()
                .uri("/ip")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    public void should_handle_modify_ip_info() {
        var request = """
                {
                    "ip": "192.168.20.58",
                    "isActive": "true"
                }
                """;

        when(ipAccessPort.modifyIpAccessInfo(any(), any())).thenReturn(
                Mono.just(TestDataGenerator.createIpAccess("test"))
        );

        webTestClient.patch()
                .uri("/ip/testId")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        verify(ipAccessPort).modifyIpAccessInfo(any(), eq("testId"));
    }

    @Test
    public void should_throw_error_on_bad_request_on_modify_ip_access() {
        var request = """
                {
                    "ip": "1912.168.20.58",
                    "isActive": "true"
                }
                """;

        webTestClient.put()
                .uri("/ip/testId")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

        verify(ipAccessPort, never()).modifyIpAccessInfo(any(), eq("testId"));
    }

    @Test
    public void should_return_404_on_ip_access_not_found_on_modify_ip_access() {
        var request = """
                {
                    "ip": "192.168.20.58",
                    "isActive": "true"
                }
                """;

        when(ipAccessPort.modifyIpAccessInfo(any(), any())).thenReturn(
                Mono.error(new IpAccessNotFoundException())
        );

        webTestClient.patch()
                .uri("/ip/testId")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void should_add_ip_access_for_user_by_telegram_id() {
        when(ipAccessPort.addIpAccessForUserByTelegramId(any(), any()))
                .thenReturn(Mono.just(TestDataGenerator.createIpAccess("test")));

        webTestClient.get()
                .uri("/activate/123456789")
                .header("X-Real-IP", "192.168.20.58")
                .exchange()
                .expectStatus().isOk();

        verify(ipAccessPort).addIpAccessForUserByTelegramId(eq(123456789L), eq("192.168.20.58"));
    }

    @Test
    public void should_return_500_if_no_ip_header_on_add_ip_access_for_user_by_telegram_id() {
        webTestClient.get()
                .uri("/activate/123456789")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    public void should_return_500_if_user_has_active_ip_on_add_ip_access_for_user_by_telegram_id() {
        when(ipAccessPort.addIpAccessForUserByTelegramId(any(), any()))
                .thenReturn(Mono.error(new UserHasActiveIpException()));

        webTestClient.get()
                .uri("/activate/123456789")
                .header("X-Real-IP", "192.168.20.58")
                .exchange()
                .expectStatus().is5xxServerError();

        verify(ipAccessPort).addIpAccessForUserByTelegramId(eq(123456789L), eq("192.168.20.58"));
    }

    @Test
    public void should_return_500_if_user_is_not_active_on_add_ip_access_for_user_by_telegram_id() {
        when(ipAccessPort.addIpAccessForUserByTelegramId(any(), any()))
                .thenReturn(Mono.error(new UserIsNotActivatedException()));

        webTestClient.get()
                .uri("/activate/123456789")
                .header("X-Real-IP", "192.168.20.58")
                .exchange()
                .expectStatus().is5xxServerError();

        verify(ipAccessPort).addIpAccessForUserByTelegramId(eq(123456789L), eq("192.168.20.58"));
    }

    @Test
    public void should_return_500_if_user_is_not_found_on_add_ip_access_for_user_by_telegram_id() {
        when(ipAccessPort.addIpAccessForUserByTelegramId(any(), any()))
                .thenReturn(Mono.error(new UserIsNotActivatedException()));

        webTestClient.get()
                .uri("/activate/123456789")
                .header("X-Real-IP", "192.168.20.58")
                .exchange()
                .expectStatus().is5xxServerError();

        verify(ipAccessPort).addIpAccessForUserByTelegramId(eq(123456789L), eq("192.168.20.58"));
    }
}
