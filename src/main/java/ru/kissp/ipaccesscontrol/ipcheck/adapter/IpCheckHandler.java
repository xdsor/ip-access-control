package ru.kissp.ipaccesscontrol.ipcheck.adapter;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.ipcheck.adapter.dto.IpCheckRequest;
import ru.kissp.ipaccesscontrol.ipcheck.adapter.dto.IpCheckResponse;
import ru.kissp.ipaccesscontrol.ipcheck.port.IpCheckPort;

@Component
@RequiredArgsConstructor
public class IpCheckHandler {

    private final IpCheckPort ipCheckPort;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    public Mono<ServerResponse> checkIfIpAllowed(ServerRequest request) {
        return request.bodyToMono(IpCheckRequest.class)
                .doOnNext(req -> logger.info("IP={} wants to check if it can pass", req.getIp()))
                .flatMap(ipCheckRequest -> ipCheckPort.checkIfHasAccess(ipCheckRequest.getIp()))
                .flatMap(isIpAllowed -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(new IpCheckResponse(isIpAllowed)));
    }
}
