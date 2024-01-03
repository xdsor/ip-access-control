package ru.kissp.ipaccesscontrol.ipcheck.usecase;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.kissp.ipaccesscontrol.appuser.repository.AppUserRepository;
import ru.kissp.ipaccesscontrol.ipaccess.repository.IpAccessRepository;
import ru.kissp.ipaccesscontrol.utils.TestDataGenerator;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class IpCheckUseCaseTest {
    @Mock
    private IpAccessRepository ipAccessRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @InjectMocks
    private IpCheckUseCase ipCheckUseCase;

    @Test
    public void should_return_true_on_check() {
        var testIp = "192.168.20.58";
        var appUser = TestDataGenerator.createAppUser();
        var ipAccess = TestDataGenerator.createIpAccess(appUser.getId());

        when(ipAccessRepository.findAllByIpAndIsActive(testIp, true)).thenReturn(Flux.just(ipAccess));
        when(appUserRepository.findById(ipAccess.getIssuedFor())).thenReturn(Mono.just(appUser));

        StepVerifier.create(ipCheckUseCase.checkIfHasAccess(testIp))
                .expectNext(Boolean.TRUE)
                .verifyComplete();
    }

    @Test
    public void should_return_false_on_no_active_ip() {
        var testIp = "192.168.20.58";

        when(ipAccessRepository.findAllByIpAndIsActive(testIp, true)).thenReturn(Flux.empty());

        StepVerifier.create(ipCheckUseCase.checkIfHasAccess(testIp))
                .expectNext(Boolean.FALSE)
                .verifyComplete();
    }

    @Test
    public void should_return_false_on_user_is_not_active() {
        var testIp = "192.168.20.58";
        var appUser = TestDataGenerator.createAppUser(false);
        var ipAccess = TestDataGenerator.createIpAccess(appUser.getId());

        when(ipAccessRepository.findAllByIpAndIsActive(testIp, true)).thenReturn(Flux.just(ipAccess));
        when(appUserRepository.findById(ipAccess.getIssuedFor())).thenReturn(Mono.just(appUser));

        StepVerifier.create(ipCheckUseCase.checkIfHasAccess(testIp))
                .expectNext(Boolean.FALSE)
                .verifyComplete();
    }
}
