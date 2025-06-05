package edu.ufp.inf.sd.rmi.drive.util;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Classe singleton responsável por gerir bloqueios (locks) em ficheiros e pastas.
// Garante acesso exclusivo a recursos partilhados para evitar conflitos entre utilizadores concorrentes.
// Usado pelo FileManager para verificar se um utilizador pode modificar um ficheiro/pasta.

public class LockManager {

    private static LockManager instance = null;
    private final Map<String, String> locks = new ConcurrentHashMap<>();

    private LockManager() {}

    public static synchronized LockManager getInstance() {
        if (instance == null) {
            instance = new LockManager();
        }
        return instance;
    }

    // Tentar bloquear o recurso
    public synchronized boolean lock(String path, String username) {
        if (locks.containsKey(path)) {
            return locks.get(path).equals(username); // Já está bloqueado por ti
        }
        locks.put(path, username);
        return true;
    }

    // Libertar o recurso
    public synchronized void unlock(String path, String username) {
        if (locks.containsKey(path) && locks.get(path).equals(username)) {
            locks.remove(path);
        }
    }

    // Verificar se o recurso está bloqueado por outro
    public synchronized boolean isLockedByOther(String path, String username) {
        return locks.containsKey(path) && !locks.get(path).equals(username);
    }
}
