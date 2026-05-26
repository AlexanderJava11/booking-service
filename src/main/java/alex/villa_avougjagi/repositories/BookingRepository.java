package alex.villa_avougjagi.repositories;

import alex.villa_avougjagi.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByCustomerId(Long customerId);

    boolean existsByRoomId(Long roomId);
}
