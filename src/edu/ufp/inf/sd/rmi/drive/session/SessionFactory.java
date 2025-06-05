package edu.ufp.inf.sd.rmi.drive.session;
import edu.ufp.inf.sd.rmi.drive.server.SubjectRI;
import java.util.concurrent.ConcurrentHashMap;

// Classe singleton para criar, guardar e remover sessões dos utilizadores.
// Permite verificar se um utilizador está autenticado e obter a sua sessão ativa.
// Usado pelo AuthImpl no momento do login e pelo cliente (DriveClient) para verificar sessão atual.

public class SessionFactory {
    private static final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    public static Session createSession(String username, SubjectRI subject) {
        Session session = new Session(username, subject);
        sessions.put(username, session);
        return session;
    }

    public static Session getSession(String username) {
        return sessions.get(username);
    }

    public static void removeSession(String username) {
        sessions.remove(username);
    }

    public static boolean isLoggedIn(String username) {
        return sessions.containsKey(username);
    }
}
