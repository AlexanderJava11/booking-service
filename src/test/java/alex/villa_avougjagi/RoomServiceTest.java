package alex.villa_avougjagi;

import alex.villa_avougjagi.dto.RoomDTO;
import alex.villa_avougjagi.models.Room;
import alex.villa_avougjagi.models.RoomType;
import alex.villa_avougjagi.repositories.BookingRepository;
import alex.villa_avougjagi.repositories.RoomRepository;
import alex.villa_avougjagi.service.RoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private RoomService roomService;

    private final LocalDate checkIn = LocalDate.of(2027, 6, 10);
    private final LocalDate checkOut = LocalDate.of(2027, 6, 12);

    @Test
    void save_shouldAllowNewRoomWithoutId() {
        RoomDTO dto = RoomDTO.builder()
                .roomNumber("401")
                .roomType("DOUBLE")
                .extraBeds(1)
                .pricePerNight(1500)
                .build();

        when(roomRepository.save(any(Room.class)))
                .thenAnswer(invocation -> {
                    Room room = invocation.getArgument(0);

                    // Ett nytt rum ska sakna id före sparningen.
                    assertNull(room.getId());

                    // Efterliknar att databasen skapar ett id.
                    room.setId(10L);
                    return room;
                });

        RoomDTO result = roomService.save(dto);

        assertEquals(Long.valueOf(10L), result.getId());
        assertEquals("401", result.getRoomNumber());
        assertEquals("DOUBLE", result.getRoomType());
        assertEquals(3, result.getCapacity());
        assertEquals(1500, result.getPricePerNight());

        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void findAvailableRooms_shouldExcludeRoomsWithTooSmallCapacity() {
        Room smallRoom = createDoubleRoom(1L, 0);
        Room largerRoom = createDoubleRoom(2L, 1);

        when(roomRepository.findAvailableRooms(
                checkIn, checkOut, 3, null
        )).thenReturn(List.of(smallRoom, largerRoom));

        List<RoomDTO> result =
                roomService.findAvailableRooms(checkIn, checkOut, 3);

        assertEquals(1, result.size());
        assertEquals(Long.valueOf(2L), result.get(0).getId());
        assertEquals(3, result.get(0).getCapacity());
    }

    @Test
    void findAvailableRooms_shouldRejectEqualDates() {
        List<RoomDTO> result =
                roomService.findAvailableRooms(checkIn, checkIn, 2);

        assertTrue(result.isEmpty());
        verifyNoInteractions(roomRepository);
    }

    @Test
    void findAvailableRooms_shouldRejectZeroGuests() {
        List<RoomDTO> result =
                roomService.findAvailableRooms(checkIn, checkOut, 0);

        assertTrue(result.isEmpty());
        verifyNoInteractions(roomRepository);
    }

    @Test
    void isRoomAvailable_shouldReturnTrueForRequestedAvailableRoom() {
        Room room = createDoubleRoom(2L, 0);

        when(roomRepository.findAvailableRooms(
                checkIn, checkOut, 2, null
        )).thenReturn(List.of(room));

        assertTrue(roomService.isRoomAvailable(
                2L, checkIn, checkOut, 2, null
        ));
    }

    @Test
    void isRoomAvailable_shouldReturnFalseWhenOnlyAnotherRoomIsAvailable() {
        Room otherRoom = createDoubleRoom(3L, 0);

        when(roomRepository.findAvailableRooms(
                checkIn, checkOut, 2, null
        )).thenReturn(List.of(otherRoom));

        assertFalse(roomService.isRoomAvailable(
                2L, checkIn, checkOut, 2, null
        ));
    }

    @Test
    void delete_shouldRejectRoomWithBookings() {
        when(roomRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.existsByRoomId(2L)).thenReturn(true);

        boolean result = roomService.delete(2L);

        assertFalse(result);
        verify(roomRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_shouldDeleteRoomWithoutBookings() {
        when(roomRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.existsByRoomId(2L)).thenReturn(false);

        boolean result = roomService.delete(2L);

        assertTrue(result);
        verify(roomRepository).deleteById(2L);
    }

    private Room createDoubleRoom(Long id, int extraBeds) {
        return Room.builder()
                .id(id)
                .roomNumber("R" + id)
                .roomType(RoomType.DOUBLE)
                .extraBeds(extraBeds)
                .pricePerNight(1500)
                .build();
    }
}