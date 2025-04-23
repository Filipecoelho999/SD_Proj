package edu.ufp.inf.sd.rmi.drive.server;

import java.rmi.RemoteException;

public class WorkspaceFactory {
    public static WorkspaceRI createWorkspace(String username, SubjectRI subject, AuthRI authRI) {
        try {
            return new WorkspaceImpl(username, subject, authRI);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
