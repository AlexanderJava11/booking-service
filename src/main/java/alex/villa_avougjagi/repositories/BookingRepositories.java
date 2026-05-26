package alex.villa_avougjagi.repositories;

import alex.villa_avougjagi.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepositories extends JpaRepository<Booking, Long> {
    boolean existsByCustomerId(Long customerId);
}
