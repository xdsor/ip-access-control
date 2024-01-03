package ru.kissp.ipaccesscontrol.telegram.adapter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendTelegramMessageDto {
    private String text;
    @JsonProperty("chat_id")
    private String chatId;
}
