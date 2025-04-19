package edu.ufp.inf.sd.rmi.drive.server;

import edu.ufp.inf.sd.rmi.drive.client.ObserverRI;
import edu.ufp.inf.sd.rmi.drive.model.User;
import edu.ufp.inf.sd.rmi.drive.model.UserStore;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class AuthImpl extends UnicastRemoteObject implements AuthRI, SubjectRI {

    private UserStore userStore;
    private final List<ObserverRI> observers;

    public AuthImpl() throws RemoteException {
        super();
        this.userStore = new UserStore();
        this.observers = new ArrayList<>();
    }

    @Override
    public boolean register(String username, String password) {
        boolean success = userStore.register(username, password, this);
        if (success) {
            try {
                notifyObservers("Novo utilizador registado: " + username);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    @Override
    public User login(String username, String password) {
        return userStore.login(username, password);
    }

    // Implementação do SubjectRI
    @Override
    public void attachObserver(ObserverRI observer) throws RemoteException {
        observers.add(observer);
        System.out.println("Observador ligado: " + observer);
    }

    @Override
    public void detachObserver(ObserverRI observer) throws RemoteException {
        observers.remove(observer);
        System.out.println("Observador desligado: " + observer);
    }

    @Override
    public void notifyObservers(String message) throws RemoteException {
        System.out.println("A notificar observadores...");
        for (ObserverRI observer : observers) {
            try {
                observer.update(message);
            } catch (RemoteException e) {
                System.out.println("Erro ao notificar um observador. Sera removido.");
                observers.remove(observer);
                break;
            }
        }
    }
}
