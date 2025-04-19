package edu.ufp.inf.sd.rmi.drive.model;

import edu.ufp.inf.sd.rmi.drive.server.SubjectRI;

import java.io.Serializable;

public class User implements Serializable {
    private String username;
    private String password;
    private Workspace workspace;

    public User(String username, String password, SubjectRI subject) {
        this.username = username;
        this.password = password;
        this.workspace = new Workspace(username, subject);
    }

    public String getUsername() {
        return username;
    }

    public boolean checkPassword(String inputPassword) {
        return password.equals(inputPassword);
    }

    public Workspace getWorkspace() {
        return workspace;
    }
}
