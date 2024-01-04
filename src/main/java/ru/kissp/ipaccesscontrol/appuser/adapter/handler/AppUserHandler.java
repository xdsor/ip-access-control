package ru.kissp.ipaccesscontrol.appuser.adapter.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.appuser.adapter.dto.CreateNewUserRequest;
import ru.kissp.ipaccesscontrol.appuser.adapter.dto.UpdateUserRequest;
import ru.kissp.ipaccesscontrol.appuser.port.AppUserPort;

import static ru.kissp.ipaccesscontrol.common.utils.ValidationUtils.validate;

@Component
@RequiredArgsConstructor
public class AppUserHandler {
    private final AppUserPort appUserPort;
    private final Validator validator;

    public Mono<ServerResponse> createNewUser(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateNewUserRequest.class)
                .flatMap(appUserPort::createUser)
                .flatMap(createdUser -> ServerResponse.created(
                    UriComponentsBuilder
                        .fromPath("users")
                        .path(createdUser.getId())
                        .build().toUri()
                ).build());
    }

    public Mono<ServerResponse> activateUser(ServerRequest serverRequest) {
        return Mono.just(serverRequest.pathVariable("id"))
                .flatMap(appUserPort::activateUser)
                .flatMap(createdUser -> ServerResponse.ok().build());
    }

    public Mono<ServerResponse> updateUser(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(UpdateUserRequest.class)
            .doOnNext(updateUserRequest -> validate(validator, updateUserRequest))
            .flatMap(updateUserRequest -> appUserPort.updateUser(updateUserRequest, serverRequest.pathVariable("id")))
            .flatMap(updatedUser -> ServerResponse.noContent().build());
    }
}
