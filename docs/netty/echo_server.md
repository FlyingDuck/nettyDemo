# EchoServer


### EchoServer

```
public class EchoServer {
    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public void start() throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();                  //#1
            b.group(group)                                              //#2
                    .channel(NioServerSocketChannel.class)              //#2
                    .localAddress(new InetSocketAddress(port))          //#2
                    .childHandler(new ChannelInitializer<SocketChannel>() { //#3
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new EchoServerHandler());                  //#4
                        }
                    });

            ChannelFuture f = b.bind().sync();              //#5
            System.out.println(EchoServer.class.getSimpleName() + " started and listener on " + f.channel().localAddress());
            f.channel().closeFuture().sync();               //#6
        } finally {
            group.shutdownGracefully().sync();              //#7
        }

    }


    public static void main(String[] args) throws Exception {
        new EchoServer(8989).start();
    }

}
```

- 首先，创建一个ServerBootstrap实例
- 指定 NioEventLoopGroup 接收新的链接，并处理已经接收的链接
- 设置通道类型为 NioServerSocketChannel （当然除了NIO，也有其他痛到可以选择，例如：OIO OioServerSocketChannel）
- 设置绑定的 InetSocketAddress
- 指定 ChannelHandler 来处理接收的链接（这里使用ChannelInitializer创建了一个子通道）

***ChannelPipeline*** 持有通道中所有不同的ChannelHandlers

***sync()*** 该方法会阻塞直到服务绑定（在关闭时同理）


### EchoServerHandler

```
@ChannelHandler.Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Active");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Read");
        System.out.println("Server received : " + msg);
        ctx.write(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Read Complete");
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        //ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
```


Netty使用前面提到了Future和Callback的概念去处理不同的事件。我们需要继承`ChannelInboundHandlerAdapter`，这样我们可以处理不同的事件回调。

- `channelRead()`方法，这个方法会在每次消息到达时回调。
- `exceptionCaught()`方法，执行异常情况下会被回调。


### EchoClient

```
public class EchoClient {
    private String host;
    private int port;

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();                        //#1 创建 bootstrap 客户端
            b.group(group)                                        //#2 这里指定 NioEventLoopGroup 处理客户端事件
                    .channel(NioSocketChannel.class)                     //#3 指定通道类型
                    .remoteAddress(new InetSocketAddress(host, port))    //#4 设置绑定地址和端口
                    .handler(new ChannelInitializer<SocketChannel>() {   //#5 使用ChannelInitializer，指定通道处理器
                        @Override
                        public void initChannel(SocketChannel ch)throws Exception {
                            ch.pipeline().addLast(new EchoClientHandler());//#6 将EchoClientHandler加入到管道
                        } });
            ChannelFuture f = b.connect().sync();                 //#7 连接到服务端
            f.channel().closeFuture().sync();                     //#8 阻塞直到客户端通道关闭
        } finally {
            group.shutdownGracefully().sync();                    //#9 关闭线程池释放资源
        }
    }


    public static void main(String[] args) throws Exception {
        new EchoClient("127.0.0.1", 8989).start();
    }
}
```


### EchoClientHandler

```
@ChannelHandler.Sharable                                                        // #1  该注解标示该处理器是可以在通道间共享的
public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf>{


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Active");
        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8)); //#2 通道连接上后写入消息 记得flush() 很重要
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf in) {
        System.out.println("Read");

        System.out.println("Client received: " + ByteBufUtil
                .hexDump(in.readBytes(in.readableBytes())));  //#4
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,              //#5
                                Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
```


- **channelRead0()** 接收到数据的时候会回调该方法。但是，该方法接收的数据是分片的。也就是说，如果服务端写入了5byte的数据，该方法并不能保证一次就接收5byte的数据，而可能回被回调两次，一次接收3byte，一次接收2byte。不过像TCP这类的协议，该方法会保证接收数据的顺序是与发送时一致的。
- **SimpleChannelInboundHandler & ChannelInboundHandlerAdapter** 我们这里使用前者的原因是后者在接收处理完数据后需要负责释放资源。在使用SimpleChannelInboundHandler时channelRead0()回调完成后Netty会帮我们完成释放。而在EchoServerHandler中我们使用ChannelInboundHandlerAdapter是因为在服务端我们需要回显（Echo）消息，在回调方法channelRead()中写入消息时又是异步写入，所以在该方法中我们并不能释放资源，而是在写入完成后由Netty帮我们完成释放。


![Server结果](http://upload-images.jianshu.io/upload_images/1366868-5406c72ebfba9ffd.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![Client结果](http://upload-images.jianshu.io/upload_images/1366868-bf0d0167f51e873e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

