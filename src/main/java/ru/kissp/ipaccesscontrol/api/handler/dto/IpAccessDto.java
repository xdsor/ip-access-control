package ru.kissp.ipaccesscontrol.api.handler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IpAccessDto {
    private String id;
    private String ip;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private IpAccessIssuedFor issuedFor;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IpAccessIssuedFor {
        private String id;
        private Long telegramId;
        private String name;
        private Boolean isActive;
    }
}
