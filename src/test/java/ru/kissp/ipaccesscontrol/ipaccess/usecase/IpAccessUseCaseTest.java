package ru.kissp.ipaccesscontrol.ipaccess.usecase;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.kissp.ipaccesscontrol.appuser.repository.AppUserRepository;
import ru.kissp.ipaccesscontrol.appuser.usecase.exceptions.UserIsNotActivatedException;
import ru.kissp.ipaccesscontrol.appuser.usecase.exceptions.UserNotFoundException;
import ru.kissp.ipaccesscontrol.ipaccess.adapter.dto.CreateIpAccessDto;
import ru.kissp.ipaccesscontrol.ipaccess.adapter.dto.IpAccessDto;
import ru.kissp.ipaccesscontrol.ipaccess.adapter.dto.ModifyIpAccessDto;
import ru.kissp.ipaccesscontrol.ipaccess.domain.IpAccess;
import ru.kissp.ipaccesscontrol.ipaccess.repository.IpAccessRepository;
import ru.kissp.ipaccesscontrol.ipaccess.usecase.exceptions.IpAccessNotFoundException;
import ru.kissp.ipaccesscontrol.ipaccess.usecase.exceptions.UserHasActiveIpException;
import ru.kissp.ipaccesscontrol.telegram.port.TelegramPort;
import ru.kissp.ipaccesscontrol.utils.TestDataGenerator;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class IpAccessUseCaseTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private IpAccessRepository ipAccessRepository;

    @Mock
    private TelegramPort telegramPort;

    @InjectMocks
    private IpAccessUseCase ipAccessUseCase;

    @Test
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void should_get_all_ip_access() {
        var users = IntStream.range(0, 10).mapToObj(x -> TestDataGenerator.createAppUser()).toList();
        var ipAccesses = users.stream().map(user -> TestDataGenerator.createIpAccess(user.getId())).toList();
        when(ipAccessRepository.findAll()).thenReturn(Flux.fromIterable(ipAccesses));
        ipAccesses.forEach(ipAccess -> when(appUserRepository.findById(ipAccess.getIssuedFor()))
                .thenReturn(Mono.just(
                        users.stream()
                                .filter((user) -> user.getId().equals(ipAccess.getIssuedFor()))
                                .findFirst()
                                .get()
                        )
                )
        );

        var verifier = StepVerifier.create(ipAccessUseCase.getAllIpAccess());
        ipAccesses.forEach(ipAccess -> {
            var issuedFor = users.stream()
                    .filter((user) -> user.getId().equals(ipAccess.getIssuedFor()))
                    .findFirst()
                    .get();

            verifier.expectNext(new IpAccessDto(
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
            ));
        });

        verifier.expectComplete().verify();
    }

    @Test
    public void should_create_new_ip_access() {
        var fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0);
        var appUser = TestDataGenerator.createAppUser();
        var userIp = "192.168.21.58";
        var createdIpAccess = new IpAccess(null, userIp, true, fixedTime, appUser.getId());

        when(appUserRepository.findByTelegramId(appUser.getTelegramId())).thenReturn(Mono.just(appUser));
        when(ipAccessRepository.save(any())).thenReturn(Mono.just(createdIpAccess));

        try (MockedStatic<LocalDateTime> mocked = Mockito.mockStatic(LocalDateTime.class)) {
            mocked.when(LocalDateTime::now).thenReturn(fixedTime);

            StepVerifier.create(ipAccessUseCase.createNewIpAccess(new CreateIpAccessDto(appUser.getTelegramId(), userIp)))
                    .expectNext(createdIpAccess)
                    .verifyComplete();
        }

        verify(ipAccessRepository).save(createdIpAccess);
    }

    @Test
    public void should_throw_error_if_no_user_on_create_new_ip_access() {
        var fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0);
        var appUser = TestDataGenerator.createAppUser();
        var userIp = "192.168.20.58";
        var createdIpAccess = new IpAccess(null, userIp, true, fixedTime, appUser.getId());

        when(appUserRepository.findByTelegramId(appUser.getTelegramId())).thenReturn(Mono.empty());

        StepVerifier.create(ipAccessUseCase.createNewIpAccess(new CreateIpAccessDto(appUser.getTelegramId(), userIp)))
                .expectError(UserNotFoundException.class)
                .verify();

        verify(ipAccessRepository, never()).save(createdIpAccess);
    }

    @Test
    public void should_modify_ip_info() {
        var ipAccess = TestDataGenerator.createIpAccess("testId");
        var request = new ModifyIpAccessDto(ipAccess.getIp(), !ipAccess.getIsActive());
        var modifiedIpAccess = new IpAccess(
                ipAccess.getId(),
                ipAccess.getIp(),
                !ipAccess.getIsActive(),
                ipAccess.getCreatedAt(),
                ipAccess.getIssuedFor()
        );
        var savedIpAccessCaptor = ArgumentCaptor.forClass(IpAccess.class);

        when(ipAccessRepository.findById(ipAccess.getId())).thenReturn(Mono.just(ipAccess));
        when(ipAccessRepository.save(savedIpAccessCaptor.capture())).thenReturn(Mono.just(modifiedIpAccess));

        StepVerifier.create(ipAccessUseCase.modifyIpAccessInfo(request, ipAccess.getId()))
                .expectNext(modifiedIpAccess)
                .verifyComplete();

        assertNotNull(savedIpAccessCaptor.getValue());
        assertEquals(!ipAccess.getIsActive(), savedIpAccessCaptor.getValue().getIsActive());
    }

    @Test
    public void should_throw_error_on_modify_ip_info_if_ip_info_not_found() {
        var ipAccess = TestDataGenerator.createIpAccess("testId");
        var request = new ModifyIpAccessDto(ipAccess.getIp(), !ipAccess.getIsActive());
        when(ipAccessRepository.findById(ipAccess.getId())).thenReturn(Mono.empty());

        StepVerifier.create(ipAccessUseCase.modifyIpAccessInfo(request, ipAccess.getId()))
                .expectError(IpAccessNotFoundException.class)
                .verify();
    }

    @Test
    public void should_add_ip_for_user_by_telegram_id() {
        var user = TestDataGenerator.createAppUser();
        var ipAccess = TestDataGenerator.createIpAccess(user.getId());

        when(appUserRepository.findByTelegramId(user.getTelegramId())).thenReturn(Mono.just(user));
        when(ipAccessRepository.findAllByIpAndIsActive(ipAccess.getIp(), true)).thenReturn(Flux.empty());
        when(ipAccessRepository.save(any())).thenReturn(Mono.just(ipAccess));
        when(telegramPort.notifyUserIpActivated(user.getTelegramId().toString())).thenReturn(Mono.just("some response"));

        StepVerifier.create(ipAccessUseCase.addIpAccessForUserByTelegramId(user.getTelegramId(), ipAccess.getIp()))
                .expectNext(ipAccess)
                .verifyComplete();

        verify(telegramPort, times(1)).notifyUserIpActivated(user.getTelegramId().toString());
    }

    @Test
    public void should_throw_exception_on_add_ip_for_user_by_telegram_id_if_user_not_found() {
        var user = TestDataGenerator.createAppUser();
        var ipAccess = TestDataGenerator.createIpAccess(user.getId());

        when(appUserRepository.findByTelegramId(user.getTelegramId())).thenReturn(Mono.empty());

        StepVerifier.create(ipAccessUseCase.addIpAccessForUserByTelegramId(user.getTelegramId(), ipAccess.getIp()))
                .expectError(UserNotFoundException.class)
                .verify();

        verify(telegramPort, never()).notifyUserIpActivated(user.getTelegramId().toString());
    }

    @Test
    public void should_throw_exception_on_add_ip_for_user_by_telegram_id_if_user_has_access() {
        var user = TestDataGenerator.createAppUser();
        var ipAccess = TestDataGenerator.createIpAccess(user.getId());

        when(appUserRepository.findByTelegramId(user.getTelegramId())).thenReturn(Mono.just(user));
        when(ipAccessRepository.findAllByIpAndIsActive(ipAccess.getIp(), true)).thenReturn(Flux.just(ipAccess));

        StepVerifier.create(ipAccessUseCase.addIpAccessForUserByTelegramId(user.getTelegramId(), ipAccess.getIp()))
                .expectError(UserHasActiveIpException.class)
                .verify();

        verify(telegramPort, never()).notifyUserIpActivated(user.getTelegramId().toString());
    }

    @Test
    public void should_throw_exception_on_add_ip_for_user_by_telegram_id_if_user_deactivated() {
        var user = TestDataGenerator.createAppUser(false);
        var ipAccess = TestDataGenerator.createIpAccess(user.getId());

        when(appUserRepository.findByTelegramId(user.getTelegramId())).thenReturn(Mono.just(user));

        StepVerifier.create(ipAccessUseCase.addIpAccessForUserByTelegramId(user.getTelegramId(), ipAccess.getIp()))
                .expectError(UserIsNotActivatedException.class)
                .verify();

        verify(telegramPort, never()).notifyUserIpActivated(user.getTelegramId().toString());
    }


}