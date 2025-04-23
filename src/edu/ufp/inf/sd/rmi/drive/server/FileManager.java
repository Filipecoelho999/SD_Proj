// FileManager.java - Singleton com métodos sincronizados
package edu.ufp.inf.sd.rmi.drive.server;

import edu.ufp.inf.sd.rmi.drive.model.FileObject;
import edu.ufp.inf.sd.rmi.drive.model.Folder;

public class FileManager {

    private static FileManager instance = null;

    private FileManager() {}

    public static synchronized FileManager getInstance() {
        if (instance == null) {
            instance = new FileManager();
        }
        return instance;
    }

    public synchronized boolean createFolder(Folder parent, String folderName) {
        if (parent.getSubFolder(folderName) == null) {
            parent.addSubFolder(new Folder(folderName));
            return true;
        }
        return false;
    }

    public synchronized boolean createFile(Folder parent, String fileName, String content) {
        if (parent.getFile(fileName) == null) {
            parent.addFile(new FileObject(fileName, content));
            return true;
        }
        return false;
    }

    public synchronized boolean deleteFile(Folder parent, String name, boolean isFolder) {
        if (isFolder) {
            if (parent.getSubFolder(name) != null) {
                parent.removeSubFolder(name);
                return true;
            }
        } else {
            if (parent.getFile(name) != null) {
                parent.removeFile(name);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean rename(Folder parent, String oldName, String newName, boolean isFolder) {
        if (isFolder) {
            Folder folder = parent.getSubFolder(oldName);
            if (folder != null) {
                parent.removeSubFolder(oldName);
                parent.addSubFolder(new Folder(newName));
                return true;
            }
        } else {
            FileObject file = parent.getFile(oldName);
            if (file != null) {
                parent.removeFile(oldName);
                parent.addFile(new FileObject(newName, file.getContent()));
                return true;
            }
        }
        return false;
    }

    public synchronized boolean move(Folder source, Folder dest, String name, boolean isFolder) {
        if (isFolder) {
            Folder folder = source.getSubFolder(name);
            if (folder != null) {
                source.removeSubFolder(name);
                dest.addSubFolder(folder);
                return true;
            }
        } else {
            FileObject file = source.getFile(name);
            if (file != null) {
                source.removeFile(name);
                dest.addFile(file);
                return true;
            }
        }
        return false;
    }
}
