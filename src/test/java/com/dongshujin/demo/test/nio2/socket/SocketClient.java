package com.dongshujin.demo.test.nio2.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by dongsj on 16/11/26.
 */
public class SocketClient {
    public void start() {

        try {
            // 开启一个通道并连接到服务端
            System.out.println("C))) Open client channel");
            AsynchronousSocketChannel client = AsynchronousSocketChannel.open();

            System.out.println("C))) Connect to server");
            Future<Void> connectFuture = client.connect(SocketServer.SERVER_ADDRESS);

            int count = 10;
            while (true) {
                if (connectFuture.isDone()) {
                    break;
                } else {
                    count--;
                    System.out.println("C))) Check connect");
                    Thread.sleep(1000);
                }
                if (count < 0) {
                    System.out.println("C))) Cancel ");
                    connectFuture.cancel(true);
                    return;
                }
            }

            ByteBuffer message = ByteBuffer.wrap("ping".getBytes());
            // wait for the response
            System.out.println("C))) Sending message to the server...");
            Future<Integer> writeFuture  = client.write(message);
            int numberBytes = writeFuture.get();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        new SocketClient().start();
    }
}
