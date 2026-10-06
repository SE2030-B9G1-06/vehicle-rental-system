package com.sliit.vehiclerental.backend.repository;

import com.sliit.vehiclerental.backend.entity.Booking;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
  List<Booking> findByCustomerIdOrderByPickupDatetimeDesc(Long customerId);

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select b from Booking b where b.id=:id")
  java.util.Optional<Booking> lockById(@Param("id") Long id);

  @Query(
      "select count(b) from Booking b "
          + "where b.vehicle.id = :vehicleId and upper(b.status) <> 'CANCELLED' "
          + "and b.pickupDatetime < :requestedReturn and b.returnDatetime > :requestedPickup")
  long countConflictingBookings(
      @Param("vehicleId") Long vehicleId,
      @Param("requestedReturn") LocalDateTime requestedReturn,
      @Param("requestedPickup") LocalDateTime requestedPickup);

  @Query(
      "select count(b) from Booking b where b.vehicle.id=:vehicleId and b.status in"
          + " ('PENDING','CONFIRMED','IN_PROGRESS') and (:excludeId is null or b.id<>:excludeId)"
          + " and b.pickupDatetime<:finish and b.returnDatetime>:start")
  long conflicts(
      @Param("vehicleId") Long vehicleId,
      @Param("excludeId") Long excludeId,
      @Param("start") LocalDateTime start,
      @Param("finish") LocalDateTime finish);

  boolean existsByVehicleIdAndStatusIn(Long vehicleId, java.util.Collection<String> statuses);

  boolean existsByPickupBranchIdAndStatusInOrReturnBranchIdAndStatusIn(
      Integer pickup,
      java.util.Collection<String> a,
      Integer returned,
      java.util.Collection<String> b);

  long countByPromotionIdAndStatusNot(Integer promotionId, String status);

  boolean existsByPromotionId(Integer promotionId);

  @Query(
      "select count(b) from Booking b where b.promotion.id=:promoId and b.status<>'CANCELLED' and"
          + " (:excludeId is null or b.id<>:excludeId)")
  long promoUses(@Param("promoId") Integer promoId, @Param("excludeId") Long excludeId);
}
