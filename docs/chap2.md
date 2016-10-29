Echo Server
-----------



```
Network Working Group                                          J. Postel
Request for Comments: 862                                            ISI
                                                                May 1983



                             Echo Protocol




This RFC specifies a standard for the ARPA Internet community.  Hosts on
the ARPA Internet that choose to implement an Echo Protocol are expected
to adopt and implement this standard.

A very useful debugging and measurement tool is an echo service.  An
echo service simply sends back to the originating source any data it
receives.

TCP Based Echo Service

   One echo service is defined as a connection based application on TCP.
   A server listens for TCP connections on TCP port 7.  Once a
   connection is established any data received is sent back.  This
   continues until the calling user terminates the connection.

UDP Based Echo Service

   Another echo service is defined as a datagram based application on
   UDP.  A server listens for UDP datagrams on UDP port 7.  When a
   datagram is received, the data from it is sent back in an answering
   datagram.
```



__EchoServerHandler.java__

1. A ChannelHandlerContext object provides various operations that enable you to trigger various I/O events and operations. Here, we invoke write(Object) to write the received message in verbatim. Please note that we did not release the received message unlike we did in the DISCARD example. It is because Netty releases it for you when it is written out to the wire.
2. ctx.write(Object) does not make the message written out to the wire. It is buffered internally, and then flushed out to the wire by ctx.flush(). Alternatively, you could call ctx.writeAndFlush(msg) for brevity.
