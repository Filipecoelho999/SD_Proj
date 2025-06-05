package edu.ufp.inf.sd.rmi.drive.server;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MainServer {
    public static void main(String[] args) {
        try {
            String modo = System.getProperty("modo", "rmiserver");
            FileManager.setModoPropagacao(modo);
            System.out.println("[MainServer] MODO_PROPAGACAO = " + modo);

            // Criar ou obter o registry
            Registry registry = LocateRegistry.getRegistry();
            try {
                registry.list(); // forçar teste de ligação
            } catch (Exception e) {
                LocateRegistry.createRegistry(1099); // se não existir, cria um
                System.out.println("[MainServer] RMI Registry criado.");
            }

            // Instanciar AuthImpl e registar no RMI Registry
            AuthRI authRI = new AuthImpl();
            Naming.rebind("auth", authRI);
            System.out.println("[MainServer] Serviço 'auth' registado com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
