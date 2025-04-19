package edu.ufp.inf.sd.rmi.drive.client;

import edu.ufp.inf.sd.rmi.drive.model.User;
import edu.ufp.inf.sd.rmi.drive.server.AuthRI;

import java.rmi.Naming;

public class MainClient {
    public static void main(String[] args) {
        try {
            AuthRI service = (AuthRI) Naming.lookup("rmi://localhost/auth");

            System.out.println("=== Registo e Login ===");
            service.register("joana", "123");
            User joana = service.login("joana", "123");

            if (joana != null) {
                System.out.println(" Login bem-sucedido!\n");

                System.out.println("=== Criacao de pastas e ficheiros ===");
                boolean docsOk = joana.getWorkspace().createFolder("/", "docs", false);
                boolean outrosOk = joana.getWorkspace().createFolder("/", "outros", false);
                boolean partilhaOk = joana.getWorkspace().createFolder("/", "partilhaPedro", true);
                boolean fileOk = joana.getWorkspace().createFile("/docs", "cv.txt", "meu CV", false);
                boolean sharedFileOk = joana.getWorkspace().createFile("/partilhaPedro", "notas.txt", "resumo", true);

                System.out.println((docsOk && outrosOk && partilhaOk && fileOk && sharedFileOk)
                        ? " OK - docs, outros, partilhaPedro, cv.txt, notas.txt criados com sucesso"
                        : " Erro ao criar pastas ou ficheiros");

                System.out.println("\n=== Renomear e mover ficheiro ===");
                boolean renameOk = joana.getWorkspace().rename("/docs", "cv.txt", "curriculum.txt", false, false);
                boolean moveOk = joana.getWorkspace().move("/docs", "curriculum.txt", "/outros", false, false);

                System.out.println((renameOk && moveOk)
                        ? " OK - cv.txt virou curriculum.txt e foi movido para /outros"
                        : " Erro ao renomear ou mover ficheiro");

                System.out.println("\n=== Apagar ficheiro ===");
                boolean deleteOk = joana.getWorkspace().delete("/partilhaPedro", "notas.txt", false, true);
                System.out.println(deleteOk
                        ? " OK - notas.txt foi apagado com sucesso"
                        : " Erro ao apagar ficheiro");

                System.out.println("\n=== Listagens finais ===");

                System.out.println("\n LOCAL /docs:");
                joana.getWorkspace().list("/docs", false).forEach(item -> System.out.println(" - " + item));

                System.out.println("\n LOCAL /outros:");
                joana.getWorkspace().list("/outros", false).forEach(item -> System.out.println(" - " + item));

                System.out.println("\n PARTILHAS /partilhaPedro:");
                joana.getWorkspace().list("/partilhaPedro", true).forEach(item -> System.out.println(" - " + item));


            } else {
                System.out.println(" Login falhou.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
