package edu.ufp.inf.sd.rmi.drive.session;

import edu.ufp.inf.sd.rmi.drive.server.SubjectRI;

import java.util.concurrent.ConcurrentHashMap;

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
