package com.hgn.sosalertservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_order")
@Getter
@Setter
@NoArgsConstructor
public class DeviceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "assigned_from", nullable = false)
    private LocalDateTime assignedFrom;

    @Column(name = "assigned_to", nullable = false)
    private LocalDateTime assignedTo;

    public DeviceOrder(Device device, Order order, LocalDateTime assignedFrom, LocalDateTime assignedTo) {
        this.device = device;
        this.order = order;
        this.assignedFrom = assignedFrom;
        this.assignedTo = assignedTo;
    }
}
