package edu.ufp.inf.sd.rabbitmqservices.test;

public class MainClientRabbitOnly {
    public static void main(String[] args) {
        try {
            System.out.println("=== Teste RabbitMQ sem RMI ===");

            WorkspaceRabbit ws = new WorkspaceRabbit("joana");

            ws.createFolder("", "docs", false);
            ws.createFile("docs", "cv.txt", "conteudo", false);
            ws.rename("docs", "cv.txt", "curriculum.txt", false, false);
            ws.move("docs", "curriculum.txt", "", false, false);
            ws.delete("", "curriculum.txt", false, false);

            System.out.println("Teste RabbitMQ concluído.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
