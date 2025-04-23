package edu.ufp.inf.sd.rmi.drive.server;

import edu.ufp.inf.sd.rmi.drive.model.User;
import edu.ufp.inf.sd.rmi.drive.model.UserStore;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthImpl extends UnicastRemoteObject implements AuthRI {

    private final Map<String, User> users;

    public AuthImpl() throws RemoteException {
        super();
        this.users = new ConcurrentHashMap<>();
    }

    @Override
    public boolean register(String username, String password) throws RemoteException {
        if (users.containsKey(username)) return false;

        WorkspaceRI workspace = new WorkspaceImpl(username, null, this);
        SubjectRI subject = new SubjectImpl(workspace);
        workspace = new WorkspaceImpl(username, subject, this);
        subject.setWorkspace(workspace);

        User user = new User(username, password, workspace);
        users.put(username, user);
        UserStore.getInstance().getUsers().put(username, user); // armazena o user completo

        return true;
    }

    @Override
    public SubjectRI login(String username, String password) throws RemoteException {
        User u = users.get(username);
        if (u != null && u.checkPassword(password)) {
            return u.getWorkspace().getSubject();
        }
        return null;
    }

    @Override
    public User getUser(String username) throws RemoteException {
        return UserStore.getInstance().getUser(username);
    }
}
