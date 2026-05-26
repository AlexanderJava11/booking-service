package alex.villa_avougjagi.controller;

import alex.villa_avougjagi.dto.BookingDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("booking", new BookingDTO());

        return "customers/home";
    }
}