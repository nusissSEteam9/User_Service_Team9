package nus.iss.se.team9.user_service_team9.service;

import jakarta.transaction.Transactional;
import nus.iss.se.team9.user_service_team9.model.Member;
import nus.iss.se.team9.user_service_team9.model.Recipe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class RecipeService {

    private final String recipeServiceUrl;
    private final RestTemplate restTemplate;
    @Autowired
    public RecipeService(RestTemplate restTemplate, @Value("${recipe.service.url}") String recipeServiceUrl) {
        this.recipeServiceUrl =recipeServiceUrl;
        this.restTemplate =restTemplate;
    }

    public List<Recipe> getPublicRecipesByMember(Member member) {
        String url = recipeServiceUrl + "/getPublicRecipesByMemberId/" + member.getId();

        try {
            ResponseEntity<List<Recipe>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Recipes not found or error, status code: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RuntimeException("Recipes for member with ID " + member.getId() + " not found.");
            } else {
                throw new RuntimeException("Error while fetching recipes: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error: " + e.getMessage());
        }
    }

    // get specific recipe by id
    public Recipe getRecipeById(Integer id) {
        String url = recipeServiceUrl + "/" + id;
        try {
            ResponseEntity<Recipe> response = restTemplate.getForEntity(url, Recipe.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Recipe not found or deleted, status code: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RuntimeException("Recipe with ID " + id + " not found.");
            } else {
                throw new RuntimeException("Error while fetching recipe: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error: " + e.getMessage());
        }
    }

    public void updateRecipeNumberOfSaved(Integer recipeId, String operation) {
        String url = recipeServiceUrl + "/setNumberOfSaved/" + recipeId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(operation, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Success: " + response.getBody());
            } else {
                System.out.println("Failed with status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Error during REST call: " + e.getMessage());
        }
    }
}
