Understand NIO
--------------

### NIO简介：
I/O（输入/输出）：指的是计算机与外部世界或者一个程序与计算机的其余部分的之间的接口。它对于任何计算机系统都非常关键，因而I/O的主体实际上是内置在操作系统中的。单独的程序一般是让系统为它们完成大部分的工作。
在Java编程中，一直使用**流**的方式完成 I/O。所有I/O都被视为单个的字节的移动，通过一个称为 Stream 的对象一次移动一个字节。

- 流I/O用于与外部世界接触。它也在内部使用，用于将对象转换为字节，然后再转换回对象。
- NIO与原来的I/O有同样的作用和目的，但是它使用不同的方式—**块**I/O，块I/O的效率可以比流 I/O 高许多。


### 为什么有NIO
NIO的创建目的是为了让 Java 程序员可以实现高速 I/O 而无需编写自定义的本机代码。NIO 将最耗时的 I/O 操作(即填充和提取缓冲区)转移回操作系统，因而可以极大地提高速度。


### 流 VS 块
原来的I/O库(java.io.*) 与 NIO最重要的区别是数据打包和传输的方式。原来的I/O以流的方式处理数据，而NIO以块的方式处理数据。

***面向流*** 的I/O系统一次一个字节地处理数据。一个输入流产生一个字节的数据，一个输出流消费一个字节的数据。为流式数据创建过滤器非常容易。链接几个过滤器，以便每个过滤器只负责单个复杂处理机制的一部分，这样也是相对简单的。不利的一面是，面向流的 I/O 通常相当慢。

***面向块*** 的 I/O 系统以块的形式处理数据。每一个操作都在一步中产生或者消费一个数据块。按块处理数据比按(流式的)字节处理数据要快得多。但是面向块的I/O缺少一些面向流的I/O所具有的优雅性和简单性。

### 集成的I/O
在 JDK 1.4 中原来的 I/O 包和 NIO 已经很好地集成了。 java.io.* 已经以 NIO 为基础重新实现了，所以现在它可以利用 NIO 的一些特性。例如， java.io.* 包中的一些类包含以块的形式读写数据的方法，这使得即使在更面向流的系统中，处理速度也会更快。
也可以用 NIO 库实现标准 I/O 功能。例如，可以容易地使用块 I/O 一次一个字节地移动数据。NIO 还提供了原 I/O 包中所没有的许多好处。


---


## 通道 & 缓冲区
通道 和 缓冲区 是 NIO 中的核心对象，几乎在每一个 I/O 操作中都要使用它们。

***Channel（通道）*** 是对原 I/O 包中的`流`的模拟。到任何目的地(或来自任何地方)的所有数据都必须通过一个 Channel 对象。

***Buffer（缓冲区）*** 是一个容器对象。发送给一个通道的所有对象都必须首先放到缓冲区中；同样地，从通道中读取的任何数据都要读到缓冲区中。


### 关于缓冲区
Buffer 包含一些要写入或者刚读出的数据。 在 NIO 中加入 Buffer 对象，体现了新库(JDK1.4)与原 I/O 库的一个重要区别。在面向流的 I/O 中，您将数据直接写入或者将数据直接读到 Stream 对象中。在 NIO 库中，所有数据都是用缓冲区处理的。在读取数据时，它是直接读到缓冲区中的。在写入数据时，它是写入到缓冲区中的。任何时候访问 NIO 中的数据，您都是将它放到缓冲区中。
缓冲区实质上是一个数组。通常它是一个字节数组，但是也可以使用其他种类的数组。但是一个缓冲区不 仅仅 是一个数组。缓冲区提供了对数据的结构化访问，而且还可以跟踪系统的读/写进程。

最常用的缓冲区类型是 ByteBuffer。一个 ByteBuffer 可以在其底层字节数组上进行 get/set 操作(即字节的获取和设置)。
对于每一种基本 Java 类型都有一种缓冲区类型：
- ByteBuffer
- CharBuffer
- ShortBuffer
- IntBuffer
- LongBuffer
- FloatBuffer
- DoubleBuffer

每一个 Buffer 类都是 Buffer 接口的一个实例。 除了 ByteBuffer，每一个 Buffer 类都有完全一样的操作，只是它们所处理的数据类型不一样。因为大多数标准 I/O 操作都使用 ByteBuffer，所以它具有所有共享的缓冲区操作以及一些特有的操作。


### 关于通道
Channel 可以通过它读取和写入数据。拿 NIO 与原来的 I/O 做个比较，通道就像是流。
正如前面提到的，所有数据都通过 Buffer 对象来处理。永远不会将字节直接写入通道中，相反，是将数据写入包含一个或者多个字节的缓冲区。同样，不会直接从通道中读取字节，而是将数据从通道读入缓冲区，再从缓冲区获取这个字节。

通道与流的不同之处在于通道是双向的。而流只是在一个方向上移动(一个流必须是 InputStream 或者 OutputStream 的子类)， 而 通道 可以用于读、写或者同时用于读写。
因为它们是双向的，所以通道可以比流更好地反映底层操作系统的真实情况。特别是在 UNIX 模型中，底层操作系统通道是双向的。

---


NIO.2
-----

### NIO.2简介

Java 7 中的 More New I/O APIs，通过在 java.nio.channels 包中增加四个异步通道，从而增强了 Java 1.4 中的 New I/O APIs（NIO）：

1. AsynchronousSocketChannel
2. AsynchronousServerSocketChannel
3. AsynchronousFileChannel
4. AsynchronousDatagramChannel

> ***异步通道*** 提供支持连接、读取、以及写入之类非锁定操作的连接，并提供对已启动操作的控制机制。

这些类在风格上与 NIO 通道 API 很相似。他们共享相同的方法与参数结构体，并且大多数对于 NIO 通道类可用的参数，对于新的异步版本仍然可用。主要区别在于新通道可使一些操作异步执行。

异步通道 API 提供两种对已启动异步操作的监测与控制机制：

- 第一种 是通过返回一个 `java.util.concurrent.Future` 对象来实现，它将会建模一个挂起操作，并可用于查询其状态以及获取结果。
- 第二种 是通过传递给操作一个新类的对象，`java.nio.channels.CompletionHandler`来完成，它会定义操作完毕后所执行的处理程序方法。每个异步通道类为每个操作定义 API 副本，这样可采用任一机制。





NIO
---

1. [NIO读写](docs/nio/read&write.md)
2. [NIO缓冲区(1)](docs/nio/buffer_1.md)
3. [NIO缓冲区(2)](docs/nio/buffer_2.md)
4. [分散/聚集](docs/nio/scatter_gather.md)
5. [文件锁定](docs/nio/file_lock.md)
6. [异步IO](docs/nio/asynchronous_io.md)
7. [字符集](docs/nio/charset.md)

NIO.2
-----

1. [异步套接字通道](docs/nio2/async_socket.md)
2. [异步文件瞳代](docs/nio2/async_file.md)



[Netty](http://netty.io/)
-------------------------

1. [Discard Server](docs/chap1.md)
2. [Echo Server](docs/chap2.md)
3. [Time Server](docs/chap3.md)



