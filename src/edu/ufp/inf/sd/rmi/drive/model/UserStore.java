package edu.ufp.inf.sd.rmi.drive.model;

import java.util.HashMap;
import java.util.Map;

public class UserStore {
    private static final UserStore INSTANCE = new UserStore();

    private final Map<String, User> users = new HashMap<>();

    private UserStore() {}

    public static UserStore getInstance() {
        return INSTANCE;
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public User getUser(String username) {
        return users.get(username);
    }
}
