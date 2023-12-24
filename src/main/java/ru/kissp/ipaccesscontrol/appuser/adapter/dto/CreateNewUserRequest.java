package ru.kissp.ipaccesscontrol.appuser.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kissp.ipaccesscontrol.appuser.domain.AppUser;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateNewUserRequest {
    private Long telegramId;
    private String name;
    private String userComment;

    public AppUser toDomain() {
        return new AppUser(
                null,
                this.telegramId,
                this.userComment,
                this.name,
                false
        );
    }
}
