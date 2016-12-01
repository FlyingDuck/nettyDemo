# Netty-鸟瞰

- **Bootstrap**：Netty应用从构建一个Bootstrap开始，通过Bootstrap可以轻松的去配置并启动应用。
- **ChannelHandler**：为了能够提供多协议并且多样的去处理数据，Netty使用handler回调对象去处理特定的事件（包括正常的数据传输事件以及异常的处理事件）。通常我们可以实现ChannelInboundHandler，这样我们可以把我们具体的业务逻辑处理封装在这个我们实现的handler中。
- **ChannelInitializer**：那我们怎么去绑定 ChannelHandler 去处理我们需要发送或者接收的消息呢？这里就用到ChannelInitializer，它的指责就是将 ChannelHandler 的实现加入到 ChannelPipeline。（事实上ChannelInitializer本身就是一个ChannelHandler，只不过这个handler会在加入其他handler的同时将自己从ChannelPipeline中移除）
- **ChannelPipeline**： ChannelPipeline 和 EventLoop、EventLoopGroup相近都与事件和事件处理相关。
- **EventLoop & EventLoopGroup**：指责在于处理通道中的IO操作，单个的 EventLoop 通常会处理多个通道上的事件。而 EventLoopGroup 包含了了多个 EventLoop ，并能用于去获取 EventLoop。
- **Channel**：一个通道代表了一个 socket 链接，或者能够进行IO处理的组件，因此这里用EventLoop来管理。
- **ChannelFuture**： Netty中的IO操作都是异步的（包括连接、读、写），这就意味着我们并不能知道操作是执行成功是否返回，但是我们需要在后续的操作中执行检测或者注册一些监听器来获取通知。Netty使用 Futures 和 ChannelFutures 去注册监听来获取通知。

    ChannelFuture是一个特殊的 java.util.concurrent.Future，它允许我们注册 ChannnelFutureListeners 到ChannelFuture。这些listener会在操作执行完成时得到通知。本质上来说，ChannelFuture是操作执行结果的占位符。所有的操作都会返回一个 ChannelFuture。

---

## EventLoop

Netty 是一个非阻塞的，事件驱动的网络框架。初看，Netty是用多线程来处理IO事件的。接触过多线程编程的人可能会想，在这样需要同步我们的代码。但事实上，Netty的设计使我们不需要做过多的这些考虑。

