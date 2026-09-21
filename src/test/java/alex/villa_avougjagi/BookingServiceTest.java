package alex.villa_avougjagi;

import alex.villa_avougjagi.dto.BookingDTO;
import alex.villa_avougjagi.models.Booking;
import alex.villa_avougjagi.models.Customer;
import alex.villa_avougjagi.models.Room;
import alex.villa_avougjagi.repositories.BookingRepository;
import alex.villa_avougjagi.repositories.CustomerRepository;
import alex.villa_avougjagi.repositories.RoomRepository;
import alex.villa_avougjagi.service.BookingService;
import alex.villa_avougjagi.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomService roomService;

    @InjectMocks
    private BookingService bookingService;

    private BookingDTO dto;

    @BeforeEach
    void setUp() {
        dto = new BookingDTO();
        dto.setCustomerId(1L);
        dto.setRoomId(2L);
        dto.setCheckInDate(LocalDate.of(2027, 6, 10));
        dto.setCheckOutDate(LocalDate.of(2027, 6, 12));
        dto.setNumberOfGuests(2);
    }

    @Test
    void saveBooking_shouldSave_whenBookingIsValid() {
        // Förbered: kunden finns och rummet är tillgängligt.
        Customer customer = mock(Customer.class);
        Room room = mock(Room.class);

        when(roomService.isRoomAvailable(
                2L,
                dto.getCheckInDate(),
                dto.getCheckOutDate(),
                2,
                null
        )).thenReturn(true);

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(roomRepository.findById(2L))
                .thenReturn(Optional.of(room));

        // Kör.
        boolean result = bookingService.saveBooking(dto);

        // Kontrollera både svaret och det som sparas.
        assertTrue(result);

        ArgumentCaptor<Booking> captor =
                ArgumentCaptor.forClass(Booking.class);

        verify(bookingRepository).save(captor.capture());

        Booking savedBooking = captor.getValue();

        assertSame(customer, savedBooking.getCustomer());
        assertSame(room, savedBooking.getRoom());
        assertEquals(dto.getCheckInDate(), savedBooking.getCheckInDate());
        assertEquals(dto.getCheckOutDate(), savedBooking.getCheckOutDate());
        assertEquals(2, savedBooking.getNumberOfGuests());
    }

    @Test
    void saveBooking_shouldReject_whenCheckOutIsBeforeCheckIn() {
        dto.setCheckOutDate(dto.getCheckInDate().minusDays(1));

        boolean result = bookingService.saveBooking(dto);

        assertFalse(result);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void saveBooking_shouldReject_whenDatesAreEqual() {
        dto.setCheckOutDate(dto.getCheckInDate());

        boolean result = bookingService.saveBooking(dto);

        assertFalse(result);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void saveBooking_shouldReject_whenCustomerIdIsMissing() {
        dto.setCustomerId(null);

        boolean result = bookingService.saveBooking(dto);

        assertFalse(result);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void saveBooking_shouldReject_whenRoomIsUnavailable() {
        when(roomService.isRoomAvailable(
                2L,
                dto.getCheckInDate(),
                dto.getCheckOutDate(),
                2,
                null
        )).thenReturn(false);

        boolean result = bookingService.saveBooking(dto);

        assertFalse(result);
        verify(bookingRepository, never()).save(any(Booking.class));
        verifyNoInteractions(customerRepository, roomRepository);
    }

    @Test
    void deleteById_shouldDelete_whenBookingExists() {
        when(bookingRepository.existsById(5L)).thenReturn(true);

        boolean result = bookingService.deleteById(5L);

        assertTrue(result);
        verify(bookingRepository).deleteById(5L);
    }

    @Test
    void deleteById_shouldReturnFalse_whenBookingDoesNotExist() {
        when(bookingRepository.existsById(5L)).thenReturn(false);

        boolean result = bookingService.deleteById(5L);

        assertFalse(result);
        verify(bookingRepository, never()).deleteById(anyLong());
    }
}