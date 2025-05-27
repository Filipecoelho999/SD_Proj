package edu.ufp.inf.sd.rmi.drive.model;

import java.io.Serializable;
import java.util.*;

public class Folder implements Serializable {
    private String name;
    private Map<String, FileObject> files = new HashMap<>();
    private Map<String, Folder> subFolders = new HashMap<>();

    public Folder(String name) {
        this.name = name;
    }

    public void addFile(FileObject file) {
        files.put(file.getName(), file);
    }

    public void removeFile(String filename) {
        files.remove(filename);
    }

    public void addSubFolder(Folder folder) {
        subFolders.put(folder.getName(), folder);
    }

    public void removeSubFolder(String folderName) {
        subFolders.remove(folderName);
    }

    public List<String> listContents() {
        List<String> result = new ArrayList<>();
        result.addAll(subFolders.keySet());
        result.addAll(files.keySet());
        return result;
    }

    public String getName() {
        return name;
    }

    public Folder getSubFolder(String folderName) {
        return subFolders.get(folderName);
    }

    public FileObject getFile(String name) {
        return files.get(name);
    }

    public Map<String, Folder> getSubFolders() {
        return subFolders;
    }
}