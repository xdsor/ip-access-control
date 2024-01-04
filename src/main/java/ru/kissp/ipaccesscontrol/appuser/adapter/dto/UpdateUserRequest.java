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
            this.userComment == null ? appUser.getUserComment() : this.userComment,
            this.name == null ? appUser.getName() : this.name,
            this.isActive == null ? appUser.getIsActive() : this.isActive
        );
    }
}
