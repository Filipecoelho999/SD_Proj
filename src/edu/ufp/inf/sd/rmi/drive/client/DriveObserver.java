package edu.ufp.inf.sd.rmi.drive.client;

import edu.ufp.inf.sd.rmi.drive.server.ObserverRI;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class DriveObserver extends UnicastRemoteObject implements ObserverRI {

    private final String username;

    public DriveObserver(String username) throws RemoteException {
        this.username = username;
    }

    @Override
    public void update(String message) throws RemoteException {
        System.out.println("[rmi][" + username + "] " + message);
    }

    @Override
    public String getUsername() throws RemoteException {
        return this.username; // ou qualquer campo que tenhas guardado no observer
    }
}
