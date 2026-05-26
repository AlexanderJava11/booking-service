package alex.villa_avougjagi.repositories;

import alex.villa_avougjagi.models.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("""
        SELECT r FROM Room r
        WHERE :guests > 0
        AND NOT EXISTS (
            SELECT b FROM Booking b
            WHERE b.room = r
            AND b.checkInDate < :checkOutDate
            AND b.checkOutDate > :checkInDate
            AND (:bookingId IS NULL OR b.id <> :bookingId)
        )
    """)
    List<Room> findAvailableRooms(
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("guests") int guests,
            @Param("bookingId") Long bookingId
            );
}
