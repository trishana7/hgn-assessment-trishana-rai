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
public class OrderDto {
    private String orderNumber;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private List<String> groupMembers;
}
