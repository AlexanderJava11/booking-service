package alex.villa_avougjagi.service;

import alex.villa_avougjagi.dto.CustomerDTO;
import alex.villa_avougjagi.models.Customer;
import alex.villa_avougjagi.repositories.BookingRepository;
import alex.villa_avougjagi.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;

    public List<CustomerDTO> findAll() {
        return customerRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public CustomerDTO findById(Long id) {
        return customerRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    public void save(CustomerDTO dto) {
        Customer customer = toEntity(dto);
        customerRepository.save(customer);
    }

    public boolean delete(Long id) {
        if (bookingRepository.existsByCustomerId(id)) {
            return false;
        }

        customerRepository.deleteById(id);
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
