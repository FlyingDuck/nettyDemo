# 异步通道组

每个异步通道都属于一个通道组，它们共享一个 Java 线程池，该线程池用于完成启动的异步 I/O 操作。这看上去有点像欺骗，因为可在自己的 Java 线程中执行大多数异步功能，来获得相同的表现，并且，可能希望能够仅仅利用操作系统的异步 I/O 能力，来执行 NIO.2 ，从而获得更优的性能。然而，在有些情况下，有必要使用 Java 线程：比如，保证 completion-handler 方法在来自线程池的线程上执行。

默认情况下，具有 open() 方法的通道属于一个全局通道组，可利用如下系统变量对其进行配置：

- java.nio.channels.DefaultThreadPoolthreadFactory，其不采用默认设置，而是定义一个 java.util.concurrent.ThreadFactory
- java.nio.channels.DefaultThreadPool.initialSize，指定线程池的初始规模

java.nio.channels.AsynchronousChannelGroup 中的三个实用方法提供了创建新通道组的方法：
- withCachedThreadPool()
- withFixedThreadPool()
- withThreadPool()

这些方法或者对线程池进行定义，如 java.util.concurrent.ExecutorService，或者是 java.util.concurrent.ThreadFactory。例如，以下调用创建了具有线程池的新的通道组，该线程池包含 10 个线程，其中每个都构造为来自 Executors 类的线程工厂：
```
AsynchronousChannelGroup tenThreadGroup = AsynchronousChannelGroup.withFixedThreadPool(10, Executors.defaultThreadFactory());
```
三个异步网络通道都具有 open() 方法的替代版本，它们采用指定的通道组而不是默认通道组。例如，当有异步操作请求时，此调用告诉 channel 使用 tenThreadGroup 而不是默认通道组来获取线程：
```
AsynchronousServerSocketChannel channel = AsynchronousServerSocketChannel.open(tenThreadGroup);
```
定义自己的通道组可更好地控制服务于操作的线程，并能提供关闭线程或者等待终止的机制：
```
//利用通道组来控制线程关闭
// first initiate a call that won't be satisfied
channel.accept(null, completionHandler);
// once the operation has been set off, the channel group can be used to control the shutdown
if (!tenThreadGroup.isShutdown()) {
    // once the group is shut down no more channels can be created with it
    tenThreadGroup.shutdown();
}
if (!tenThreadGroup.isTerminated()) {
    // forcibly shutdown, the channel will be closed and the accept will abort
    tenThreadGroup.shutdownNow();
}
// the group should be able to terminate now, wait for a maximum of 10 seconds
tenThreadGroup.awaitTermination(10, TimeUnit.SECONDS);
```
AsynchronousFileChannel 在此处与其他通道不同，为了使用定制的线程池，open() 方法采用 ExecutorService 而不是 AsynchronousChannelGroup。

---

```
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
```



