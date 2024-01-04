package ru.kissp.ipaccesscontrol.ipaccess.usecase;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.appuser.repository.AppUserRepository;
import ru.kissp.ipaccesscontrol.appuser.usecase.exceptions.UserIsNotActivatedException;
import ru.kissp.ipaccesscontrol.appuser.usecase.exceptions.UserNotFoundException;
import ru.kissp.ipaccesscontrol.common.annotations.CrudMethod;
import ru.kissp.ipaccesscontrol.ipaccess.adapter.dto.CreateIpAccessDto;
import ru.kissp.ipaccesscontrol.ipaccess.adapter.dto.IpAccessDto;
import ru.kissp.ipaccesscontrol.ipaccess.adapter.dto.ModifyIpAccessDto;
import ru.kissp.ipaccesscontrol.ipaccess.domain.IpAccess;
import ru.kissp.ipaccesscontrol.ipaccess.port.IpAccessPort;
import ru.kissp.ipaccesscontrol.ipaccess.repository.IpAccessRepository;
import ru.kissp.ipaccesscontrol.ipaccess.usecase.exceptions.IpAccessNotFoundException;
import ru.kissp.ipaccesscontrol.ipaccess.usecase.exceptions.UserHasActiveIpException;
import ru.kissp.ipaccesscontrol.telegram.port.TelegramPort;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class IpAccessUseCase implements IpAccessPort {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final IpAccessRepository ipAccessRepository;
    private final AppUserRepository appUserRepository;
    private final TelegramPort telegramPort;

    @Override
    @CrudMethod
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
    @CrudMethod
    public Mono<IpAccess> createNewIpAccess(CreateIpAccessDto createIpAccessDto) {
        return appUserRepository.findByTelegramId(createIpAccessDto.getUserTelegramId())
                .doOnNext(appUser -> logger.info("Found user {} for create new Ip request {}", appUser, createIpAccessDto))
                .switchIfEmpty(Mono.error(new UserNotFoundException()))
                .map(appUser -> new IpAccess(
                        null,
                        createIpAccessDto.getIpAddress(),
                        true,
                        LocalDateTime.now(),
                        appUser.getId()
                )).flatMap(ipAccessRepository::save)
                .doOnNext(ipAccess -> logger.info("IP {} has saved for request {}", ipAccess, createIpAccessDto))
                .doOnError(UserNotFoundException.class::isInstance,
                        err -> logger.error(
                                "Error {} happened trying to get user by telegram id for request {}",
                                err,
                                createIpAccessDto
                        )
                );
    }

    @Override
    @CrudMethod
    public Mono<IpAccess> modifyIpAccessInfo(ModifyIpAccessDto modifyIpAccessDto, String ipAccessId) {
        logger.info("Got request for modifying ip access {} by id {}", modifyIpAccessDto, ipAccessId);
        return ipAccessRepository.findById(ipAccessId)
                .switchIfEmpty(Mono.error(new IpAccessNotFoundException()))
                .map(ipAccess -> new IpAccess(
                        ipAccess.getId(),
                        modifyIpAccessDto.getIp(),
                        modifyIpAccessDto.getIsActive(),
                        ipAccess.getCreatedAt(),
                        ipAccess.getIssuedFor()
                ))
                .flatMap(ipAccessRepository::save)
                .doOnError(IpAccessNotFoundException.class::isInstance, e -> logger.error("Ip access by id {} not found", ipAccessId));
    }

    @Override
    public Mono<IpAccess> addIpAccessForUserByTelegramId(Long telegramId, String ip) {
        return appUserRepository.findByTelegramId(telegramId)
                .flatMap(appUser -> {
                    if (appUser.getIsActive()) {
                        return ipAccessRepository.findAllByIpAndIsActive(ip, true)
                                .hasElements()
                                .flatMap(haveActiveIp -> {
                                    if (haveActiveIp) {
                                        return Mono.error(new UserHasActiveIpException());
                                    } else {
                                        return ipAccessRepository.save(new IpAccess(
                                                null,
                                                ip,
                                                true,
                                                LocalDateTime.now(),
                                                appUser.getId()
                                        ));
                                    }
                                })
                                .doOnNext(ipAccess -> logger.info("Created IP access {}", ipAccess))
                                .doOnError(UserHasActiveIpException.class::isInstance, err -> logger.error(
                                        "User with telegram id {} has active IP!", telegramId
                                ));
                    } else {
                        return Mono.error(new UserIsNotActivatedException());
                    }
                })
                .flatMap(ipAccess -> telegramPort.notifyUserIpActivated(telegramId.toString())
                        .then(Mono.just(ipAccess)))
                .switchIfEmpty(Mono.error(new UserNotFoundException()))
                .doOnError(UserNotFoundException.class::isInstance, err -> logger.error("User by telegram id {} not found", telegramId))
                .doOnError(UserIsNotActivatedException.class::isInstance, err -> logger.error("User by telegram id {} is deactivated", telegramId));
    }
}
