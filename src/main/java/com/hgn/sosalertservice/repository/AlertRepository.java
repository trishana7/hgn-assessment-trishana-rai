package com.hgn.sosalertservice.repository;

import com.hgn.sosalertservice.entity.Alert;
import com.hgn.sosalertservice.entity.Device;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    @Query("SELECT a FROM Alert a " +
           "WHERE a.device = :device " +
           "AND a.timestamp BETWEEN :startTime AND :endTime " +
           "AND a.status <> 'CLAIMED'")
    List<Alert> findDuplicateCandidates(
            @Param("device") Device device,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Alert a WHERE a.id = :id")
    Optional<Alert> findWithLockById(@Param("id") Long id);

    @Query("SELECT a FROM Alert a WHERE a.status = 'NEW' AND a.timestamp < :thresholdTime")
    List<Alert> findNewAlertsOlderThan(@Param("thresholdTime") LocalDateTime thresholdTime);
}
