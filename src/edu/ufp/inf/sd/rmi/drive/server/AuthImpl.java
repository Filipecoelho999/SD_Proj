package edu.ufp.inf.sd.rmi.drive.server;

import edu.ufp.inf.sd.rmi.drive.model.UserStore;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class AuthImpl extends UnicastRemoteObject implements AuthRI {
    private final UserStore userStore;

    public AuthImpl() throws RemoteException {
        super();
        userStore = new UserStore();
    }

    @Override
    public boolean register(String username, String password) {
        return userStore.register(username, password);
    }

    @Override
    public boolean login(String username, String password) {
        return userStore.login(username, password);
    }
}
