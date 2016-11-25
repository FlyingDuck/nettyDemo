package com.dongshujin.demo.test.nio;

import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Created by dongsj on 16/11/25.
 * NIO 缓冲区测试
 */
public class NIOBufferTest {

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
        buffer.position(0);
        buffer.limit( buffer.capacity() );

        while (buffer.remaining()>0) {
            System.out.println( buffer.get() );
        }
    }

}
