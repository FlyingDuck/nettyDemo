# NIO 读写

读和写是 I/O 的基本过程。从一个通道中读取很简单：只需创建一个缓冲区，然后让通道将数据读到这个缓冲区中。写入也相当简单：创建一个缓冲区，用数据填充它，然后让通道用这些数据来执行写入操作。

## 从文件中读取
从一个文件中读取一些数据。

- I/O方式： 使用原来的 I/O，那么我们只需创建一个 FileInputStream 并从它那里读取。  
- NIO方式： 先从 FileInputStream 获取一个 Channel 对象，然后使用这个通道来读取数据。

在 NIO 系统中，任何时候执行一个读操作，都是从通道中读取，但不是直接从通道读取。因为所有数据最终都驻留在缓冲区中，所以是从通道读到缓冲区中。
因此读取文件涉及三个步骤：
1. 从 FileInputStream 获取 Channel;
2. 创建 Buffer;
3. 将数据从 Channel 读到 Buffer 中。

##### 三个步骤
- 第一步 获取通道。我们从 FileInputStream 获取通道：  
    ```
    FileInputStream fin = new FileInputStream( "readandshow.log" );
    FileChannel fc = fin.getChannel();
    ```
- 第二步 创建缓冲区：  
    ```
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    ``` 
- 第三步 将数据从通道读到缓冲区中，如下所示：  
    ```
    fc.read( buffer );
    ```
    
会注意到，我们不需要告诉通道要读多少数据到缓冲区中。每一个缓冲区都有复杂的内部统计机制，它会跟踪已经读了多少数据以及还有多少空间可以容纳更多的数据。关于缓冲区统计机制我们稍后再讨论

```
@Test
public void testRead() {
    try {
        FileInputStream fin = new FileInputStream("/Users/dongsj/workspace/dsj/javaSpace/nettyDemo/src/test/resources/nio/readandshow.log");
        FileChannel fileChannel = fin.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int result = fileChannel.read(buffer);
            
        System.out.println("read : " + result);
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

## 读&写

将一个文件的所有内容拷贝到另一个文件中。执行三个基本操作：(1)创建一个Buffer，(2)从源文件中将数据读到这个缓冲区中，(3)将缓冲区写入目标文件。这个程序不断重复，直到源文件结束。
程序让我们看到如何检查操作的状态，以及如何使用 clear() 和 flip() 方法重设缓冲区，并准备缓冲区以便将新读取的数据写到另一个通道中。

- **读写**
因为缓冲区会跟踪它自己的数据，所以程序的内部循环 (inner loop) 非常简单，如下所示：  
    ```
    fcin.read( buffer );   // 将数据从输入通道 fcin 中读入缓冲区
    fcout.write( buffer ); // 将这些数据写到输出通道 fcout
    ```
    
- **检查状态**
检查拷贝何时完成。当没有更多的数据时，拷贝就算完成，并且可以在 read() 方法返回 -1 是判断这一点：  
    ```
    int r = fcin.read( buffer );
    if (r==-1) {
         break;
    }
    ```
    
- **重设缓冲区**
从输入通道读入缓冲区之前，我们调用 clear() 方法。同样，在将缓冲区写入输出通道之前，我们调用 flip() 方法，如下所示：  
    ```
    buffer.clear();
    int r = fcin.read( buffer );
    
    if (r==-1) {
         break;
    }
    
    buffer.flip();
    fcout.write( buffer );
    ```
    
    `clear()` 方法重设缓冲区，使它可以接受读入的数据  
    `flip()` 方法让缓冲区可以将新读入的数据写入另一个通道。
    
    > ***在netty中同样有缓冲区`ByteBuf`，但是，并不需要使用flip()方法，因为`ByteBuf`使用读写双指针来表示数据的起至点，关于Netty后续将会涉及到。***


```
    @Test
    public void testReadAndWrite() {
        FileInputStream fin;
        FileOutputStream fout;
        FileChannel finChannel, foutChannel;

        try {
            fin = new FileInputStream("/Users/dongsj/workspace/dsj/javaSpace/nettyDemo/src/test/resources/nio/readandshow.log");
            fout = new FileOutputStream("/Users/dongsj/workspace/dsj/javaSpace/nettyDemo/src/test/resources/nio/readandshow.log_copy");
            finChannel = fin.getChannel();
            foutChannel = fout.getChannel();

            ByteBuffer buffer = ByteBuffer.allocate(10);

            int count = -1;
            do {
                count = finChannel.read(buffer);

                buffer.flip();
                foutChannel.write(buffer);
                buffer.clear();
            } while (-1 != count);
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // TODO release
        }
    }

```

![结果](http://upload-images.jianshu.io/upload_images/1366868-4d5dc390e3f1fb2d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

