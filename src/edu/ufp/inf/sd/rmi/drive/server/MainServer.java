package edu.ufp.inf.sd.rmi.drive.server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class MainServer {

    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            AuthRI authService = new AuthImpl();
            Naming.rebind("auth", authService);
            System.out.println("Servidor RMI pronto.");
        } catch (Exception e) {
            System.err.println("Erro a iniciar servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
