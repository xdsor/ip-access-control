package ru.kissp.ipaccesscontrol.security.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.security.adapter.dto.LoginRequest;
import ru.kissp.ipaccesscontrol.security.port.AuthenticationPort;

@Component
@RequiredArgsConstructor
public class AuthenticationUseCase implements AuthenticationPort {
    private final ReactiveAuthenticationManager authenticationManager;
    private final WebSessionServerSecurityContextRepository securityContextRepository;

    @Override
    public Mono<Void> authenticate(LoginRequest loginRequest, ServerRequest serverRequest) {
        var authenticationToken = UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.username(), loginRequest.password());
        return authenticationManager.authenticate(authenticationToken).flatMap(authentication -> {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            return securityContextRepository.save(serverRequest.exchange(), context);
        });
    }
}
