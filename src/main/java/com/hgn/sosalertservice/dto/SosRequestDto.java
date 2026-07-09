package com.hgn.sosalertservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SosRequestDto {
    private String deviceId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
}
