package alex.villa_avougjagi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

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

    @GetMapping("/new")
    public String showBookingForm(Model model) {
        model.addAttribute("booking", new BookingDTO());
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("rooms", List.of());
        return "customers/bookings/form";
    }

    @PostMapping("/search")
    public String searchRooms(@ModelAttribute("booking") BookingDTO bookingDTO, Model model) {
        model.addAttribute("customers", customerService.findAll());

        if (bookingDTO.getCheckInDate() != null &&
            bookingDTO.getCheckOutDate() != null &&
            bookingDTO.getNumberOfGuests() > 0) {

            model.addAttribute("rooms", roomService.findAvailableRooms(
                    bookingDTO.getCheckInDate(),
                    bookingDTO.getCheckOutDate(),
                    bookingDTO.getNumberOfGuests(),
                    bookingDTO.getId()
            ));
        } else {
            model.addAttribute("rooms", List.of());
            model.addAttribute("error", "Fyll i datum och antal personer först");
        }

        model.addAttribute("booking", bookingDTO);
        return "customers/bookings/form";
    }
}
