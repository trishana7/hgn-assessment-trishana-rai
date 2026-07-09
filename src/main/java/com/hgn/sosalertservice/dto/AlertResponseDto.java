package com.hgn.sosalertservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponseDto {
    private Long id;
    private String deviceId;
    private String deviceName;
    private Long orderId;
    private String orderNumber;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
    private String status;
    private String claimedBy;
    private Boolean urgent;
    private List<GroupMemberResponseDto> groupMembers;
}
