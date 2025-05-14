package edu.ufp.inf.sd.rmi.drive.server;

import edu.ufp.inf.sd.rmi.drive.session.Session;
import edu.ufp.inf.sd.rmi.drive.session.SessionFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class AuthImpl extends UnicastRemoteObject implements AuthRI {

    private final Map<String, String> users = new HashMap<>();
    private final Map<String, FileManagerRI> drives = new HashMap<>();
    private final Map<String, Map<String, String>> partilhasRecebidas = new HashMap<>();

    public AuthImpl() throws RemoteException {
        super();
    }

    @Override
    public boolean register(String username, String password) throws RemoteException {
        if (users.containsKey(username)) return false;
        users.put(username, password);
        drives.put(username, new FileManager(username, this));
        partilhasRecebidas.put(username, new HashMap<>());
        System.out.println("Utilizador registado: " + username);
        return true;
    }

    @Override
    public FileManagerRI login(String username, String password, ObserverRI observer) throws RemoteException {
        if (!users.containsKey(username) || !users.get(username).equals(password)) return null;

        FileManagerRI fm = drives.get(username);
        if (fm instanceof FileManager) {
            ((FileManager) fm).setMyObserver(observer);
        }

        // ✅ Criar nova sessão usando o SessionFactory
        SubjectRI subject = new SubjectImpl(); // ou obter subject real se já existir
        SessionFactory.createSession(username, subject);

        System.out.println("Login efetuado: " + username);
        return fm;
    }

    @Override
    public boolean adicionarPartilha(String targetUser, String pastaPartilhada) throws RemoteException {
        return adicionarPartilha(targetUser, pastaPartilhada, "read");
    }

    public boolean adicionarPartilha(String targetUser, String pastaPartilhada, String permissao) throws RemoteException {
        partilhasRecebidas.putIfAbsent(targetUser, new HashMap<>());
        partilhasRecebidas.get(targetUser).put(pastaPartilhada, permissao);
        return true;
    }

    @Override
    public List<String> getPartilhasRecebidas(String username) throws RemoteException {
        return new ArrayList<>(partilhasRecebidas.getOrDefault(username, new HashMap<>()).keySet());
    }

    @Override
    public FileManagerRI getDrive(String username) throws RemoteException {
        return drives.get(username);
    }

    @Override
    public boolean removerPartilha(String targetUser, String pastaPartilhada) throws RemoteException {
        if (partilhasRecebidas.containsKey(targetUser)) {
            return partilhasRecebidas.get(targetUser).remove(pastaPartilhada) != null;
        }
        return false;
    }

    public boolean temPermissaoEscrita(String username, String pasta) {
        return partilhasRecebidas.containsKey(username)
                && "write".equalsIgnoreCase(partilhasRecebidas.get(username).get(pasta));
    }

    public List<String> getUsersWithAccessToFolder(String owner, String folderName) {
        List<String> usersWithAccess = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> entry : partilhasRecebidas.entrySet()) {
            Map<String, String> shared = entry.getValue();
            if (shared.containsKey(folderName)) {
                usersWithAccess.add(entry.getKey());
            }
        }
        return usersWithAccess;
    }
}
