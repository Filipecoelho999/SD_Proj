package edu.ufp.inf.sd.rmi.drive.server;
import edu.ufp.inf.sd.rmi.drive.model.User;
import java.rmi.Remote;
import java.rmi.RemoteException;


public interface AuthRI extends Remote {
    boolean register(String username, String password) throws RemoteException;
    User login(String username, String password) throws RemoteException;
}
