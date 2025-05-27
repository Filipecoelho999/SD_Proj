package edu.ufp.inf.sd.rmi.drive.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface AuthRI extends Remote {
    boolean register(String username, String password) throws RemoteException;
    FileManagerRI login(String username, String password, ObserverRI observer) throws RemoteException;
    List<String> getPartilhasRecebidas(String username) throws RemoteException;
    boolean adicionarPartilha(String targetUser, String pastaPartilhada) throws RemoteException;
    FileManagerRI getDrive(String username) throws RemoteException; // NOVO!
    boolean removerPartilha(String targetUser, String pastaPartilhada) throws RemoteException;

}
