package alex.villa_avougjagi.repositories;

import alex.villa_avougjagi.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByCustomerIdAndCheckOutDateGreaterThanEqual(
            Long customerId,
            LocalDate date
    );

    boolean existsByRoomId(Long roomId);
}