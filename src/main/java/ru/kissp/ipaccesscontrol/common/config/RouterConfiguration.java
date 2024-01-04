package ru.kissp.ipaccesscontrol.common.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.kissp.ipaccesscontrol.ipcheck.adapter.IpCheckHandler;
import ru.kissp.ipaccesscontrol.ipaccess.adapter.handler.IpAccessHandler;
import ru.kissp.ipaccesscontrol.appuser.adapter.handler.AppUserHandler;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration(proxyBeanMethods = false)
public class RouterConfiguration {

    @Bean
    public RouterFunction<ServerResponse> route(
            IpCheckHandler ipCheckHandler,
            IpAccessHandler ipAccessHandler,
            AppUserHandler appUserHandler
    ) {

        return RouterFunctions
                .route()
                .POST("/check", ipCheckHandler::checkIfIpAllowed)
                .GET("/activate/{telegramId}", ipAccessHandler::addIpForUserByTelegramId)
                .path("/ip", builder -> builder
                        .GET(accept(APPLICATION_JSON), ipAccessHandler::getAllIpAccess)
                        .PATCH("/{id}", ipAccessHandler::modifyIpInfo)
                        .PUT(ipAccessHandler::addNewIp)
                )
                .path("/users", builder -> builder
                        .POST("/{id}/activate", appUserHandler::activateUser)
                        .PATCH("/{id}", appUserHandler::updateUser)
                        .PUT(appUserHandler::createNewUser)
                )
                .build();
    }
}
