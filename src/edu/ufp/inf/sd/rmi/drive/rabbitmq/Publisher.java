package edu.ufp.inf.sd.rmi.drive.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Publisher {

    private static final String QUEUE_NAME = "drive_notifications";

    public static void publish(String message) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // RabbitMQ no localhost
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // Declara a fila (cria se não existir)
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);

            // Publica a mensagem
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
            System.out.println(" [Publisher] Mensagem enviada: '" + message + "'");

        } catch (Exception e) {
            System.err.println(" [Publisher] Erro ao enviar mensagem: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
