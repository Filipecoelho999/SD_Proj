package edu.ufp.inf.sd.rmi.drive.server;

import java.rmi.RemoteException;

public class SubjectFactory {

    public static SubjectRI createSubject() throws RemoteException {
        return new SubjectImpl(null);
    }

    public static SubjectRI createSubject(WorkspaceRI workspace) throws RemoteException {
        return new SubjectImpl(workspace);
    }
}
