package edu.ufp.inf.sd.rmi.drive.rabbitmq;

import com.rabbitmq.client.*;

public class Consumer {

    private static final String QUEUE_NAME = "drive_notifications";
    public static String currentUser;
    private static Connection connection;
    private static Channel channel;

    public static void start() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");

            connection = factory.newConnection();
            channel = connection.createChannel();

            channel.queueDeclare(QUEUE_NAME, true, false, false, null);

            System.out.println(" [Consumer] À espera de mensagens...");
            System.out.println(" [Consumer] Utilizador atual: " + currentUser);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");

                // ✅ Exibir se mensagem contiver o nome do utilizador
                if (currentUser != null && message.contains("[" + currentUser + "]")) {
                    System.out.println("🔔 " + message);
                }

                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };

            channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {});

        } catch (Exception e) {
            System.err.println(" [Consumer] Erro ao consumir mensagens: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void stop() {
        try {
            if (channel != null) channel.close();
            if (connection != null) connection.close();
        } catch (Exception e) {
            System.err.println(" [Consumer] Erro ao fechar conexões: " + e.getMessage());
        }
    }
}
