package alex.villa_avougjagi.service;

import alex.villa_avougjagi.dto.CustomerDTO;
import alex.villa_avougjagi.models.Customer;
import alex.villa_avougjagi.repositories.BookingRepositories;
import alex.villa_avougjagi.repositories.CustomerRepositories;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepositories customerRepositories;
    private final BookingRepositories bookingRepositories;

    public List<CustomerDTO> findAll() {
        return customerRepositories.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public CustomerDTO findById(Long id) {
        return customerRepositories.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    public void save(CustomerDTO dto) {
        Customer customer = toEntity(dto);
        customerRepositories.save(customer);
    }

    public boolean delete(Long id) {
        if (bookingRepositories.existsByCustomerId(id)) {
            return false;
        }

        customerRepositories.deleteById(id);
        return true;
    }

    private CustomerDTO toDTO(Customer customer) {
        return CustomerDTO.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .build();
    }

    private Customer toEntity(CustomerDTO dto) {
        return Customer.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .build();
    }
}
