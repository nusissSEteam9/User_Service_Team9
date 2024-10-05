package nus.iss.se.team9.user_service_team9.repo;

import nus.iss.se.team9.user_service_team9.enu.Status;
import nus.iss.se.team9.user_service_team9.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member,Integer>{
	List<Member> findByMemberStatusNot(Status status);

	Member findByUsername(String username);
}
