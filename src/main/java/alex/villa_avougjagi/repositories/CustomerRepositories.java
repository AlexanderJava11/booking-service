package alex.villa_avougjagi.repositories;

import alex.villa_avougjagi.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepositories extends JpaRepository<Customer, Long> {
}
