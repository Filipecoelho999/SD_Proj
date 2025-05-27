package edu.ufp.inf.sd.rabbitmqservices.drive;

import com.rabbitmq.client.*;

import java.io.IOException;

public class SubscribeClient {
    private static final String EXCHANGE_NAME = "updatesExchange";
    private static final String ROUTING_KEY = "drive.update";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // Exchange tipo "topic"
        channel.exchangeDeclare(EXCHANGE_NAME, "topic", true); // <-- tem de bater com o Publisher
        String queueName = channel.queueDeclare().getQueue(); // fila temporária

        channel.queueBind(queueName, EXCHANGE_NAME, ROUTING_KEY);

        System.out.println("A aguardar mensagens...");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("Mensagem recebida: " + message);
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }
}
