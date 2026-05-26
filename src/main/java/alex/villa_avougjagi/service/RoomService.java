package alex.villa_avougjagi.service;

import alex.villa_avougjagi.dto.RoomDTO;
import alex.villa_avougjagi.models.Room;
import alex.villa_avougjagi.models.RoomType;
import alex.villa_avougjagi.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public List<RoomDTO> findAll() {
        return roomRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public RoomDTO findById(Long id) {
        return roomRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Rummet finns inte"));
    }

    public RoomDTO save(RoomDTO dto) {
        Room savedRoom = roomRepository.save(toEntity(dto));
        return toDTO(savedRoom);
    }

    public void delete(Long id) {
        roomRepository.deleteById(id);
    }

    public List<RoomDTO> findAvailableRooms(LocalDate checkIn, LocalDate checkOut, int guests) {
        return findAvailableRooms(checkIn, checkOut, guests, null);
    }
    public List<RoomDTO> findAvailableRooms(LocalDate checkIn, LocalDate checkOut, int guests, Long bookingId) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn) || guests < 1) {
            return List.of();
        }

        return roomRepository.findAvailableRooms(checkIn, checkOut, guests, bookingId)
                .stream()
                .filter(room -> room.getCapacity() >= guests)
                .map(this::toDTO)
                .toList();
    }

    public boolean isRoomAvailable(Long roomId,
                                   LocalDate checkIn,
                                   LocalDate checkOut,
                                   int guests,
                                   Long bookingId) {

        if (roomId == null) {
            return false;
        }

        return findAvailableRooms(checkIn, checkOut, guests, bookingId)
                .stream()
                .anyMatch(room -> room.getId().equals(roomId));
    }

    private RoomDTO toDTO(Room room) {
        return RoomDTO.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType().name())
                .extraBeds(room.getExtraBeds())
                .pricePerNight(room.getPricePerNight())
                .capacity(room.getCapacity())
                .build();
    }

    private Room toEntity(RoomDTO dto) {
        RoomType type = RoomType.valueOf(dto.getRoomType());
        int extraBeds = type == RoomType.SINGLE ? 0 : dto.getExtraBeds();
        int price = dto.getPricePerNight() > 0 ? dto.getPricePerNight() : defaultPrice(type, extraBeds);

        return Room.builder()
                .id(dto.getId())
                .roomNumber(dto.getRoomNumber())
                .roomType(type)
                .extraBeds(extraBeds)
                .pricePerNight(price)
                .build();
    }

    private int defaultPrice(RoomType type, int extraBeds) {
        return type == RoomType.SINGLE ? 995 : 1495 + (extraBeds * 250);
    }

}
