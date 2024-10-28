package nus.iss.se.team9.user_service_team9;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import nus.iss.se.team9.user_service_team9.dto.ReviewDTO;
import nus.iss.se.team9.user_service_team9.model.*;
import nus.iss.se.team9.user_service_team9.service.JwtService;
import nus.iss.se.team9.user_service_team9.service.RecipeService;
import nus.iss.se.team9.user_service_team9.service.ShoppingListItemService;
import nus.iss.se.team9.user_service_team9.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RecipeService recipeService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private ShoppingListItemService shoppingListItemService;

    // action for test
    @GetMapping("/member")
    public ResponseEntity<?> getMember(@RequestHeader("Authorization") String token) {
        Integer id = jwtService.extractId(token);
        if (id == null) {
            return ResponseEntity.status(401).body("User is not logged in.");
        }
        return ResponseEntity.ok(userService.getMemberById(id));
    }
    @GetMapping("/health")
    public String checkHealth(){
        return "API is connected";
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<Map<String, Object>> viewUserProfile(@PathVariable("id") Integer memberId,@RequestHeader("Authorization") String token) {
        Member member = userService.getMemberById(memberId);
        if (member == null || member.getMemberStatus() == Status.DELETED) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found or has been deleted."));
        }
        List<Recipe> publicRecipes = recipeService.getPublicRecipesByMember(member);
        Boolean isAdmin = jwtService.extractRole(token).equals("admin");
        Map<String, Object> response = new HashMap<>();
        response.put("member", member);
        response.put("publicRecipes", publicRecipes);
        response.put("ifAdmin", isAdmin);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/create")
    public ResponseEntity<Integer> createMember(@RequestBody Map<String, String> memberData) {
        try {
            String username = memberData.get("username");
            String password = memberData.get("password");
            String email = memberData.get("email");
            Member newMember = userService.createMember(username, password, email);
            return ResponseEntity.status(HttpStatus.OK).body(newMember.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @PostMapping("/validate-login")
    public ResponseEntity<Map<String, Object>> validateLogin(@RequestBody Map<String, String> credentials) {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            User user = userService.getUserByUsername(username);
            if (user != null && Objects.equals(user.getPassword(), password)) {
                String role = userService.checkIfAdmin(user) ? "admin" : "member";
                responseBody.put("isValidLogin", true);
                responseBody.put("role", role);
                responseBody.put("userId", user.getId());

                if ("member".equals(role)) {
                    Member member = userService.getMemberById(user.getId());
                    if (member != null && member.getMemberStatus() == Status.DELETED) {
                        responseBody.put("status", "deleted");
                    }
                }
            } else {
                responseBody.put("isValidLogin", false);
            }
            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            responseBody.put("error", "Internal server error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        }
    }
    @GetMapping("/validate-username/{username}")
    public ResponseEntity<Boolean> checkUsernameExist(@PathVariable String username) {
        System.out.println("Validate username");
        boolean exists = userService.CheckIfUsernameExist(username);
        return ResponseEntity.ok(exists);
    }
    @GetMapping("/getAllMembersNotDeleted")
    public ResponseEntity<List<Member>> getAllMemberNotDeleted(){
        List<Member> members = userService.getAllMembersNotDeleted();
        return ResponseEntity.ok(members);
    }

    @GetMapping("/checkIfRecipeSaved")
    public ResponseEntity<Boolean> checkIfRecipeSaved(@RequestParam("recipeId") Integer recipeId,@RequestHeader() String token) {
        boolean isSaved = userService.CheckRecipeSavedStatus(recipeId, jwtService.extractId(token));
        return new ResponseEntity<>(isSaved, HttpStatus.OK);
    }

    @PostMapping("/member/saveRecipe/{recipeId}")
    public ResponseEntity<String> addRecipeToSaved(@RequestHeader("Authorization") String token,
                                                   @PathVariable Integer recipeId) {
        Member member = userService.getMemberById(jwtService.extractId(token));
        Recipe recipe = recipeService.getRecipeById(recipeId);

        if (member == null || recipe == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }

        if (!member.getSavedRecipes().contains(recipe)) {
            member.getSavedRecipes().add(recipe);
            userService.saveMember(member);
            recipeService.updateRecipeNumberOfSaved(recipeId, "save");
            return ResponseEntity.ok("Recipe saved successfully");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Recipe already saved");
        }
    }

    @PostMapping("/member/removeSavedRecipe/{recipeId}")
    public ResponseEntity<String> removeRecipeFromSaved(@RequestHeader("Authorization") String token,
                                                        @PathVariable Integer recipeId) {
        Member member = userService.getMemberById(jwtService.extractId(token));
        Recipe recipe = recipeService.getRecipeById(recipeId);

        if (member == null || recipe == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }

        if (member.getSavedRecipes().contains(recipe)) {
            member.getSavedRecipes().remove(recipe);
            userService.saveMember(member);
            System.out.println(recipe.getNumberOfSaved());
            recipeService.updateRecipeNumberOfSaved(recipeId, "remove");
            return ResponseEntity.ok("Recipe removed successfully");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Recipe not found in saved list");
        }
    }

    @GetMapping("/member/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Integer id) {
        Member member = userService.getMemberById(id);
        System.out.println(member);
        if (member != null) {
            return ResponseEntity.ok(member);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @DeleteMapping("/member/{id}")
    public ResponseEntity<Void> deleteMemberById(@PathVariable Integer id) {
        Member member = userService.getMemberById(id);
        if (member != null) {
            member.setMemberStatus(Status.DELETED);
            userService.saveMember(member);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/member/savedList")
    public ResponseEntity<?> showSavedList(@RequestHeader("Authorization") String token) {
        Integer id = jwtService.extractId(token);
        if (id == null) {
            return ResponseEntity.status(401).body("User is not logged in.");
        }
        Member member = userService.getMemberById(id);
        List<Recipe> recipes = member.getSavedRecipes();
        return ResponseEntity.ok(recipes);
    }
    @GetMapping("/member/myRecipeList")
    public ResponseEntity<?> showMyRecipeList(@RequestHeader("Authorization") String token) {
        Integer id = jwtService.extractId(token);
        if (id == null) {
            return ResponseEntity.status(401).body("User is not logged in.");
        }
        Member member = userService.getMemberById(id);
        List<Recipe> recipes = member.getAddedRecipes().stream()
                .filter(r -> r.getStatus() != Status.DELETED)
                .collect(Collectors.toList());
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/member/myReview")
    public ResponseEntity<?> showMyReviewList(@RequestHeader("Authorization") String token) {
        Integer id = jwtService.extractId(token);
        if (id == null) {
            return ResponseEntity.status(401).body("User is not logged in.");
        }
        Member member = userService.getMemberById(id);
        List<ReviewDTO> reviewDTOs = member.getReviews().stream()
                .map(review -> new ReviewDTO(
                        review.getId(),
                        review.getRecipe().getName(),
                        review.getRecipe().getId(),
                        review.getRating(),
                        review.getComment()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(reviewDTOs);
    }

    @GetMapping("/member/myProfile")
    public ResponseEntity<Member> viewMemberProfile(@RequestHeader("Authorization") String token) {
        Integer id = jwtService.extractId(token);
        if (id == null) {
            return ResponseEntity.status(401).build();
        }
        Member member = userService.getMemberById(id);
        if (member == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(member);
    }
    @PostMapping("/member/saveProfile")
    public ResponseEntity<String> saveProfile(@RequestBody @Valid Member member, BindingResult bindingResult, @RequestHeader("Authorization") String token) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Profile data is invalid. Please check the input.");
        }
        Integer id = jwtService.extractId(token);
        if (id == null) {
            return ResponseEntity.status(401).body("User is not logged in.");
        }
        member.setId(id);
        userService.saveMember(member);
        return ResponseEntity.ok("Profile updated successfully.");
    }

    @GetMapping("/member/shoppingList/add/{id}")
    public ResponseEntity<Recipe> getRecipeForShoppingList(@PathVariable("id") int recipeId) {
        Recipe recipe = recipeService.getRecipeById(recipeId);
        if (recipe == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(recipe);
    }

    @PostMapping("/member/shoppingList/add")
    public ResponseEntity<String> addShoppingListIngredient(
            @RequestBody @Valid AddIngredientForm addIngredientForm,
            BindingResult bindingResult, @RequestHeader("Authorization") String token) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid ingredient data");
        }
        Member member = userService.getMemberById(jwtService.extractId(token));
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found or unauthorized");
        }

        List<String> ingredientNames = addIngredientForm.getIngredientNames();
        List<Integer> selectedIngredients = addIngredientForm.getSelectedIngredients();

        for (int pos : selectedIngredients) {
            if (pos >= 0 && pos < ingredientNames.size()) {
                String ingredientName = ingredientNames.get(pos);
                ShoppingListItem shoppingListItem = new ShoppingListItem(member, ingredientName);
                shoppingListItemService.saveShoppingListItem(shoppingListItem);
                member.getShoppingList().add(shoppingListItem);
            } else {
                return ResponseEntity.badRequest().body("Invalid ingredient selection");
            }
        }
        userService.saveMember(member);
        return ResponseEntity.ok("Ingredients added to shopping list");
    }

    // View the shopping list
    @GetMapping("/member/shoppingList/view")
    public ResponseEntity<List<ShoppingListItem>> viewShoppingListIngredient(@RequestHeader("Authorization") String token) {
        Member member = userService.getMemberById(jwtService.extractId(token));
        List<ShoppingListItem> shoppingList = member.getShoppingList();
        return ResponseEntity.ok(shoppingList);
    }

    @GetMapping("/member/shoppingList/edit")
    public ResponseEntity<List<ShoppingListItem>> editShoppingList(@RequestHeader("Authorization") String token) {
        Member member = userService.getMemberById(jwtService.extractId(token));
        List<ShoppingListItem> shoppingList = member.getShoppingList();
        return ResponseEntity.ok(shoppingList);
    }

    // Clear shopping list items
    @PostMapping("/member/shoppingList/clearItems")
    public ResponseEntity<Void> clearItems(@RequestBody Map<String, Object> payload, @RequestHeader("Authorization") String token) {
        List<Integer> ids= (List<Integer>) payload.get("ids");
        Member member = userService.getMemberById(jwtService.extractId(token));
        List<ShoppingListItem> itemsToDelete = shoppingListItemService.getShoppingListItemsByIdsAndMemberId(ids, member.getId());
        shoppingListItemService.deleteShoppingListItems(itemsToDelete);
        return ResponseEntity.ok().build();
    }

    // Add shopping list item manually
    @PostMapping("/member/shoppingList/addItem")
    public ResponseEntity<Map<String, Object>> addItem(@RequestBody Map<String, Object> payload, @RequestHeader("Authorization") String token) {
        String ingredientName = (String) payload.get("ingredientName");
        Member member = userService.getMemberById(jwtService.extractId(token));
        ShoppingListItem newItem = new ShoppingListItem(member, ingredientName);
        ShoppingListItem savedItem = shoppingListItemService.saveShoppingListItem(newItem);
        int id = savedItem.getId();

        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        return ResponseEntity.ok(response);
    }

}


