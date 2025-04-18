package edu.ufp.inf.sd.rmi.drive.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AuthRI extends Remote {
    boolean register(String username, String password) throws RemoteException;
    boolean login(String username, String password) throws RemoteException;
}
