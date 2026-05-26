package alex.villa_avougjagi.config;

import alex.villa_avougjagi.models.Room;
import alex.villa_avougjagi.models.RoomType;
import alex.villa_avougjagi.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoomRepository roomRepository;

    @Override
    public void run(String... args) {
        if (roomRepository.count() > 0) {
            return;
        }

        roomRepository.save(Room.builder()
                .roomNumber("101")
                .roomType(RoomType.SINGLE)
                .extraBeds(0)
                .pricePerNight(995)
                .build());

        roomRepository.save(Room.builder()
                .roomNumber("201")
                .roomType(RoomType.DOUBLE)
                .extraBeds(1)
                .pricePerNight(1495)
                .build());

        roomRepository.save(Room.builder()
                .roomNumber("301")
                .roomType(RoomType.DOUBLE)
                .extraBeds(2)
                .pricePerNight(1895)
                .build());
    }
}