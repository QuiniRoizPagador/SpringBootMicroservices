package io.javabrains.moviecatalogservice.resources;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import io.javabrains.moviecatalogservice.models.CatalogItem;
import io.javabrains.moviecatalogservice.models.Movie;
import io.javabrains.moviecatalogservice.models.UserRating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/catalog")
public class MovieCatalogResource {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Environment env;
    //@Autowired
    //private WebClient.Builder webClientBuilder;
    //@Autowired
    //private DiscoveryClient discoveryClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(MovieCatalogResource.class);

    @RequestMapping("/")
    public String home() {
        // This is useful for debugging
        // When having multiple instance of gallery service running at different ports.
        // We load balance among them, and display which instance received the request.
        return "Hello from Gallery Service running at port: " + env.getProperty("local.server.port");
    }

    @RequestMapping("/{userId}")
    @PreAuthorize("hasPermission('MovieCatalogResource','UPDATE')")
    @HystrixCommand(fallbackMethod = "fallback")
    public List<CatalogItem> getCatalog(@PathVariable("userId") String userId) {
        LOGGER.info("Creando listado de valoraciones....");
        UserRating ratings = restTemplate
                .getForObject("http://ratings-data-service/ratingsdata/users/" + userId, UserRating.class);
        LOGGER.info("Devolviendo catáloo de películas...");
        return ratings
                .getUserRating()
                .stream()
                .map(rating -> {
                    Movie movie = restTemplate.getForObject("http://movie-info-service/movies/" + rating.getMovieId(), Movie.class);

                    /*Movie movie = webClientBuilder.build()
                            .get()
                            .uri("http://localhost:8082/movies/" + rating.getMovieId())
                            .retrieve()
                            .bodyToMono(Movie.class)
                            .block();*/

                    return new CatalogItem(movie.getName(), "Desc", rating.getRating());
                })
                .collect(Collectors.toList());

    }

    // a fallback method to be called if failure happened
    public List<CatalogItem> fallback(@PathVariable("userId") String userId) {
        return Arrays.asList(new CatalogItem("No Catalog", "", 0));
    }
}
