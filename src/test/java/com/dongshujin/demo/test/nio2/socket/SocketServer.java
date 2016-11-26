package com.dongshujin.demo.test.nio2.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by dongsj on 16/11/26.
 *
 */
public class SocketServer {
    public static final SocketAddress SERVER_ADDRESS = new InetSocketAddress("localhost", 9090);

    private void start() {
        try {
            // 打开一个服务端通道并自动绑定到一个地址
            System.err.println("S>>> Open server channel");

            AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open().bind(SERVER_ADDRESS);
            System.err.println("S>>> Initiate accept on");
            Future<AsynchronousSocketChannel> future = server.accept();

            // 阻塞当前线程等待结果(Accept)
            AsynchronousSocketChannel worker = future.get();
            System.err.println("S>>> Accept completed");


            ByteBuffer readBuffer = ByteBuffer.allocate(100);
            // 从客户端读取消息 设置超时时间为10s
            worker.read(readBuffer).get(10, TimeUnit.SECONDS);
            System.err.println("S>>> Message received from client: " + new String(readBuffer.array()));

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.err.println("S>>> Client didn't respond in time");
        }
    }

    public static void main(String[] args) throws IOException {
        new SocketServer().start();

    }
}