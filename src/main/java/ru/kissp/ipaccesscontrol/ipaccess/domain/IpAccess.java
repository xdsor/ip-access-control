package ru.kissp.ipaccesscontrol.ipaccess.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@AllArgsConstructor
public class IpAccess {
    @Id
    private final String id;
    private final String ip;
    private final Boolean isActive;
    private final LocalDateTime createdAt;
    private final String issuedFor;
}
