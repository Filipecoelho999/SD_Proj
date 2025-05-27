package edu.ufp.inf.sd.rmi.drive.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class SubjectImpl extends UnicastRemoteObject implements SubjectRI {

    private final Map<String, ObserverRI> observers;

    public SubjectImpl() throws RemoteException {
        super();
        this.observers = new HashMap<>();
    }

    @Override
    public void attachObserver(ObserverRI observer) throws RemoteException {
        if (FileManager.MODO_PROPAGACAO.equalsIgnoreCase("rmiserver")) {
            observers.put(observer.toString(), observer);
            System.out.println("[SubjectImpl] Observer adicionado: " + observer.getUsername());
        }
    }

    @Override
    public void detachObserver(ObserverRI observer) throws RemoteException {
        if (FileManager.MODO_PROPAGACAO.equalsIgnoreCase("rmiserver") && observer != null) {
            String username = observer.getUsername();
            observers.remove(username);
            System.out.println("[SubjectImpl] Observer removido: " + username);
        }
    }
    @Override
    public void notifyObservers(String message) throws RemoteException {
        if (FileManager.MODO_PROPAGACAO.equalsIgnoreCase("rmiserver")) {
            for (ObserverRI observer : observers.values()) {
                observer.update(message);
            }
            System.out.println("[SubjectImpl] Notificados " + observers.size() + " observers.");
        }
    }

    @Override
    public Map<String, ObserverRI> getObservers() throws RemoteException {
        return observers;
    }
}
