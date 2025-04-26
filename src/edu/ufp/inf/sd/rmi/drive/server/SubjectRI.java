package edu.ufp.inf.sd.rmi.drive.server;
import edu.ufp.inf.sd.rmi.drive.server.ObserverRI;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface SubjectRI extends Remote {
    void attachObserver(ObserverRI observer) throws RemoteException;
    void detachObserver(ObserverRI observer) throws RemoteException;
    void notifyObservers(String message) throws RemoteException;
    Map<String, ObserverRI> getObservers() throws RemoteException;
}
