package edu.ufp.inf.sd.rmi.drive.model;


import edu.ufp.inf.sd.rmi.drive.server.SubjectRI;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

public class Workspace implements Serializable {
    private static final long serialVersionUID = 1L;

    private Folder local;
    private Folder shared;
    private final String username;
    private final SubjectRI subject;

    public Workspace(String username, SubjectRI subject) {
        this.username = username;
        this.subject = subject;
        this.local = new Folder("local_" + username);
        this.shared = new Folder("shared_" + username);
    }


    public Folder getLocalFolder() {
        return local;
    }

    public Folder getSharedFolder() {
        return shared;
    }

    public boolean createFolder(String path, String folderName, boolean isShared) {
        Folder target = getTargetFolder(path, isShared);
        if (target != null) {
            target.addSubFolder(new Folder(folderName));
            notifyAll(username + " criou a pasta: " + path + "/" + folderName);
            return true;
        }
        return false;
    }

    public boolean createFile(String path, String fileName, String content, boolean isShared) {
        Folder target = getTargetFolder(path, isShared);
        if (target != null) {
            target.addFile(new FileObject(fileName, content));
            notifyAll(username + " criou o ficheiro: " + path + "/" + fileName);
            return true;
        }
        return false;
    }

    public boolean delete(String path, String name, boolean isFolder, boolean isShared) {
        Folder target = getTargetFolder(path, isShared);
        if (target != null) {
            String tipo = isFolder ? "pasta" : "ficheiro";
            String fullPath = path + "/" + name;

            if (isFolder) target.removeSubFolder(name);
            else target.removeFile(name);

            notifyAll(username + " apagou o " + tipo + ": " + fullPath);
            return true;
        }
        return false;
    }

    public boolean rename(String path, String oldName, String newName, boolean isFolder, boolean isShared) {
        Folder target = getTargetFolder(path, isShared);
        if (target == null) return false;

        if (isFolder) {
            Folder folder = target.getSubFolder(oldName);
            if (folder == null) return false;
            target.removeSubFolder(oldName);
            folder = new Folder(newName);
            target.addSubFolder(folder);
            notifyAll(username + " renomeou a pasta: " + oldName + " para " + newName);
        } else {
            FileObject file = target.getFile(oldName);
            if (file == null) return false;
            target.removeFile(oldName);
            file = new FileObject(newName, file.getContent());
            target.addFile(file);
            notifyAll(username + " renomeou o ficheiro: " + oldName + " para " + newName);
        }
        return true;
    }

    public boolean move(String sourcePath, String name, String destPath, boolean isFolder, boolean isShared) {
        Folder source = getTargetFolder(sourcePath, isShared);
        Folder dest = getTargetFolder(destPath, isShared);

        if (source == null || dest == null) return false;

        if (isFolder) {
            Folder folder = source.getSubFolder(name);
            if (folder == null) return false;
            source.removeSubFolder(name);
            dest.addSubFolder(folder);
            notifyAll(username + " moveu a pasta: " + name + " de " + sourcePath + " para " + destPath);
        } else {
            FileObject file = source.getFile(name);
            if (file == null) return false;
            source.removeFile(name);
            dest.addFile(file);
            notifyAll(username + " moveu o ficheiro: " + name + " de " + sourcePath + " para " + destPath);
        }
        return true;
    }

    public List<String> list(String path, boolean isShared) {
        Folder target = getTargetFolder(path, isShared);
        if (target != null) {
            return target.listContents();
        }
        return Collections.emptyList();
    }

    private Folder getTargetFolder(String path, boolean isShared) {
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
                subject.notifyObservers(message); // RMI
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao notificar por RMI: " + e.getMessage());
        }
    }
}
