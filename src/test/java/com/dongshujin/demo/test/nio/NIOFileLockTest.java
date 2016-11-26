package com.dongshujin.demo.test.nio;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Created by dongsj on 16/11/26.
 *
 */
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
