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

            // Declara o exchange com durabilidade igual ao Publisher
            String EXCHANGE_NAME = "drive_updates";
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT, true);

            // Cria uma queue anónima exclusiva
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, EXCHANGE_NAME, "");

            System.out.println(" [Consumer] A espera de mensagens...");
            if (currentUser != null) {
                System.out.println(" [Consumer] Utilizador atual: " + currentUser);
            }

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");

                // Se quiseres filtrar por utilizador:
                if (currentUser == null || message.contains("[" + currentUser + "]")) {
                    System.out.println(" " + message);
                }

                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };

            channel.basicConsume(queueName, false, deliverCallback, consumerTag -> {});

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

    // Agora adicionamos o ponto de entrada principal
    public static void main(String[] args) {
        try {
            currentUser = System.getenv("USER"); // opcionalmente ir buscar user do SO
            start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
