package edu.ufp.inf.sd.rmi.drive.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class Workspace implements Serializable {
    private Folder local;
    private Folder shared;

    public Workspace(String userName) {
        this.local = new Folder("local_" + userName);
        this.shared = new Folder("shared_" + userName);
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
            return true;
        }
        return false;
    }

    public boolean createFile(String path, String fileName, String content, boolean isShared) {
        Folder target = getTargetFolder(path, isShared);
        if (target != null) {
            target.addFile(new FileObject(fileName, content));
            return true;
        }
        return false;
    }

    public boolean delete(String path, String name, boolean isFolder, boolean isShared) {
        Folder target = getTargetFolder(path, isShared);
        if (target != null) {
            if (isFolder) target.removeSubFolder(name);
            else target.removeFile(name);
            return true;
        }
        return false;
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
    public boolean rename(String path, String oldName, String newName, boolean isFolder, boolean isShared) {
        Folder target = getTargetFolder(path, isShared);
        if (target == null) return false;

        if (isFolder) {
            Folder folder = target.getSubFolder(oldName);
            if (folder == null) return false;
            target.removeSubFolder(oldName);
            folder = new Folder(newName); // novo objeto com novo nome
            target.addSubFolder(folder);
        } else {
            FileObject file = target.getFile(oldName);
            if (file == null) return false;
            target.removeFile(oldName);
            file = new FileObject(newName, file.getContent());
            target.addFile(file);
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
        } else {
            FileObject file = source.getFile(name);
            if (file == null) return false;
            source.removeFile(name);
            dest.addFile(file);
        }
        return true;
    }


}
