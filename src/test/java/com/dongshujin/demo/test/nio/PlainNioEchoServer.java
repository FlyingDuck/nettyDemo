package com.dongshujin.demo.test.nio;

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
 * Created by dongsj on 16/11/29.
 */
public class PlainNioEchoServer {
    public void serve(int port) throws IOException {
        System.out.println("Listening for connections on port " + port);

        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        ServerSocket ss = serverChannel.socket();
        InetSocketAddress address = new InetSocketAddress(port);
        ss.bind(address);                                                //#1
        serverChannel.configureBlocking(false);
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);        //#2
        while (true) {
            try {
                selector.select();                                       //#3
            } catch (IOException ex) {
                ex.printStackTrace();
                // handle in a proper way
                break;
            }
            Set readyKeys = selector.selectedKeys();                     //#4
            Iterator iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = (SelectionKey) iterator.next();
                iterator.remove();                                       //#5
                try {
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel)key.channel();
                        SocketChannel client = server.accept();          //#6
                        System.out.println("Accepted connection from : " + client);
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, ByteBuffer.allocate(100));              //#7
                    }
                    if (key.isReadable()) {                              //#8
                        System.out.println("Readable...");
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer output = (ByteBuffer) key.attachment();
                        client.read(output);                             //#9
                    }
                    if (key.isWritable()) {                             //#10
                        //System.out.println("Writable...");
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer output = (ByteBuffer) key.attachment();
                        output.flip();
                        client.write(output);                           //#11
                        output.compact();
                    }
                } catch (IOException ex) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException cex) {
                    }
                } }
        }
    }

    public static void main(String[] args) throws IOException {
        new PlainNioEchoServer().serve(8989);
    }
}
