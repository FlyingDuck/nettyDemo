# 缓冲区

前面的例子没有超出标准的读/写过程种类，在原来的 I/O 中可以像在 NIO 中一样容易地实现这样的标准读写过程。缓冲区有一些更复杂的方面，比如缓冲区分配、包装和分片。以及NIO 带给 Java 平台的一些新功能。

---

### 缓冲区分配和包装

创建缓冲区，必须分配它一定的空间。我们使用静态方法 `allocate()` 来分配缓冲区：

```
ByteBuffer buffer = ByteBuffer.allocate( 1024 );
```
allocate() 方法分配一个具有指定大小的底层数组，并将它包装到一个缓冲区对象中 ― 在本例中是一个 ByteBuffer。

还可以将一个现有的数组转换为缓冲区，如下所示：
```
byte array[] = new byte[1024];
ByteBuffer buffer = ByteBuffer.wrap( array );
```
本例使用了 wrap() 方法将一个数组包装为缓冲区。必须非常小心地进行这类操作。一旦完成包装，底层数据就可以通过缓冲区或者直接访问。


## 缓冲区分片

slice() `让我想到了Golang的slice 呵呵` 方法根据现有的缓冲区创建一种 子缓冲区 。也就是说，它创建一个新的缓冲区，新缓冲区与原来的缓冲区的一部分共享数据。
使用例子可以最好地说明这点。让我们首先创建一个长度为 10 的 ByteBuffer：

```
ByteBuffer buffer = ByteBuffer.allocate( 10 );
```
然后使用数据来填充这个缓冲区，在第 n 个槽中放入数字 n：
```
for (int i=0; i<buffer.capacity(); ++i) {
     buffer.put( (byte)i );
}
```
现在我们对这个缓冲区分片，以创建一个包含槽 3 到槽 6 的子缓冲区。在某种意义上，子缓冲区就像原来的缓冲区中的一个窗口。窗口的起始和结束位置通过设置 position 和 limit 值来指定，然后调用 Buffer 的 slice() 方法：
```
buffer.position( 3 );
buffer.limit( 7 );
ByteBuffer slice = buffer.slice();
```
片段是缓冲区的子缓冲区。不过，片段和缓冲区共享同一个底层数据数组。


### 缓冲区分片和数据共享

我们已经创建了原缓冲区的子缓冲区，并且我们知道缓冲区和子缓冲区共享同一个底层数据数组。让我们看看这意味着什么。
我们遍历子缓冲区，将每一个元素乘以11来改变它。例如，5 会变成 55。
```
for (int i=0; i<slice.capacity(); ++i) {
     byte b = slice.get( i );
     b *= 11;
     slice.put( i, b );
}
```
最后，再看一下原缓冲区中的内容：
```
buffer.position( 0 );
buffer.limit( buffer.capacity() );

while (buffer.remaining()>0) {
     System.out.println( buffer.get() );
}
```

测试代码：
```
    @Test
    public void testSlice() {
        ByteBuffer buffer = ByteBuffer.allocate(10);

        for (int i=0; i<10; i++) {
            buffer.put((byte) i);
        }

        // 创建分片
        buffer.position(3);
        buffer.limit(7);
        ByteBuffer slice = buffer.slice();

        // 操作分片数据
        for (int i=0; i<slice.capacity(); ++i) {
            byte b = slice.get(i);
            b *= 11;
            slice.put( i, b );
        }

        // 遍历缓冲区
        buffer.position( 0 );
        buffer.limit( buffer.capacity() );

        while (buffer.remaining()>0) {
            System.out.println( buffer.get() );
        }
    }
```

![结果](http://upload-images.jianshu.io/upload_images/1366868-9ef27ed10e9ee01c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 只读缓冲区

只读缓冲区非常简单 ― *可以读取它们，但是不能向它们写入*。可以通过调用缓冲区的`asReadOnlyBuffer()` 方法，将任何常规缓冲区转换为只读缓冲区，这个方法返回一个与原缓冲区完全相同的缓冲区(并与其共享数据)，只不过它是只读的。
只读缓冲区对于保护数据很有用。在将缓冲区传递给某个对象的方法时，您无法知道这个方法是否会修改缓冲区中的数据。创建一个只读的缓冲区可以保证该缓冲区不会被修改。

> 但是，不能将只读的缓冲区转换为可写的缓冲区。

### 直接和间接缓冲区

另一种有用的缓冲区是直接缓冲区。

- ***直接缓冲区*** 是为加快 I/O 速度，而以一种特殊的方式分配其内存的缓冲区。
实际上，直接缓冲区的准确定义是与实现相关的。Sun 的文档是这样描述直接缓冲区的：
给定一个直接字节缓冲区，Java 虚拟机将尽最大努力直接对它执行本机 I/O 操作。也就是说，它会在每一次调用底层操作系统的本机 I/O 操作之前(或之后)，尝试避免将缓冲区的内容拷贝到一个中间缓冲区中(或者从一个中间缓冲区中拷贝数据)。


### 内存映射文件 I/O

内存映射文件 I/O 是一种读和写文件数据的方法，它可以比常规的基于流或者基于通道的 I/O 快得多。

内存映射文件 I/O 是通过使文件中的数据神奇般地出现为内存数组的内容来完成的。这其初听起来似乎不过就是将整个文件读到内存中，但是事实上并不是这样。一般来说，只有文件中实际读取或者写入的部分才会送入（或者 映射）到内存中。

内存映射并不真的多么神奇。现代操作系统一般根据需要将文件的部分映射为内存的部分，从而实现文件系统。Java 内存映射机制不过是在底层操作系统中可以采用这种机制时，提供了对该机制的访问。
尽管创建内存映射文件相当简单，但是向它写入可能是危险的。仅只是改变数组的单个元素这样的简单操作，就可能会直接修改磁盘上的文件。修改数据与将数据保存到磁盘是没有分开的。

在下面的例子中，我们要将一个 FileChannel (它的全部或者部分)映射到内存中。为此我们将使用 FileChannel.map() 方法。下面代码行将文件的前 1024 个字节映射到内存中：
```
MappedByteBuffer mbb = fc.map( FileChannel.MapMode.READ_WRITE, 0, 1024 );
```
map() 方法返回一个 MappedByteBuffer，它是 ByteBuffer 的子类。因此，您可以像使用其他任何 ByteBuffer 一样使用新映射的缓冲区，操作系统会在需要时负责执行行映射。


