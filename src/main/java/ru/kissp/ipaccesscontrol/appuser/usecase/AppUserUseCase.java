package ru.kissp.ipaccesscontrol.appuser.usecase;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.appuser.adapter.dto.CreateNewUserRequest;
import ru.kissp.ipaccesscontrol.appuser.domain.AppUser;
import ru.kissp.ipaccesscontrol.appuser.port.AppUserPort;
import ru.kissp.ipaccesscontrol.appuser.repository.AppUserRepository;
import ru.kissp.ipaccesscontrol.appuser.usecase.exceptions.UserAlreadyExistsException;
import ru.kissp.ipaccesscontrol.appuser.usecase.exceptions.UserNotFoundException;
import ru.kissp.ipaccesscontrol.telegram.port.TelegramPort;


@Component
@RequiredArgsConstructor
public class AppUserUseCase implements AppUserPort {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AppUserRepository appUserRepository;
    private final TelegramPort telegramPort;

    @Override
    public Mono<AppUser> createUser(CreateNewUserRequest createNewUserRequest) {
        logger.info("Got request for create new user {}", createNewUserRequest);
        return appUserRepository.findByTelegramId(createNewUserRequest.getTelegramId())
                .hasElement()
                .flatMap(userWithTelegramIdExist -> {
                    if (userWithTelegramIdExist) {
                        return Mono.error(new UserAlreadyExistsException());
                    } else {
                        return appUserRepository.save(createNewUserRequest.toDomain());
                    }
                })
                .doOnNext(savedUser -> logger.info("Created user {} for request {}", savedUser, createNewUserRequest))
                .doOnError(UserAlreadyExistsException.class::isInstance, err ->
                        logger.error("User with telegram id {} already exist", createNewUserRequest.getTelegramId()));
    }

    @Override
    public Mono<AppUser> activateUser(String userId) {
        logger.info("Trying to activate user with id {}", userId);
        return appUserRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException()))
                .flatMap(appUser -> appUserRepository.save(appUser.getActivatedUser()))
                .flatMap(savedUser -> Mono.defer(() -> {
                    logger.info("User {} is activated!", savedUser);
                    return telegramPort.notifyUserActivated(savedUser.getTelegramId().toString())
                            .then(Mono.just(savedUser));
                }))
                .doOnError(UserNotFoundException.class::isInstance, err -> logger.error("User with id {} not found", userId));
    }
}
