package ru.kissp.ipaccesscontrol.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.api.handler.dto.CreateNewUserRequest;
import ru.kissp.ipaccesscontrol.domain.AppUser;
import ru.kissp.ipaccesscontrol.repository.AppUserRepository;
import ru.kissp.ipaccesscontrol.service.AppUserService;

import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {
    private final Logger logger = Logger.getLogger(this.getClass().getCanonicalName());
    private final AppUserRepository appUserRepository;

    @Override
    public Mono<AppUser> createUser(CreateNewUserRequest createNewUserRequest) {
        logger.info(String.format("Got request for create new user %s", createNewUserRequest));
        return appUserRepository.save(createNewUserRequest.toDomain())
                .doOnNext(savedUser -> logger.info(String.format("Created user %s for request %s", savedUser, createNewUserRequest)));
    }
}
