package ru.kissp.ipaccesscontrol.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.api.handler.dto.CreateNewIpRequest;
import ru.kissp.ipaccesscontrol.api.handler.dto.IpAccessDto;
import ru.kissp.ipaccesscontrol.api.handler.dto.ModifyIpAccessRequest;
import ru.kissp.ipaccesscontrol.domain.AppUser;
import ru.kissp.ipaccesscontrol.domain.IpAccess;
import ru.kissp.ipaccesscontrol.repository.AppUserRepository;
import ru.kissp.ipaccesscontrol.repository.IpAccessRepository;
import ru.kissp.ipaccesscontrol.service.IpAccessService;
import ru.kissp.ipaccesscontrol.service.impl.exceptions.IpAccessNotFoundException;

import java.time.LocalDateTime;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class IpAccessServiceImpl implements IpAccessService {
    private final Logger logger = Logger.getLogger(this.getClass().getCanonicalName());
    private final IpAccessRepository ipAccessRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public Mono<Boolean> checkIfHasAccess(String ip) {
        logger.info(String.format("Checking if ip %s can pass", ip));
        return ipAccessRepository.findAllByIpAndIsActive(ip, true)
                .flatMap(ipAccess -> appUserRepository.findById(ipAccess.getIssuedFor())
                        .map(AppUser::getIsActive)
                ).filter(Boolean::booleanValue)
                .hasElements();
    }

    @Override
    public Flux<IpAccessDto> getAllIpAccess() {
        return ipAccessRepository.findAll()
                .flatMap(ipAccess -> appUserRepository.findById(ipAccess.getIssuedFor())
                        .map(appUser -> new IpAccessDto(
                                ipAccess.getId(),
                                ipAccess.getIp(),
                                ipAccess.getIsActive(),
                                ipAccess.getCreatedAt(),
                                new IpAccessDto.IpAccessIssuedFor(
                                        appUser.getId(),
                                        appUser.getTelegramId(),
                                        appUser.getName(),
                                        appUser.getIsActive()
                                )
                        ))
                );
    }

    @Override
    public Mono<IpAccess> createNewIp(CreateNewIpRequest createNewIpRequest) {
        return appUserRepository.findByTelegramId(createNewIpRequest.getUserTelegramId())
                .doOnNext(appUser -> logger.info(String.format("Found user %s for create new Ip request %s", appUser, createNewIpRequest)))
                .switchIfEmpty(Mono.error(new RuntimeException()))
                .doOnError(err -> logger.warning(String.format("Error %s happened trying to get user for reqeust %s", err, createNewIpRequest)))
                .map(appUser -> new IpAccess(
                        null,
                        createNewIpRequest.getIpAddress(),
                        true,
                        LocalDateTime.now(),
                        appUser.getId()
                )).flatMap(ipAccessRepository::save)
                .doOnNext(ipAccess -> logger.info(String.format("IP %s has saved for request %s", ipAccess, createNewIpRequest)));
    }

    @Override
    public Mono<IpAccess> modifyIpInfo(ModifyIpAccessRequest modifyIpAccessRequest, String ipAccessId) {
        logger.info(String.format("Got request for modifying ip access %s by id %s", modifyIpAccessRequest, ipAccessId));
        return ipAccessRepository.findById(ipAccessId)
                .switchIfEmpty(Mono.error(new IpAccessNotFoundException()))
                .map(ipAccess -> new IpAccess(
                        ipAccess.getId(),
                        modifyIpAccessRequest.getIp(),
                        modifyIpAccessRequest.getIsActive(),
                        ipAccess.getCreatedAt(),
                        ipAccess.getIssuedFor()
                ))
                .flatMap(ipAccessRepository::save)
                .doOnError(IpAccessNotFoundException.class::isInstance, e -> logger.warning(String.format("Ip access by id %s not found", ipAccessId)));
    }
}
