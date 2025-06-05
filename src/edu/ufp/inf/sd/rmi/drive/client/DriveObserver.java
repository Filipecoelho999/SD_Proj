package edu.ufp.inf.sd.rmi.drive.client;
import edu.ufp.inf.sd.rmi.drive.server.ObserverRI;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

// Implementa a interface ObserverRI do lado do cliente.
// Recebe notificações do servidor em tempo real (via RMI) quando há alterações relevantes.
// É usado pelo DriveClient após o login para escutar mudanças no sistema de ficheiros.

public class DriveObserver extends UnicastRemoteObject implements ObserverRI {

    private final String username;

    public DriveObserver(String username) throws RemoteException {
        this.username = username;
    }

    @Override
    public void update(String message) throws RemoteException {
        System.out.println("[rmi][" + username + "] " + message);
    }

    @Override
    public String getUsername() throws RemoteException {
        return this.username; // ou qualquer campo que tenhas guardado no observer
    }
}
