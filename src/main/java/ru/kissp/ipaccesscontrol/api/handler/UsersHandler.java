package ru.kissp.ipaccesscontrol.api.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.api.handler.dto.CreateNewUserRequest;
import ru.kissp.ipaccesscontrol.service.AppUserService;

@Component
@RequiredArgsConstructor
public class UsersHandler {
    private final AppUserService appUserService;

    public Mono<ServerResponse> createNewUser(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateNewUserRequest.class)
                .flatMap(appUserService::createUser)
                .flatMap(createdUser -> ServerResponse.ok().build());
    }
}
