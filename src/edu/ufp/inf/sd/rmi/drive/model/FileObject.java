package edu.ufp.inf.sd.rmi.drive.model;

import java.io.Serializable;

public class FileObject implements Serializable {
    private String name;
    private String content; // simplificado, pode ser byte[] para ficheiros reais

    public FileObject(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }
}
