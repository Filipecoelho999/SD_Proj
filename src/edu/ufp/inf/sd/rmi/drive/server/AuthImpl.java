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

        SubjectRI subject = new SubjectImpl(); // ou obter subject real se já existir
        SessionFactory.createSession(username, subject);

        System.out.println("Login efetuado: " + username);
        return fm;
    }

    @Override
    public boolean adicionarPartilha(String targetUser, String pastaPartilhada, String permissao, String dono) throws RemoteException {
        partilhasRecebidas.putIfAbsent(targetUser, new HashMap<>());
        partilhasRecebidas.get(targetUser).put(pastaPartilhada, dono + ":" + permissao);
        System.out.println("[DEBUG][PARTILHA] Partilha adicionada: " + targetUser + " → " + pastaPartilhada + " = " + dono + ":" + permissao);
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
        if (!partilhasRecebidas.containsKey(username)) return false;
        String valor = partilhasRecebidas.get(username).get(pasta);
        if (valor == null || !valor.contains(":")) return false;
        String[] parts = valor.split(":");
        return parts.length == 2 && parts[1].equalsIgnoreCase("write");
    }

    public List<String> getUsersWithAccessToFolder(String owner, String folderName) {
        // garantir que comparas sempre com a pasta raiz
        String topFolder = folderName.contains("/") ? folderName.split("/")[0] : folderName;

        List<String> usersComPermissao = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> entry : partilhasRecebidas.entrySet()) {
            String receiver = entry.getKey();
            Map<String, String> partilhas = entry.getValue();
            String valor = partilhas.get(topFolder);
            if (valor != null && valor.startsWith(owner + ":")) {
                usersComPermissao.add(receiver);
            }
        }
        return usersComPermissao;
    }


}
