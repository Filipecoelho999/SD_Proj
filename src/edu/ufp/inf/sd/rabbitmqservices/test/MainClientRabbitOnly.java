package edu.ufp.inf.sd.rabbitmqservices.test;

import edu.ufp.inf.sd.rabbitmqservices.util.RabbitUtils;
import edu.ufp.inf.sd.rmi.drive.model.Workspace;

public class MainClientRabbitOnly {
    public static void main(String[] args) {
        System.out.println("=== Teste RabbitMQ sem RMI ===");

        // Workspace dummy sem subject
        Workspace ws = new Workspace("joana", null);

        ws.createFolder("/", "docs", false);
        ws.createFile("/docs", "cv.txt", "conteudo", false);
        ws.rename("/docs", "cv.txt", "curriculum.txt", false, false);
        ws.move("/docs", "curriculum.txt", "/", false, false);
        ws.delete("/", "curriculum.txt", false, false);

        System.out.println("✅ Teste RabbitMQ concluído.");
    }
}
