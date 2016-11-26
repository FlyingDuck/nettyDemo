## 异步套接字通道


> AsynchronousServerSocketChannel 和 AsynchronousSocketChannel 


我们将实现一个简单的 服务端／客户端


### 服务器

- **第一步** 打开 AsychronousServerSocketChannel 并将其绑定到类似于 ServerSocketChannel 的地址：
```
AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open().bind(null); // null 表示使用一个空闲的端口
```
方法 bind() 将一个套接字地址作为其参数。找到空闲端口的便利方法是传递一个 null 地址，它会自动将套接字绑定到本地主机地址，并使用空闲的 临时 端口。

- **第二步** 告诉通道接受一个连接：
```
Future<AsynchronousSocketChannel> acceptFuture = server.accept();
```
这是与 NIO 的第一个不同之处。接受调用总会立刻返回，并且，—— 不同于 ServerSocketChannel.accept() 返回一个 `SocketChannel` —— 这里返回一个 `Future<AsynchronousSocketChannel>` 对象，该对象可在以后用于检索 AsynchronousSocketChannel。 Future 对象的通用类型是实际操作的结果。比如，读取或写入操作会因为操作返回读或写的字节数，而返回一个 Future<Integer>。

利用 Future 对象，当前线程可阻塞来等待结果：
```
AsynchronousSocketChannel worker = future.get();
```
此处，其阻塞超时时间为 10 秒：
```
AsynchronousSocketChannel worker = future.get(10, TimeUnit.SECONDS);
```
或者轮询操作的当前状态，还可取消操作：
```
if (!future.isDone()) {
    future.cancel(true);
}
```
cancel() 方法可利用一个布尔标志来执行接受的线程是否可被中断。这是个很有用的增强；在以前的 Java 版本中，只能通过关闭套接字来中止此类阻塞 I/O 操作。

---

### 客户端

- 通过打开并连接与服务器之间的 AsynchronousSocketChannel，来设置客户端：
```
AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
client.connect(server.getLocalAddress()).get();
```
一旦客户端与服务器建立连接，可通过使用字节缓存的通道来执行读写操作：
```
//使用读写字节缓存
// 发送消息到服务端
ByteBuffer message = ByteBuffer.wrap("ping".getBytes());
client.write(message).get();

// 从客户端读取消息
worker.read(readBuffer).get(10, TimeUnit.SECONDS);
System.out.println("Message: " + new String(readBuffer.array()));
```
还支持异步地分散读操作与写操作，该操作需要大量字节缓存。

新异步通道的 API 完全从底层套接字中抽取掉：无法直接获取套接字，而以前可以调用 socket() ，例如，SocketChannel。

引入了两个新的方法 —— getOption 和 setOption —— 来在异步网络通道中查询并设置套接字选项。例如，可通过channel.getOption(StandardSocketOption.SO_RCVBUF) 而不是 channel.socket().getReceiveBufferSize() 来检索接收缓存大小。

---

#### 服务端

```
package com.dongshujin.demo.test.nio2.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by dongsj on 16/11/26.
 *
 */
public class SocketServer {
    public static final SocketAddress SERVER_ADDRESS = new InetSocketAddress("localhost", 9090);

    private void start() {
        try {
            // 打开一个服务端通道并自动绑定到一个地址
            System.err.println("S>>> Open server channel");

            AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open().bind(SERVER_ADDRESS);
            System.err.println("S>>> Initiate accept on");
            Future<AsynchronousSocketChannel> future = server.accept();

            // 阻塞当前线程等待结果(Accept)
            AsynchronousSocketChannel worker = future.get();
            System.err.println("S>>> Accept completed");


            ByteBuffer readBuffer = ByteBuffer.allocate(100);
            // 从客户端读取消息 设置超时时间为10s
            worker.read(readBuffer).get(10, TimeUnit.SECONDS);
            System.err.println("S>>> Message received from client: " + new String(readBuffer.array()));

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.err.println("S>>> Client didn't respond in time");
        }
    }
    
    public static void main(String[] args) throws IOException {
        new SocketServer().start();

    }
}
```


#### 客户端

```
package com.dongshujin.demo.test.nio2.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by dongsj on 16/11/26.
 */
public class SocketClient {
    public void start() {

        try {
            // 开启一个通道并连接到服务端
            System.out.println("C))) Open client channel");
            AsynchronousSocketChannel client = AsynchronousSocketChannel.open();

            System.out.println("C))) Connect to server");
            Future<Void> connectFuture = client.connect(SocketServer.SERVER_ADDRESS);

            int count = 10;
            while (true) {
                if (connectFuture.isDone()) {
                    break;
                } else {
                    count--;
                    System.out.println("C))) Check connect");
                    Thread.sleep(1000);
                }
                if (count < 0) {
                    System.out.println("C))) Cancel ");
                    connectFuture.cancel(true);
                    return;
                }
            }

            ByteBuffer message = ByteBuffer.wrap("ping".getBytes());
            // wait for the response
            System.out.println("C))) Sending message to the server...");
            Future<Integer> writeFuture  = client.write(message);
            int numberBytes = writeFuture.get();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
    

    public static void main(String[] args) {
        new SocketClient().start();
    }
}

```


![服务端](http://upload-images.jianshu.io/upload_images/1366868-c137b5788382469f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


![客户端](http://upload-images.jianshu.io/upload_images/1366868-faf54437e3bfdc8e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

