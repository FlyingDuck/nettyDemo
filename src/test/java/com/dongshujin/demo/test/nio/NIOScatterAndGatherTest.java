package com.dongshujin.demo.test.nio;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by dongsj on 16/11/25.
 * NIO 分散读取 & 聚集写入
 */
public class NIOScatterAndGatherTest {

    @Test
    public void scatteringTest() {
        try {
            FileInputStream fin = new FileInputStream("/Users/dongsj/workspace/dsj/javaSpace/nettyDemo/src/test/resources/nio/readandshow.log");
            FileChannel fileChannel = fin.getChannel();

            ByteBuffer headerBuffer = ByteBuffer.allocate(6);
            ByteBuffer contentBuffer = ByteBuffer.allocate(20);

            ByteBuffer[] buffers = new ByteBuffer[]{headerBuffer, contentBuffer};
            // 读取
            long result = fileChannel.read(buffers);

            System.out.println("result : " + result);
            System.out.println("headerBuffer: capacity=" + headerBuffer.capacity()+ " position=" + headerBuffer.position());
            System.out.println("contentBuffer: capacity="+ contentBuffer.capacity() +" position=" + contentBuffer.position());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void gatheringTest() {

        byte[] msgHeader = "Header".getBytes();
        byte[] msgContent = "This is a message".getBytes();

        try {
            FileOutputStream fout = new FileOutputStream("/Users/dongsj/workspace/dsj/javaSpace/nettyDemo/src/test/resources/nio/writeshow.log");

            FileChannel channelout = fout.getChannel();

            ByteBuffer headerBuffer = ByteBuffer.allocate(6);
            ByteBuffer contentBuffer = ByteBuffer.allocate(20);

            headerBuffer.put(msgHeader);
            headerBuffer.flip();
            contentBuffer.put(msgContent);
            contentBuffer.flip();

            ByteBuffer[] buffers = new ByteBuffer[]{headerBuffer, contentBuffer};

            // 写入
            channelout.write(buffers);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
