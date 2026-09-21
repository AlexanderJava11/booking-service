package alex.villa_avougjagi.service;

import alex.villa_avougjagi.dto.BookingDTO;
import alex.villa_avougjagi.models.Booking;
import alex.villa_avougjagi.repositories.BookingRepository;
import alex.villa_avougjagi.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import alex.villa_avougjagi.client.CustomerClient;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final RoomService roomService;
    private final CustomerClient customerClient;

    public List<BookingDTO> findAll() {
        return bookingRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public BookingDTO findById(Long id) {
        return bookingRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    public boolean saveBooking(BookingDTO dto) {
        try {
            if (customerClient.findById(dto.getCustomerId()) == null) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        if (dto.getCheckInDate() == null || dto.getCheckOutDate() == null) {
            return false;
        }

        if (!dto.getCheckOutDate().isAfter(dto.getCheckInDate())) {
            return false;
        }

        boolean available = roomService.isRoomAvailable(
                dto.getRoomId(),
                dto.getCheckInDate(),
                dto.getCheckOutDate(),
                dto.getNumberOfGuests(),
                dto.getId()
        );

        if (!available) {
            return false;
        }

        Booking booking = dto.getId() != null
                ? bookingRepository.findById(dto.getId()).orElse(new Booking())
                : new Booking();

        booking.setCustomerId(dto.getCustomerId());
        booking.setRoom(roomRepository.findById(dto.getRoomId()).orElseThrow());
        booking.setCheckInDate(dto.getCheckInDate());
        booking.setCheckOutDate(dto.getCheckOutDate());
        booking.setNumberOfGuests(dto.getNumberOfGuests());

        bookingRepository.save(booking);
        return true;
    }
    public boolean hasActiveBookings(Long customerId) {
        return bookingRepository
                .existsByCustomerIdAndCheckOutDateGreaterThanEqual(
                        customerId,
                        java.time.LocalDate.now()
                );
    }

    public boolean deleteById(Long id) {
        if (!bookingRepository.existsById(id)) {
            return false;
        }

        bookingRepository.deleteById(id);
        return true;
    }

    private BookingDTO toDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();

        dto.setId(booking.getId());

        dto.setCustomerId(booking.getCustomerId());
        try {
            var customer = customerClient.findById(booking.getCustomerId());

            if (customer != null) {
                dto.setCustomerFirstName(customer.getFirstName());
                dto.setCustomerLastName(customer.getLastName());
            }
        } catch (Exception e) {
            dto.setCustomerFirstName("Okänd");
            dto.setCustomerLastName("kund");
        }

        dto.setRoomId((long) booking.getRoom().getId());
        dto.setRoomNumber(booking.getRoom().getRoomNumber());
        dto.setRoomType(booking.getRoom().getRoomType().getDisplayName());
        dto.setRoomPricePerNight((int) booking.getRoom().getPricePerNight());

        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setNumberOfGuests(booking.getNumberOfGuests());

        long nights = ChronoUnit.DAYS.between(
                booking.getCheckInDate(),
                booking.getCheckOutDate()
        );

        dto.setNights(nights);
        dto.setTotalPrice(
                (int) (nights * booking.getRoom().getPricePerNight())
        );

        return dto;
    }
}