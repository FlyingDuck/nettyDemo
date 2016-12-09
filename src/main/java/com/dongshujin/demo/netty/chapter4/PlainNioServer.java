package com.dongshujin.demo.netty.chapter4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by dongsj on 16/12/7.
 *
 * Asynchronous Non-Blocking I/O
 */
public class PlainNioServer {

    public void server(int port) throws IOException {
        System.out.println("Listening for connection on port " + port);

        ServerSocketChannel serverChannel = null;
        Selector selector = null;

        serverChannel = ServerSocketChannel.open();
        ServerSocket ss = serverChannel.socket();
        InetSocketAddress address = new InetSocketAddress(port);
        ss.bind(address);                                           //# 绑定端口
        serverChannel.configureBlocking(false);
        selector = Selector.open();                                 //# 打开选择器处理通道
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);   //# 将serveChannel注册到选择器上，并指定感兴趣的事件

        final ByteBuffer msg = ByteBuffer.wrap("Hi!\n".getBytes());

        while (true) {
            try {
                selector.select();                                  //# 等待新的事件准备就绪（这里会阻塞）
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();                                  //# 这里要从集合里面移除已经处理过的事件
                try {
                    if (key.isAcceptable()) {
                        ServerSocketChannel sever = (ServerSocketChannel) key.channel();
                        SocketChannel client = sever.accept();
                        System.out.println("Accepted connection from " + client);
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_WRITE|SelectionKey.OP_READ, msg.duplicate());
                    }

                    if (key.isWritable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        while (buffer.hasRemaining()) {
                            if (client.write(buffer) == 0) {
                                break;
                            }
                        }
                        client.close();
                    }

                } catch (IOException e) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new PlainNioServer().server(8080);
    }
}
