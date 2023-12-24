package ru.kissp.ipaccesscontrol.api.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.kissp.ipaccesscontrol.api.handler.IpCheckHandler;
import ru.kissp.ipaccesscontrol.api.handler.IpAccessHandler;
import ru.kissp.ipaccesscontrol.api.handler.UsersHandler;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration(proxyBeanMethods = false)
public class RouterConfiguration {

    @Bean
    public RouterFunction<ServerResponse> route(
            IpCheckHandler ipCheckHandler,
            IpAccessHandler ipAccessHandler,
            UsersHandler usersHandler
    ) {

        return RouterFunctions
                .route()
                .POST("/check", ipCheckHandler::checkIfIpAllowed)
                .path("/ip", builder -> builder
                        .GET(accept(APPLICATION_JSON), ipAccessHandler::getAllIpAccess)
                        .PUT("/{id}", ipAccessHandler::modifyIpInfo)
                        .PUT(ipAccessHandler::addNewIp)
                )
                .path("/users", builder -> builder
                        .POST(usersHandler::createNewUser))
                .build();
    }
}
