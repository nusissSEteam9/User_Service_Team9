package nus.iss.se.team9.user_service_team9.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class MemberReport extends Report {
	@ManyToOne
	@JsonBackReference(value = "member-reportsToMember")
	private Member memberReported;
}
