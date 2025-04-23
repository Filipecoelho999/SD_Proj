package edu.ufp.inf.sd.rmi.drive.server;

import edu.ufp.inf.sd.rmi.drive.client.ObserverRI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SubjectRI extends Remote {
    void attachObserver(ObserverRI observer) throws RemoteException;
    void detachObserver(ObserverRI observer) throws RemoteException;
    void notifyObservers(String message) throws RemoteException;
    WorkspaceRI getWorkspace() throws RemoteException;
    void setWorkspace(WorkspaceRI workspace) throws RemoteException;
}