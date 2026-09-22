package alex.villa_avougjagi.controller;

import alex.villa_avougjagi.dto.BookingDTO;
import alex.villa_avougjagi.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingApiController {

    private final BookingService bookingService;

    @GetMapping("/customer/{customerId}/active")
    public boolean hasActiveBookings(@PathVariable Long customerId) {
        return bookingService.hasActiveBookings(customerId);
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingDTO bookingDTO) {
        bookingDTO.setId(null);

        if (bookingService.saveBooking(bookingDTO)) {
            return ResponseEntity.ok("Bokningen skapades");
        }

        return ResponseEntity.badRequest().body("Bokningen kunde inte skapas");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(
            @PathVariable Long id,
            @RequestBody BookingDTO bookingDTO) {

        if (bookingService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }

        bookingDTO.setId(id);

        if (bookingService.saveBooking(bookingDTO)) {
            return ResponseEntity.ok("Bokningen uppdaterades");
        }

        return ResponseEntity.badRequest().body("Bokningen kunde inte uppdateras");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {

        if (bookingService.deleteById(id)) {
            return ResponseEntity.ok("Bokningen avbokades");
        }

        return ResponseEntity.notFound().build();
    }
}