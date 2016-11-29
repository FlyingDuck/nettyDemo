package com.dongshujin.demo.test.nio2.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by dongsj on 16/11/29.
 *
 */
public class FileWriterEnd {

    public void write() {
        try {
            final AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(
                    Paths.get("writeshow.log"),
                    StandardOpenOption.WRITE,
                    //StandardOpenOption.DELETE_ON_CLOSE,
                    StandardOpenOption.CREATE
            );

            CompletionHandler<Integer, Object> handler = new CompletionHandler<Integer, Object>() {
                @Override
                public void completed(Integer result, Object attachment) {
                    System.out.println(attachment + "S>>> completed with " + result + " bytes written");
                }

                @Override
                public void failed(Throwable e, Object attachment) {
                    if (e instanceof AsynchronousCloseException) {
                        System.out.println("S>>> File was closed before " + attachment + " executed");
                    } else {
                        System.err.println("S>>> " + attachment + " failed with:");
                        e.printStackTrace();
                    }
                }
            };

            int count = 0;
            int position = 0;
            while (count < 10) {
                byte[] contents = "hello  ".getBytes();
                System.out.println("S>>> Initiating write operation " + count);
                fileChannel.write(ByteBuffer.wrap(contents), position , "Write operation "+count + " ", handler);
                position += contents.length;
                count++;

                Thread.sleep(1000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new FileWriterEnd().write();
    }
}
