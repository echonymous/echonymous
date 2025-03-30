package com.echonymous.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ToggleEchoResultDTO {
    private boolean isEchoed;
    private int echoesCount;
}
