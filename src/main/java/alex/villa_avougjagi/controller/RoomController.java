package alex.villa_avougjagi.controller;

import alex.villa_avougjagi.models.RoomType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public String listRooms(Model model) {
        model.addAttribute("rooms", roomService.findAll());
        return "customers/room/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("room", new RoomDTO());
        model.addAttribute("roomTypes", RoomType.values());
        return "customers/room/form";
    }

    @PostMapping("/save")
    public String saveRoom(@Valid @ModelAttribute("room") RoomDTO roomDTO,
                           BindingResult result,
                           Model model) {

        if (result.hasErrors()) {
            model.addAttribute("roomTypes", RoomType.values());
            return "customers/room/form";
        }

        roomService.save(roomDTO);
        return "redirect:/rooms";
    }
}
