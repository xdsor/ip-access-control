package ru.kissp.ipaccesscontrol.service;

import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.api.handler.dto.CreateNewUserRequest;
import ru.kissp.ipaccesscontrol.domain.AppUser;

public interface AppUserService {
    Mono<AppUser> createUser(CreateNewUserRequest createNewUserRequest);
}
