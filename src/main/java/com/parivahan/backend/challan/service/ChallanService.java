package com.parivahan.backend.challan.service;

import com.parivahan.backend.challan.dto.ChallanDetailDto;
import com.parivahan.backend.challan.dto.ChallanSummaryDto;
import com.parivahan.backend.challan.dto.PaymentReceiptDto;
import com.parivahan.backend.challan.entity.Challan;
import com.parivahan.backend.challan.enums.ChallanStatus;
import com.parivahan.backend.challan.repository.ChallanDisputeRepository;
import com.parivahan.backend.challan.repository.ChallanRepository;
import com.parivahan.backend.common.exception.ResourceNotFoundException;
import com.parivahan.backend.user.domain.User;
import com.parivahan.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallanService {

    private final ChallanRepository challanRepository;
    private final ChallanDisputeRepository challanDisputeRepository;
    private final UserRepository userRepository;

    /** All challans across all vehicles owned by the current user. */
    @Transactional(readOnly = true)
    public List<ChallanSummaryDto> getAllChallansForCurrentUser() {
        User user = getCurrentUser();
        return challanRepository.findAllByUserId(user.getId())
                .stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    /** Full detail for a single challan — ownership enforced. */
    @Transactional(readOnly = true)
    public ChallanDetailDto getChallanDetail(Long challanId) {
        Challan challan = getOwnedChallan(challanId);
        boolean hasDispute = challanDisputeRepository.findByChallanId(challanId).isPresent();
        return toDetailDto(challan, hasDispute);
    }

    /** Current status of a challan. */
    @Transactional(readOnly = true)
    public ChallanStatus getChallanStatus(Long challanId) {
        return getOwnedChallan(challanId).getStatus();
    }

    /** Pay a pending challan online (mock payment). */
    @Transactional
    public PaymentReceiptDto payChallan(Long challanId) {
        Challan challan = getOwnedChallan(challanId);

        if (challan.getStatus() == ChallanStatus.PAID) {
            throw new IllegalStateException("This challan has already been paid.");
        }
        if (challan.getStatus() == ChallanStatus.DISPUTED) {
            throw new IllegalStateException("Cannot pay a challan that is under dispute.");
        }

        String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        challan.setStatus(ChallanStatus.PAID);
        challan.setPaymentDate(LocalDate.now());
        challan.setTransactionId(txnId);
        challanRepository.save(challan);

        return PaymentReceiptDto.builder()
                .receiptNumber("RCPT-" + txnId)
                .registrationNumber(challan.getVehicle().getRegistrationNumber())
                .offence(challan.getOffence())
                .amountPaid(challan.getAmount())
                .paymentDate(challan.getPaymentDate())
                .transactionId(txnId)
                .paymentMode("Online (Mock)")
                .message("Payment successful. Your challan has been cleared.")
                .build();
    }

    /** Returns pending challans for a vehicle — used internally by Dashboard. */
    @Transactional(readOnly = true)
    public List<Challan> getPendingChallansForVehicle(Long vehicleId) {
        return challanRepository.findPendingByVehicleId(vehicleId);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Challan getOwnedChallan(Long challanId) {
        Challan challan = challanRepository.findById(challanId)
                .orElseThrow(() -> new ResourceNotFoundException("Challan not found"));
        User user = getCurrentUser();
        if (!challan.getVehicle().getUser().getId().equals(user.getId())) {
            throw new SecurityException("Access denied: You do not own this challan");
        }
        return challan;
    }

    private static final java.util.Map<String, String> OFFENCE_LOCATION_MAP = java.util.Map.of(
        "Over Speeding",  "Silk Board Junction, Bengaluru",
        "Signal Jumping", "Marathahalli Bridge, Bengaluru",
        "No Parking",     "MG Road, Bengaluru",
        "Wrong Parking",  "Koramangala 4th Block, Bengaluru",
        "No Helmet",      "Indiranagar 100ft Road, Bengaluru",
        "Triple Riding",  "Whitefield Main Road, Bengaluru"
    );

    private ChallanSummaryDto toSummaryDto(Challan c) {
        boolean hasDispute = challanDisputeRepository.findByChallanId(c.getId()).isPresent();
        String location = OFFENCE_LOCATION_MAP.getOrDefault(c.getOffence(), "Bengaluru, Karnataka");
        return ChallanSummaryDto.builder()
                .id(c.getId())
                .registrationNumber(c.getVehicle().getRegistrationNumber())
                .vehicleModel(c.getVehicle().getManufacturer() + " " + c.getVehicle().getModel())
                .vehicleNickname(c.getVehicle().getNickname())
                .offence(c.getOffence())
                .location(location)
                .amount(c.getAmount())
                .challanDate(c.getChallanDate())
                .dueDate(c.getChallanDate() != null ? c.getChallanDate().plusDays(30) : null)
                .paymentDate(c.getPaymentDate())
                .transactionId(c.getTransactionId())
                .status(c.getStatus())
                .hasActiveDispute(hasDispute)
                .build();
    }

    private ChallanDetailDto toDetailDto(Challan c, boolean hasDispute) {
        return ChallanDetailDto.builder()
                .id(c.getId())
                .registrationNumber(c.getVehicle().getRegistrationNumber())
                .vehicleModel(c.getVehicle().getManufacturer() + " " + c.getVehicle().getModel())
                .offence(c.getOffence())
                .amount(c.getAmount())
                .challanDate(c.getChallanDate())
                .status(c.getStatus())
                .paymentDate(c.getPaymentDate())
                .transactionId(c.getTransactionId())
                .hasActiveDispute(hasDispute)
                .build();
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = principal instanceof UserDetails ud ? ud.getUsername() : principal.toString();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
