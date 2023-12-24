package ru.kissp.ipaccesscontrol.ipcheck.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IpCheckResponse {
    public Boolean allowed;
}
