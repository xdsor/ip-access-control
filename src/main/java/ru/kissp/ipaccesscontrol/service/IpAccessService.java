package ru.kissp.ipaccesscontrol.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.api.handler.dto.CreateNewIpRequest;
import ru.kissp.ipaccesscontrol.api.handler.dto.IpAccessDto;
import ru.kissp.ipaccesscontrol.api.handler.dto.ModifyIpAccessRequest;
import ru.kissp.ipaccesscontrol.domain.IpAccess;

public interface IpAccessService {
    Mono<Boolean> checkIfHasAccess(String ip);
    Flux<IpAccessDto> getAllIpAccess();
    Mono<IpAccess> createNewIp(CreateNewIpRequest createNewIpRequest);
    Mono<IpAccess> modifyIpInfo(ModifyIpAccessRequest modifyIpAccessRequest, String ipAccessId);
}
