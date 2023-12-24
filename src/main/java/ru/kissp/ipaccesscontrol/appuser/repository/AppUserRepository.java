package ru.kissp.ipaccesscontrol.appuser.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.appuser.domain.AppUser;

public interface AppUserRepository extends ReactiveCrudRepository<AppUser, String>  {
    Mono<AppUser> findByTelegramId(Long telegramId);
}
