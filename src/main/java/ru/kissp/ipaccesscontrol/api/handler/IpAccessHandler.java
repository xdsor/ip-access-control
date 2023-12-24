package ru.kissp.ipaccesscontrol.api.handler;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.api.handler.dto.CreateNewIpRequest;
import ru.kissp.ipaccesscontrol.api.handler.dto.ModifyIpAccessRequest;
import ru.kissp.ipaccesscontrol.service.IpAccessService;

import java.net.URI;
import java.util.logging.Logger;

@Configuration
@RequiredArgsConstructor
public class IpAccessHandler {
    private final IpAccessService ipAccessService;
    private final Logger logger = Logger.getLogger(this.getClass().getCanonicalName());

    public Mono<ServerResponse> getAllIpAccess(ServerRequest serverRequest) {
        return ipAccessService.getAllIpAccess()
                .collectList()
                .flatMap(list -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(list)
                );
    }

    public Mono<ServerResponse> addNewIp(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateNewIpRequest.class)
                .doOnNext(createNewIpRequest -> logger.info("Got create new IP request " + createNewIpRequest))
                .flatMap(ipAccessService::createNewIp)
                .flatMap(savedEntity -> ServerResponse.created(
                        URI.create(String.format("/ip/%s", savedEntity.getIp()))
                ).build())
                .switchIfEmpty(Mono.error(new RuntimeException("Nothing has saved")))
                .doOnError(err -> {
                    logger.warning("Nothing saved");
                });
    }

    public Mono<ServerResponse> modifyIpInfo(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(ModifyIpAccessRequest.class)
                .flatMap(modifyIpAccessRequest -> ipAccessService.modifyIpInfo(modifyIpAccessRequest, serverRequest.pathVariable("id")))
                .flatMap(updatedEntity -> ServerResponse.ok().build());
    }
}
