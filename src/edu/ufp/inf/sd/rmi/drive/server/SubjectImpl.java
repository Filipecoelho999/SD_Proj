package edu.ufp.inf.sd.rmi.drive.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SubjectImpl extends UnicastRemoteObject implements SubjectRI {

    private final Map<String, ObserverRI> observers = new ConcurrentHashMap<>();

    public SubjectImpl() throws RemoteException {
        super();
    }
    @Override
    public Map<String, ObserverRI> getObservers() throws RemoteException {
        return observers;
    }


    @Override
    public void attachObserver(ObserverRI observer) throws RemoteException {
        observers.put(observer.getUsername(), observer);
        System.out.println("[DEBUG Subject] Observer ligado: " + observer.getUsername());
    }

    @Override
    public void detachObserver(ObserverRI observer) throws RemoteException {
        observers.remove(observer.getUsername());
        System.out.println("[DEBUG Subject] Observer removido: " + observer.getUsername());
    }

    @Override
    public void notifyObservers(String message) throws RemoteException {
        for (ObserverRI observer : observers.values()) {
            observer.update(message);
            System.out.println("[DEBUG Subject] Update enviado para: " + observer.getUsername());
        }
    }
}
