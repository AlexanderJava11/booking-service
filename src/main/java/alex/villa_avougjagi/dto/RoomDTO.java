package alex.villa_avougjagi.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDTO {

    private Long id;

    @NotBlank(message = "Rumsnummer får inte vara tomt")
    private String roomNumber;

    @NotBlank(message = "Rumstyp måste väljas")
    private String roomType;

    @Min(value = 0, message = "Extrasängar kan inte vara mindre än 0")
    @Max(value = 2, message = "Max 2 extrasängar")
    private int extraBeds;

    @Min(value = 0, message = "Pris per natt kan inte vara negativt")
    private int pricePerNight;

    private int capacity;
}
