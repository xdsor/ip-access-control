package ru.kissp.ipaccesscontrol.appuser.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@AllArgsConstructor
public class AppUser {
    @Id
    private final String id;
    private final Long telegramId;
    private final String userComment;
    private final String name;
    private final Boolean isActive;

    public AppUser getActivatedUser() {
        return new AppUser(
                this.getId(),
                this.getTelegramId(),
                this.getUserComment(),
                this.getName(),
                true
        );
    }
}
