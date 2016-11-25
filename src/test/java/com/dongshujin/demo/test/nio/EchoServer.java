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
 * Created by dongsj on 16/11/25.
 *
 * Echo Server
 */
public class EchoServer {
    final int[] ports = new int[]{8080, 8181, 8282};


    private void start() {
        try {
            //
            Selector selector = Selector.open();

            //
            for (int i=0; i< ports.length; i++) {
                ServerSocketChannel ssc = ServerSocketChannel.open();
                ssc.configureBlocking(false);

                ServerSocket ss = ssc.socket();
                InetSocketAddress address = new InetSocketAddress(ports[i]);
                ss.bind(address);

                SelectionKey selectionKey = ssc.register(selector, SelectionKey.OP_ACCEPT);
            }

            while (true) {
                //
                int num = selector.select();

                Set<SelectionKey> set = selector.selectedKeys();
                Iterator<SelectionKey> iterator = set.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();

                    if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
                        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                        SocketChannel sc = ssc.accept();

                        sc.configureBlocking(false);
                        SelectionKey newKey = sc.register(selector, SelectionKey.OP_READ);

                        iterator.remove();

                        System.out.println(">>> ACCEPT");
                    } else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                        System.out.println(">>> Start Read Data...");

                        //FileOutputStream fout = new FileOutputStream("/Users/dongsj/workspace/dsj/javaSpace/nettyDemo/src/test/resources/nio/writeshow.log", true);
                        //FileChannel fileChannel = fout.getChannel();

                        SocketChannel sc = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(10);
                        int result = -1;
                        do {
                            result = sc.read(buffer);
                            buffer.flip();

                            // fileChannel.write(buffer);
                            sc.write(buffer);

                            buffer.clear();
                        } while (-1 == result);

                        System.out.println(">>> End Read Data");
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        new EchoServer().start();
    }
}
