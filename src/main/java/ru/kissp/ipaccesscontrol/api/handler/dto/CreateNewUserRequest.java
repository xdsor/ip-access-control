package ru.kissp.ipaccesscontrol.api.handler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kissp.ipaccesscontrol.domain.AppUser;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateNewUserRequest {
    private Long telegramId;
    private String name;

    public AppUser toDomain() {
        return new AppUser(
                null,
                this.telegramId,
                this.name,
                true
        );
    }
}
