package edu.ufp.inf.sd.rmi.drive.client;

import edu.ufp.inf.sd.rmi.drive.model.*;

public class WorkspaceTest {
    public static void main(String[] args) {
        Workspace workspace = new Workspace("joana");

        Folder local = workspace.getLocalFolder();
        local.addFile(new FileObject("fatura.pdf", "conteúdo"));
        local.addSubFolder(new Folder("imagens"));

        Folder shared = workspace.getSharedFolder();
        Folder pastaPedro = new Folder("tpcPedro");
        pastaPedro.addFile(new FileObject("resumo.txt", "blabla"));
        shared.addSubFolder(pastaPedro);

        System.out.println(" Local:");
        local.listContents().forEach(f -> System.out.println(" - " + f));

        System.out.println("\n Partilhas:");
        shared.listContents().forEach(f -> System.out.println(" - " + f));
    }
}
