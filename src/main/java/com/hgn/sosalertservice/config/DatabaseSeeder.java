package com.hgn.sosalertservice.config;

import com.hgn.sosalertservice.entity.*;
import com.hgn.sosalertservice.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final DeviceRepository deviceRepository;
    private final OrderRepository orderRepository;
    private final DeviceOrderRepository deviceOrderRepository;

    public DatabaseSeeder(DeviceRepository deviceRepository,
                          OrderRepository orderRepository,
                          DeviceOrderRepository deviceOrderRepository) {
        this.deviceRepository = deviceRepository;
        this.orderRepository = orderRepository;
        this.deviceOrderRepository = deviceOrderRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (deviceRepository.count() == 0) {
            logger.info("Initializing database with test data...");

            // 1. Create Devices
            Device device1 = new Device("GPS-001", "Garmin", "ACTIVE");
            Device device2 = new Device("GPS-002", "Spot", "ACTIVE");
            deviceRepository.saveAll(List.of(device1, device2));

            // 2. Create Orders
            Order order1 = new Order("TREK001", 
                    LocalDateTime.of(2026, 7, 1, 0, 0), 
                    LocalDateTime.of(2026, 7, 10, 23, 59, 59), 
                    "ACTIVE");
            
            Order order2 = new Order("TREK002", 
                    LocalDateTime.of(2026, 7, 15, 0, 0), 
                    LocalDateTime.of(2026, 7, 25, 23, 59, 59), 
                    "ACTIVE");

            // 3. Create Group Members
            GroupMember member1 = new GroupMember("Alice Smith", order1);
            GroupMember member2 = new GroupMember("Bob Jones", order1);
            order1.getGroupMembers().addAll(List.of(member1, member2));

            GroupMember member3 = new GroupMember("Charlie Brown", order2);
            order2.getGroupMembers().add(member3);

            orderRepository.saveAll(List.of(order1, order2));

            // 4. Create Device Orders Mappings
            DeviceOrder deviceOrder1 = new DeviceOrder(device1, order1, 
                    LocalDateTime.of(2026, 7, 1, 0, 0), 
                    LocalDateTime.of(2026, 7, 10, 23, 59, 59));

            DeviceOrder deviceOrder2 = new DeviceOrder(device2, order2, 
                    LocalDateTime.of(2026, 7, 15, 0, 0), 
                    LocalDateTime.of(2026, 7, 25, 23, 59, 59));

            deviceOrderRepository.saveAll(List.of(deviceOrder1, deviceOrder2));

            logger.info("Database seeding completed successfully. Seeded 2 devices, 2 orders, and 2 device mappings.");
        } else {
            logger.info("Database already contains data. Skipping seeder.");
        }
    }
}
