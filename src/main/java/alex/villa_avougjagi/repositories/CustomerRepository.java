package alex.villa_avougjagi.repositories;

import alex.villa_avougjagi.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
