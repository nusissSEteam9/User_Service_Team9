package nus.iss.se.team9.user_service_team9.service;

import jakarta.transaction.Transactional;
import nus.iss.se.team9.user_service_team9.enu.Status;
import nus.iss.se.team9.user_service_team9.model.Member;
import nus.iss.se.team9.user_service_team9.model.Recipe;
import nus.iss.se.team9.user_service_team9.repo.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RecipeService {
    @Autowired
    RecipeRepository recipeRepo;
    public List<Recipe> getAllRecipesByMember(Member member, Status status) {
        return recipeRepo.findByMember(member, status);
    }
    // get specific recipe by id
    public Recipe getRecipeById(Integer id) {
        Optional<Recipe> recipe = recipeRepo.findById(id);
        return recipe.orElse(null);
    };
}
