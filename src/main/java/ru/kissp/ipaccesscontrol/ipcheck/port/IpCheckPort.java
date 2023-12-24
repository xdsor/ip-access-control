package ru.kissp.ipaccesscontrol.ipcheck.port;

import reactor.core.publisher.Mono;

public interface IpCheckPort {
    Mono<Boolean> checkIfHasAccess(String ip);
}
