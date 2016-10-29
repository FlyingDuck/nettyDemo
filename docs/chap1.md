Discard Server
--------------

```
Network Working Group                                          J. Postel
Request for Comments: 863                                            ISI
                                                                May 1983



                            Discard Protocol




This RFC specifies a standard for the ARPA Internet community.  Hosts on
the ARPA Internet that choose to implement a Discard Protocol are
expected to adopt and implement this standard.

A useful debugging and measurement tool is a discard service.  A discard
service simply throws away any data it receives.

TCP Based Discard Service

   One discard service is defined as a connection based application on
   TCP.  A server listens for TCP connections on TCP port 9.  Once a
   connection is established any data received is thrown away.  No
   response is sent.  This continues until the calling user terminates
   the connection.

UDP Based Discard Service

   Another discard service is defined as a datagram based application on
   UDP.  A server listens for UDP datagrams on UDP port 9.  When a
   datagram is received, it is thrown away.  No response is sent.


```


__[DiscardServerHandler.java](src/main/java/com/dongshujin/demo/netty/discard/DiscardServerHandler.java)__

1. DiscardServerHandler extends ChannelInboundHandlerAdapter, which is an implementation of ChannelInboundHandler. ChannelInboundHandler provides various event handler methods that you can override. For now, it is just enough to extend ChannelInboundHandlerAdapter rather than to implement the handler interface by yourself.
2. We override the channelRead() event handler method here. This method is called with the received message, whenever new data is received from a client. In this example, the type of the received message is ByteBuf.
3. To implement the DISCARD protocol, the handler has to ignore the received message. ByteBuf is a reference-counted object which has to be released explicitly via the release() method. Please keep in mind that it is the handler's responsibility to release any reference-counted object passed to the handler. 
4. The exceptionCaught() event handler method is called with a Throwable when an exception was raised by Netty due to an I/O error or by a handler implementation due to the exception thrown while processing events. In most cases, the caught exception should be logged and its associated channel should be closed here, although the implementation of this method can be different depending on what you want to do to deal with an exceptional situation. For example, you might want to send a response message with an error code before closing the connection.



__DiscardServer.java__

1. NioEventLoopGroup is a multithreaded event loop that handles I/O operation. Netty provides various EventLoopGroup implementations for different kind of transports. We are implementing a server-side application in this example, and therefore two  NioEventLoopGroup will be used. The first one, often called 'boss', accepts an incoming connection. The second one, often called 'worker', handles the traffic of the accepted connection once the boss accepts the connection and registers the accepted connection to the worker. How many Threads are used and how they are mapped to the created Channels depends on the EventLoopGroup implementation and may be even configurable via a constructor.
2. ServerBootstrap is a helper class that sets up a server. You can set up the server using a Channel directly. However, please note that this is a tedious process, and you do not need to do that in most cases.
3. Here, we specify to use the NioServerSocketChannel class which is used to instantiate a new Channel to accept incoming connections.
4. The handler specified here will always be evaluated by a newly accepted Channel. The ChannelInitializer is a special handler that is purposed to help a user configure a new Channel. It is most likely that you want to configure the ChannelPipeline of the new Channel by adding some handlers such as DiscardServerHandler to implement your network application. As the application gets complicated, it is likely that you will add more handlers to the pipeline and extract this anonymous class into a top level class eventually.
5. You can also set the parameters which are specific to the Channel implementation. We are writing a TCP/IP server, so we are allowed to set the socket options such as tcpNoDelay and keepAlive. Please refer to the apidocs of ChannelOption and the specific ChannelConfig implementations to get an overview about the supported ChannelOptions.
6. Did you notice option() and childOption()? option() is for the NioServerSocketChannel that accepts incoming connections. childOption() is for the Channels accepted by the parent ServerChannel, which is NioServerSocketChannel in this case.
7. We are ready to go now. What's left is to bind to the port and to start the server. Here, we bind to the port 8080 of all NICs (network interface cards) in the machine. You can now call the bind() method as many times as you want (with different bind addresses.)
