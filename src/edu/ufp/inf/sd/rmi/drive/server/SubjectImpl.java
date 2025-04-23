// Classe SubjectImpl.java atualizada
package edu.ufp.inf.sd.rmi.drive.server;

import edu.ufp.inf.sd.rmi.drive.client.ObserverRI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class SubjectImpl extends UnicastRemoteObject implements SubjectRI {

    private final List<ObserverRI> observers = new ArrayList<>();
    private WorkspaceRI workspace;

    public SubjectImpl(WorkspaceRI workspace) throws RemoteException {
        super();
        this.workspace = workspace;
    }

    @Override
    public void attachObserver(ObserverRI observer) throws RemoteException {
        observers.add(observer);
        System.out.println("Observer ligado: " + observer);
    }

    @Override
    public void detachObserver(ObserverRI observer) throws RemoteException {
        observers.remove(observer);
        System.out.println("Observer removido: " + observer);
    }

    @Override
    public void notifyObservers(String message) throws RemoteException {
        for (ObserverRI o : observers) {
            o.update(message);
        }
    }

    @Override
    public WorkspaceRI getWorkspace() throws RemoteException {
        return workspace;
    }

    @Override
    public void setWorkspace(WorkspaceRI ws) throws RemoteException {
        this.workspace = ws;
    }
} 