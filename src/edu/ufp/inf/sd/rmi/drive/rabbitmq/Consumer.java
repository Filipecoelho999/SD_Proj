package edu.ufp.inf.sd.rmi.drive.rabbitmq;

import com.rabbitmq.client.*;

public class Consumer {

    private static final String QUEUE_NAME = "drive_notifications";

    public static void start() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost"); // RabbitMQ local
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            // Declara a fila (caso ainda não tenha sido criada)
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            System.out.println(" [Consumer] À espera de mensagens...");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println("🔔 [Consumer] Nova mensagem: '" + message + "'");
            };

            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
            });

        } catch (Exception e) {
            System.err.println(" [Consumer] Erro ao consumir mensagens: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
