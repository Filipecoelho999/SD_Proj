package edu.ufp.inf.sd.rmi.drive.server;

import edu.ufp.inf.sd.rmi.drive.model.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AuthRI extends Remote {
    boolean register(String username, String password) throws RemoteException;
    SubjectRI login(String username, String password) throws RemoteException;

    // Novo método para resolver partilhas entre clientes
    User getUser(String username) throws RemoteException;
}
