package nus.iss.se.team9.user_service_team9.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class RecipeReport extends Report{
	private Integer recipeReportedId;
}
