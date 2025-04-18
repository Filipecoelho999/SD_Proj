package edu.ufp.inf.sd.rmi.drive.model;

import java.util.HashMap;
import java.util.Map;

public class UserStore {

    private Map<String, String> users = new HashMap<>();


    public boolean register(String username, String password) {
        if (users.containsKey(username)) {
            return false; // User already exists
        }
        users.put(username, password);
        return true; // Registration successful
    }

    public boolean login(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(password);
    }
}
