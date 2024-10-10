package nus.iss.se.team9.user_service_team9.service;

import jakarta.transaction.Transactional;
import nus.iss.se.team9.user_service_team9.enu.Status;
import nus.iss.se.team9.user_service_team9.model.Admin;
import nus.iss.se.team9.user_service_team9.model.Member;
import nus.iss.se.team9.user_service_team9.model.User;
import nus.iss.se.team9.user_service_team9.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    RecipeRepository recipeRepository;


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

    public boolean checkIfUserExist(String username) {
        return userRepository.findByUsername(username) != null;
    }

    public Set<String> getRandomUniqueTags(int count) {
        List<String> allTags = new ArrayList<>(getAllUniqueTags());
        Collections.shuffle(allTags, new Random());
        return allTags.stream().limit(count).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // get all unique tags
    public Set<String> getAllUniqueTags() {
        List<String> tagLists = recipeRepository.findAllDistinctTags();
        Set<String> uniqueTags = new HashSet<>();
        for (String tags : tagLists) {
            uniqueTags.addAll(Arrays.asList(tags.split(",")));
        }
        return uniqueTags;
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

    public Status getMemberStatus(Member member) {
        return member.getMemberStatus();
    }
}
