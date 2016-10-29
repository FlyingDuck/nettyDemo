Time Server
-----------


```
Network Working Group                                    J. Postel - ISI
Request for Comments: 868                           K. Harrenstien - SRI
                                                                May 1983



                             Time Protocol




This RFC specifies a standard for the ARPA Internet community.  Hosts on
the ARPA Internet that choose to implement a Time Protocol are expected
to adopt and implement this standard.

This protocol provides a site-independent, machine readable date and
time.  The Time service sends back to the originating source the time in
seconds since midnight on January first 1900.

One motivation arises from the fact that not all systems have a
date/time clock, and all are subject to occasional human or machine
error.  The use of time-servers makes it possible to quickly confirm or
correct a system's idea of the time, by making a brief poll of several
independent sites on the network.

This protocol may be used either above the Transmission Control Protocol
(TCP) or above the User Datagram Protocol (UDP).

When used via TCP the time service works as follows:

   S: Listen on port 37 (45 octal).

   U: Connect to port 37.

   S: Send the time as a 32 bit binary number.

   U: Receive the time.

   U: Close the connection.

   S: Close the connection.

   The server listens for a connection on port 37.  When the connection
   is established, the server returns a 32-bit time value and closes the
   connection.  If the server is unable to determine the time at its
   site, it should either refuse the connection or close it without
   sending anything.
```

__TimeServerHandler.java__

1. As explained, the channelActive() method will be invoked when a connection is established and ready to generate traffic. Let's write a 32-bit integer that represents the current time in this method.
2. To send a new message, we need to allocate a new buffer which will contain the message. We are going to write a 32-bit integer, and therefore we need a ByteBuf whose capacity is at least 4 bytes. Get the current ByteBufAllocator via ChannelHandlerContext.alloc() and allocate a new buffer.
3. As usual, we write the constructed message.
    But wait, where's the flip? Didn't we used to call java.nio.ByteBuffer.flip() before sending a message in NIO? ByteBuf does not have such a method because it has two pointers; one for read operations and the other for write operations. The writer index increases when you write something to a ByteBuf while the reader index does not change. The reader index and the writer index represents where the message starts and ends respectively.
    
    In contrast, NIO buffer does not provide a clean way to figure out where the message content starts and ends without calling the flip method. You will be in trouble when you forget to flip the buffer because nothing or incorrect data will be sent. Such an error does not happen in Netty because we have different pointer for different operation types. You will find it makes your life much easier as you get used to it -- a life without flipping out!
    
    Another point to note is that the ChannelHandlerContext.write() (and writeAndFlush()) method returns a ChannelFuture. A  ChannelFuture represents an I/O operation which has not yet occurred. It means, any requested operation might not have been performed yet because all operations are asynchronous in Netty. For example, the following code might close the connection even before a message is sent:
    ```
    Channel ch = ...;
    ch.writeAndFlush(message);
    ch.close();
    ```
    
    Therefore, you need to call the close() method after the ChannelFuture is complete, which was returned by the write() method, and it notifies its listeners when the write operation has been done. Please note that, close() also might not close the connection immediately, and it returns a ChannelFuture.
4. How do we get notified when a write request is finished then? This is as simple as adding a ChannelFutureListener to the returned ChannelFuture. Here, we created a new anonymous ChannelFutureListener which closes the Channel when the operation is done.
   Alternatively, you could simplify the code using a pre-defined listener:
   ```
   f.addListener(ChannelFutureListener.CLOSE);    
   ```

__TimeClient.java__

1. Bootstrap is similar to ServerBootstrap except that it's for non-server channels such as a client-side or connectionless channel.
2. If you specify only one EventLoopGroup, it will be used both as a boss group and as a worker group. The boss worker is not used for the client side though.
3. Instead of NioServerSocketChannel, NioSocketChannel is being used to create a client-side Channel.
4. Note that we do not use childOption() here unlike we did with ServerBootstrap because the client-side SocketChannel does not have a parent.
5. We should call the connect() method instead of the bind() method.


__TimeClientHandler.java__

1. In TCP/IP, Netty reads the data sent from a peer into a ByteBuf.

