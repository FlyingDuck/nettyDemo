# 字符集

根据 Sun 的文档，一个 Charset 是“十六位 Unicode 字符序列与字节序列之间的一个命名的映射”。实际上，一个 Charset 允许您以尽可能最具可移植性的方式读写字符序列。

Java 语言被定义为基于 Unicode。然而在实际上，许多人编写代码时都假设一个字符在磁盘上或者在网络流中用一个字节表示。这种假设在许多情况下成立，但是并不是在所有情况下都成立，而且随着计算机变得对 Unicode 越来越友好，这个假设就日益变得不能成立了。

---

### 编码/解码

要读和写文本，我们要分别使用 CharsetDecoder 和 CharsetEncoder。将它们称为 解码器 和 编码器 是有道理的。

一个 字符 不再表示一个特定的位模式，而是表示字符系统中的一个实体。因此，由某个实际的位模式表示的字符必须以某种特定的 编码 来表示。

CharsetDecoder 用于将逐位表示的一串字符转换为具体的 char 值。同样，一个 CharsetEncoder 用于将字符转换回位。


### 处理文本的正确方式

现在我们将分析这样一个程序。这个程序非常简单 ― 它从一个文件中读取一些文本，并将该文本写入另一个文件。但是它把该数据当作文本数据，并使用 CharBuffer 来将该数句读入一个 CharsetDecoder 中。同样，它使用 CharsetEncoder 来写回该数据。

我们将假设字符以 GBK 字符集的形式储存在磁盘上。尽管我们必须为使用 Unicode 做好准备，但是也必须认识到不同的文件是以不同的格式储存的，而 ASCII 无疑是非常普遍的一种格式。事实上，每种 Java 实现都要求对以下字符编码提供完全的支持：

- GBK
- GB2312
- US-ASCII
- ISO-8859-1
- UTF-8
- UTF-16BE
- UTF-16LE
- UTF-16

在打开相应的文件、将输入数据读入名为 inputData 的 ByteBuffer 之后，我们的程序必须创建 GBK 字符集的一个实例：
```
Charset gbk = Charset.forName( "GBK" );
```
然后，创建一个解码器（用于读取）和一个编码器 （用于写入）：
```
CharsetDecoder decoder = gbk.newDecoder();
CharsetEncoder encoder = gbk.newEncoder();
```
为了将字节数据解码为一组字符，我们把 ByteBuffer 传递给 CharsetDecoder，结果得到一个 CharBuffer：
```
CharBuffer cb = decoder.decode( inputData );
```
如果想要处理字符，我们可以在程序的此处进行。但是我们只想无改变地将它写回，所以没有什么要做的。
要写回数据，我们必须使用 CharsetEncoder 将它转换回字节：
```
ByteBuffer outputData = encoder.encode( cb );
```
在转换完成之后，我们就可以将数据写到文件中了。

---

### 场景描述：
我们要将一个GBK编码的文件(`gbk.txt`)转存到一个UTF编码的文件(`utf.txt`)中。

- 我们的`gbk.txt`文件是一个 ‘GBK’编码保存的文件文件中有一段中文：


![10 个中文字符＋2个换行符](http://upload-images.jianshu.io/upload_images/1366868-50c9e2bb83a20c02.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 使用Charset 对GBK文件进行GBK解码，然后再进行UTF编码保存，代码如下

```
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
```

- 结果

![UTF文件](http://upload-images.jianshu.io/upload_images/1366868-e796d98fcede84fb.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![字节数](http://upload-images.jianshu.io/upload_images/1366868-6f48ea75670017b7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


- 如果依然采用GBK编码保存的话，结果一定是乱码，毕竟是中文

![乱码](http://upload-images.jianshu.io/upload_images/1366868-c5ac72005dc19c96.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


