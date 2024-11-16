package nus.iss.se.team9.user_service_team9;

import nus.iss.se.team9.user_service_team9.model.Member;
import nus.iss.se.team9.user_service_team9.model.Status;

public class UserFactory {
    public static Member createMember(String username, String password, String email, Status status) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Username and Password are required.");
        }
        return new Member(username, password, email,status);
    }
}