![EventLoop](http://upload-images.jianshu.io/upload_images/1366868-fc8b0b590bde620e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

如图中所示，Netty使用 EventLoopGroup 的组件里面有一个或者多个 EventLoop。当一个通道(Channel)被注册进来，Netty会绑定这个通道到一个单独的 EventLoop （当然也是在一个单独的线程中），并且这个通道的生命周期只会与这一个 EventLoop 绑定。这也就是为什么在我们的应用在Netty框架下不需要做同步处理（所有的IO操作都是在给定的通道及同一个线程中）

> EventLoop 总是被绑定到一个单独的线程中，在其生命周期中绝不会更换线程。

![EventLoop](http://upload-images.jianshu.io/upload_images/1366868-9894084dc39cc758.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

如图：EventLoop 和 EventLoopGroup 是一种 "is-a"关系

> 一个 EventLoop 就是一个 EventLoopGroup，这也就意味着我们在传入一个 EventLoopGroup 的地方同样也能指定一个 EventLoop。


## BootStrap & ServeBootStrap

***BootStrap***：用于创建客户端；
***ServerBootStrap***：用于创建服务端；

#### 不同点一：

ServerBootStrap 绑定到一个端口去监听客户端的链接；BootStrap 通常调用 connect() / bind()，然后在稍后使用 Channel （包含在ChannelFuture中）来进行连接。

#### 不同点二：

客户端 BootStrap 使用一个单独的EventLoopGroup；然而，ServerBootStrap 使用两个 EventLoopGroup （事实上使用同一个也是可以的），第一个集合包含一个单独的 ServerChannel 代表服务端自己的socket（这个socket被绑定到本地的一个端口上了），第二个集合包含所有的服务端接收的链接通道。

![Two EventLoopGroup](http://upload-images.jianshu.io/upload_images/1366868-1315d5246efd4552.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

如图，EventLoopGroupA 唯一的目的是接收链接然后将它们交付到 EventLoopGroupB。

Netty这样做的根本目的是为了客服链接瓶颈。在一个高并发的场景下，可能会有极其多的链接接入，当只有一个Group时，处理已有链接已经很繁忙，以至于无法接收新的链接，这最终会导致很多链接会超时。而使用两个Group，接收链接和处理链接分开，这样所有的链接都可以被接收。


> EventLoopGroup 可能包含多个EventLoop（不过也取决与我们的具体配置），每一个通道会有一个 EventLoop 与它绑定并且在整个生命周期内都不会更换。不过，由于 EventLoopGroup 中的 EventLoop 会比通道小，所以会有很多通道共享一个 EventLoop，这也意味着在同一个 EventLoop 中，一个通道处理繁忙的话，将不允许去处理其他的通道，因此不要使用阻塞EventLoop的原因。

![One EvetLoopGroup](http://upload-images.jianshu.io/upload_images/1366868-764b358e3005d7b0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

如图，当只有一个group时，同一个实例会被使用两次。


## ChannelHandler

我们很容易想到 ChannelHandler 是用来处理数据流的，但是实际上 ChannelHandler 还能有很多其他的应用。

![ChannelHandler](http://upload-images.jianshu.io/upload_images/1366868-b93797a87d33d852.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

如图，从类继承关系上可以看出，我们有两种 ChannelHandler，也反映出数据流是双向的（数据可以从我们的应用向外流出，也能从远端流入我们的应用）。

数据从一段流到另一端的过程中，会经过一个或者多个 ChannelHandler 的处理。这个 ChannelHandler 会被加入到应用中，并且它们加入的顺序决定了它们处理数据的顺序。

既然会设计到多个 ChannelHandler 协作，必然会有一定的规则需要遵守。这里的规则很简单：ChannelPipeline 就是这写 ChannelHandler 的约束。每一个 ChannelHandler 处理完自己的部分后都会将数据传递到同一个 ChannelPipeline 中的下一个 ChannelHandler，直到没有 ChannelHandler 为止。

![ChannelPipeline](http://upload-images.jianshu.io/upload_images/1366868-02a705898d5aa838.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


如图：反映了 ChannelInboundHandler 和 ChannelOutboundHandler 能够同时存在于一个 ChannelPipeline 中。

由于我们的 ChannelHandler 通常实现自 ChannelInboundHandler 或 ChannelOutboundHandler 所以Netty会知道各个handler的类型，这样在一个流出的事件中就可以跳过所有的 ChannelInboundHandler。

每一个加入 ChannelPipeline 中的 ChannelHandler 会得到一个 ChannelHandlerContext。通常获得 ChannelHandlerContext 的引用是安全的，但是在 UDP 协议下可能不一定。 这个 ChannelHandlerContext 可以用于获取底层的 channel 用于 write/send 消息。这样就存在两种方式来发送消息：直接写到通道 或者 通过 ChannelHandlerContext 来写消息，它们的主要区别是，直接写到通道中的消息会从 ChannelPipeline 的尾部开始，写到 ChannelHandlerContext 中的消息会传递给下一个handler


> 通过回调方法中携带的 ChannelHandlerContext 参数，我们可以将一个事件可以定向到下一个 ChannelInboundHandler 或者 前一个 ChannelOutboundHandler 中。（Netty为我们提供的抽象基类 ChannelInboundHandlerAdapter 和 ChannelOutboundHandlerAdapter 只提供单方向的传递，但是我们不需要手动调用传递方法）


## Encoder & Decoder

每一个通道都有传递Netty事件的职责，Netty类中 *Adapter 结尾的类帮我们实现了这一过程，这样我们不需要去关注这部分的工作，我们只需要去处理我们感兴趣的部分。除了 *Adapter 的类外，同样还有很多其他功能扩展的类我们可以使用，比如 encode/decode 消息。

当我们接收到消息时，我们必须将其从 bytes 转化成 Java对象。当发送消息时，我们同样需要将消息从Java对象转换成bytes。这样的操作很频繁，因此Netty为我们提供了很多基础类，类似于 ByteToMessageDecoder 和 MessageToByteEncoder 就提供这样的功能。我们应用中用的最多的可能是读取消息并解码然后再进行一系列的其他处理，我们可以继承 SimpleChannelInboundHandler<T> （T 就是我们要处理的消息类型），这个handler的主要方法channelRead0(ChannelHandlerContext,T)，不能何时调用该方法，T 对象就是我们要处理的消息。


> 在IO线程中，不能进行阻塞的操作。Netty 允许在添加 ChannelHandler 到 ChannelPipeline 中时指定一个 EventExecutorGroup， 它会被用于获取一个 EventExecutor 对象，这个 EventExecutor 将用于执行所有的ChannelHandler的操作（EventExecutor 会使用一个另外的线程）



