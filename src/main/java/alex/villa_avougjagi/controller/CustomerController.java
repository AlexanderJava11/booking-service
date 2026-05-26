package alex.villa_avougjagi.controller;

import alex.villa_avougjagi.models.Customer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @GetMapping
    public String listCustomers(Model model) {
        model.addAttribute("customers", customerService.findAll());
        return "customers/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("customer", new CustomerDTO());
        return "customers/form";
    }

    @PostMapping("/save")
    public String saveCustomer(@Valid @ModelAttribute("customer") CustomerDTO customerDTO, BindingResult result) {
        if (result.hasErrors()) {
            return "customers/form";
        }
        customerService.save(customerDTO);
        return "redirect:/customers";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        CustomerDTO customer = customerService.findById(id);

        if (customer == null) {
            return "redirect:/customers";
        }

        model.addAttribute("customer", customer);
        return "customers/form";
    }

}
