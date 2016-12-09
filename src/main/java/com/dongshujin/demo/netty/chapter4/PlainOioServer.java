package com.dongshujin.demo.netty.chapter4;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Created by dongsj on 16/12/7.
 *
 * 这里使用的Blocking I/O （非Netty）
 *
 *
 *
 */
public class PlainOioServer {

    public void server(int port) throws IOException {
        final ServerSocket socket = new ServerSocket(port); //# 绑定服务的端口

        while (true) {
            final Socket clientSocket = socket.accept();    //# 接收链接
            System.out.println("Accepted connection from " + clientSocket);

            new Thread(                                     //# 创建一个新线程去处理链接
                    new Runnable() {
                        @Override
                        public void run() {
                            OutputStream out = null;
                            try {
                                out = clientSocket.getOutputStream();
                                out.write("Hi!\n".getBytes(Charset.forName("UTF-8"))); //# 写入消息到接入的客户端链接
                                out.flush();
                                clientSocket.close();           //# 关闭链接
                            } catch (IOException e) {
                                e.printStackTrace();
                                try {
                                    clientSocket.close();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }
            ).start();              //# 开启线程
        }
    }

    public static void main(String[] args) throws IOException {
        new PlainOioServer().server(8080);
    }
}
