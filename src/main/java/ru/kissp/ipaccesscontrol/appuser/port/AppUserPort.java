package ru.kissp.ipaccesscontrol.appuser.port;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.kissp.ipaccesscontrol.appuser.adapter.dto.CreateNewUserRequest;
import ru.kissp.ipaccesscontrol.appuser.adapter.dto.UpdateUserRequest;
import ru.kissp.ipaccesscontrol.appuser.adapter.dto.UserDto;
import ru.kissp.ipaccesscontrol.appuser.domain.AppUser;

public interface AppUserPort {
    Mono<AppUser> createUser(CreateNewUserRequest createNewUserRequest);
    Mono<AppUser> activateUser(String userId);
    Mono<AppUser> updateUser(UpdateUserRequest updateUserRequest, String userId);
    Mono<UserDto> getUserById(String userId);
    Flux<UserDto> getAllUsers();
}
