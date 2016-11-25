package com.dongshujin.demo.test.nio;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by dongsj on 16/11/23.
 */
public class NIORandWTest {


    @Test
    public void testRead() {
        try {
            FileInputStream fin = new FileInputStream("/Users/dongsj/workspace/dsj/javaSpace/nettyDemo/src/test/resources/nio/readandshow.log");
            FileChannel fileChannel = fin.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int result = fileChannel.read(buffer);

            System.out.println("read : " + result);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWrite() {
        byte[] message = "some bytes to write".getBytes();

        try {
            FileOutputStream fout = new FileOutputStream("/Users/dongsj/workspace/dsj/javaSpace/nettyDemo/src/test/resources/nio/writeshow.log");
            FileChannel fileChannel = fout.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            for (int i=0; i < message.length; i++) {
                buffer.put(message[i]);
            }
            buffer.flip();

            fileChannel.write(buffer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testReadAndWrite() {
        FileInputStream fin;
        FileOutputStream fout;
        FileChannel finChannel, foutChannel;

        try {
            fin = new FileInputStream("/Users/dongsj/workspace/dsj/javaSpace/nettyDemo/src/test/resources/nio/readandshow.log");
            fout = new FileOutputStream("/Users/dongsj/workspace/dsj/javaSpace/nettyDemo/src/test/resources/nio/readandshow.log_copy");
            finChannel = fin.getChannel();
            foutChannel = fout.getChannel();

            ByteBuffer buffer = ByteBuffer.allocate(10);

            int count = -1;
            do {
                count = finChannel.read(buffer);

                buffer.flip();
                foutChannel.write(buffer);
                buffer.clear();
            } while (-1 != count);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
    }


}
