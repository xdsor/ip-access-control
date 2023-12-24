package ru.kissp.ipaccesscontrol.ipaccess.adapter.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.appuser.usecase.exceptions.UserIsNotActivatedException;
import ru.kissp.ipaccesscontrol.appuser.usecase.exceptions.UserNotFoundException;
import ru.kissp.ipaccesscontrol.common.utils.ExceptionHandler;
import ru.kissp.ipaccesscontrol.ipaccess.usecase.exceptions.IpAccessNotFoundException;
import ru.kissp.ipaccesscontrol.ipaccess.usecase.exceptions.UserHasActiveIpException;

@Component
public class IpAccessExceptionHandler implements ExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Mono<ServerResponse> apply(Throwable throwable) {
        logger.error("Handling exception {} in the {}", throwable, this.getClass().getName());
        return switch (throwable) {
            case IpAccessNotFoundException ex -> ServerResponse.notFound().build();
            case UserNotFoundException ex -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .bodyValue("User not found");
            case UserHasActiveIpException ex -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .bodyValue("User has active IP");
            case UserIsNotActivatedException ex -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .bodyValue("User is not activated!");
            case ServerWebInputException ex -> ServerResponse.status(HttpStatus.BAD_REQUEST).build();
            default -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        };
    }
}
