package nus.iss.se.team9.user_service_team9;

import nus.iss.se.team9.user_service_team9.model.Member;

public class UserFactory {
    public static Member createMember(String username, String password, String email) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Username and Password are required.");
        }
        return new Member(username, password, email);
    }
}
