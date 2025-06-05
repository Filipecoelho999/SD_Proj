package edu.ufp.inf.sd.rmi.drive.server;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

// Implementa a interface ObserverRI do lado do servidor.
// Atua como representador do cliente ligado, que será notificado por um Subject.
// É usado pelo SubjectImpl para enviar mensagens de evento (propagação síncrona).

public class ObserverImpl extends UnicastRemoteObject implements ObserverRI {

    private final String username;

    public ObserverImpl(String username) throws RemoteException {
        super();
        this.username = username;
    }

    @Override
    public void update(String message) throws RemoteException {
        System.out.println("Notificação recebida: " + message);
    }

    @Override
    public String getUsername() throws RemoteException {
        return username;
    }
}
