package com.dongshujin.demo.test.nio2.group;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by dongsj on 16/11/29.
 */
public class ChannelGroup {

    public ChannelGroup() throws IOException, InterruptedException {
        // 创建通道组
        AsynchronousChannelGroup tenThreadGroup = AsynchronousChannelGroup.withFixedThreadPool(10, Executors.defaultThreadFactory());
        System.out.print("Create a channel with a channel group");
        // 打开通道时，指定通道组
        AsynchronousServerSocketChannel channel = AsynchronousServerSocketChannel.open(tenThreadGroup).bind(null);

        System.out.println("and start an accept that won't be satisfied");
        channel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>(){

            @Override
            public void completed(AsynchronousSocketChannel result, Object attachment) {
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
            }
        }
        );

        if (!tenThreadGroup.isShutdown()) {
            System.out.println("Shutdown channel group");
            // mark as shutdown, no more channels can now be created with this pool
            tenThreadGroup.shutdown();
        }
        if (!tenThreadGroup.isTerminated()) {
            System.out.println("Terminate channel group");
            // forcibly shutdown, the channel will be closed and the read will abort
            tenThreadGroup.shutdownNow();
        }
        System.out.println("Wait for termination");
        // the group should be able to terminate now, wait for a maximum of 10 seconds
        boolean terminated = tenThreadGroup.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("Group is terminated? " + terminated);
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        new ChannelGroup();
    }
}
