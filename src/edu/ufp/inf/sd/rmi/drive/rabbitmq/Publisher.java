
package edu.ufp.inf.sd.rmi.drive.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Publisher {
    private final static String EXCHANGE_NAME = "drive_updates";
    private static Channel channel;

    static {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setUsername("guest");
            factory.setPassword("guest");

            Connection connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT, true); // <-- define durable=true
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void publish(String user, String message) {
        try {
            String fullMessage = "[rabbitmq][" + user + "] " + message;
            channel.basicPublish(EXCHANGE_NAME, "", null, fullMessage.getBytes());
            System.out.println(" [Publisher] Mensagem enviada: '" + fullMessage + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
