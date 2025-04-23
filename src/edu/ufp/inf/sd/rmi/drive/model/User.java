package edu.ufp.inf.sd.rmi.drive.model;

import edu.ufp.inf.sd.rmi.drive.server.WorkspaceRI;
import java.io.Serializable;

public class User implements Serializable {
    private String username;
    private String password;
    private WorkspaceRI workspace;

    public User(String username, String password, WorkspaceRI workspace) {
        this.username = username;
        this.password = password;
        this.workspace = workspace;
    }

    public void setWorkspace(WorkspaceRI workspace) {
        this.workspace = workspace;
    }

    public WorkspaceRI getWorkspace() {
        return workspace;
    }

    public String getUsername() {
        return username;
    }

    public boolean checkPassword(String inputPassword) {
        return password.equals(inputPassword);
    }
}
