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
public class DeviceOrderDto {
    private String deviceId;
    private String orderNumber;
    private LocalDateTime assignedFrom;
    private LocalDateTime assignedTo;
}
