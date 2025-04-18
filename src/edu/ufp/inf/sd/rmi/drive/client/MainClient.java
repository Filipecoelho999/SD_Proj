package edu.ufp.inf.sd.rmi.drive.client;

import edu.ufp.inf.sd.rmi.drive.server.AuthRI;

import java.rmi.Naming;

public class MainClient {
    public static void main(String[] args) {
        try {
            AuthRI service = (AuthRI) Naming.lookup("rmi://localhost/auth");

            System.out.println("Registo Alice: " + service.register("joana", "124"));
            System.out.println("Login Alice: " + service.login("alice", "123"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
