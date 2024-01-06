package ru.kissp.ipaccesscontrol.appuser.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kissp.ipaccesscontrol.appuser.domain.AppUser;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDto {
    private String id;
    private Long telegramId;
    private String userComment;
    private String name;
    private Boolean isActive;
    private Boolean isApproved;

    public static UserDto fromDomain(AppUser appUser) {
        return new UserDto(
            appUser.getId(),
            appUser.getTelegramId(),
            appUser.getUserComment(),
            appUser.getName(),
            appUser.getIsActive(),
            appUser.getIsApproved()
        );
    }
}
