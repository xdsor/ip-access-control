package ru.kissp.ipaccesscontrol.api.handler;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;
import ru.kissp.ipaccesscontrol.api.handler.dto.IpCheckRequest;
import ru.kissp.ipaccesscontrol.api.handler.dto.IpCheckResponse;
import ru.kissp.ipaccesscontrol.service.IpAccessService;

import java.util.Objects;
import java.util.logging.Logger;

@Component
public class IpCheckHandler {

    private final IpAccessService ipAccessService;

    private final Logger logger = Logger.getLogger(this.getClass().getCanonicalName());

    public IpCheckHandler(IpAccessService ipAccessService) {
        this.ipAccessService = ipAccessService;
    }

    public Mono<ServerResponse> checkIfIpAllowed(ServerRequest request) {
        return request.bodyToMono(IpCheckRequest.class)
                .doOnNext(req -> logger.info(String.format("IP=%s wants to check if it can pass", req.getIp())))
                .flatMap(ipCheckRequest -> ipAccessService.checkIfHasAccess(ipCheckRequest.getIp())
                        .map(isIpAllowed -> {
                            logger.info(String.format("Check result for IP %s is %s", ipCheckRequest.getIp(), isIpAllowed));
                            return isIpAllowed;
                        }))
                .flatMap(isIpAllowed -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(new IpCheckResponse(isIpAllowed))
                );
    }
}
