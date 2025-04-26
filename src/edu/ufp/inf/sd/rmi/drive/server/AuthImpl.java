package edu.ufp.inf.sd.rmi.drive.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class AuthImpl extends UnicastRemoteObject implements AuthRI {

    private final Map<String, String> users = new HashMap<>();
    private final Map<String, FileManagerRI> drives = new HashMap<>();
    private final Map<String, List<String>> partilhasRecebidas = new HashMap<>();

    public AuthImpl() throws RemoteException {
        super();
    }

    @Override
    public boolean register(String username, String password) throws RemoteException {
        if (users.containsKey(username)) {
            return false;
        }
        users.put(username, password);
        drives.put(username, new FileManager(username, this));
        partilhasRecebidas.put(username, new ArrayList<>());
        System.out.println("Utilizador registado: " + username);
        return true;
    }

    @Override
    public FileManagerRI login(String username, String password, ObserverRI observer) throws RemoteException {
        if (!users.containsKey(username) || !users.get(username).equals(password)) {
            return null;
        }
        FileManagerRI fm = drives.get(username);
        fm.getSubject().attachObserver(observer);
        System.out.println("Login efetuado: " + username);
        return fm;
    }

    @Override
    public boolean adicionarPartilha(String targetUser, String pastaPartilhada) throws RemoteException {
        if (!partilhasRecebidas.containsKey(targetUser)) {
            partilhasRecebidas.put(targetUser, new ArrayList<>());
        }
        return partilhasRecebidas.get(targetUser).add(pastaPartilhada);
    }

    @Override
    public List<String> getPartilhasRecebidas(String username) throws RemoteException {
        return partilhasRecebidas.getOrDefault(username, new ArrayList<>());
    }

    @Override
    public FileManagerRI getDrive(String username) throws RemoteException {
        return drives.get(username);
    }
    @Override
    public boolean removerPartilha(String targetUser, String pastaPartilhada) throws RemoteException {
        if (partilhasRecebidas.containsKey(targetUser)) {
            return partilhasRecebidas.get(targetUser).remove(pastaPartilhada);
        }
        return false;
    }
}
