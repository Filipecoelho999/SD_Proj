package edu.ufp.inf.sd.rmi.drive.server;

import edu.ufp.inf.sd.rmi.drive.model.FileObject;
import edu.ufp.inf.sd.rmi.drive.model.Folder;
import edu.ufp.inf.sd.rmi.drive.model.User;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.List;

public class WorkspaceImpl extends UnicastRemoteObject implements WorkspaceRI {
    private final String username;
    private final Folder local;
    private final Folder shared;
    private final SubjectRI subject;
    private final AuthRI authRI; // NOVO campo para aceder remotamente ao UserStore

    public WorkspaceImpl(String username, SubjectRI subject, AuthRI authRI) throws RemoteException {
        super();
        this.username = username;
        this.subject = subject;
        this.authRI = authRI;
        this.local = new Folder("local_" + username);
        this.shared = new Folder("shared_" + username);
    }

    @Override
    public String getUsername() throws RemoteException {
        return username;
    }

    @Override
    public SubjectRI getSubject() throws RemoteException {
        return subject;
    }

    @Override
    public boolean createFolder(String path, String folderName, boolean isShared) throws RemoteException {
        Folder target = getTargetFolder(path, isShared);
        if (target != null) {
            target.addSubFolder(new Folder(folderName));
            notifyAll(username + " criou a pasta: " + path + "/" + folderName);
            return true;
        }
        return false;
    }

    @Override
    public boolean createFile(String path, String fileName, String content, boolean isShared) throws RemoteException {
        Folder target = getTargetFolder(path, isShared);
        if (target != null) {
            target.addFile(new FileObject(fileName, content));
            notifyAll(username + " criou o ficheiro: " + path + "/" + fileName);
            return true;
        }
        return false;
    }

    @Override
    public boolean delete(String path, String name, boolean isFolder, boolean isShared) throws RemoteException {
        Folder target = getTargetFolder(path, isShared);
        if (target != null) {
            if (isFolder) target.removeSubFolder(name);
            else target.removeFile(name);
            notifyAll(username + " apagou o " + (isFolder ? "pasta" : "ficheiro") + ": " + path + "/" + name);
            return true;
        }
        return false;
    }

    @Override
    public boolean rename(String path, String oldName, String newName, boolean isFolder, boolean isShared) throws RemoteException {
        Folder target = getTargetFolder(path, isShared);
        if (target == null) return false;

        if (isFolder) {
            Folder folder = target.getSubFolder(oldName);
            if (folder == null) return false;
            target.removeSubFolder(oldName);
            target.addSubFolder(new Folder(newName));
        } else {
            FileObject file = target.getFile(oldName);
            if (file == null) return false;
            target.removeFile(oldName);
            target.addFile(new FileObject(newName, file.getContent()));
        }

        notifyAll(username + " renomeou " + (isFolder ? "a pasta" : "o ficheiro") + ": " + oldName + " para " + newName);
        return true;
    }

    @Override
    public boolean move(String sourcePath, String name, String destPath, boolean isFolder, boolean isShared) throws RemoteException {
        Folder source = getTargetFolder(sourcePath, isShared);
        Folder dest = getTargetFolder(destPath, isShared);
        if (source == null || dest == null) return false;

        if (isFolder) {
            Folder folder = source.getSubFolder(name);
            if (folder == null) return false;
            source.removeSubFolder(name);
            dest.addSubFolder(folder);
        } else {
            FileObject file = source.getFile(name);
            if (file == null) return false;
            source.removeFile(name);
            dest.addFile(file);
        }

        notifyAll(username + " moveu " + (isFolder ? "a pasta" : "o ficheiro") + ": " + name + " de " + sourcePath + " para " + destPath);
        return true;
    }

    @Override
    public List<String> list(String path, boolean isShared) throws RemoteException {
        Folder target = getTargetFolder(path, isShared);
        if (target != null) {
            return target.listContents();
        }
        return Collections.emptyList();
    }

    private Folder getTargetFolder(String path, boolean isShared) {
        if (path == null || path.isEmpty()) return isShared ? shared : local;

        String[] parts = path.split("/");

        if (path.startsWith("shared_")) {
            String owner = parts[0].substring("shared_".length());
            String internalPath = path.length() > parts[0].length() + 1 ? path.substring(parts[0].length() + 1) : "";

            Folder sharedOwnerRoot = shared.getSubFolder("shared_" + owner);
            if (sharedOwnerRoot == null) return null;

            if (internalPath.isEmpty()) {
                return sharedOwnerRoot;
            }

            Folder current = sharedOwnerRoot;
            String[] subParts = internalPath.split("/");
            for (String p : subParts) {
                if (p.isEmpty()) continue;
                current = current.getSubFolder(p);
                if (current == null) return null;
            }
            return current;
        }

        Folder current = isShared ? shared : local;
        for (String p : parts) {
            if (p.isEmpty()) continue;
            current = current.getSubFolder(p);
            if (current == null) return null;
        }
        return current;
    }

    public Folder getFolderByPath(String path, boolean isShared) {
        String[] parts = path.split("/");
        Folder current = isShared ? shared : local;
        for (String p : parts) {
            if (p.isEmpty()) continue;
            current = current.getSubFolder(p);
            if (current == null) return null;
        }
        return current;
    }

    private void notifyAll(String message) {
        try {
            if (subject != null) {
                subject.notifyObservers(message);
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao notificar por RMI: " + e.getMessage());
        }
    }

    @Override
    public boolean share(String path, String targetUser, boolean isFolder) throws RemoteException {
        String normalizedPath = path.replaceAll("/+", "/").replaceAll("/$", "");
        System.out.println("[DEBUG WorkspaceImpl] share: username=" + username + " path=" + normalizedPath + " target=" + targetUser);
        SharedFileRegistry.share(this.username, normalizedPath, isFolder, targetUser);
        notifyAll("📢 " + this.username + " partilhou '" + normalizedPath + "' com " + targetUser);
        return true;
    }

    @Override
    public boolean unshare(String path, String targetUser) throws RemoteException {
        return SharedFileRegistry.unshare(this.username, path, targetUser);
    }

    @Override
    public List<SharedReference> getSharedWithMe(String username) {
        return SharedFileRegistry.getSharedWithUser(username);
    }

    @Override
    public boolean entershared(String owner) throws RemoteException {
        Folder sharedRoot = new Folder("shared_" + owner);

        List<SharedReference> sharedWithMe = SharedFileRegistry.getSharedWithUser(this.username);

        for (SharedReference ref : sharedWithMe) {
            if (ref.owner.equals(owner)) {
                try {
                    User user = authRI.getUser(owner);
                    if (user != null) {
                        WorkspaceRI ownerWS = user.getWorkspace();
                        if (ownerWS instanceof WorkspaceImpl ownerWorkspace) {
                            Folder original = ownerWorkspace.getFolderByPath(ref.path, false);
                            if (original != null) {
                                String normalizedPath = ref.path.replaceAll("/+", "/").replaceAll("/+$", "");
                                System.out.println("[DEBUG entershared] ref.path=" + ref.path);
                                String sharedName = normalizedPath.substring(normalizedPath.lastIndexOf("/") + 1);
                                sharedRoot.getSubFolders().put(sharedName, original);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao aceder à workspace partilhada: " + e.getMessage());
                }
            }
        }

        this.shared.getSubFolders().put("shared_" + owner, sharedRoot);
        return true;
    }
}
