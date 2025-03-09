package com.echonymous.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseDTO {
    private int status;
    private boolean success;
    private String details;
    private String token;
    private Map<String, Object> responseData;

    public ApiResponseDTO(int status, boolean success, String details) {
        this.status = status;
        this.success = success;
        this.details = details;
    }
}
