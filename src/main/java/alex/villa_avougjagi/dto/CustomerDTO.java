package alex.villa_avougjagi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDTO {

    private Long id;

    @NotBlank(message = "Förnamn får inte vara tomt")
    @Size(max = 50, message = "Förnamn får max vara 50 tecken")
    private String firstName;

    @NotBlank(message = "Efternamn får inte vara tomt")
    @Size(max = 50, message = "Efternamn får max vara 50 tecken")
    private String lastName;

    @NotBlank(message = "E-post får inte vara tomt")
    @Email(message = "E-post måste vara giltig")
    private String email;

    @Pattern(
            regexp = "^\\+?[0-9]{1,4}?[ .-]?[0-9]{6,12}$",
            message = "Telefonnummer är ogiltigt"
    )
    private String phone;
}
