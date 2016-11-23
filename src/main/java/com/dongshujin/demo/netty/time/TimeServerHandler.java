package com.dongshujin.demo.netty.time;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by dongsj on 16/10/29.
 */
public class TimeServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 链接建立时调用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {     // (1)
//        super.channelActive(ctx);
        final ByteBuf time = ctx.alloc().buffer(4);     // (2)
        time.writeInt((int)(System.currentTimeMillis()/1000 + 2208988800L));    // (3)

        final ChannelFuture channelFuture = ctx.writeAndFlush(time);
        channelFuture.addListener(new ChannelFutureListener() {

            public void operationComplete(ChannelFuture future) throws Exception {
                assert future == channelFuture;
                //ctx.close();  // 这里关闭
            }
        }); // (4)
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.write(msg);
        ctx.flush();
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }
}
