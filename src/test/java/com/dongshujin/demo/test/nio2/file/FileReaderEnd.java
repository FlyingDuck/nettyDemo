package com.dongshujin.demo.test.nio2.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by dongsj on 16/11/29.
 */
public class FileReaderEnd {

    public void read() {
        try {
            System.err.println("The Thread ID : " + Thread.currentThread().getId());

            final AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(
                    Paths.get("writeshow.log"),
                    StandardOpenOption.READ
            );

            final ByteBuffer buffer = ByteBuffer.allocate(7);
            CompletionHandler<Integer, Object> handler= new CompletionHandler<Integer, Object>(){
                @Override
                public void completed(Integer result, Object attachment) {
                    System.err.println("The Thread ID : " + Thread.currentThread().getId());
                    System.out.println("C))) Read operation completed, file contents is: " + new String(buffer.array()));
                    clearUp();
                }
                @Override
                public void failed(Throwable e, Object attachment) {
                    System.err.println("C))) Exception performing write");
                    e.printStackTrace();
                    clearUp();
                }

                private void clearUp() {
                    try {
                        fileChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };


            System.out.println("C))) Initiating read operation");


            //Future<Integer> future = fileChannel.read(buffer, 0);
            //System.out.println("future : " + future.get());
            fileChannel.read(buffer, 0, null, handler);

            Thread.sleep(3*1000);

        } catch (IOException e) {
            e.printStackTrace();
        } /*catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }*/ catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new FileReaderEnd().read();
    }

}
