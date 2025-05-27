package edu.ufp.inf.sd.rmi.drive.server;

import java.util.*;

public class SharedFileRegistry {

    private static final Map<String, List<SharedReference>> sharedWithUsers = new HashMap<>();
    private static final Map<String, List<String>> resourceToUsers = new HashMap<>();

    private static String normalizePath(String path) {
        if (!path.startsWith("/")) path = "/" + path;
        return path.replaceAll("//+", "/").replaceAll("/$", "");
    }

    public static synchronized void share(String owner, String path, boolean isFolder, String targetUser) {
        System.out.println("[DEBUG SharedFileRegistry] FINAL shared path: " + path);
        path = normalizePath(path);
        SharedReference ref = new SharedReference(owner, path, isFolder);

        sharedWithUsers.computeIfAbsent(targetUser, k -> new ArrayList<>()).add(ref);

        String key = owner + ":" + path;
        resourceToUsers.computeIfAbsent(key, k -> new ArrayList<>()).add(targetUser);

        System.out.println("[DEBUG] " + owner + " partilhou " + path + " com " + targetUser);
    }

    public static synchronized boolean unshare(String owner, String path, String targetUser) {
        path = normalizePath(path);
        List<SharedReference> refs = sharedWithUsers.get(targetUser);
        if (refs == null) return false;

        final String pathFixed = path; // para ser effectively final
        boolean removed = refs.removeIf(ref -> ref.owner.equals(owner) && normalizePath(ref.path).equals(pathFixed));

        if (removed) {
            String key = owner + ":" + path;
            List<String> users = resourceToUsers.get(key);
            if (users != null) {
                users.remove(targetUser);
                if (users.isEmpty()) {
                    resourceToUsers.remove(key);
                }
            }
            System.out.println("[DEBUG] Partilha removida: " + path);
        }
        return removed;
    }

    public static synchronized List<SharedReference> getSharedWithUser(String username) {
        System.out.println("[DEBUG getSharedWithUser] chamado para: " + username);
        return sharedWithUsers.getOrDefault(username, new ArrayList<>());
    }
}
