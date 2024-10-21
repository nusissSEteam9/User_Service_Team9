package nus.iss.se.team9.user_service_team9.service;

import jakarta.transaction.Transactional;
import nus.iss.se.team9.user_service_team9.model.Member;
import nus.iss.se.team9.user_service_team9.model.Recipe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
//<<<<<<< HEAD
//import org.springframework.http.HttpEntity;
//=======
//>>>>>>> acc1341adb6c163f373d1747775b446c06b0b024
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public Set<String> getAllUniqueTags() {
        String url = recipeServiceUrl + "/getAllUniqueTags";
        try {
            ResponseEntity<Set<String>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to fetch unique tags, status code: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error while fetching unique tags: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error: " + e.getMessage());
        }
    }
//<<<<<<< HEAD
//
//    public String createRecipe(Recipe recipe, Member member) {
//        String url = recipeServiceUrl + "/create";
//
//        // 准备请求体
//        HashMap<Object, Object> payload = new HashMap<>();
//        payload.put("name", recipe.getName());
//        payload.put("description", recipe.getDescription());
//        payload.put("servings", recipe.getServings());
//        payload.put("preparationTime", recipe.getPreparationTime());
//        payload.put("notes", recipe.getNotes());
//        payload.put("status", recipe.getStatus().toString());
//        payload.put("image", recipe.getImage());
//        payload.put("steps", recipe.getSteps());
//        payload.put("tags", recipe.getTags());
//
//
//        try {
//            HttpEntity<HashMap<Object, Object>> requestEntity = new HttpEntity<>(payload);
//
//            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
//
//            if (response.getStatusCode() == HttpStatus.OK) {
//                return "Recipe created successfully"; // 返回成功消息
//            } else {
//                throw new RuntimeException("Failed to create recipe");
//            }
//        } catch (HttpClientErrorException e) {
//            throw new RuntimeException("Error while adding recipe: " + e.getMessage());
//        } catch (Exception e) {
//            throw new RuntimeException("Unexpected error: " + e.getMessage());
//        }
//    }
//
//
//=======
//>>>>>>> acc1341adb6c163f373d1747775b446c06b0b024
}
