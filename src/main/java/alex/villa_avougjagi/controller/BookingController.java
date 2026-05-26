package alex.villa_avougjagi.controller;

import alex.villa_avougjagi.dto.BookingDTO;
import alex.villa_avougjagi.service.BookingService;
import alex.villa_avougjagi.service.CustomerService;
import alex.villa_avougjagi.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @PostMapping("/save")
    public String saveBooking(@Valid @ModelAttribute("booking") BookingDTO bookingDTO,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("customers", customerService.findAll());
            model.addAttribute("rooms", List.of());
            return "customers/bookings/form";
        }

        boolean success = bookingService.saveBooking(bookingDTO);

        if (!success) {
            model.addAttribute("error", "Bokningen kunde inte sparas. Kontrollera datum, rum och antal personer.");
            model.addAttribute("customers", customerService.findAll());
            model.addAttribute("rooms", roomService.findAvailableRooms(
                    bookingDTO.getCheckInDate(),
                    bookingDTO.getCheckOutDate(),
                    bookingDTO.getNumberOfGuests(),
                    bookingDTO.getId()
            ));
            return "customers/bookings/form";
        }

        redirectAttributes.addFlashAttribute("message", "Bokningen sparades.");
        return "redirect:/bookings";
    }

    @GetMapping("/change/{id}")
    public String showChangeForm(@PathVariable Long id, Model model) {
        BookingDTO bookingDTO = bookingService.findById(id);

        if (bookingDTO == null) {
            return "redirect:/bookings";
        }

        model.addAttribute("booking", bookingDTO);
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("rooms", roomService.findAvailableRooms(
                bookingDTO.getCheckInDate(),
                bookingDTO.getCheckOutDate(),
                bookingDTO.getNumberOfGuests(),
                bookingDTO.getId()
        ));
        return "customers/bookings/form";
    }

    @GetMapping("/delete/{id}")
    public String deleteBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean deleted = bookingService.deleteById(id);

        if (deleted) {
            redirectAttributes.addFlashAttribute("message", "Bokningen togs bort.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Kunde inte ta bort bokningen.");
        }
        return "redirect:/bookings";
    }
}
