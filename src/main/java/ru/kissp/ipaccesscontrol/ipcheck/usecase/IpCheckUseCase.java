package ru.kissp.ipaccesscontrol.ipcheck.usecase;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.appuser.domain.AppUser;
import ru.kissp.ipaccesscontrol.appuser.repository.AppUserRepository;
import ru.kissp.ipaccesscontrol.ipaccess.repository.IpAccessRepository;
import ru.kissp.ipaccesscontrol.ipcheck.port.IpCheckPort;

@Component
@RequiredArgsConstructor
public class IpCheckUseCase implements IpCheckPort {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final IpAccessRepository ipAccessRepository;
    private final AppUserRepository appUserRepository;


    @Override
    public Mono<Boolean> checkIfHasAccess(String ip) {
        logger.info("Checking if ip {} can pass", ip);
        return ipAccessRepository.findAllByIpAndIsActive(ip, true)
                .flatMap(ipAccess -> appUserRepository.findById(ipAccess.getIssuedFor())
                        .map(AppUser::getIsActive)
                ).filter(Boolean::booleanValue)
                .hasElements()
                .doOnNext(canGrantAccessByIp -> logger.info("IP access check result is {}", canGrantAccessByIp));
    }
}
