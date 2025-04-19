package edu.ufp.inf.sd.rmi.drive.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ObserverRI extends Remote {
    void update(String message) throws RemoteException;
}
