package com.hgn.sosalertservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "device")
@Getter
@Setter
@NoArgsConstructor
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", unique = true, nullable = false)
    private String deviceId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "status", nullable = false)
    private String status;

    public Device(String deviceId, String name, String status) {
        this.deviceId = deviceId;
        this.name = name;
        this.status = status;
    }
}
