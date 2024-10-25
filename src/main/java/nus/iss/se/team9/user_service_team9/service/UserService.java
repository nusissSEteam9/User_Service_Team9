package nus.iss.se.team9.user_service_team9.service;

import jakarta.transaction.Transactional;
import nus.iss.se.team9.user_service_team9.entity.*;
import nus.iss.se.team9.user_service_team9.model.Status;
import nus.iss.se.team9.user_service_team9.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Transactional
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    private RecipeService recipeService;


    public void saveMember(Member member) {
        memberRepository.save(member);
    }

    public Member createMember(String username, String password, String email){
        Member newMember = new Member();
        newMember.setMemberStatus(Status.CREATED);
        newMember.setUsername(username);
        newMember.setPassword(password);
        newMember.setEmail(email);
        return memberRepository.save(newMember);
    }
    // Searching and Filtering methods
    public Member getMemberById(Integer id) {
        Optional<Member> member = memberRepository.findById(id);
        return member.orElse(null);
    }

    public List<Member> getAllMembersNotDeleted(){
        return memberRepository.findByMemberStatusNot(Status.DELETED);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean checkIfAdmin(User user) {
        if (user instanceof Admin) {
            return true;
        } else if (user instanceof Member) {
            return false;
        }
        return false;
    }

    public boolean CheckIfUsernameExist(String username) {
        return userRepository.existsByUsername(username);
    }
}
