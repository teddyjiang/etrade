package com.ibm.cdl.itaas.stomp;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.*;

public class Program {

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("test.mo-jiu.com");
        factory.setUsername("admin");
        factory.setPassword("mojiu.rmq.2017");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare("e.ws.notify", "direct",true);

        String destS2C = "shop";     // Server-to-client 方向
        String destC2S = "msgFromClient"; // Client-to-server 方向

        String message="";

        try {
            /***********************************************************************
             *  服务端信息推送
             *  Client 采用 '/exchange/<exchangeName>/<pattern>方式订阅服务端通知
             *  每个Client有独立queue, binding（i.e. <pattern>）可定义在人或店层面
             ***********************************************************************/
            message = "{\"name\":1200,\"name\":\"Welcome to RabbitMQ message push!\"}";
            channel.basicPublish("e.ws.notify", destS2C, null, message.getBytes());
            System.out.println("[x] Sent Message:" + message);



            /***********************************************************************
             *  订阅前端信息
             *  同一个 binding 用于C2S方向消息传递
             ***********************************************************************/
            channel.basicConsume(destC2S, new Consumer() {
                @Override
                public void handleConsumeOk(String s) {
                    System.out.println("handleConsumeOk:" + s);
                }

                @Override
                public void handleCancelOk(String s) {
                    System.out.println("handleCancelOk:" + s);
                }

                @Override
                public void handleCancel(String s) throws IOException {
                    System.out.println("handleCancelOk:" + s);
                }

                @Override
                public void handleShutdownSignal(String s, ShutdownSignalException e) {
                    System.out.println("handleShutdownSignal:" + s);
                }

                @Override
                public void handleRecoverOk(String s) {
                    System.out.println("handleRecoverOk:" + s);
                }

                @Override
                public void handleDelivery(String s, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] bytes) throws IOException {
                    System.out.println("handleDelivery:" + new String(bytes));

                    // Client 事件处理， meta信息可放在Properties, 如: shopId, userId, deviceId 等

                }
            });

            while (true) {
                Thread.sleep(500);
            }

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            channel.close();
            connection.close();
        }
    }

}