package ru.kissp.ipaccesscontrol.api.handler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IpCheckResponse {
    public Boolean allowed;
}
