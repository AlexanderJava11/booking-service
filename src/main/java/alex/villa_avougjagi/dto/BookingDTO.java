package alex.villa_avougjagi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingDTO {

    private Long id;

    @NotNull(message = "Kund måste väljas")
    private Long customerId;

    @NotNull(message = "Rum måste väljas")
    private Long roomId;

    @NotNull(message = "Incheckningsdatum krävs")
    private LocalDate checkInDate;

    @NotNull(message = "Utcheckningsdatum krävs")
    private LocalDate checkOutDate;

    @Min(value = 1, message = "Minst 1 person krävs")
    private int numberOfGuests;

    private String customerFirstName;
    private String customerLastName;
    private String roomNumber;
    private String roomType;
    private int roomPricePerNight;
    private long nights;
    private int totalPrice;

}
