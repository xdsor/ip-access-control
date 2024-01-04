package ru.kissp.ipaccesscontrol.appuser.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kissp.ipaccesscontrol.appuser.domain.AppUser;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    private String userComment;
    private String name;
    private Boolean isActive;

    public AppUser updateDomainUser(AppUser appUser) {
        return new AppUser(
            appUser.getId(),
            appUser.getTelegramId(),
            this.userComment,
            this.name,
            this.isActive
        );
    }
}
