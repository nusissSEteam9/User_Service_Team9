package nus.iss.se.team9.user_service_team9.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class AddIngredientForm {
	private List<String> ingredientNames;
	private List<Integer> selectedIngredients;
	
	public AddIngredientForm() {
		ingredientNames = new ArrayList<>();
		selectedIngredients = new ArrayList<>();
	}
}
