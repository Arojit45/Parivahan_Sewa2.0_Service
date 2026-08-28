package com.parivahan.backend.drivinglicense.seeder;

import com.parivahan.backend.drivinglicense.entity.DrivingSchool;
import com.parivahan.backend.drivinglicense.repository.DrivingSchoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(10)
public class DrivingSchoolSeeder implements CommandLineRunner {

    private final DrivingSchoolRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info("Driving schools already seeded — skipping.");
            return;
        }

        List<DrivingSchool> schools = List.of(
            // Delhi
            school("Delhi Motor Training School", "Plot 14, Rohini Sector 7, New Delhi", "Delhi", "Delhi", "110085", "011-27051234", new BigDecimal("4.3"), true, "DL-MTS-001"),
            school("Capital Driving Academy", "Shop 3, Laxmi Nagar, New Delhi", "Delhi", "Delhi", "110092", "011-22451890", new BigDecimal("4.1"), false, "DL-CDA-002"),
            school("Saarthi Driving School", "Near Dwarka Sec 10 Metro, New Delhi", "Delhi", "Delhi", "110075", "9811234567", new BigDecimal("4.5"), true, "DL-SDS-003"),
            school("New India Motor Training School", "Pitam Pura, New Delhi", "Delhi", "Delhi", "110034", "9999012345", new BigDecimal("3.9"), false, "DL-NIM-004"),

            // Mumbai
            school("Mumbai Driving School", "SV Road, Andheri West, Mumbai", "Maharashtra", "Mumbai", "400058", "022-26281234", new BigDecimal("4.2"), true, "MH-MDS-001"),
            school("Thane Motor School", "Pokhran Road, Thane", "Maharashtra", "Thane", "400601", "022-25341567", new BigDecimal("4.0"), false, "MH-TMS-002"),
            school("Kohinoor Driving Academy", "Kurla West, Mumbai", "Maharashtra", "Mumbai", "400070", "9820123456", new BigDecimal("4.4"), true, "MH-KDA-003"),

            // Pune
            school("Pune Motor Training", "Kothrud, Pune", "Maharashtra", "Pune", "411038", "020-25460123", new BigDecimal("4.1"), true, "MH-PMT-004"),
            school("Sahyadri Driving School", "Hadapsar, Pune", "Maharashtra", "Pune", "411028", "9876543210", new BigDecimal("3.8"), false, "MH-SDS-005"),

            // Bangalore
            school("Bangalore Driving Academy", "MG Road, Bengaluru", "Karnataka", "Bengaluru", "560001", "080-22344567", new BigDecimal("4.6"), true, "KA-BDA-001"),
            school("Karnataka Motor Training", "Jayanagar, Bengaluru", "Karnataka", "Bengaluru", "560041", "9945123456", new BigDecimal("4.3"), true, "KA-KMT-002"),
            school("Whitefield Drive School", "Whitefield, Bengaluru", "Karnataka", "Bengaluru", "560066", "9880123456", new BigDecimal("4.0"), false, "KA-WDS-003"),

            // Chennai
            school("Chennai Motor School", "T Nagar, Chennai", "Tamil Nadu", "Chennai", "600017", "044-24341234", new BigDecimal("4.2"), true, "TN-CMS-001"),
            school("Madras Driving Academy", "Anna Nagar, Chennai", "Tamil Nadu", "Chennai", "600040", "9841234567", new BigDecimal("4.0"), false, "TN-MDA-002"),
            school("Kodambakkam Driving School", "Kodambakkam, Chennai", "Tamil Nadu", "Chennai", "600024", "044-24830123", new BigDecimal("3.9"), false, "TN-KDS-003"),

            // Kolkata
            school("Kolkata Driving School", "Salt Lake Sector V, Kolkata", "West Bengal", "Kolkata", "700091", "033-23571234", new BigDecimal("4.1"), true, "WB-KDS-001"),
            school("Howrah Motor Academy", "GT Road, Howrah", "West Bengal", "Howrah", "711101", "9831234567", new BigDecimal("3.8"), false, "WB-HMA-002"),
            school("Jadavpur Driving Centre", "Jadavpur, Kolkata", "West Bengal", "Kolkata", "700032", "033-24731234", new BigDecimal("4.0"), true, "WB-JDC-003"),

            // Hyderabad
            school("Hyderabad Driving School", "Banjara Hills, Hyderabad", "Telangana", "Hyderabad", "500034", "040-23551234", new BigDecimal("4.4"), true, "TS-HDS-001"),
            school("Secunderabad Motor School", "MG Road, Secunderabad", "Telangana", "Hyderabad", "500003", "9848012345", new BigDecimal("4.1"), false, "TS-SMS-002"),

            // Ahmedabad
            school("Ahmedabad Driving Academy", "CG Road, Ahmedabad", "Gujarat", "Ahmedabad", "380009", "079-26441234", new BigDecimal("4.2"), true, "GJ-ADA-001"),
            school("Surat Motor Training School", "Ring Road, Surat", "Gujarat", "Surat", "395002", "9825012345", new BigDecimal("3.9"), false, "GJ-SMS-002"),

            // Jaipur
            school("Jaipur Driving School", "Tonk Road, Jaipur", "Rajasthan", "Jaipur", "302015", "0141-2701234", new BigDecimal("4.0"), true, "RJ-JDS-001"),
            school("Pink City Motor Academy", "Malviya Nagar, Jaipur", "Rajasthan", "Jaipur", "302017", "9414012345", new BigDecimal("3.8"), false, "RJ-PMA-002"),

            // Lucknow
            school("Lucknow Motor Training School", "Hazratganj, Lucknow", "Uttar Pradesh", "Lucknow", "226001", "0522-2231234", new BigDecimal("4.1"), true, "UP-LMT-001"),
            school("Gomti Nagar Driving Academy", "Gomti Nagar, Lucknow", "Uttar Pradesh", "Lucknow", "226010", "9415012345", new BigDecimal("3.9"), false, "UP-GDA-002")
        );

        repository.saveAll(schools);
        log.info("Seeded {} driving schools.", schools.size());
    }

    private DrivingSchool school(String name, String address, String state, String city,
                                  String pin, String phone, BigDecimal rating,
                                  boolean govt, String license) {
        return DrivingSchool.builder()
                .name(name)
                .address(address)
                .state(state)
                .city(city)
                .pincode(pin)
                .phone(phone)
                .rating(rating)
                .isGovernmentApproved(govt)
                .licenseNumber(license)
                .build();
    }
}
