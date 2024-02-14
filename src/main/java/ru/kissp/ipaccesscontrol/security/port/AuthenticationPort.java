package ru.kissp.ipaccesscontrol.security.port;

import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.security.adapter.dto.LoginRequest;

public interface AuthenticationPort {
    Mono<Void> authenticate(LoginRequest loginRequest, ServerRequest serverRequest);
}
