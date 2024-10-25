package nus.iss.se.team9.user_service_team9.repo;

import nus.iss.se.team9.user_service_team9.entity.*;
import nus.iss.se.team9.user_service_team9.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member,Integer>{
	List<Member> findByMemberStatusNot(Status status);

	Member findByUsername(String username);
}
