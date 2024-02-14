package ru.kissp.ipaccesscontrol.security.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.session.DefaultWebSessionManager;
import org.springframework.web.server.session.HeaderWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionManager;
import org.springframework.web.util.pattern.PathPatternParser;

import static org.springframework.web.server.adapter.WebHttpHandlerBuilder.WEB_SESSION_MANAGER_BEAN_NAME;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${security.user.name}")
    private String adminUserName;
    @Value("${security.user.password}")
    private String adminPassword;
    @Value("${security.cors.allowed-origin}")
    private String corsAllowedOrigin;


    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
        ServerHttpSecurity http,
        WebSessionServerSecurityContextRepository sessionRepository
    ) {
        http
            .httpBasic(Customizer.withDefaults())
            .securityContextRepository(sessionRepository)
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/activate/*").permitAll()
                .pathMatchers("/login").permitAll()
                .anyExchange().authenticated()
            )
            .csrf(ServerHttpSecurity.CsrfSpec::disable);
        return http.build();
    }

    @Bean
    CorsWebFilter corsFilter() {

        CorsConfiguration config = new CorsConfiguration();

        config.addAllowedOrigin(corsAllowedOrigin);
        config.setAllowCredentials(true);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addExposedHeader("SESSION");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }

    @Bean
    public WebSessionServerSecurityContextRepository sessionRepository() {
        return new WebSessionServerSecurityContextRepository();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        UserDetails user = User.builder()
            .username(adminUserName)
            .password(String.format("{noop}%s", adminPassword))
            .build();
        return new MapReactiveUserDetailsService(user);
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager(MapReactiveUserDetailsService userDetailsService) {
        return new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
    }

    @Bean(WEB_SESSION_MANAGER_BEAN_NAME)
    public WebSessionManager sessionManager() {
        var webSessionManager = new DefaultWebSessionManager();
        webSessionManager.setSessionIdResolver(new HeaderWebSessionIdResolver());
        return webSessionManager;
    }
}
