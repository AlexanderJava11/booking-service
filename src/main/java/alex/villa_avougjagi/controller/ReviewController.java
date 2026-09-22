package alex.villa_avougjagi.controller;

import alex.villa_avougjagi.client.ReviewClient;
import alex.villa_avougjagi.dto.ReviewDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewClient reviewClient;

    @GetMapping
    public String listReviews(Model model) {
        model.addAttribute("reviews", reviewClient.findAll());
        model.addAttribute("review", new ReviewDTO());
        return "reviews";
    }

    @PostMapping("/save")
    public String saveReview(
            @Valid @ModelAttribute("review") ReviewDTO review,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("reviews", reviewClient.findAll());
            return "reviews";
        }

        if (review.getId() == null) {
            reviewClient.create(review);
        } else {
            reviewClient.update(review.getId(), review);
        }

        return "redirect:/reviews";
    }

    @GetMapping("/edit/{id}")
    public String editReview(@PathVariable Long id, Model model) {
        model.addAttribute("review", reviewClient.findById(id));
        model.addAttribute("reviews", reviewClient.findAll());
        return "reviews";
    }

    @PostMapping("/delete/{id}")
    public String deleteReview(@PathVariable Long id) {
        reviewClient.delete(id);
        return "redirect:/reviews";
    }
}