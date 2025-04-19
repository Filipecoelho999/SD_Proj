package edu.ufp.inf.sd.rmi.drive.client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ObserverImpl extends UnicastRemoteObject implements ObserverRI {

    private final String username;

    public ObserverImpl(String username) throws RemoteException {
        super();
        this.username = username;
    }

    @Override
    public void update(String message) throws RemoteException {
        System.out.println("[Notificação para " + username + "] " + message);
    }
}
