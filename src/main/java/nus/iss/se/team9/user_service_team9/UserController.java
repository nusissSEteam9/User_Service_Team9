package nus.iss.se.team9.user_service_team9;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import nus.iss.se.team9.user_service_team9.enu.Status;
import nus.iss.se.team9.user_service_team9.model.*;
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
    private ShoppingListItemService shoppingListItemService;

    //Non-Member
    //在前端verify成功后跳转
    @PostMapping("/verifyEmail")
    public ResponseEntity<?> verifyEmail(@RequestBody Member member, HttpSession httpSession) {
        userService.saveMember(member);
        httpSession.setAttribute("userId", member.getId());
        if (member.getPerfenceList() == null || member.getPerfenceList().isEmpty()) {
            return ResponseEntity.ok("Email verified. Please set your preferences.");
        }
        return ResponseEntity.ok("Email verified successfully.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload, HttpSession sessionObj) {
        String username = payload.get("username");
        String password = payload.get("password");
        User user = userService.getUserByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            return ResponseEntity.status(401).body("Invalid username or password.");
        }else if(userService.checkIfAdmin(user)){
            sessionObj.setAttribute("userId", user.getId());
            sessionObj.setAttribute("userType", "admin");
            sessionObj.setAttribute("isLoggedIn", true);
            return ResponseEntity.ok("Admin login successful.");
        }else {
            Member member = userService.getMemberById(user.getId());
            switch (member.getMemberStatus()) {
                case DELETED:
                    return ResponseEntity.status(401).body("User account has been deleted.");
                case PENDING:
                    return ResponseEntity.status(401).body("Please verify your email first.");
                case REJECTED:
                    return ResponseEntity.status(401).body("User account has been suspended.");
                default:
                    sessionObj.setAttribute("userId", user.getId());
                    sessionObj.setAttribute("userType", "member");
                    sessionObj.setAttribute("isLoggedIn", true);
                    return ResponseEntity.ok("Login successful.");
            }
        }
    }

    @GetMapping("/sessionInfo")
    public ResponseEntity<?> getSessionInfo(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        String userType = (String) session.getAttribute("userType");
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");

        if (userId == null || userType == null) {
            return ResponseEntity.status(401).body("No session available or user not logged in.");
        }

        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("userId", userId);
        sessionInfo.put("userType", userType);
        sessionInfo.put("isLoggedIn", isLoggedIn);
        return ResponseEntity.ok(sessionInfo);
    }

    // 查看用户的profile
    @GetMapping("/profile/{id}")
    public ResponseEntity<Map<String, Object>> viewUserProfile(@PathVariable("id") Integer memberId, HttpSession sessionObj) {
        Member member = userService.getMemberById(memberId);
        if (member == null || member.getMemberStatus() == Status.DELETED) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found or has been deleted."));
        }
        List<Recipe> publicRecipes = recipeService.getAllRecipesByMember(member, Status.PUBLIC);
        Boolean isAdmin = sessionObj.getAttribute("userId") != null && sessionObj.getAttribute("userType").equals("admin");
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
    public ResponseEntity<String> receivePreference(@RequestParam(value = "tags", required = false) List<String> tags, HttpSession session) {
        List<String> oldTags = (List<String>) session.getAttribute("tags");
        Member member = userService.getMemberById((int) session.getAttribute("userId"));
        if (oldTags == null) {
            member.setPrefenceList(tags);
        } else {
            Set<String> selectedTags = new HashSet<>(oldTags);
            selectedTags.addAll(tags);
            List<String> combinedTags = new ArrayList<>(selectedTags);
            member.setPrefenceList(combinedTags);
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


    // Member
    @GetMapping("/member/savedList")
    public ResponseEntity<?> showSavedList(HttpSession sessionObj) {
        Integer id = (Integer) sessionObj.getAttribute("userId");
        if (id == null) {
            return ResponseEntity.status(401).body("User is not logged in.");
        }
        Member member = userService.getMemberById(id);
        List<Recipe> recipes = member.getSavedRecipes();
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/member/myRecipeList")
    public ResponseEntity<?> showMyRecipeList(HttpSession sessionObj) {
        Integer id = (Integer) sessionObj.getAttribute("userId");
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
    public ResponseEntity<?> showMyReviewList(HttpSession sessionObj) {
        Integer id = (Integer) sessionObj.getAttribute("userId");
        if (id == null) {
            return ResponseEntity.status(401).body("User is not logged in.");
        }
        Member member = userService.getMemberById(id);
        List<Review> reviews = member.getReviews();
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/member/myProfile")
    public ResponseEntity<Member> viewMemberProfile(HttpSession sessionObj) {
        Integer id = (Integer) sessionObj.getAttribute("userId");
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
    public ResponseEntity<String> saveProfile(@RequestBody @Valid Member member, BindingResult bindingResult, HttpSession sessionObj) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Profile data is invalid. Please check the input.");
        }
        Integer userId = (Integer) sessionObj.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body("User is not logged in.");
        }
        member.setId(userId);
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
            BindingResult bindingResult, HttpSession sessionObj) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid ingredient data");
        }
        Member member = userService.getMemberById((int) sessionObj.getAttribute("userId"));
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
    public ResponseEntity<List<ShoppingListItem>> viewShoppingListIngredient(HttpSession sessionObj) {
        Member member = userService.getMemberById((int) sessionObj.getAttribute("userId"));
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
    public ResponseEntity<List<ShoppingListItem>> editShoppingList(HttpSession sessionObj) {
        Member member = userService.getMemberById((int) sessionObj.getAttribute("userId"));
        List<ShoppingListItem> shoppingList = member.getShoppingList();
        return ResponseEntity.ok(shoppingList);
    }

    // Clear shopping list items
    @PostMapping("/member/shoppingList/clearItems")
    public ResponseEntity<Void> clearItems(@RequestBody Map<String, Object> payload, HttpSession sessionObj) {
        String message = (String) payload.get("message");
        Member member = userService.getMemberById((int) sessionObj.getAttribute("userId"));
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
    public ResponseEntity<Map<String, Object>> addItem(@RequestBody Map<String, Object> payload, HttpSession sessionObj) {
        String ingredientName = (String) payload.get("ingredientName");
        Member member = userService.getMemberById((int) sessionObj.getAttribute("userId"));
        ShoppingListItem newItem = new ShoppingListItem(member, ingredientName);
        ShoppingListItem savedItem = shoppingListItemService.saveShoppingListItem(newItem);
        int id = savedItem.getId();

        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        return ResponseEntity.ok(response);
    }
}


