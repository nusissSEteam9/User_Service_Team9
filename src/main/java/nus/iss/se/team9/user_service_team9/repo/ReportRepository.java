package nus.iss.se.team9.user_service_team9.repo;


import nus.iss.se.team9.user_service_team9.enu.Status;
import nus.iss.se.team9.user_service_team9.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report,Integer>{
	long countByStatus(Status status);
}
