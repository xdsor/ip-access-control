package ru.kissp.ipaccesscontrol.ipaccess.adapter.handler;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.ipaccess.adapter.dto.CreateIpAccessDto;
import ru.kissp.ipaccesscontrol.ipaccess.adapter.dto.ModifyIpAccessDto;
import ru.kissp.ipaccesscontrol.ipaccess.port.IpAccessPort;

import java.net.URI;

import static ru.kissp.ipaccesscontrol.common.utils.ValidationUtils.validate;

@Configuration
@RequiredArgsConstructor
public class IpAccessHandler {
    private final IpAccessPort ipAccessPort;
    private final Validator validator;
    private final IpAccessExceptionHandler exceptionHandler;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Mono<ServerResponse> getAllIpAccess(ServerRequest serverRequest) {
        return ipAccessPort.getAllIpAccess()
                .collectList()
                .flatMap(list -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(list)
                );
    }

    public Mono<ServerResponse> addNewIp(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateIpAccessDto.class)
                .doOnNext(createIpAccessDto -> validate(validator, createIpAccessDto))
                .doOnNext(createIpAccessDto -> logger.info("Got create new IP request {}", createIpAccessDto))
                .flatMap(ipAccessPort::createNewIpAccess)
                .flatMap(savedEntity -> ServerResponse.created(
                        URI.create(String.format("/ip/%s", savedEntity.getIp()))
                ).build())
                .onErrorResume(exceptionHandler);
    }

    public Mono<ServerResponse> modifyIpInfo(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(ModifyIpAccessDto.class)
                .doOnNext(modifyIpAccessDto -> validate(validator, modifyIpAccessDto))
                .flatMap(modifyIpAccessDto -> ipAccessPort.modifyIpAccessInfo(modifyIpAccessDto, serverRequest.pathVariable("id")))
                .flatMap(updatedEntity -> ServerResponse.noContent().build())
                .onErrorResume(exceptionHandler);
    }

    public Mono<ServerResponse> addIpForUserByTelegramId(ServerRequest serverRequest) {
        return Mono.defer(() -> {
            var telegramId = serverRequest.pathVariable("telegramId");
            var ip = serverRequest.headers().firstHeader("X-Real-IP");
            if (ip == null) {
                logger.error("Missing X-Real-IP header");
                return Mono.error(new RuntimeException());
            }
            return ipAccessPort.addIpAccessForUserByTelegramId(Long.valueOf(telegramId), ip);
        }).flatMap(updatedEntity -> ServerResponse.ok().build())
                .onErrorResume(exceptionHandler);
    }
}
