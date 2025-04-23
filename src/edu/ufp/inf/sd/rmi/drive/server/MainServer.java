package edu.ufp.inf.sd.rmi.drive.server;

import edu.ufp.inf.sd.rmi.drive.model.UserStore;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class MainServer {

    public static UserStore getUserStore() {
        return UserStore.getInstance();
    }

    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            AuthRI service = new AuthImpl();
            Naming.rebind("rmi://localhost/AuthService", service);
            System.out.println("Servidor RMI pronto.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}