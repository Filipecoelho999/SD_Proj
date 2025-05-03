package edu.ufp.inf.sd.rmi.drive.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Publisher {

    private static final String QUEUE_NAME = "drive_notifications";

    public static void publish(String message) {
        publish("global", message);
    }

    public static void publish(String user, String message) {
        sendMessage("[rabbitmq][" + user + "] " + message);
    }

    // ✅ NOVO MÉTODO para suportar tag customizada (ex: "rabbitmq", "info", etc)
    public static void publish(String tag, String user, String message) {
        sendMessage("[" + tag + "][" + user + "] " + message);
    }

    private static void sendMessage(String formattedMessage) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, formattedMessage.getBytes("UTF-8"));
            System.out.println(" [Publisher] Mensagem enviada: '" + formattedMessage + "'");

        } catch (Exception e) {
            System.err.println(" [Publisher] Erro ao enviar mensagem: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
