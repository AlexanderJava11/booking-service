package alex.villa_avougjagi.config;

import alex.villa_avougjagi.models.Room;
import alex.villa_avougjagi.models.RoomType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoomRepositories roomRepositories;

    @Override
    public void run(String... args) {
        if (roomRepositories.count () > 0) {
            return;
        }

        roomRepositories.save(Room.builder()
                .roomNumber("101")
                .roomType(RoomType.SINGLE)
                .extraBeds(0)
                .pricePerNight(995)
                .build());

        roomRepositories.save(Room.builder()
                .roomNumber("201")
                .roomType(RoomType.DOUBLE)
                .extraBeds(1)
                .pricePerNight(1495)
                .build());

        roomRepositories.save(Room.builder()
                .roomNumber("301")
                .roomType(RoomType.DOUBLE)
                .extraBeds(2)
                .pricePerNight(1895)
                .build());
    }
}
