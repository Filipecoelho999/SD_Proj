package edu.ufp.inf.sd.rabbitmqservices.drive;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class PublishClient {
    private static final String EXCHANGE_NAME = "updatesExchange";
    private static final String ROUTING_KEY = "drive.update";

    public static void main(String[] argv) {
        try {
            // Conexão com o RabbitMQ
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {

                // Tipo "topic" permite routing por chave
                channel.exchangeDeclare(EXCHANGE_NAME, "topic");

                String message = "[RabbitMQ] Ficheiro criado em /docs/cv.txt";
                channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, message.getBytes());

                System.out.println("Mensagem publicada: " + message);
            }
        } catch (Exception e) {
            System.err.println("Erro ao publicar: " + e.getMessage());
        }
    }
}
