package nus.iss.se.team9.user_service_team9.repo;


import nus.iss.se.team9.user_service_team9.enu.Status;
import nus.iss.se.team9.user_service_team9.model.Member;
import nus.iss.se.team9.user_service_team9.model.MemberReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberReportRepository extends JpaRepository<MemberReport,Integer>{

	List<MemberReport> findByMemberReportedAndStatus(Member member, Status approved);

	List<MemberReport> findByStatus(Status pending);

}
