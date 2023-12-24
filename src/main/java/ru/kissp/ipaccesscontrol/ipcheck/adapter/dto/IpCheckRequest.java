package ru.kissp.ipaccesscontrol.ipcheck.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class IpCheckRequest {
    private String ip;
}
