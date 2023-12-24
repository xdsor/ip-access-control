package ru.kissp.ipaccesscontrol.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.domain.AppUser;

public interface AppUserRepository extends ReactiveCrudRepository<AppUser, String>  {
    Mono<AppUser> findByTelegramId(Long telegramId);
}
