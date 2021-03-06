# 文件锁定

文件锁定初看起来可能让人迷惑。它似乎指的是防止程序或者用户访问特定文件。事实上，文件锁就像常规的 Java 对象锁 ― 它们是劝告式的（advisory）锁。它们不阻止任何形式的数据访问，相反，它们通过锁的共享和获取赖允许系统的不同部分相互协调。

可以锁定整个文件或者文件的一部分。如果获取一个`排它锁`，那么其他人就不能获得同一个文件或者文件的一部分的锁。如果获得一个`共享锁`，那么其他人可以获得同一个文件或者文件一部分的共享锁，但是不能获得排它锁。文件锁定并不总是出于保护数据的目的。例如，可能临时锁定一个文件以保证特定的写操作成为原子的，而不会有其他程序的干扰。
大多数操作系统提供了文件系统锁，但是它们并不都是采用同样的方式。有些实现提供了共享锁，而另一些仅提供了排它锁。事实上，有些实现使得文件的锁定部分不可访问，尽管大多数实现不是这样的。

---

### 锁定文件

要获取文件的一部分的锁，您要调用一个打开的 FileChannel 上的 lock() 方法。**注意，如果要获取一个排它锁，您必须以写方式打开文件。**
```
RandomAccessFile raf = new RandomAccessFile( "usefilelocks.txt", "rw" );
FileChannel fc = raf.getChannel();
FileLock lock = fc.lock( start, end, false );
```
在拥有锁之后，您可以执行需要的任何敏感操作，然后再释放锁：
```
lock.release();
```
在释放锁后，尝试获得锁的其他任何程序都有机会获得它。


### 文件锁定和可移植性

文件锁定可能是一个复杂的操作，特别是考虑到不同的操作系统是以不同的方式实现锁这一事实。下面的指导原则将帮助尽可能保持代码的可移植性：

- 只使用排它锁。
- 将所有的锁视为劝告式的（advisory）。


```
public class NIOFileLockTest {

    private final int start = 10;
    private final int end = 20;

    @Test
    public void fileLockTest() {

        try {
            RandomAccessFile raf = new RandomAccessFile(
                    "/Users/dongsj/workspace/dsj/javaSpace/nettyDemo/src/test/resources/nio/readandshow.log",
                    "rw");
            FileChannel fc = raf.getChannel();

            System.out.println("获取锁");
            FileLock lock = fc.lock(start, end, false);
            System.out.println("获取锁成功");

            System.out.println("暂停3s");
            Thread.sleep(3000);


            System.out.println("释放锁");
            lock.release();
            System.out.println("释放锁成功");

            raf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

