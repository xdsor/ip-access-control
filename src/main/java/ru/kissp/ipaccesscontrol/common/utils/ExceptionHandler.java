package ru.kissp.ipaccesscontrol.common.utils;

import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Function;


public interface ExceptionHandler extends Function<Throwable, Mono<ServerResponse>> {
}
