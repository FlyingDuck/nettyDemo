package com.dongshujin.demo.test.nio;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * Created by dongsj on 16/11/25.
 */
public class CharsetTest {


    @Test
    public void charsetTest() {
        try {
            FileInputStream fin = new FileInputStream("/Users/dongsj/workspace/dsj/javaSpace/nettyDemo/src/test/resources/nio/gbk.txt");
            FileChannel fileChannel = fin.getChannel();
            ByteBuffer inputData = ByteBuffer.allocate(1024);
            int result = fileChannel.read(inputData);
            inputData.flip();  // !!!

            System.out.println("Read result : " + result);


            Charset gbk = Charset.forName("GBK");
            Charset utf = Charset.forName("UTF-8");

            CharsetDecoder gbkDecoder = gbk.newDecoder();   // gbk解码器
            //CharsetEncoder gbkEncoder = gbk.newEncoder(); // gbk编码器
            CharsetEncoder utfEncoder = utf.newEncoder();   // utf编码器

            CharBuffer charBuffer = gbkDecoder.decode(inputData);
            //ByteBuffer outputBuffer = gbkEncoder.encode(charBuffer);  // 使用gbk编码
            ByteBuffer outputBuffer = utfEncoder.encode(charBuffer);    // 使用utf编码

            FileOutputStream fout = new FileOutputStream("/Users/dongsj/workspace/dsj/javaSpace/nettyDemo/src/test/resources/nio/utf.txt");
            FileChannel channelout = fout.getChannel();

            result = channelout.write(outputBuffer);
            System.out.println("Write result : " + result);

        } catch (CharacterCodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
