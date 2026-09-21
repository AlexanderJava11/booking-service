package alex.villa_avougjagi.client;

import alex.villa_avougjagi.dto.CustomerDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class CustomerClient {

    private final RestTemplate restTemplate;
    private final String customersUrl;

    public CustomerClient(
            @Value("${customer.service.url}") String baseUrl) {

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);

        this.restTemplate = new RestTemplate(factory);
        this.customersUrl = baseUrl.replaceAll("/+$", "")
                + "/api/customers";
    }

    public List<CustomerDTO> findAll() {
        CustomerDTO[] customers = restTemplate.getForObject(
                customersUrl,
                CustomerDTO[].class
        );

        return customers == null
                ? List.of()
                : Arrays.asList(customers);
    }

    public CustomerDTO findById(Long id) {
        return restTemplate.getForObject(
                customersUrl + "/" + id,
                CustomerDTO.class
        );
    }

    public void save(CustomerDTO customer) {
        if (customer.getId() == null) {
            restTemplate.postForObject(
                    customersUrl,
                    customer,
                    CustomerDTO.class
            );
        } else {
            restTemplate.put(
                    customersUrl + "/" + customer.getId(),
                    customer
            );
        }
    }
    public void delete(Long id) {
        restTemplate.delete(
                customersUrl + "/" + id
        );
    }
}