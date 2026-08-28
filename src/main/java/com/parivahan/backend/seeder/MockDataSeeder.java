package com.parivahan.backend.seeder;

import com.parivahan.backend.challan.entity.Challan;
import com.parivahan.backend.challan.enums.ChallanStatus;
import com.parivahan.backend.challan.repository.ChallanRepository;
import com.parivahan.backend.fleet.domain.FleetAlert;
import com.parivahan.backend.fleet.domain.FleetRegistration;
import com.parivahan.backend.fleet.domain.FleetRoute;
import com.parivahan.backend.fleet.domain.FleetVehicle;
import com.parivahan.backend.fleet.enums.FleetAlertType;
import com.parivahan.backend.fleet.enums.FleetRouteStatus;
import com.parivahan.backend.fleet.enums.FleetStatus;
import com.parivahan.backend.fleet.repository.FleetAlertRepository;
import com.parivahan.backend.fleet.repository.FleetRegistrationRepository;
import com.parivahan.backend.fleet.repository.FleetRouteRepository;
import com.parivahan.backend.fleet.repository.FleetVehicleRepository;
import com.parivahan.backend.livelocation.entity.VehicleLocation;
import com.parivahan.backend.livelocation.repository.VehicleLocationRepository;
import com.parivahan.backend.user.domain.Role;
import com.parivahan.backend.user.domain.User;
import com.parivahan.backend.user.repository.UserRepository;
import com.parivahan.backend.vehicle.domain.RcRegistry;
import com.parivahan.backend.vehicle.domain.Vehicle;
import com.parivahan.backend.vehicle.enums.VehicleStatus;
import com.parivahan.backend.vehicle.repository.RcRegistryRepository;
import com.parivahan.backend.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Seeds the database with mock data for development/testing.
 * Checks per-email so it can safely run on a non-empty database
 * and still add new test users if they are missing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MockDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final RcRegistryRepository rcRegistryRepository;
    private final ChallanRepository challanRepository;
    private final VehicleLocationRepository vehicleLocationRepository;
    private final PasswordEncoder passwordEncoder;
    private final FleetRegistrationRepository fleetRegistrationRepository;
    private final FleetVehicleRepository fleetVehicleRepository;
    private final FleetRouteRepository fleetRouteRepository;
    private final FleetAlertRepository fleetAlertRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== Running MockDataSeeder ===");
        seedJohnDoe();
        seedSinha();
        seedPaula();
        seedRcRegistry();
        seedFleet();
        seedFleetPaula();
        log.info("=== MockDataSeeder complete ===");
    }

    // johndoe@example.com — original dev user
    private void seedJohnDoe() {
        if (userRepository.findByEmail("johndoe@example.com").isPresent()) return;

        User john = userRepository.save(User.builder()
                .fullName("John Doe")
                .email("johndoe@example.com")
                .mobileNumber("9876543210")
                .password(passwordEncoder.encode("password123"))
                .preferredLanguage("English")
                .role(Role.CITIZEN)
                .build());
        log.info("Seeded: johndoe@example.com / password123");

        Vehicle nexon = vehicleRepository.save(Vehicle.builder()
                .registrationNumber("MH12AB1234").nickname("My Nexon")
                .manufacturer("Tata Motors").model("Nexon").vehicleClass("SUV").fuelType("PETROL")
                .registrationDate("2021-08-15").rto("PUNE RTO")
                .insuranceProvider("HDFC ERGO")
                .vehicleImageUrl("https://imgd.aeplcdn.com/664x374/n/cw/ec/141867/nexon-exterior-right-front-three-quarter-6.jpeg")
                .insuranceValidTill(LocalDate.now().plusMonths(5))
                .pucValidTill(LocalDate.now().plusDays(17))
                .taxValidTill(LocalDate.now().plusMonths(8))
                .vehicleStatus(VehicleStatus.ACTIVE).user(john).build());

        vehicleRepository.save(Vehicle.builder()
                .registrationNumber("DL01CA5678").nickname("Delhi Creta")
                .manufacturer("Hyundai").model("Creta").vehicleClass("SUV").fuelType("DIESEL")
                .registrationDate("2022-01-10").rto("DELHI RTO")
                .insuranceProvider("Bajaj Allianz")
                .vehicleImageUrl("https://imgd.aeplcdn.com/664x374/n/cw/ec/19009/hyundai-creta-right-front-three-quarter.jpeg")
                .insuranceValidTill(LocalDate.now().plusMonths(10))
                .pucValidTill(LocalDate.now().plusMonths(6))
                .taxValidTill(LocalDate.now().plusMonths(14))
                .vehicleStatus(VehicleStatus.ACTIVE).user(john).build());

        challanRepository.save(Challan.builder()
                .vehicle(nexon).offence("Signal Jump")
                .amount(new BigDecimal("1000.00"))
                .challanDate(LocalDate.now().minusDays(30))
                .status(ChallanStatus.PENDING).build());

        vehicleLocationRepository.save(VehicleLocation.builder()
                .vehicle(nexon).latitude(18.5204).longitude(73.8567)
                .speed(46.0).heading("North").address("MG Road, Pune, Maharashtra")
                .lastUpdated(LocalDateTime.now().minusMinutes(2)).build());
    }

    // sinhaarijit368@gmail.com — full dashboard test user WITH vehicles
    private void seedSinha() {
        if (userRepository.findByEmail("sinhaarijit368@gmail.com").isPresent()) return;

        User sinha = userRepository.save(User.builder()
                .fullName("Arijit Sinha")
                .email("sinhaarijit368@gmail.com")
                .mobileNumber("9812345678")
                .password(passwordEncoder.encode("password123"))
                .preferredLanguage("English")
                .role(Role.CITIZEN)
                .build());
        log.info("Seeded: sinhaarijit368@gmail.com / password123");

        Vehicle creta = vehicleRepository.save(Vehicle.builder()
                .registrationNumber("WB12AB1234").nickname("My Creta")
                .manufacturer("Hyundai").model("Creta").vehicleClass("SUV").fuelType("PETROL")
                .registrationDate("2022-09-11").rto("KOLKATA (WB-12)")
                .insuranceProvider("HDFC ERGO")
                .vehicleImageUrl("https://imgd.aeplcdn.com/664x374/n/cw/ec/19009/hyundai-creta-right-front-three-quarter.jpeg")
                .insuranceValidTill(LocalDate.now().plusMonths(7))
                .pucValidTill(LocalDate.now().plusDays(17))
                .taxValidTill(LocalDate.now().plusMonths(9))
                .vehicleStatus(VehicleStatus.ACTIVE).user(sinha).build());

        challanRepository.save(Challan.builder()
                .vehicle(creta).offence("Signal Jump")
                .amount(new BigDecimal("1000.00"))
                .challanDate(LocalDate.now().minusDays(30))
                .status(ChallanStatus.PENDING).build());

        vehicleLocationRepository.save(VehicleLocation.builder()
                .vehicle(creta).latitude(22.5726).longitude(88.3639)
                .speed(46.0).heading("North").address("Park Street, Kolkata, West Bengal")
                .lastUpdated(LocalDateTime.now().minusMinutes(2)).build());

        Vehicle nexon = vehicleRepository.save(Vehicle.builder()
                .registrationNumber("WB02ZA5678").nickname("Nexon EV")
                .manufacturer("Tata Motors").model("Nexon EV").vehicleClass("SUV").fuelType("ELECTRIC")
                .registrationDate("2023-03-15").rto("KOLKATA (WB-02)")
                .insuranceProvider("Bajaj Allianz")
                .vehicleImageUrl("https://imgd.aeplcdn.com/664x374/n/cw/ec/141867/nexon-exterior-right-front-three-quarter-6.jpeg")
                .insuranceValidTill(LocalDate.now().plusMonths(14))
                .pucValidTill(LocalDate.now().plusMonths(10))
                .taxValidTill(LocalDate.now().plusMonths(18))
                .vehicleStatus(VehicleStatus.ACTIVE).user(sinha).build());

        vehicleLocationRepository.save(VehicleLocation.builder()
                .vehicle(nexon).latitude(22.5958).longitude(88.4028)
                .speed(0.0).heading("Parked").address("Salt Lake Sector V, Kolkata, West Bengal")
                .lastUpdated(LocalDateTime.now().minusHours(3)).build());
    }

    // paularijit368@gmail.com — primary test user with full mock data
    private void seedPaula() {
        User paula = userRepository.findByEmail("paularijit368@gmail.com").orElseGet(() -> {
            User newUser = userRepository.save(User.builder()
                    .fullName("Arijit Paul")
                    .email("paularijit368@gmail.com")
                    .mobileNumber("9000012345")
                    .password(passwordEncoder.encode("password123"))
                    .preferredLanguage("English")
                    .role(Role.CITIZEN)
                    .build());
            log.info("Seeded: paularijit368@gmail.com / password123");
            return newUser;
        });

        // --- Vehicle 1: Tata Nexon — insurance expiring soon, diverse challans ---
        Vehicle nexon = vehicleRepository.findByRegistrationNumber("KA03MN4567").orElseGet(() -> {
            Vehicle v = vehicleRepository.save(Vehicle.builder()
                    .registrationNumber("KA03MN4567").nickname("My Nexon")
                    .manufacturer("Tata Motors").model("Nexon").vehicleClass("SUV").fuelType("PETROL")
                    .registrationDate("2020-05-20").rto("BENGALURU (KA-03)")
                    .insuranceProvider("HDFC ERGO")
                    .vehicleImageUrl("https://imgd.aeplcdn.com/664x374/n/cw/ec/141867/nexon-exterior-right-front-three-quarter-6.jpeg")
                    .insuranceValidTill(LocalDate.now().plusDays(20))
                    .pucValidTill(LocalDate.now().plusMonths(4))
                    .taxValidTill(LocalDate.now().plusMonths(11))
                    .vehicleStatus(VehicleStatus.ACTIVE).user(paula).build());
                    
            vehicleLocationRepository.save(VehicleLocation.builder()
                    .vehicle(v).latitude(12.9352).longitude(77.6245)
                    .speed(55.0).heading("East").address("Koramangala, Bengaluru, Karnataka")
                    .lastUpdated(LocalDateTime.now().minusMinutes(8)).build());
            log.info("Seeded Nexon for paularijit368@gmail.com");
            return v;
        });

        if (challanRepository.findPendingByVehicleId(nexon.getId()).isEmpty() && 
            challanRepository.findAllByUserId(paula.getId()).stream().noneMatch(c -> c.getVehicle().getId().equals(nexon.getId()))) {
            // Pending challan
            challanRepository.save(Challan.builder()
                    .vehicle(nexon).offence("Over Speeding")
                    .amount(new BigDecimal("2000.00"))
                    .challanDate(LocalDate.now().minusDays(15))
                    .status(ChallanStatus.PENDING).build());

            // Overdue challan (challanDate > 30 days ago, never paid)
            challanRepository.save(Challan.builder()
                    .vehicle(nexon).offence("Signal Jumping")
                    .amount(new BigDecimal("1500.00"))
                    .challanDate(LocalDate.now().minusDays(45))
                    .status(ChallanStatus.PENDING).build());

            // Paid challan
            challanRepository.save(Challan.builder()
                    .vehicle(nexon).offence("No Parking")
                    .amount(new BigDecimal("500.00"))
                    .challanDate(LocalDate.now().minusDays(60))
                    .status(ChallanStatus.PAID)
                    .paymentDate(LocalDate.now().minusDays(55))
                    .transactionId("TXN-NEXON001").build());

            // Disputed challan
            challanRepository.save(Challan.builder()
                    .vehicle(nexon).offence("Wrong Parking")
                    .amount(new BigDecimal("1000.00"))
                    .challanDate(LocalDate.now().minusDays(20))
                    .status(ChallanStatus.DISPUTED).build());

            log.info("Seeded diverse challans for Nexon");
        }

        // --- Vehicle 2: Honda City — all docs healthy, mix of challans ---
        Vehicle city = vehicleRepository.findByRegistrationNumber("KA01PQ7890").orElseGet(() -> {
            Vehicle v = vehicleRepository.save(Vehicle.builder()
                    .registrationNumber("KA01PQ7890").nickname("City Cruiser")
                    .manufacturer("Honda").model("City").vehicleClass("SEDAN").fuelType("PETROL")
                    .registrationDate("2021-11-10").rto("BENGALURU (KA-01)")
                    .insuranceProvider("Bajaj Allianz")
                    .vehicleImageUrl("https://imgd.aeplcdn.com/664x374/n/cw/ec/27074/city-exterior-right-front-three-quarter-2.jpeg")
                    .insuranceValidTill(LocalDate.now().plusMonths(11))
                    .pucValidTill(LocalDate.now().plusMonths(8))
                    .taxValidTill(LocalDate.now().plusMonths(16))
                    .vehicleStatus(VehicleStatus.ACTIVE).user(paula).build());
                    
            vehicleLocationRepository.save(VehicleLocation.builder()
                    .vehicle(v).latitude(12.9767).longitude(77.5713)
                    .speed(0.0).heading("Parked").address("Indiranagar, Bengaluru, Karnataka")
                    .lastUpdated(LocalDateTime.now().minusHours(2)).build());
            log.info("Seeded City for paularijit368@gmail.com");
            return v;
        });

        if (challanRepository.findPendingByVehicleId(city.getId()).isEmpty() && 
            challanRepository.findAllByUserId(paula.getId()).stream().noneMatch(c -> c.getVehicle().getId().equals(city.getId()))) {
            // Pending challan
            challanRepository.save(Challan.builder()
                    .vehicle(city).offence("No Helmet")
                    .amount(new BigDecimal("500.00"))
                    .challanDate(LocalDate.now().minusDays(5))
                    .status(ChallanStatus.PENDING).build());

            // Paid challan
            challanRepository.save(Challan.builder()
                    .vehicle(city).offence("Triple Riding")
                    .amount(new BigDecimal("1000.00"))
                    .challanDate(LocalDate.now().minusDays(90))
                    .status(ChallanStatus.PAID)
                    .paymentDate(LocalDate.now().minusDays(85))
                    .transactionId("TXN-CITY001").build());

            log.info("Seeded diverse challans for City");
        }
    }

    // RC Registry entries for vehicle registration flow
    private void seedRcRegistry() {
        saveRcIfAbsent("MH12AB1234", "John Doe",     "9876543210", "Tata Motors",   "Nexon",    "SUV",       "PETROL",   "2021-08-15", "PUNE RTO");
        saveRcIfAbsent("DL01CA5678", "John Doe",     "9876543210", "Hyundai",       "Creta",    "SUV",       "DIESEL",   "2022-01-10", "DELHI RTO");
        saveRcIfAbsent("WB12AB1234", "Arijit Sinha", "9812345678", "Hyundai",       "Creta",    "SUV",       "PETROL",   "2022-09-11", "KOLKATA (WB-12)");
        saveRcIfAbsent("WB02ZA5678", "Arijit Sinha", "9812345678", "Tata Motors",   "Nexon EV", "SUV",       "ELECTRIC", "2023-03-15", "KOLKATA (WB-02)");
        saveRcIfAbsent("KA03MN4567", "Arijit Paul",  "9000012345", "Tata Motors",   "Nexon",    "SUV",       "PETROL",   "2020-05-20", "BENGALURU (KA-03)");
        saveRcIfAbsent("KA01PQ7890", "Arijit Paul",  "9000012345", "Honda",         "City",     "SEDAN",     "PETROL",   "2021-11-10", "BENGALURU (KA-01)");
        saveRcIfAbsent("KA01XY9999", "John Doe",     "9876543210", "Maruti Suzuki", "Swift",    "HATCHBACK", "PETROL",   "2023-03-20", "BENGALURU RTO");
        saveRcIfAbsent("MH02ZZ1111", "John Doe",     "9876543210", "Honda",         "City",     "SEDAN",     "PETROL",   "2020-11-05", "MUMBAI RTO");
    }

    private void saveRcIfAbsent(String regNum, String ownerName, String ownerMobile,
                                 String manufacturer, String model, String vehicleClass,
                                 String fuelType, String regDate, String rto) {
        if (rcRegistryRepository.findByRegistrationNumber(regNum).isPresent()) return;
        rcRegistryRepository.save(RcRegistry.builder()
                .registrationNumber(regNum).ownerName(ownerName).ownerMobile(ownerMobile)
                .manufacturer(manufacturer).model(model).vehicleClass(vehicleClass)
                .fuelType(fuelType).registrationDate(regDate).rto(rto).build());
    }

    // -----------------------------------------------------------------------
    // Fleet seed — approved fleet for sinhaarijit368@gmail.com
    // -----------------------------------------------------------------------
    private void seedFleet() {
        if (fleetRegistrationRepository.findAll().stream()
                .anyMatch(f -> "FLT-2026-001245".equals(f.getFleetRegistrationNumber()))) return;

        User sinha = userRepository.findByEmail("sinhaarijit368@gmail.com").orElse(null);
        if (sinha == null) return;

        // Get existing seeded vehicles for sinha
        Vehicle wb12 = vehicleRepository.findByRegistrationNumber("WB12AB1234").orElse(null);
        Vehicle wb02 = vehicleRepository.findByRegistrationNumber("WB02ZA5678").orElse(null);

        // Seed a third vehicle for the fleet (GPS offline scenario)
        Vehicle wb34 = vehicleRepository.findByRegistrationNumber("WB34CD5678").orElseGet(() -> {
            Vehicle v = vehicleRepository.save(Vehicle.builder()
                    .registrationNumber("WB34CD5678").nickname("Fleet Truck 3")
                    .manufacturer("Tata Motors").model("Ace Gold").vehicleClass("GOODS CARRIER").fuelType("DIESEL")
                    .registrationDate("2023-06-10").rto("KOLKATA (WB-34)")
                    .insuranceProvider("New India Assurance")
                    .insuranceValidTill(LocalDate.now().plusDays(5)) // expiring soon!
                    .pucValidTill(LocalDate.now().plusMonths(3))
                    .taxValidTill(LocalDate.now().plusMonths(8))
                    .vehicleStatus(VehicleStatus.ACTIVE).user(sinha).build());
            // GPS offline — last updated 15 min ago (triggers GPS offline alert)
            vehicleLocationRepository.save(VehicleLocation.builder()
                    .vehicle(v).latitude(22.4700).longitude(88.3300)
                    .speed(0.0).heading("Parked").address("Jadavpur, Kolkata, West Bengal")
                    .lastUpdated(LocalDateTime.now().minusMinutes(15)).build());
            saveRcIfAbsent("WB34CD5678", "Arijit Sinha", "9812345678",
                    "Tata Motors", "Ace Gold", "GOODS CARRIER", "DIESEL", "2023-06-10", "KOLKATA (WB-34)");
            log.info("Seeded WB34CD5678 for fleet");
            return v;
        });

        if (wb12 == null || wb02 == null) return;

        // Update WB02ZA5678 GPS to simulate route deviation (moved off corridor)
        vehicleLocationRepository.findByVehicleId(wb02.getId()).ifPresent(loc -> {
            // Simulate vehicle deviating 2km off route (Kolkata→Durgapur corridor)
            loc.setLatitude(22.6500); // moved significantly off the corridor
            loc.setLongitude(88.1000);
            loc.setSpeed(72.0);
            loc.setHeading("North-West");
            loc.setAddress("Serampore, Hooghly, West Bengal");
            loc.setLastUpdated(LocalDateTime.now().minusMinutes(2));
            vehicleLocationRepository.save(loc);
        });

        // Create approved fleet
        FleetRegistration fleet = fleetRegistrationRepository.save(FleetRegistration.builder()
                .owner(sinha)
                .fleetName("Sinha Transports")
                .fleetRegistrationNumber("FLT-2026-001245")
                .vehicleRegistrationNumber("WB12AB1234")
                .status(FleetStatus.APPROVED)
                .document1Base64("MOCK_DOC_1")
                .document2Base64("MOCK_DOC_2")
                .businessProofBase64("MOCK_BUSINESS_PROOF")
                .build());

        // Add 3 vehicles to fleet
        fleetVehicleRepository.save(FleetVehicle.builder()
                .fleet(fleet).vehicle(wb12).active(true).addedAt(LocalDateTime.now().minusDays(30)).build());
        fleetVehicleRepository.save(FleetVehicle.builder()
                .fleet(fleet).vehicle(wb02).active(true).addedAt(LocalDateTime.now().minusDays(25)).build());
        fleetVehicleRepository.save(FleetVehicle.builder()
                .fleet(fleet).vehicle(wb34).active(true).addedAt(LocalDateTime.now().minusDays(20)).build());

        // Active route: WB12AB1234 — Kolkata → Durgapur (on-route)
        fleetRouteRepository.save(FleetRoute.builder()
                .fleet(fleet).vehicle(wb12)
                .startLocation("Kolkata").destination("Durgapur")
                .startLat(22.5726).startLng(88.3639) // Kolkata
                .destLat(23.5204).destLng(87.3119)   // Durgapur
                .toleranceMeters(5000) // 5km for demo so WB12 shows ON-ROUTE
                .routeStatus(FleetRouteStatus.ACTIVE)
                .startedAt(LocalDateTime.now().minusHours(2))
                .build());

        // Active route: WB02ZA5678 — Kolkata → Asansol (with deviation)
        FleetRoute deviatingRoute = fleetRouteRepository.save(FleetRoute.builder()
                .fleet(fleet).vehicle(wb02)
                .startLocation("Kolkata").destination("Asansol")
                .startLat(22.5726).startLng(88.3639)  // Kolkata
                .destLat(23.6850).destLng(86.9820)    // Asansol
                .toleranceMeters(500)
                .routeStatus(FleetRouteStatus.ACTIVE)
                .startedAt(LocalDateTime.now().minusHours(1))
                .build());

        // Pre-seed fleet alerts
        // 1. Route deviation for WB02ZA5678
        fleetAlertRepository.save(FleetAlert.builder()
                .fleet(fleet).vehicle(wb02)
                .alertType(FleetAlertType.ROUTE_DEVIATION)
                .message("Vehicle WB02ZA5678 has deviated from assigned route Kolkata → Asansol. Detected near Serampore, Hooghly.")
                .status("OPEN")
                .lastTriggeredAt(LocalDateTime.now().minusMinutes(3))
                .build());

        // 2. GPS offline for WB34CD5678
        fleetAlertRepository.save(FleetAlert.builder()
                .fleet(fleet).vehicle(wb34)
                .alertType(FleetAlertType.GPS_OFFLINE)
                .message("Vehicle WB34CD5678 GPS offline. Last seen 15 minutes ago near Jadavpur, Kolkata.")
                .status("OPEN")
                .lastTriggeredAt(LocalDateTime.now().minusMinutes(5))
                .build());

        log.info("Seeded Fleet 'Sinha Transports' FLT-2026-001245 for sinhaarijit368@gmail.com");
    }

    // -----------------------------------------------------------------------
    // Fleet seed — approved fleet for paularijit368@gmail.com
    // -----------------------------------------------------------------------
    private void seedFleetPaula() {
        if (fleetRegistrationRepository.findAll().stream()
                .anyMatch(f -> "FLT-2026-009988".equals(f.getFleetRegistrationNumber()))) return;

        User paula = userRepository.findByEmail("paularijit368@gmail.com").orElse(null);
        if (paula == null) return;

        // Get existing seeded vehicles for paula
        Vehicle nexon = vehicleRepository.findByRegistrationNumber("KA03MN4567").orElse(null);
        Vehicle city = vehicleRepository.findByRegistrationNumber("KA01PQ7890").orElse(null);

        // Seed a third vehicle for the fleet (GPS offline scenario)
        Vehicle ace = vehicleRepository.findByRegistrationNumber("KA04XY9876").orElseGet(() -> {
            Vehicle v = vehicleRepository.save(Vehicle.builder()
                    .registrationNumber("KA04XY9876").nickname("Delivery Ace")
                    .manufacturer("Tata Motors").model("Ace Gold").vehicleClass("GOODS CARRIER").fuelType("DIESEL")
                    .registrationDate("2023-08-15").rto("BENGALURU (KA-04)")
                    .insuranceProvider("ICICI Lombard")
                    .insuranceValidTill(LocalDate.now().plusMonths(5))
                    .pucValidTill(LocalDate.now().plusMonths(3))
                    .taxValidTill(LocalDate.now().plusMonths(12))
                    .vehicleStatus(VehicleStatus.ACTIVE).user(paula).build());
            // GPS offline — last updated 20 min ago
            vehicleLocationRepository.save(VehicleLocation.builder()
                    .vehicle(v).latitude(12.9716).longitude(77.5946)
                    .speed(0.0).heading("Parked").address("Majestic, Bengaluru, Karnataka")
                    .lastUpdated(LocalDateTime.now().minusMinutes(20)).build());
            saveRcIfAbsent("KA04XY9876", "Arijit Paul", "9000012345",
                    "Tata Motors", "Ace Gold", "GOODS CARRIER", "DIESEL", "2023-08-15", "BENGALURU (KA-04)");
            log.info("Seeded KA04XY9876 for fleet");
            return v;
        });

        if (nexon == null || city == null) return;

        // Update Honda City GPS to simulate route deviation (moved off corridor)
        vehicleLocationRepository.findByVehicleId(city.getId()).ifPresent(loc -> {
            // Simulate vehicle deviating off route (Bengaluru→Mysuru corridor)
            loc.setLatitude(12.7200); // Moved off the expected route
            loc.setLongitude(77.2800);
            loc.setSpeed(60.0);
            loc.setHeading("South-West");
            loc.setAddress("Ramanagara, Karnataka");
            loc.setLastUpdated(LocalDateTime.now().minusMinutes(3));
            vehicleLocationRepository.save(loc);
        });

        // Create approved fleet
        FleetRegistration fleet = fleetRegistrationRepository.save(FleetRegistration.builder()
                .owner(paula)
                .fleetName("Paul Fleet Services")
                .fleetRegistrationNumber("FLT-2026-009988")
                .vehicleRegistrationNumber("KA03MN4567")
                .status(FleetStatus.APPROVED)
                .document1Base64("MOCK_DOC_1")
                .document2Base64("MOCK_DOC_2")
                .businessProofBase64("MOCK_BUSINESS_PROOF")
                .build());

        // Add 3 vehicles to fleet
        fleetVehicleRepository.save(FleetVehicle.builder()
                .fleet(fleet).vehicle(nexon).active(true).addedAt(LocalDateTime.now().minusDays(15)).build());
        fleetVehicleRepository.save(FleetVehicle.builder()
                .fleet(fleet).vehicle(city).active(true).addedAt(LocalDateTime.now().minusDays(10)).build());
        fleetVehicleRepository.save(FleetVehicle.builder()
                .fleet(fleet).vehicle(ace).active(true).addedAt(LocalDateTime.now().minusDays(5)).build());

        // Active route: KA03MN4567 (Nexon) — Bengaluru → Hosur (on-route)
        fleetRouteRepository.save(FleetRoute.builder()
                .fleet(fleet).vehicle(nexon)
                .startLocation("Bengaluru").destination("Hosur")
                .startLat(12.9716).startLng(77.5946) // Bengaluru
                .destLat(12.7409).destLng(77.8253)   // Hosur
                .toleranceMeters(5000)
                .routeStatus(FleetRouteStatus.ACTIVE)
                .startedAt(LocalDateTime.now().minusHours(1))
                .build());

        // Active route: KA01PQ7890 (City) — Bengaluru → Mysuru (with deviation)
        fleetRouteRepository.save(FleetRoute.builder()
                .fleet(fleet).vehicle(city)
                .startLocation("Bengaluru").destination("Mysuru")
                .startLat(12.9716).startLng(77.5946)  // Bengaluru
                .destLat(12.2958).destLng(76.6394)    // Mysuru
                .toleranceMeters(500)
                .routeStatus(FleetRouteStatus.ACTIVE)
                .startedAt(LocalDateTime.now().minusHours(2))
                .build());

        // Pre-seed fleet alerts
        // 1. Route deviation for City
        fleetAlertRepository.save(FleetAlert.builder()
                .fleet(fleet).vehicle(city)
                .alertType(FleetAlertType.ROUTE_DEVIATION)
                .message("Vehicle KA01PQ7890 has deviated from assigned route Bengaluru → Mysuru. Detected near Ramanagara.")
                .status("OPEN")
                .lastTriggeredAt(LocalDateTime.now().minusMinutes(5))
                .build());

        // 2. GPS offline for Ace
        fleetAlertRepository.save(FleetAlert.builder()
                .fleet(fleet).vehicle(ace)
                .alertType(FleetAlertType.GPS_OFFLINE)
                .message("Vehicle KA04XY9876 GPS offline. Last seen 20 minutes ago near Majestic, Bengaluru.")
                .status("OPEN")
                .lastTriggeredAt(LocalDateTime.now().minusMinutes(10))
                .build());

        log.info("Seeded Fleet 'Paul Fleet Services' FLT-2026-009988 for paularijit368@gmail.com");
    }
}
