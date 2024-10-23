package nus.iss.se.team9.user_service_team9.repo;

import nus.iss.se.team9.user_service_team9.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Integer> {
	User findByUsername(String username);
	boolean existsByUsername(String username);
}
