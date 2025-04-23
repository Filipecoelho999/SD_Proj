package edu.ufp.inf.sd.rmi.drive.server;

import java.io.Serializable;

public class SharedReference implements Serializable {
    public String owner;
    public String path;
    public boolean isFolder;

    public SharedReference(String owner, String path, boolean isFolder) {
        this.owner = owner;
        this.path = path;
        this.isFolder = isFolder;
    }

    @Override
    public String toString() {
        return "/shared_" + owner + "/" + path;
    }
}
