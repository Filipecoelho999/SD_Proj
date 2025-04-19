package edu.ufp.inf.sd.rmi.drive.model;

import edu.ufp.inf.sd.rmi.drive.server.SubjectRI;

import java.util.HashMap;
import java.util.Map;

public class UserStore {
    private Map<String, User> users = new HashMap<>();

    public boolean register(String username, String password, SubjectRI subject) {
        if (users.containsKey(username)) return false;
        users.put(username, new User(username, password, subject));
        return true;
    }

    public User login(String username, String password) {
        User user = users.get(username);
        if (user != null && user.checkPassword(password)) {
            return user;
        }
        return null;
    }
}
