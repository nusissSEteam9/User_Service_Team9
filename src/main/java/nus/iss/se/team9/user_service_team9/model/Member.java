package nus.iss.se.team9.user_service_team9.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
public class Member extends User {
	@Column
	private Double height;
	@Column
	private Double weight;
	@Column
	private Integer age;
	@Column
	private LocalDate birthdate;
	@Column
	private String gender;
	@Column
	private Double calorieIntake;
	@Column
	private LocalDate registrationDate;
	@Column
	@Enumerated(EnumType.STRING)
	private Status memberStatus;
	@ElementCollection
	private List<String> preferenceList;

	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
//	@JsonManagedReference(value = "member-shoppingList")
	@JsonIgnore
	private List<ShoppingListItem> shoppingList;

<<<<<<< HEAD
//	@ManyToMany(mappedBy = "membersWhoSave")
//	@JsonBackReference(value = "members-savedRecipes")
	@ManyToMany
	@JoinTable(
			name = "recipe_members_who_save",
			joinColumns = @JoinColumn(name = "members_who_save_id"),
			inverseJoinColumns = @JoinColumn(name = "saved_recipes_id")
	)
=======
	@ManyToMany(mappedBy = "membersWhoSave")
//	@JsonBackReference(value = "members-savedRecipes")
>>>>>>> acc1341adb6c163f373d1747775b446c06b0b024
	@JsonIgnore
	private List<Recipe> savedRecipes;

	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
	@JsonManagedReference(value = "member-addedRecipes")
	private List<Recipe> addedRecipes;

	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
//	@JsonManagedReference (value = "member-reviews")
	@JsonIgnore
	private List<Review> reviews;

	@OneToMany(mappedBy = "member")
//	@JsonManagedReference (value = "member-reports")
	@JsonIgnore
	private List<Report> reports;

	@OneToMany(mappedBy = "memberReported")
	@JsonManagedReference (value = "member-reportsToMember")
	private List<MemberReport> reportsToMember;

	public Member() {}

	public Member(String username, String password, double height, double weight, LocalDate birthdate, String gender, String email) {
		super(username, password);
		this.height = height;
		this.weight = weight;
		this.birthdate = birthdate;
		this.age = calculateAge();
		this.gender = gender;
		this.calorieIntake = calculateCalorieIntake();
		this.setEmail(email);
		shoppingList = new ArrayList<>();
		savedRecipes = new ArrayList<>();
		addedRecipes = new ArrayList<>();
		reviews = new ArrayList<>();
		reports = new ArrayList<>();
		reportsToMember = new ArrayList<>();
		this.memberStatus = Status.CREATED;
		this.setRegistrationDate(LocalDate.now());
	}

	public int calculateAge() {
		LocalDate curDate = LocalDate.now();
		return Period.between(birthdate, curDate).getYears();
	}

	public Double calculateCalorieIntake() {
		// Using Harris-Benedict formula to calculate Basal Metabolic Rate
		double BMR = 0.0;
		if (gender.equals("Male")) {
			BMR = 66 + (13.7 * weight) + (5 * height) - (6.8 * age);
		} else {
			BMR = 655 + (9.6 * weight) + (1.8 * height) - (4.7 * age);
		}
		return (Math.round(BMR * 10) / 10.0);
	}
}