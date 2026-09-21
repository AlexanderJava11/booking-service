package alex.villa_avougjagi;

import alex.villa_avougjagi.client.CustomerClient;
import alex.villa_avougjagi.dto.BookingDTO;
import alex.villa_avougjagi.dto.CustomerDTO;
import alex.villa_avougjagi.models.Booking;
import alex.villa_avougjagi.models.Room;
import alex.villa_avougjagi.repositories.BookingRepository;
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
    private RoomRepository roomRepository;

    @Mock
    private RoomService roomService;

    @Mock
    private CustomerClient customerClient;

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
        CustomerDTO customer = new CustomerDTO();
        customer.setId(1L);

        Room room = mock(Room.class);

        when(customerClient.findById(1L))
                .thenReturn(customer);

        when(roomService.isRoomAvailable(
                2L,
                dto.getCheckInDate(),
                dto.getCheckOutDate(),
                2,
                null
        )).thenReturn(true);

        when(roomRepository.findById(2L))
                .thenReturn(Optional.of(room));

        boolean result = bookingService.saveBooking(dto);

        assertTrue(result);

        ArgumentCaptor<Booking> captor =
                ArgumentCaptor.forClass(Booking.class);

        verify(bookingRepository).save(captor.capture());

        Booking savedBooking = captor.getValue();

        assertEquals(1L, savedBooking.getCustomerId());
        assertSame(room, savedBooking.getRoom());
        assertEquals(dto.getCheckInDate(), savedBooking.getCheckInDate());
        assertEquals(dto.getCheckOutDate(), savedBooking.getCheckOutDate());
        assertEquals(2, savedBooking.getNumberOfGuests());
    }

    @Test
    void saveBooking_shouldReject_whenCheckOutIsBeforeCheckIn() {
        CustomerDTO customer = new CustomerDTO();
        customer.setId(1L);

        when(customerClient.findById(1L))
                .thenReturn(customer);

        dto.setCheckOutDate(dto.getCheckInDate().minusDays(1));

        boolean result = bookingService.saveBooking(dto);

        assertFalse(result);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void saveBooking_shouldReject_whenDatesAreEqual() {
        CustomerDTO customer = new CustomerDTO();
        customer.setId(1L);

        when(customerClient.findById(1L))
                .thenReturn(customer);

        dto.setCheckOutDate(dto.getCheckInDate());

        boolean result = bookingService.saveBooking(dto);

        assertFalse(result);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void saveBooking_shouldReject_whenCustomerDoesNotExist() {
        when(customerClient.findById(1L))
                .thenReturn(null);

        boolean result = bookingService.saveBooking(dto);

        assertFalse(result);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void saveBooking_shouldReject_whenRoomIsUnavailable() {
        CustomerDTO customer = new CustomerDTO();
        customer.setId(1L);

        when(customerClient.findById(1L))
                .thenReturn(customer);

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
        verify(roomRepository, never()).findById(anyLong());
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