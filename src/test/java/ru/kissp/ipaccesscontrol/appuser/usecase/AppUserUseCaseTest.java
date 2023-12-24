package ru.kissp.ipaccesscontrol.appuser.usecase;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.kissp.ipaccesscontrol.appuser.adapter.dto.CreateNewUserRequest;
import ru.kissp.ipaccesscontrol.appuser.domain.AppUser;
import ru.kissp.ipaccesscontrol.appuser.repository.AppUserRepository;
import ru.kissp.ipaccesscontrol.appuser.usecase.exceptions.UserAlreadyExistsException;
import ru.kissp.ipaccesscontrol.appuser.usecase.exceptions.UserNotFoundException;
import ru.kissp.ipaccesscontrol.telegram.port.TelegramPort;
import ru.kissp.ipaccesscontrol.utils.TestDataGenerator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class AppUserUseCaseTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private TelegramPort telegramPort;

    @InjectMocks
    private AppUserUseCase appUserUseCase;

    @Test
    public void should_create_new_user() {
        CreateNewUserRequest request = TestDataGenerator.createNewUserRequest();
        AppUser appUser = TestDataGenerator.createAppUser();

        when(appUserRepository.findByTelegramId(request.getTelegramId())).thenReturn(Mono.empty());
        when(appUserRepository.save(any(AppUser.class))).thenReturn(Mono.just(appUser));

        StepVerifier.create(appUserUseCase.createUser(request))
                .expectNext(appUser)
                .verifyComplete();
    }

    @Test
    public void should_throw_exception_on_create_if_user_already_exists() {
        CreateNewUserRequest request = TestDataGenerator.createNewUserRequest();
        AppUser appUser = TestDataGenerator.createAppUser();

        when(appUserRepository.findByTelegramId(request.getTelegramId())).thenReturn(Mono.just(appUser));

        StepVerifier.create(appUserUseCase.createUser(request))
                .expectError(UserAlreadyExistsException.class)
                .verify();
    }

    @Test
    public void should_activate_user() {
        AppUser appUser = TestDataGenerator.createAppUser(false);

        when(appUserRepository.findById(appUser.getId())).thenReturn(Mono.just(appUser));
        when(appUserRepository.save(appUser.getActivatedUser())).thenReturn(Mono.just(appUser.getActivatedUser()));
        when(telegramPort.notifyUserActivated(any(String.class))).thenReturn(Mono.just(""));

        StepVerifier.create(appUserUseCase.activateUser(appUser.getId()))
                .expectNext(appUser.getActivatedUser())
                .verifyComplete();

        verify(appUserRepository).save(appUser.getActivatedUser());
        verify(telegramPort).notifyUserActivated(appUser.getTelegramId().toString());
    }

    @Test
    public void should_not_activate_user_if_user_not_found() {
        AppUser appUser = TestDataGenerator.createAppUser(false);

        when(appUserRepository.findById(appUser.getId())).thenReturn(Mono.empty());

        StepVerifier.create(appUserUseCase.activateUser(appUser.getId()))
                .expectError(UserNotFoundException.class)
                .verify();

        verify(appUserRepository, never()).save(appUser.getActivatedUser());
        verify(telegramPort, never()).notifyUserActivated(appUser.getTelegramId().toString());
    }

}