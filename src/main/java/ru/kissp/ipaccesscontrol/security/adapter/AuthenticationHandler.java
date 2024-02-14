package ru.kissp.ipaccesscontrol.security.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.security.adapter.dto.LoginRequest;
import ru.kissp.ipaccesscontrol.security.port.AuthenticationPort;

@Component
@RequiredArgsConstructor
public class AuthenticationHandler {
    private final AuthenticationPort authenticationPort;

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginRequest.class)
            .flatMap(loginRequest -> authenticationPort.authenticate(loginRequest, request))
            .then(ServerResponse.ok().build());
    }
}
