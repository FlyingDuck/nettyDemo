#  缓冲区

缓冲区的两个重要组件：状态变量和访问方法 (accessor)。

**状态变量**  每一个读/写操作都会改变缓冲区的状态。通过记录和跟踪这些变化，缓冲区就可能够内部地管理自己的资源。  

**访问方法** 在从通道读取数据时，数据被放入到缓冲区。在有些情况下，可以将这个缓冲区直接写入另一个通道，但是在一般情况下，您还需要查看数据，使用 访问方法 get() 来完成的。同样，如果要将原始数据放入缓冲区中，就要使用访问方法 put()。

在[NIO读写](http://www.jianshu.com/p/00f0ed8c8e2e)一文中，介绍写文件时对`flip()` 和 `allocate()`做了相应的阐述，已经对缓冲区内部机制有了一些介绍。

---

## 状态变量

可以用三个值指定缓冲区在任意时刻的状态：
- position
- limit
- capacity

这三个变量一起可以跟踪缓冲区的状态和它所包含的数据。

> - ***mark <= position <= limit <= capacity***
> - **mark** : 标示了缓冲区中执行`reset`操作时，position应该置于的位置
> - **position** : 标示缓冲区中下一个能够进行读写的位置
> - **limit** : 标示缓冲区中第一个不能进行读写的位置
> - **capacity**: 用来指定缓冲区的最大容量，它是不变的

---

### position

缓冲区实际上就是丰富化的数组。  

在从通道读取时，您将所读取的数据放到底层的数组中。 position 变量跟踪已经写了多少数据。更准确地说，它指定了下一个字节将放到数组的哪一个元素中。因此，如果您从通道中读三个字节到缓冲区中，那么缓冲区的 position 将会设置为3，指向数组中第四个元素。  

在写入通道时，您是从缓冲区中获取数据。 position 值跟踪从缓冲区中获取了多少数据。更准确地说，它指定下一个字节来自数组的哪一个元素。因此如果从缓冲区写了5个字节到通道中，那么缓冲区的 position 将被设置为5，指向数组的第六个元素。

### limit

limit 变量表明还有多少数据需要取出(在从缓冲区写入通道时)，或者还有多少空间可以放入数据(在从通道读入缓冲区时)。

> position <= limit。


### capacity

缓冲区的 capacity 表明可以储存在缓冲区中的最大数据容量。实际上，它指定了底层数组的大小 ― 至少是指定了准许我们使用的底层数组的容量。

> limit <= capacity。


### 观察变量变化

- ##### 我们创建一个8个字节的缓冲区

> `allocate(8)` 从源码中可以看出：
> 在调用allocate()方法分配缓冲区时，实际上将limit 和 capacity 设置成了同样的大小。更多细节可以参考 Buffer & ByteBuffer源码
> ```
> public static ByteBuffer allocate(int capacity) {
>         if (capacity < 0)
>             throw new IllegalArgumentException();
>         return new HeapByteBuffer(capacity, capacity);
>     }
> ```


![创建缓冲区](http://upload-images.jianshu.io/upload_images/1366868-fa31bc27203ce204.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

limit <= capacity 所以这两个都指向数组的尾部一个不存在的位置

position 设置为0。如果我们写一些数据到缓冲区中，那么下一个写入的数据就进入 slot 0 。如果我们从缓冲区读一些数据，从缓冲区读取的下一个字节也来自 slot 0 。

![position](http://upload-images.jianshu.io/upload_images/1366868-64107a98a3fa6d1d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


- ##### 第一次写入缓冲区

首先从输入通道中读一些数据到缓冲区中。第一次读取得到3个字节。它们被放到数组中从 position 开始的位置，这时 position 初始位置为 0。写入之后，position 就增加到 3，如下所示： 

![第一次写入缓冲区](http://upload-images.jianshu.io/upload_images/1366868-7f40548e2d497186.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


- ##### 第二次写入缓冲区

在第二次写入时，我们从输入通道读取另外2个字节到缓冲区中。这两个字节储存在由 position 所指定的位置上， position 因而增加 2：
 
![第二次写入缓冲区](http://upload-images.jianshu.io/upload_images/1366868-56750629f0fed30b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- ##### buffer.flip()

> `flip()` 从源代码可以看出，该方法将limit指向了缓冲区当前位置 `position`，并将`position`设置为0，将`mark`丢弃
> ```
> public final Buffer flip() {
>        limit = position;
>        position = 0;
>        mark = -1;
>        return this;
>    }
> ```


![buffer#flip()](http://upload-images.jianshu.io/upload_images/1366868-a6ec952006a9cdab.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


现在可以将数据从缓冲区写入通道了。 position 被设置为 0，这意味着我们得到的下一个字节是第一个字节。 limit 已被设置为原来的 position，这意味着它包括以前读到的所有字节，并且一个字节也不多。

- ##### 第一次读取缓冲区

我们从缓冲区中取4个字节并将它们写入输出通道。这使得 position 增加到 4，而 limit 不变，如下所示：

![第一次读取缓冲区](http://upload-images.jianshu.io/upload_images/1366868-35d565864f6cc89d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


- ##### 第二次读取缓冲区

只剩下一个字节可读了。 limit在我们调用 flip() 时被设置为 5，并且 position 不能超过 limit。所以最后一次写入操作从缓冲区取出一个字节并将它写入输出通道。这使得 position 增加到 5，并保持 limit 不变，如下所示：

![第二次读取缓冲区](http://upload-images.jianshu.io/upload_images/1366868-c4849bbe9990d2ad.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


- ##### buffer.clear()

> `clear()` 从源码中我们可以看出 该方法将position设置为0，limit归位与capacity保持一致
> ```
> public final Buffer clear() {
>         position = 0;
>         limit = capacity;
>         mark = -1;
>         return this;
>     }
> ```

![buffer#clear()](http://upload-images.jianshu.io/upload_images/1366868-64107a98a3fa6d1d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

---

## 访问方法

到目前为止，我们只是使用缓冲区将数据从一个通道转移到另一个通道。然而，程序经常需要直接处理数据。例如：可能需要将用户数据保存到磁盘，在这种情况下，必须将这些数据直接放入缓冲区，然后用通道将缓冲区写入磁盘。
或者，可能想要从磁盘读取用户数据，在这种情况下，要将数据从通道读到缓冲区中，然后检查缓冲区中的数据。


#### put()方法

ByteBuffer 类中有五个 put() 方法：

1. ByteBuffer put( byte b );   // 写入单个字节
2. ByteBuffer put( byte src[] ); // 写入来自一个数组的一组字节
3. ByteBuffer put( byte src[], int offset, int length ); // 写入来自一个数组的一组字节
4. ByteBuffer put( ByteBuffer src ); // 将数据从一个给定的源 ByteBuffer 写入这个 ByteBuffer
5. ByteBuffer put( int index, byte b ); // 将字节写入缓冲区中特定的 位置

返回 ByteBuffer 的方法只是返回调用它们的缓冲区的 this 值。

与 get() 方法一样，我们将把 put() 方法划分为 相对 或者 绝对 的。前四个方法是相对的，而第五个方法是绝对的。
上面显示的方法对应于 ByteBuffer 类。其他类有等价的 put() 方法，这些方法除了不是处理字节之外，其它方面是完全一样的。它们处理的是与该缓冲区类相适应的类型。

#### 类型化的 get() 和 put() 方法

除了前些小节中描述的 get() 和 put() 方法， ByteBuffer 还有用于读写不同类型的值的其他方法，如下所示：
- getByte()
- getChar()
- getShort()
- getInt()
- getLong()
- getFloat()
- getDouble()
- putByte()
- putChar()
- putShort()
- putInt()
- putLong()
- putFloat()
- putDouble()

事实上，这其中的每个方法都有两种类型 ― 一种是相对的，另一种是绝对的。它们对于读取格式化的二进制数据（如图像文件的头部）很有用。


