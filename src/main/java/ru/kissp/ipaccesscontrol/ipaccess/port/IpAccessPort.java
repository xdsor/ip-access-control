package ru.kissp.ipaccesscontrol.ipaccess.port;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.ipaccess.adapter.dto.CreateIpAccessDto;
import ru.kissp.ipaccesscontrol.ipaccess.adapter.dto.IpAccessDto;
import ru.kissp.ipaccesscontrol.ipaccess.adapter.dto.ModifyIpAccessDto;
import ru.kissp.ipaccesscontrol.ipaccess.domain.IpAccess;

public interface IpAccessPort {
    Flux<IpAccessDto> getAllIpAccess();
    Mono<IpAccess> createNewIpAccess(CreateIpAccessDto createIpAccessDto);

    Mono<IpAccess> modifyIpAccessInfo(ModifyIpAccessDto modifyIpAccessDto, String ipAccessId);
    Mono<IpAccess> addIpAccessForUserByTelegramId(Long telegramId, String ip);
}
