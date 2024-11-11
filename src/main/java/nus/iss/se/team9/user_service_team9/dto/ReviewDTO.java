package nus.iss.se.team9.user_service_team9.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDTO {
    private Integer reviewId;
    private String recipeName;
    private Integer recipeId;
    private Integer rating;
    private String comment;

    // Constructors
    public ReviewDTO(Integer reviewId, String recipeName, Integer recipeId, Integer rating, String comment) {
        this.reviewId = reviewId;
        this.recipeName = recipeName;
        this.recipeId = recipeId;
        this.rating = rating;
        this.comment = comment;
    }
}

