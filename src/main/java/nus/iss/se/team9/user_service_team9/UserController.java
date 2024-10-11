package nus.iss.se.team9.user_service_team9;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import nus.iss.se.team9.user_service_team9.enu.Status;
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

    @PostMapping("/member/{memberId}/saveRecipe")
    public ResponseEntity<String> addRecipeToSaved(@PathVariable Integer memberId, @RequestBody Recipe recipe) {
        Member member = userService.getMemberById(memberId);
        if (member == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Member not found");
        }
        member.getSavedRecipes().add(recipe);
        userService.saveMember(member);
        return ResponseEntity.ok("Recipe saved successfully");
    }

    @PostMapping("/member/{memberId}/removeRecipe")
    public ResponseEntity<String> removeRecipeFromSaved(@PathVariable Integer memberId, @RequestBody Recipe recipe) {
        Member member = userService.getMemberById(memberId);
        if (member == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Member not found");
        }
        member.getSavedRecipes().remove(recipe);
        userService.saveMember(member);
        return ResponseEntity.ok("Recipe removed successfully");
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

    @PostMapping("/checkIfUsernameAvailable")
    public ResponseEntity<Map<String, Object>> checkIfUsernameAvailable(@RequestBody Map<String, Object> payload) {
        String username = (String) payload.get("username");
        Boolean userAlrExists = userService.checkIfUserExist(username);
        Map<String, Object> response = new HashMap<>();
        response.put("userAlrExists", userAlrExists);
        return ResponseEntity.ok(response);
    }

    // Set preferences
    @GetMapping("/setPreference")
    public ResponseEntity<Set<String>> setPreference() {
        Set<String> tags = userService.getRandomUniqueTags(7);
        return ResponseEntity.ok(tags);
    }

    @PostMapping("/setPreference")
    public ResponseEntity<String> receivePreference(@RequestParam(value = "tags", required = false) List<String> tags,@RequestHeader("Authorization") String token, HttpSession session) {
        List<String> oldTags = (List<String>) session.getAttribute("tags");
        Member member = userService.getMemberById(jwtService.extractId(token));
        if (oldTags == null) {
            member.setPreferenceList(tags);
        } else {
            Set<String> selectedTags = new HashSet<>(oldTags);
            selectedTags.addAll(tags);
            List<String> combinedTags = new ArrayList<>(selectedTags);
            member.setPreferenceList(combinedTags);
        }
        userService.saveMember(member);
        return ResponseEntity.ok("Preferences updated");
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshTags(@RequestParam("tags") List<String> tags, HttpSession session) {
        List<String> oldTags = (List<String>) session.getAttribute("tags");
        Set<String> newTags;

        if (oldTags == null) {
            session.setAttribute("tags", tags);
        } else {
            Set<String> selectedTags = new HashSet<>(oldTags);
            selectedTags.addAll(tags);
            List<String> combinedTags = new ArrayList<>(selectedTags);
            session.setAttribute("tags", combinedTags);
        }
        newTags = userService.getRandomUniqueTags(7);
        Map<String, Object> response = new HashMap<>();
        response.put("updatedTags", session.getAttribute("tags"));
        response.put("newTags", newTags);
        return ResponseEntity.ok(response);
    }

    // action for test
    @GetMapping("/member")
    public ResponseEntity<?> getMember(@RequestHeader("Authorization") String token) {
        Integer id = jwtService.extractId(token);
        if (id == null) {
            return ResponseEntity.status(401).body("User is not logged in.");
        }
        return ResponseEntity.ok(userService.getMemberById(id));
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

    // Member
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
        List<Review> reviews = member.getReviews();
        return ResponseEntity.ok(reviews);
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

    // Update shopping list item status
    @PostMapping("/member/shoppingList/updateCheckedStatus")
    public ResponseEntity<Void> updateCheckedStatus(@RequestBody Map<String, Object> payload) {
        int id = (int) payload.get("id");
        boolean isChecked = (boolean) payload.get("isChecked");
        ShoppingListItem shoppingListItem = shoppingListItemService.getShoppingListItemById(id);
        shoppingListItem.setChecked(isChecked);
        shoppingListItemService.saveShoppingListItem(shoppingListItem);
        return ResponseEntity.ok().build();
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
        String message = (String) payload.get("message");
        Member member = userService.getMemberById(jwtService.extractId(token));
        List<ShoppingListItem> shoppingList = member.getShoppingList();

        Iterator<ShoppingListItem> iterator = shoppingList.iterator();
        while (iterator.hasNext()) {
            ShoppingListItem item = iterator.next();
            if ((message.equals("clearChecked") && item.isChecked()) || message.equals("clearAll")) {
                iterator.remove();
                shoppingListItemService.deleteShoppingListItem(item);
            }
        }

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


