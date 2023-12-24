package ru.kissp.ipaccesscontrol.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document
@Data
@AllArgsConstructor
public class IpAccess {
    @Id
    private String id;
    private String ip;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String issuedFor;
}
