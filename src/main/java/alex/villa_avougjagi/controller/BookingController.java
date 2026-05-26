package alex.villa_avougjagi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final CustomerService customerService;
    private final RoomService roomService;
    private final BookingService bookingService;

    @GetMapping
    public String listBookings(Model model) {
        model.addAttribute("bookings", bookingService.findAll());
        return "customers/bookings/list";
    }
}
