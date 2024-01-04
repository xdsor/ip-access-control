package ru.kissp.ipaccesscontrol.appuser.adapter.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kissp.ipaccesscontrol.appuser.domain.AppUser;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateNewUserRequest {
    @NotNull
    private Long telegramId;
    @NotNull
    private String name;
    @NotNull
    private String userComment;

    public AppUser toDomain() {
        return new AppUser(
                null,
                this.telegramId,
                this.userComment,
                this.name,
                false,
                false
        );
    }
}
