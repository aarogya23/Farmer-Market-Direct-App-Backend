package com.commerce.FarmerDirectMarkert.Controller;

import com.commerce.FarmerDirectMarkert.dto.*;
import com.commerce.FarmerDirectMarkert.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Slf4j
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * Create a new reservation
     */
    @PostMapping("/create")
    public ResponseEntity<ReservationDto> createReservation(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CreateReservationRequest request) {
        log.info("Creating new reservation for product: {}", request.getProductId());
        try {
            String buyerEmail = extractEmailFromToken(token);
            ReservationDto reservation = reservationService.createReservation(buyerEmail, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
        } catch (Exception e) {
            log.error("Error creating reservation", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all reservations for the current buyer
     */
    @GetMapping("/my-reservations")
    public ResponseEntity<List<ReservationDto>> getMyReservations(
            @RequestHeader("Authorization") String token) {
        try {
            String buyerEmail = extractEmailFromToken(token);
            List<ReservationDto> reservations = reservationService.getBuyerReservations(buyerEmail);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            log.error("Error fetching buyer reservations", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all reservations for farmer's products
     */
    @GetMapping("/farmer-reservations")
    public ResponseEntity<List<ReservationDto>> getFarmerReservations(
            @RequestHeader("Authorization") String token) {
        try {
            String farmerEmail = extractEmailFromToken(token);
            List<ReservationDto> reservations = reservationService.getFarmerReservations(farmerEmail);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            log.error("Error fetching farmer reservations", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get reservations for a specific product
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReservationDto>> getProductReservations(@PathVariable Long productId) {
        try {
            List<ReservationDto> reservations = reservationService.getProductReservations(productId);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            log.error("Error fetching product reservations", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get reservation by ID
     */
    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationDto> getReservationById(@PathVariable Long reservationId) {
        try {
            ReservationDto reservation = reservationService.getReservationById(reservationId);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            log.error("Error fetching reservation", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get reservation by code
     */
    @GetMapping("/code/{reservationCode}")
    public ResponseEntity<ReservationDto> getReservationByCode(@PathVariable String reservationCode) {
        try {
            ReservationDto reservation = reservationService.getReservationByCode(reservationCode);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            log.error("Error fetching reservation by code", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Confirm a reservation
     */
    @PatchMapping("/{reservationId}/confirm")
    public ResponseEntity<ReservationDto> confirmReservation(@PathVariable Long reservationId) {
        try {
            ReservationDto reservation = reservationService.confirmReservation(reservationId);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            log.error("Error confirming reservation", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cancel a reservation
     */
    @PatchMapping("/{reservationId}/cancel")
    public ResponseEntity<ReservationDto> cancelReservation(@PathVariable Long reservationId) {
        try {
            ReservationDto reservation = reservationService.cancelReservation(reservationId);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            log.error("Error cancelling reservation", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete a reservation
     */
    @PatchMapping("/{reservationId}/complete")
    public ResponseEntity<ReservationDto> completeReservation(@PathVariable Long reservationId) {
        try {
            ReservationDto reservation = reservationService.completeReservation(reservationId);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            log.error("Error completing reservation", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Helper method to extract email from token
     */
    private String extractEmailFromToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // Implement your JWT parsing logic here
        return "buyer@example.com";
    }
}
