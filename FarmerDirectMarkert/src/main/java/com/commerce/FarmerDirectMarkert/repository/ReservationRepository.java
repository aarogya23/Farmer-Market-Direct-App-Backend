package com.commerce.FarmerDirectMarkert.repository;

import com.commerce.FarmerDirectMarkert.model.Reservation;
import com.commerce.FarmerDirectMarkert.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByReservationCode(String reservationCode);
    List<Reservation> findByBuyerId(String buyerId);
    List<Reservation> findByFarmerId(String farmerId);
    List<Reservation> findByProductId(Long productId);
    List<Reservation> findByBuyerIdAndStatus(String buyerId, ReservationStatus status);
    List<Reservation> findByProductIdAndStatus(Long productId, ReservationStatus status);
    List<Reservation> findByStatus(ReservationStatus status);
    List<Reservation> findByReservedUntilTimeBeforeAndStatus(LocalDateTime dateTime, ReservationStatus status);
}
