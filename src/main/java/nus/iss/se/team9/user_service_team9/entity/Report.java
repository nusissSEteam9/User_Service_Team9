package nus.iss.se.team9.user_service_team9.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import nus.iss.se.team9.user_service_team9.model.Status;

@Getter
@Setter
@Entity
public abstract class Report {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@Column
	@NotBlank(message = "Reason is required")
	private String reason;
	@Enumerated(EnumType.STRING)
	private Status status;
	@ManyToOne
	@JsonIgnore
	private Member member;
	
}
