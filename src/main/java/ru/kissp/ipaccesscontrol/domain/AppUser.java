package ru.kissp.ipaccesscontrol.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
@Data
@AllArgsConstructor
public class AppUser {
    @Id
    private String id;
    private Long telegramId;
    private String name;
    private Boolean isActive;
}
