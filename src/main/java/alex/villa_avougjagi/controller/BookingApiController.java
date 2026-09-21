package alex.villa_avougjagi.controller;

import alex.villa_avougjagi.service.BookingService;
import lombok.RequiredArgsConstructor;
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
}