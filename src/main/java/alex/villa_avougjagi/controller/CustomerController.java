package alex.villa_avougjagi.controller;

import alex.villa_avougjagi.client.CustomerClient;
import alex.villa_avougjagi.dto.CustomerDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerClient customerClient;

    @GetMapping
    public String listCustomers(Model model) {
        try {
            model.addAttribute("customers", customerClient.findAll());
        } catch (RestClientException exception) {
            model.addAttribute("customers", List.of());
            model.addAttribute(
                    "error",
                    "Kunde inte hämta kunder. Försök igen senare."
            );
        }

        return "customers/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("customer", new CustomerDTO());
        return "customers/form";
    }

    @PostMapping("/save")
    public String saveCustomer(
            @Valid @ModelAttribute("customer") CustomerDTO customerDTO,
            BindingResult result,
            RedirectAttributes redirect) {

        if (result.hasErrors()) {
            return "customers/form";
        }

        try {
            customerClient.save(customerDTO);

            redirect.addFlashAttribute(
                    "message",
                    "Kunduppgifterna sparades."
            );

            return "redirect:/customers";

        } catch (HttpClientErrorException.NotFound exception) {
            result.reject(
                    "customer.notFound",
                    "Kunden finns inte längre. Uppdatera kundlistan."
            );

        } catch (HttpClientErrorException.BadRequest exception) {
            result.reject(
                    "customer.invalid",
                    "Kundtjänsten godkände inte uppgifterna. Kontrollera fälten."
            );

        } catch (RestClientException exception) {
            result.reject(
                    "customer.unavailable",
                    "Kunde inte bekräfta att uppgifterna sparades. "
                            + "Kontrollera kundlistan innan du försöker igen."
            );
        }

        return "customers/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirect) {

        try {
            CustomerDTO customer = customerClient.findById(id);

            if (customer == null) {
                redirect.addFlashAttribute(
                        "error",
                        "Kundtjänsten returnerade inga kunduppgifter."
                );
                return "redirect:/customers";
            }

            model.addAttribute("customer", customer);
            return "customers/form";

        } catch (HttpClientErrorException.NotFound exception) {
            redirect.addFlashAttribute(
                    "error",
                    "Kunden kunde inte hittas."
            );

        } catch (RestClientException exception) {
            redirect.addFlashAttribute(
                    "error",
                    "Kunde inte hämta kunden. Försök igen senare."
            );
        }

        return "redirect:/customers";
    }

    @GetMapping("/delete/{id}")
    public String deleteCustomer(
            @PathVariable Long id,
            RedirectAttributes redirect) {

        redirect.addFlashAttribute(
                "error",
                "Borttagning är tillfälligt avstängd medan "
                        + "kontrollen av aktiva bokningar kopplas in."
        );

        return "redirect:/customers";
    }
}