package nus.iss.se.team9.user_service_team9;

import nus.iss.se.team9.user_service_team9.dto.ReviewDTO;
import nus.iss.se.team9.user_service_team9.model.*;
import nus.iss.se.team9.user_service_team9.service.JwtService;
import nus.iss.se.team9.user_service_team9.service.RecipeService;
import nus.iss.se.team9.user_service_team9.service.ShoppingListItemService;
import nus.iss.se.team9.user_service_team9.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private RecipeService recipeService;

    @Mock
    private JwtService jwtService;

    @Mock
    private ShoppingListItemService shoppingListItemService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // Test: getMember
    @Test
    public void testGetMember_Success() throws Exception {
        Integer memberId = 1;
        Member member = new Member();
        member.setId(memberId);

        when(jwtService.extractId("token")).thenReturn(memberId);
        when(userService.getMemberById(memberId)).thenReturn(member);

        mockMvc.perform(get("/user/member").header("Authorization", "token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(memberId));
    }

    // Test: checkHealth
    @Test
    public void testCheckHealth() throws Exception {
        mockMvc.perform(get("/user/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("API is connected"));
    }

    // Test: viewUserProfile
    @Test
    public void testViewUserProfile_UserNotFound() throws Exception {
        Integer memberId = 1;
        when(userService.getMemberById(memberId)).thenReturn(null);

        mockMvc.perform(get("/user/profile/{id}", memberId).header("Authorization", "token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found or has been deleted."));
    }

    // Test: createMember
    @Test
    public void testCreateMember_Success() throws Exception {
        Member newMember = new Member();
        newMember.setId(1);

        when(userService.createMember("username", "password", "email")).thenReturn(newMember);

        mockMvc.perform(post("/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"username\", \"password\":\"password\", \"email\":\"email\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    // Test: validateLogin - invalid credentials
    @Test
    public void testValidateLogin_InvalidCredentials() throws Exception {
        when(userService.getUserByUsername("username")).thenReturn(null);

        mockMvc.perform(post("/user/validate-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"username\", \"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isValidLogin").value(false));
    }

    // Test: checkUsernameExist
    @Test
    public void testCheckUsernameExist() throws Exception {
        when(userService.CheckIfUsernameExist("username")).thenReturn(true);

        mockMvc.perform(get("/user/validate-username/username"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // Test: getAllMemberNotDeleted
    @Test
    public void testGetAllMemberNotDeleted() throws Exception {
        Member member = new Member();
        member.setId(1);
        when(userService.getAllMembersNotDeleted()).thenReturn(Collections.singletonList(member));

        mockMvc.perform(get("/user/getAllMembersNotDeleted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // Test: checkIfRecipeSaved
    @Test
    public void testCheckIfRecipeSaved() throws Exception {
        Integer memberId = 1;
        when(jwtService.extractId("token")).thenReturn(memberId);
        when(userService.CheckRecipeSavedStatus(1, memberId)).thenReturn(true);

        mockMvc.perform(get("/user/checkIfRecipeSaved")
                        .param("recipeId", "1")
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // Test: addRecipeToSaved
    @Test
    public void testAddRecipeToSaved_RecipeAlreadySaved() throws Exception {
        Integer memberId = 1;
        Recipe recipe = new Recipe();
        recipe.setId(1);
        Member member = new Member();
        member.setId(memberId);
        member.getSavedRecipes().add(recipe);

        when(jwtService.extractId("token")).thenReturn(memberId);
        when(userService.getMemberById(memberId)).thenReturn(member);
        when(recipeService.getRecipeById(1)).thenReturn(recipe);

        mockMvc.perform(post("/user/member/saveRecipe/1").header("Authorization", "token"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Recipe already saved"));
    }

    // Test: removeRecipeFromSaved
    @Test
    public void testRemoveRecipeFromSaved_RecipeNotSaved() throws Exception {
        Integer memberId = 1;
        Recipe recipe = new Recipe();
        recipe.setId(1);
        Member member = new Member();
        member.setId(memberId);

        when(jwtService.extractId("token")).thenReturn(memberId);
        when(userService.getMemberById(memberId)).thenReturn(member);
        when(recipeService.getRecipeById(1)).thenReturn(recipe);

        mockMvc.perform(post("/user/member/removeSavedRecipe/1").header("Authorization", "token"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Recipe not found in saved list"));
    }

    // Test: viewSavedList
    @Test
    public void testViewSavedList() throws Exception {
        Integer memberId = 1;
        Recipe recipe = new Recipe();
        recipe.setId(1);
        Member member = new Member();
        member.setId(memberId);
        member.getSavedRecipes().add(recipe);

        when(jwtService.extractId("token")).thenReturn(memberId);
        when(userService.getMemberById(memberId)).thenReturn(member);

        mockMvc.perform(get("/user/member/savedList").header("Authorization", "token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // Test: viewShoppingList
    @Test
    public void testViewShoppingList() throws Exception {
        Integer memberId = 1;
        ShoppingListItem item = new ShoppingListItem();
        item.setId(1);
        Member member = new Member();
        member.setId(memberId);
        member.getShoppingList().add(item);

        when(jwtService.extractId("token")).thenReturn(memberId);
        when(userService.getMemberById(memberId)).thenReturn(member);

        mockMvc.perform(get("/user/member/shoppingList/view").header("Authorization", "token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // Test: getMemberById
    @Test
    public void testGetMemberById_Success() throws Exception {
        Member member = new Member();
        member.setId(1);
        when(userService.getMemberById(1)).thenReturn(member);

        mockMvc.perform(get("/user/member/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // Test: deleteMemberById
    @Test
    public void testDeleteMemberById_MemberNotFound() throws Exception {
        when(userService.getMemberById(1)).thenReturn(null);

        mockMvc.perform(delete("/user/member/{id}", 1))
                .andExpect(status().isNotFound());
    }

    // Test: showSavedList
    @Test
    public void testShowSavedList_Unauthorized() throws Exception {
        when(jwtService.extractId("invalidToken")).thenReturn(null);

        mockMvc.perform(get("/user/member/savedList")
                        .header("Authorization", "invalidToken"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User is not logged in."));
    }

    // Test: showMyRecipeList
    @Test
    public void testShowMyRecipeList_NoDeletedRecipes() throws Exception {
        Integer memberId = 1;
        Member member = new Member();
        member.setId(memberId);
        Recipe recipe = new Recipe();
        recipe.setStatus(Status.CREATED);
        member.getAddedRecipes().add(recipe);

        when(jwtService.extractId("token")).thenReturn(memberId);
        when(userService.getMemberById(memberId)).thenReturn(member);

        mockMvc.perform(get("/user/member/myRecipeList")
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    // Test: showMyReviewList
    @Test
    public void testShowMyReviewList_Success() throws Exception {
        Integer memberId = 1;
        Member member = new Member();
        member.setId(memberId);
        Review review = new Review();
        review.setId(1);
        review.setComment("Good recipe");
        member.getReviews().add(review);

        when(jwtService.extractId("token")).thenReturn(memberId);
        when(userService.getMemberById(memberId)).thenReturn(member);

        mockMvc.perform(get("/user/member/myReview")
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comment").value("Good recipe"));
    }

    // Test: viewMemberProfile
    @Test
    public void testViewMemberProfile_Unauthorized() throws Exception {
        when(jwtService.extractId("invalidToken")).thenReturn(null);

        mockMvc.perform(get("/user/member/myProfile")
                        .header("Authorization", "invalidToken"))
                .andExpect(status().isUnauthorized());
    }

    // Test: saveProfile
    @Test
    public void testSaveProfile_Success() throws Exception {
        Integer memberId = 1;
        Member member = new Member();
        member.setId(memberId);

        when(jwtService.extractId("token")).thenReturn(memberId);

        mockMvc.perform(post("/user/member/saveProfile")
                        .header("Authorization", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newUser\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Profile updated successfully."));
    }

    // Test: getRecipeForShoppingList
    @Test
    public void testGetRecipeForShoppingList_NotFound() throws Exception {
        when(recipeService.getRecipeById(1)).thenReturn(null);

        mockMvc.perform(get("/user/member/shoppingList/add/{id}", 1))
                .andExpect(status().isNotFound());
    }

    // Test: addShoppingListIngredient
    @Test
    public void testAddShoppingListIngredient_InvalidData() throws Exception {
        Integer memberId = 1;
        Member member = new Member();
        member.setId(memberId);

        when(jwtService.extractId("token")).thenReturn(memberId);
        when(userService.getMemberById(memberId)).thenReturn(member);

        mockMvc.perform(post("/user/member/shoppingList/add")
                        .header("Authorization", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ingredientNames\":[],\"selectedIngredients\":[0]}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid ingredient selection"));
    }

    // Test: viewShoppingListIngredient
    @Test
    public void testViewShoppingListIngredient_Success() throws Exception {
        Integer memberId = 1;
        Member member = new Member();
        member.setId(memberId);
        ShoppingListItem item = new ShoppingListItem();
        item.setId(1);
        member.getShoppingList().add(item);

        when(jwtService.extractId("token")).thenReturn(memberId);
        when(userService.getMemberById(memberId)).thenReturn(member);

        mockMvc.perform(get("/user/member/shoppingList/view")
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // Test: editShoppingList
    @Test
    public void testEditShoppingList_Success() throws Exception {
        Integer memberId = 1;
        Member member = new Member();
        member.setId(memberId);
        ShoppingListItem item = new ShoppingListItem();
        item.setId(1);
        member.getShoppingList().add(item);

        when(jwtService.extractId("token")).thenReturn(memberId);
        when(userService.getMemberById(memberId)).thenReturn(member);

        mockMvc.perform(get("/user/member/shoppingList/edit")
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // Test: clearItems
    @Test
    public void testClearItems_Success() throws Exception {
        Integer memberId = 1;
        Member member = new Member();
        member.setId(memberId);

        when(jwtService.extractId("token")).thenReturn(memberId);
        when(userService.getMemberById(memberId)).thenReturn(member);

        mockMvc.perform(post("/user/member/shoppingList/clearItems")
                        .header("Authorization", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[1]}"))
                .andExpect(status().isOk());
    }

    // Test: addItem
    @Test
    public void testAddItem_Success() throws Exception {
        Integer memberId = 1;
        Member member = new Member();
        member.setId(memberId);

        when(jwtService.extractId("token")).thenReturn(memberId);
        when(userService.getMemberById(memberId)).thenReturn(member);

        mockMvc.perform(post("/user/member/shoppingList/addItem")
                        .header("Authorization", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ingredientName\":\"Tomato\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }
}
