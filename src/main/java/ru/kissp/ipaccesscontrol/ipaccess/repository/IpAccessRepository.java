package ru.kissp.ipaccesscontrol.ipaccess.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.kissp.ipaccesscontrol.ipaccess.domain.IpAccess;

public interface IpAccessRepository extends ReactiveCrudRepository<IpAccess, String> {
    Flux<IpAccess> findAllByIpAndIsActive(String ip, Boolean isActive);
}
