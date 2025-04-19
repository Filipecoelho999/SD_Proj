package edu.ufp.inf.sd.rmi.drive.client;

import edu.ufp.inf.sd.rmi.drive.model.User;
import edu.ufp.inf.sd.rmi.drive.server.AuthRI;
import edu.ufp.inf.sd.rmi.drive.server.SubjectRI;

import java.rmi.Naming;

public class MainClient {
    public static void main(String[] args) {
        try {
            // Ligação ao serviço remoto
            AuthRI service = (AuthRI) Naming.lookup("rmi://localhost/auth");

            // Criar e registar este cliente como observador
            String username = "joana";
            ObserverRI observer = new ObserverImpl(username);
            ((SubjectRI) service).attachObserver(observer);
            System.out.println("Observador registado com sucesso!");

            // Registo e Login
            System.out.println("=== Registo e Login ===");
            service.register(username, "123");
            User joana = service.login(username, "123");

            if (joana != null) {
                System.out.println("Login bem-sucedido!\n");

                System.out.println("=== Criação de pastas e ficheiros ===");
                boolean docsOk = joana.getWorkspace().createFolder("/", "docs", false);
                boolean outrosOk = joana.getWorkspace().createFolder("/", "outros", false);
                boolean partilhaOk = joana.getWorkspace().createFolder("/", "partilhaPedro", true);
                boolean fileOk = joana.getWorkspace().createFile("/docs", "cv.txt", "meu CV", false);
                boolean sharedFileOk = joana.getWorkspace().createFile("/partilhaPedro", "notas.txt", "resumo", true);

                System.out.println((docsOk && outrosOk && partilhaOk && fileOk && sharedFileOk)
                        ? "OK - docs, outros, partilhaPedro, cv.txt, notas.txt criados com sucesso"
                        : "Erro ao criar pastas ou ficheiros");

                System.out.println("\n=== Renomear e mover ficheiro ===");
                boolean renameOk = joana.getWorkspace().rename("/docs", "cv.txt", "curriculum.txt", false, false);
                boolean moveOk = joana.getWorkspace().move("/docs", "curriculum.txt", "/outros", false, false);

                System.out.println((renameOk && moveOk)
                        ? "OK - cv.txt virou curriculum.txt e foi movido para /outros"
                        : "Erro ao renomear ou mover ficheiro");

                System.out.println("\n=== Apagar ficheiro ===");
                boolean deleteOk = joana.getWorkspace().delete("/partilhaPedro", "notas.txt", false, true);
                System.out.println(deleteOk
                        ? "OK - notas.txt foi apagado com sucesso"
                        : "Erro ao apagar ficheiro");

                System.out.println("\n=== Listagens finais ===");

                System.out.println("\nLOCAL /docs:");
                joana.getWorkspace().list("/docs", false).forEach(item -> System.out.println(" - " + item));

                System.out.println("\nLOCAL /outros:");
                joana.getWorkspace().list("/outros", false).forEach(item -> System.out.println(" - " + item));

                System.out.println("\nPARTILHAS /partilhaPedro:");
                joana.getWorkspace().list("/partilhaPedro", true).forEach(item -> System.out.println(" - " + item));

            } else {
                System.out.println("Login falhou.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
