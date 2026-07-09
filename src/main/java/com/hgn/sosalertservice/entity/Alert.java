package com.hgn.sosalertservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert")
@Getter
@Setter
@NoArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "status", nullable = false)
    private String status = "NEW";

    @Column(name = "claimed_by")
    private String claimedBy;

    @Column(name = "urgent", nullable = false)
    private Boolean urgent = false;

    public Alert(Device device, Order order, Double latitude, Double longitude, LocalDateTime timestamp) {
        this.device = device;
        this.order = order;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }
}
