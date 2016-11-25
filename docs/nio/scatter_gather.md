# 分散和聚集

分散/聚集 是使用多个而不是单个缓冲区来保存数据的读写方法。

一个分散的读取就像一个常规通道读取，只不过它是将数据读到一个缓冲区数组中而不是读到单个缓冲区中。同样地，一个聚集写入是向缓冲区数组而不是向单个缓冲区写入数据。

分散/聚集 I/O 对于将数据流划分为单独的部分很有用，这有助于实现复杂的数据格式。

---

### 分散读取

通道可以有选择地实现两个新的接口： ScatteringByteChannel 和 GatheringByteChannel。

- ScatteringByteChannel 是一个具有两个附加读方法的通道：
```
long read( ByteBuffer[] dsts );
long read( ByteBuffer[] dsts, int offset, int length );
```
这些 long read() 方法很像标准的 read 方法，只不过它们不是取单个缓冲区而是取一个缓冲区数组。
在 分散读取 中，通道依次填充每个缓冲区。填满一个缓冲区后，它就开始填充下一个。在某种意义上，缓冲区数组就像一个大缓冲区。

 测试数据：
 ![测试数据](http://upload-images.jianshu.io/upload_images/1366868-483b65ead5627f59.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

```
    @Test
    public void scatteringTest() {
        try {
            FileInputStream fin = new FileInputStream("/Users/dongsj/workspace/dsj/javaSpace/nettyDemo/src/test/resources/nio/readandshow.log");
            FileChannel fileChannel = fin.getChannel();

            ByteBuffer headerBuffer = ByteBuffer.allocate(6);
            ByteBuffer contentBuffer = ByteBuffer.allocate(20);

            ByteBuffer[] buffers = new ByteBuffer[]{headerBuffer, contentBuffer};
            // 读取
            long result = fileChannel.read(buffers);

            System.out.println("result : " + result);
            System.out.println("headerBuffer: capacity=" + headerBuffer.capacity()+ " position=" + headerBuffer.position());
            System.out.println("contentBuffer: capacity="+ contentBuffer.capacity() +" position=" + contentBuffer.position());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
```

![结果](http://upload-images.jianshu.io/upload_images/1366868-7a024d5769cab531.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



### 分散/聚集的应用

分散/聚集 I/O 对于将数据划分为几个部分很有用。例如，一个使用消息对象的网络应用程序，每一个消息被划分为固定长度的头部和固定长度的正文。可以创建一个刚好可以容纳头部的缓冲区和另一个刚好可以容难正文的缓冲区。当您将它们放入一个数组中并使用分散读取来向它们读入消息时，头部和正文将整齐地划分到这两个缓冲区中。

我们从缓冲区所得到的方便性对于缓冲区数组同样有效。因为每一个缓冲区都跟踪自己还可以接受多少数据，所以分散读取会自动找到有空间接受数据的第一个缓冲区。在这个缓冲区填满后，它就会移动到下一个缓冲区。


### 聚集写入

聚集写入 类似于分散读取，只不过是用来写入。它也有接受缓冲区数组的方法：
```
long write( ByteBuffer[] srcs );
long write( ByteBuffer[] srcs, int offset, int length );
```
聚集写对于把一组单独的缓冲区中组成单个数据流很有用。为了与上面的消息例子保持一致，可以使用聚集写入来自动将网络消息的各个部分组装为单个数据流，以便跨越网络传输消息。



```
    @Test
    public void gatheringTest() {

        byte[] msgHeader = "Header".getBytes();
        byte[] msgContent = "This is a message".getBytes();

        try {
            FileOutputStream fout = new FileOutputStream("/Users/dongsj/workspace/dsj/javaSpace/nettyDemo/src/test/resources/nio/writeshow.log");

            FileChannel channelout = fout.getChannel();

            ByteBuffer headerBuffer = ByteBuffer.allocate(6);
            ByteBuffer contentBuffer = ByteBuffer.allocate(20);

            headerBuffer.put(msgHeader);
            headerBuffer.flip();
            contentBuffer.put(msgContent);
            contentBuffer.flip();

            ByteBuffer[] buffers = new ByteBuffer[]{headerBuffer, contentBuffer};

            // 写入
            channelout.write(buffers);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

```
![结果](http://upload-images.jianshu.io/upload_images/1366868-05f5e597ac384525.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


> FileChannel 实现了GatheringByteChannel, ScatteringByteChannel 接口