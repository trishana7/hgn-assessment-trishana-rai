package com.hgn.sosalertservice.repository;

import com.hgn.sosalertservice.entity.Device;
import com.hgn.sosalertservice.entity.DeviceOrder;
import com.hgn.sosalertservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DeviceOrderRepository extends JpaRepository<DeviceOrder, Long> {

    @Query("SELECT do.order FROM DeviceOrder do " +
           "WHERE do.device = :device " +
           "AND :timestamp >= do.assignedFrom " +
           "AND :timestamp <= do.assignedTo")
    Optional<Order> findActiveOrderByDeviceAndTimestamp(
            @Param("device") Device device,
            @Param("timestamp") LocalDateTime timestamp
    );
}
