## 异步文件通道－通过handler处理

> AsynchronousFileChannel

使用 Future 对象的替代机制，是向异步操作注册一个 callback 。接口 CompletionHandler 有两个方法：

- void completed(V result, A attachment) // 在任务完成结果中具有类型 V 时执行。
- void failed(Throwable e, A attachment) // 在任务由于 Throwable e 而失败时执行。

两个方法的附件参数都是一个传递到异步操作的对象。如果相同的对象用于多个操作，其可用于追踪哪个操作已完成。

#### Open 命令

我们来看一个使用 AsynchronousFileChannel 类的例子。可通过将 java.nio.file.Path 对象传递到静态 open() 方法中，来创建一个新的通道：
```
AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(Paths.get("myfile"));
```

> `FileChannel 的新 open 命令`  
> 用于异步通道的 open 命令格式已被移植到 FileChannel 类。在 NIO 中，通过在 FileInputStream、FileOutputStream、或者 RandomAccessFile 上调用 getChannel() 来获取 FileChannel。借助 NIO.2，可利用 open() 方法来直接创建 FileChannel。

默认情况下，该文件已打开以供读取。open() 方法可利用附加选项来指定如何打开该文件。例如，此调用打开文件以供读取或写入，如果必要将创建该文件，并在通道关闭或者 JVM 终止时尝试删除文件：
```
fileChannel = AsynchronousFileChannel.open(
            Paths.get("afile"), 
            StandardOpenOption.READ, 
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE, 
            StandardOpenOption.DELETE_ON_CLOSE
            );
```
替代方法 open() 提供了对通道的更好的控制，允许设置文件属性。


#### 实现一个完成处理程序

接下来，可将这些写入文件，写入完成后，就可执行一些操作。 首先要构造一个封装了 “ something ” 的 CompletionHandler ：
```
// 创建完成处理程序
CompletionHandler<Integer, Object> handler =
    new CompletionHandler<Integer, Object>() {
        @Override
        public void completed(Integer result, Object attachment) {
            System.out.println(attachment + " completed with " + result + " bytes written");
        }
        @Override
        public void failed(Throwable e, Object attachment) {
            System.err.println(attachment + " failed with:");
            e.printStackTrace();
        }
    };
```
现在可以进行写入：
```
fileChannel.write(ByteBuffer.wrap(bytes), 0, "Write operation 1", handler);
```
write() 方法参数：
- 包含要写入内容的 ByteBuffer
- 文件中的绝对位置
- 要传递给完成处理程序方法的附件对象
- 完成处理程序

操作必须给出进行读或写的文件中的绝对位置。文件具有内部位置标记，来指出读/写发生的位置，这样做没有意义，因为在上一个操作完成之前，就可以启动新操作，它们的发生顺序无法得到保证。由于相同的原因，在 AsynchronousFileChannel API 中没有用于设置或查询位置的方法，在 FileChannel 中同样也没有。

除了读写方法之外，还支持异步锁定方法，因此，如果当前有其他线程保持锁定时，可对文件进行执行访问锁定，而不必在当前线程中锁定（或者利用 tryLock 轮询）。

---

写文件：
```
public class FileWriterEnd {

    public void write() {
        try {
            final AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(
                    Paths.get("writeshow.log"),
                    StandardOpenOption.WRITE,
                    //StandardOpenOption.DELETE_ON_CLOSE,
                    StandardOpenOption.CREATE
            );

            CompletionHandler<Integer, Object> handler = new CompletionHandler<Integer, Object>() {
                @Override
                public void completed(Integer result, Object attachment) {
                    System.out.println(attachment + "S>>> completed with " + result + " bytes written");
                }

                @Override
                public void failed(Throwable e, Object attachment) {
                    if (e instanceof AsynchronousCloseException) {
                        System.out.println("S>>> File was closed before " + attachment + " executed");
                    } else {
                        System.err.println("S>>> " + attachment + " failed with:");
                        e.printStackTrace();
                    }
                }
            };

            int count = 0;
            int position = 0;
            while (count < 10) {
                byte[] contents = "hello  ".getBytes();
                System.out.println("S>>> Initiating write operation " + count);
                fileChannel.write(ByteBuffer.wrap(contents), position , "Write operation "+count + " ", handler);
                position += contents.length;
                count++;

                Thread.sleep(1000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new FileWriterEnd().write();
    }
}
```

![写入](http://upload-images.jianshu.io/upload_images/1366868-923c87662dc48d4c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


读文件：

```
public class FileReaderEnd {
    
    public void read() {
        try {
            final AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(
                    Paths.get("writeshow.log"),
                    StandardOpenOption.READ
            );
            
            final ByteBuffer buffer = ByteBuffer.allocate(7);
            CompletionHandler<Integer, Object> handler= new CompletionHandler<Integer, Object>(){
                @Override
                public void completed(Integer result, Object attachment) {
                    System.out.println("C))) Read operation completed, file contents is: " + new String(buffer.array()));
                    clearUp();
                }
                @Override
                public void failed(Throwable e, Object attachment) {
                    System.err.println("C))) Exception performing write");
                    e.printStackTrace();
                    clearUp();
                }

                private void clearUp() {
                    try {
                        fileChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };


            System.out.println("C))) Initiating read operation");
            //Future<Integer> future = fileChannel.read(buffer, 0); // 
            //System.out.println("future : " + future.get());
            fileChannel.read(buffer, 0, null, handler);

            Thread.sleep(3*1000); // 由于handler处理是异步的，防止主线程过早结束，等待handler处理

        } catch (IOException e) {
            e.printStackTrace();
        } /*catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }*/ catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new FileReaderEnd().read();
    }

}
```

![读取](http://upload-images.jianshu.io/upload_images/1366868-1b82b7d5e02da3fa.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
