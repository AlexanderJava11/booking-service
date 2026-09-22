package alex.villa_avougjagi.client;

import alex.villa_avougjagi.dto.ReviewDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class ReviewClient {

    private final RestTemplate restTemplate;
    private final String reviewsUrl;

    public ReviewClient(@Value("${review.service.url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.reviewsUrl = baseUrl.replaceAll("/+$", "") + "/api/reviews";
    }

    public List<ReviewDTO> findAll() {
        ResponseEntity<List<ReviewDTO>> response = restTemplate.exchange(
                reviewsUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ReviewDTO>>() {}
        );

        return response.getBody();
    }

    public ReviewDTO findById(Long id) {
        return restTemplate.getForObject(
                reviewsUrl + "/" + id,
                ReviewDTO.class
        );
    }

    public ReviewDTO create(ReviewDTO review) {
        return restTemplate.postForObject(
                reviewsUrl,
                review,
                ReviewDTO.class
        );
    }

    public void update(Long id, ReviewDTO review) {
        restTemplate.put(
                reviewsUrl + "/" + id,
                review
        );
    }

    public void delete(Long id) {
        restTemplate.delete(
                reviewsUrl + "/" + id
        );
    }
}