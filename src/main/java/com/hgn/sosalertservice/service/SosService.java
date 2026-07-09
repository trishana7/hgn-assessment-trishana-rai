package com.hgn.sosalertservice.service;

import com.hgn.sosalertservice.dto.*;
import com.hgn.sosalertservice.entity.*;
import org.springframework.web.server.ResponseStatusException;
import com.hgn.sosalertservice.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SosService {

    private static final Logger logger = LoggerFactory.getLogger(SosService.class);

    private final DeviceRepository deviceRepository;
    private final DeviceOrderRepository deviceOrderRepository;
    private final AlertRepository alertRepository;
    private final OrderRepository orderRepository;

    public SosService(DeviceRepository deviceRepository,
                      DeviceOrderRepository deviceOrderRepository,
                      AlertRepository alertRepository,
                      OrderRepository orderRepository) {
        this.deviceRepository = deviceRepository;
        this.deviceOrderRepository = deviceOrderRepository;
        this.alertRepository = alertRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public AlertResponseDto receiveSos(SosRequestDto request) {
        String deviceId = request.getDeviceId();
        LocalDateTime timestamp = request.getTimestamp();

        // 1. Find the device
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found with deviceId: " + deviceId));

        // 2. Find the active order
        Order order = deviceOrderRepository.findActiveOrderByDeviceAndTimestamp(device, timestamp)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No active booking found for device " + deviceId + " at " + timestamp));

        // 3. Check for duplicates (within 2 minutes, same device, and not claimed)
        LocalDateTime startTime = timestamp.minusMinutes(2);
        LocalDateTime endTime = timestamp.plusMinutes(2);
        List<Alert> duplicateCandidates = alertRepository.findDuplicateCandidates(device, startTime, endTime);

        if (!duplicateCandidates.isEmpty()) {
            Alert existingAlert = duplicateCandidates.get(0);
            logger.info("Deduplicated SOS message for device {}. Returning existing alert ID {}", deviceId, existingAlert.getId());
            return convertToResponse(existingAlert);
        }

        // 4. Save new Alert
        Alert alert = new Alert(device, order, request.getLatitude(), request.getLongitude(), timestamp);
        Alert savedAlert = alertRepository.save(alert);
        logger.info("Saved new SOS alert for device {} associated with order {}. Alert ID {}", 
                deviceId, order.getOrderNumber(), savedAlert.getId());

        return convertToResponse(savedAlert);
    }

    @Transactional
    public AlertResponseDto claimAlert(Long alertId, String coordinator) {
        logger.info("Coordinator {} is attempting to claim alert ID {}", coordinator, alertId);

        // Fetch and lock the alert row
        Alert alert = alertRepository.findWithLockById(alertId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found with ID: " + alertId));

        // Check if already claimed
        if ("CLAIMED".equals(alert.getStatus())) {
            logger.warn("Alert ID {} was already claimed by {}", alertId, alert.getClaimedBy());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Alert with ID " + alertId + " is already claimed by " + alert.getClaimedBy());
        }

        // Update the alert status and coordinator
        alert.setStatus("CLAIMED");
        alert.setClaimedBy(coordinator);

        Alert savedAlert = alertRepository.save(alert);
        logger.info("Alert ID {} successfully claimed by {}", alertId, coordinator);

        return convertToResponse(savedAlert);
    }

    public List<AlertResponseDto> getAllAlerts() {
        return alertRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public AlertResponseDto getAlertById(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found with ID: " + alertId));
        return convertToResponse(alert);
    }

    private AlertResponseDto convertToResponse(Alert alert) {
        AlertResponseDto response = new AlertResponseDto();
        response.setId(alert.getId());
        response.setDeviceId(alert.getDevice().getDeviceId());
        response.setDeviceName(alert.getDevice().getName());
        if (alert.getOrder() != null) {
            response.setOrderId(alert.getOrder().getId());
            response.setOrderNumber(alert.getOrder().getOrderNumber());
            List<GroupMemberResponseDto> members = alert.getOrder().getGroupMembers().stream()
                    .map(m -> new GroupMemberResponseDto(m.getId(), m.getName()))
                    .collect(Collectors.toList());
            response.setGroupMembers(members);
        }
        response.setLatitude(alert.getLatitude());
        response.setLongitude(alert.getLongitude());
        response.setTimestamp(alert.getTimestamp());
        response.setStatus(alert.getStatus());
        response.setClaimedBy(alert.getClaimedBy());
        response.setUrgent(alert.getUrgent());
        return response;
    }

    @Transactional
    public Device createDevice(DeviceDto dto) {
        if (deviceRepository.findByDeviceId(dto.getDeviceId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Device with ID " + dto.getDeviceId() + " already exists");
        }
        Device device = new Device(dto.getDeviceId(), dto.getName(), dto.getStatus());
        return deviceRepository.save(device);
    }

    @Transactional
    public Order createOrder(OrderDto dto) {
        if (orderRepository.findByOrderNumber(dto.getOrderNumber()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order with number " + dto.getOrderNumber() + " already exists");
        }
        Order order = new Order(dto.getOrderNumber(), dto.getStartDate(), dto.getEndDate(), dto.getStatus());
        if (dto.getGroupMembers() != null) {
            for (String name : dto.getGroupMembers()) {
                GroupMember member = new GroupMember(name, order);
                order.getGroupMembers().add(member);
            }
        }
        return orderRepository.save(order);
    }

    @Transactional
    public DeviceOrder createDeviceOrder(DeviceOrderDto dto) {
        Device device = deviceRepository.findByDeviceId(dto.getDeviceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found with ID: " + dto.getDeviceId()));
        Order order = orderRepository.findByOrderNumber(dto.getOrderNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with number: " + dto.getOrderNumber()));
        
        DeviceOrder deviceOrder = new DeviceOrder(device, order, dto.getAssignedFrom(), dto.getAssignedTo());
        return deviceOrderRepository.save(deviceOrder);
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<DeviceOrder> getAllDeviceOrders() {
        return deviceOrderRepository.findAll();
    }
}
