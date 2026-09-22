package alex.villa_avougjagi.client;

import alex.villa_avougjagi.dto.CustomerDTO;
import alex.villa_avougjagi.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class CustomerClient {

    private final RestTemplate restTemplate;
    private final String customersUrl;
    private final JwtService jwtService;

    public CustomerClient(
            @Value("${customer.service.url}") String baseUrl,
            JwtService jwtService) {

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);

        this.restTemplate = new RestTemplate(factory);
        this.customersUrl = baseUrl.replaceAll("/+$", "")
                + "/api/customers";
        this.jwtService = jwtService;
    }

    private HttpHeaders createHeaders() {
        String token = jwtService.generateToken("booking-service");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        return headers;
    }

    public List<CustomerDTO> findAll() {
        ResponseEntity<CustomerDTO[]> response = restTemplate.exchange(
                customersUrl,
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()),
                CustomerDTO[].class
        );

        CustomerDTO[] customers = response.getBody();

        return customers == null
                ? List.of()
                : Arrays.asList(customers);
    }

    public CustomerDTO findById(Long id) {
        ResponseEntity<CustomerDTO> response = restTemplate.exchange(
                customersUrl + "/" + id,
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()),
                CustomerDTO.class
        );

        return response.getBody();
    }

    public void save(CustomerDTO customer) {
        if (customer.getId() == null) {
            restTemplate.exchange(
                    customersUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(customer, createHeaders()),
                    CustomerDTO.class
            );
        } else {
            restTemplate.exchange(
                    customersUrl + "/" + customer.getId(),
                    HttpMethod.PUT,
                    new HttpEntity<>(customer, createHeaders()),
                    Void.class
            );
        }
    }

    public void delete(Long id) {
        restTemplate.exchange(
                customersUrl + "/" + id,
                HttpMethod.DELETE,
                new HttpEntity<>(createHeaders()),
                Void.class
        );
    }
}