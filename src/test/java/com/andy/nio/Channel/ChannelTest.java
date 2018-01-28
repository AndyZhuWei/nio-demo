package com.andy.nio.Channel;

import org.junit.Test;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.util.Map;
import java.util.Set;

/**
 * 一、通道(Channel):用于源节点与目标节点的连接。在Java NIO中负责缓冲区数据的传输。Channel本身不存储数据，因此需要配合缓冲区继续传输。
 *
 * 二、通道的主要实现类
 *   java.nio.channels.Channel接口
 *       |--FileChannel
 *       |--SocketChannel
 *       |--ServerSocketChannel
 *       |--DatagramChanel
 *  三、获取通道
 *  1.Java 针对支持通道的类提供了GetChannel()方法
 *      本地IO:
 *      FileInputStream/FileOutputStream
 *      RandomAccessFile
 *
 *      网络IO:
 *      Socket
 *      ServerSocket
 *      DatagramSocket
 *  2.在JDK 1.7中的NIO.2针对各个通道提供了静态方法open()
 *  3.在JDK 1.7中的NIO.2的Files工具类的newByteChanel()
 *
 * 四、通道之间的数据传输
 *  transferFrom()
 *  transferTo()
 *
 *五、分散（Scatter）于聚集（Gather）
 * 分散读取（Scatter Reads）:将通道中的数据分散到多个缓冲区中
 * 聚集写入(Gather Writes):将多个缓冲区的数据聚集到通道中
 *
 *六、字符集：Charset
 *  编码：字符串->字节数组
 *  解码：字节数组—>字符串
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
public class ChannelTest {

    //字符集
    @Test
    public void test6() throws IOException{
        Charset cs1 = Charset.forName("GBK");

        //获取编码器
        CharsetEncoder ce = cs1.newEncoder();

        //获取解码器
        CharsetDecoder cd = cs1.newDecoder();

        CharBuffer cBuf = CharBuffer.allocate(1024);
        cBuf.put("中国");
        cBuf.flip();

        //编码
        ByteBuffer bBuf = ce.encode(cBuf);

        for(int i=0;i<4;i++) {
            System.out.println(bBuf.get());
        }

        //解码
        bBuf.flip();
        CharBuffer cBuf2 = cd.decode(bBuf);
        System.out.println(cBuf2.toString());

        System.out.println("------------------------------");

        bBuf.flip();
        Charset cs2 = Charset.forName("UTF-8");
        CharBuffer cBuf3 = cs2.decode(bBuf);
        System.out.println(cBuf3.toString());



    }

    @Test
    public void test5() {
        Map<String,Charset> map = Charset.availableCharsets();

        Set<Map.Entry<String,Charset>> set = map.entrySet();

        for (Map.Entry<String,Charset> entry :set) {
            System.out.println(entry.getKey()+"="+entry.getValue());
        }
    }

    //分散和聚集
    @Test
    public void test4() throws IOException{
        RandomAccessFile raf1 = new RandomAccessFile("1.txt","rw");

        //1.获取通道
        FileChannel channel1 = raf1.getChannel();

        //2.分配指定大小的缓冲区
        ByteBuffer buf1 = ByteBuffer.allocate(100);
        ByteBuffer buf2 = ByteBuffer.allocate(1024);

        //3.分散读取
        ByteBuffer[] bufs = {buf1,buf2};
        channel1.read(bufs);

        for (ByteBuffer byteBuffer:bufs) {
            byteBuffer.flip();
        }

        System.out.println(new String(bufs[0].array(),0,bufs[0].limit()));
        System.out.println("---------------------------");
        System.out.println(new String(bufs[1].array(),0,bufs[1].limit()));

        //4.聚集写入
        RandomAccessFile raf2 = new RandomAccessFile("2.txt","rw");
        FileChannel channel2 = raf2.getChannel();

        channel2.write(bufs);
    }

    //通道之间的数据传输(直接缓冲区)
    @Test
    public void test3() throws  IOException {
        FileChannel inChanel = FileChannel.open(Paths.get("Java NIO.pdf"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get("Java NIO2.pdf"),StandardOpenOption.WRITE,StandardOpenOption.READ,StandardOpenOption.CREATE);

       // inChanel.transferTo(0,inChanel.size(),outChannel);
        outChannel.transferFrom(inChanel,0,inChanel.size());

        inChanel.close();
        outChannel.close();
    }

    //使用直接缓冲区完成文件的复制(内存映射文件)
    @Test
    public void test2() throws IOException {
        Long start = Clock.systemDefaultZone().millis();

        FileChannel inChanel = FileChannel.open(Paths.get("Java NIO.pdf"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get("Java NIO2.pdf"),StandardOpenOption.WRITE,StandardOpenOption.READ,StandardOpenOption.CREATE);

        //内存映射文件
        MappedByteBuffer inMappedBuf = inChanel.map(FileChannel.MapMode.READ_ONLY,0,inChanel.size());
        MappedByteBuffer outMappedBuf = outChannel.map(FileChannel.MapMode.READ_WRITE,0,inChanel.size());

        //直接对缓冲区进行数据的读写操作
        byte[] dst = new byte[inMappedBuf.limit()];
        inMappedBuf.get(dst);
        outMappedBuf.put(dst);

        inChanel.close();
        outChannel.close();
        Long end = Clock.systemDefaultZone().millis();

        System.out.println("耗时："+(end-start));

    }

    //1.利用通道完成文件的复制(非直接缓冲区)
    @Test
    public void test1() {
        Long start = Clock.systemDefaultZone().millis();
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            fis = new FileInputStream("Java NIO.pdf");
            fos = new FileOutputStream("Java NIO2.pdf");

            //1)获取通道
            inChannel = fis.getChannel();
            outChannel = fos.getChannel();

            //2)分配指定大小的缓冲区
            ByteBuffer buf = ByteBuffer.allocate(1024);

            //3)将通道中的数据存入缓冲区中
            while(inChannel.read(buf) != -1) {
                buf.flip();//切换读取数据的模式
                //4)将缓冲区中的数据写入通道中
                buf.clear();//清空缓冲区
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(outChannel!=null) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(inChannel!=null){
                try {
                    inChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fos!=null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fis!=null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Long end = Clock.systemDefaultZone().millis();

        System.out.println("耗时："+(end-start));
    }
}
