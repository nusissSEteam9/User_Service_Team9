package nus.iss.se.team9.user_service_team9.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ShoppingListItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column
	private String ingredientName;
	@Column
	private boolean isChecked;
	@ManyToOne
	@JsonIgnore
	private Member member;
	public ShoppingListItem() {}
	
	public ShoppingListItem(Member member, String ingredientName) {
		this.member = member;
		this.ingredientName = ingredientName;
		isChecked = false;
	}
}
