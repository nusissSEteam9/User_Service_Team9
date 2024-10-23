package nus.iss.se.team9.user_service_team9.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Setter
@Getter
@Entity
public class Review {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column
	private Integer recipeId;
	@Column
	private Integer rating;
	@Column(length = 1200)
	private String comment;
	@Column
	private LocalDate reviewDate;
    @ManyToOne
	@JsonIgnore
	private Member member;
	
	public Review() {
		setReviewDate(LocalDate.now());
	}
	
	public Review(int rating, String comment, Member member, Integer recipeId) {
		this.rating = rating;
		this.comment = comment;
		this.member = member;
		this.recipeId = recipeId;
	}
}
