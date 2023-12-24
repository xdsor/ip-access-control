package ru.kissp.ipaccesscontrol.api.handler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNewIpRequest {
    private Long userTelegramId;
    private String ipAddress;
}
