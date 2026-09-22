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
public class ReviewDTO {

    private Long id;

    @NotNull(message = "Kund måste anges")
    private Long customerId;

    @Min(value = 1, message = "Betyget måste vara minst 1")
    @Max(value = 5, message = "Betyget får max vara 5")
    private int rating;

    @NotBlank(message = "Kommentaren får inte vara tom")
    private String comment;
}