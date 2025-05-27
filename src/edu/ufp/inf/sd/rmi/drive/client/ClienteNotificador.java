package edu.ufp.inf.sd.rmi.drive.client;

import edu.ufp.inf.sd.rmi.drive.model.User;
import edu.ufp.inf.sd.rmi.drive.server.AuthRI;

import java.rmi.Naming;

public class ClienteNotificador {
    public static void main(String[] args) {
        try {
            AuthRI service = (AuthRI) Naming.lookup("rmi://localhost/auth");

            System.out.println("=== Cliente Notificador ===");

            service.register("maria", "abc");
            User maria = service.login("maria", "abc");

            if (maria != null) {
                maria.getWorkspace().createFolder("/", "projetos", false);
                maria.getWorkspace().createFile("/projetos", "relatorio.txt", "conteúdo", false);
                maria.getWorkspace().rename("/projetos", "relatorio.txt", "final.txt", false, false);
                maria.getWorkspace().move("/projetos", "final.txt", "/", false, false);
                maria.getWorkspace().delete("/", "final.txt", false, false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
