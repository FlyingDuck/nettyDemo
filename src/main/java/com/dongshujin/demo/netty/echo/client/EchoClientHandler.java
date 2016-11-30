package com.dongshujin.demo.netty.echo.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

/**
 * Created by dongsj on 16/11/30.
 */
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

/*
channelRead0() 接收到数据的时候会回调该方法。但是，该方法接收的数据是分片的。也就是说，如果服务端写入了5byte的数据，该方法并不能保证一次就接收5byte的数据，而可能回被回调两次，一次接收3byte，一次接收2byte。不过像TCP这类的协议，该方法会保证接收数据的顺序是与发送时一直的。

SimpleChannelInboundHandler & ChannelInboundHandlerAdapter 我们这里使用前者的原因是后者在接收处理完数据后需要负责释放资源。在使用SimpleChannelInboundHandler时channelRead0()回调完成后Netty会帮我们完成释放。而在EchoServerHandler中我们使用ChannelInboundHandlerAdapter是因为在服务端我们需要回显（Echo）消息，在回调方法channelRead()中写入消息时又是异步写入，所以在该方法中我们并不能释放资源，而是在写入完成后由Netty帮我们完成释放。

 */
