package com.andy.nio.blockingNIO;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 *  使用NIO完成网络通信的三个核心
 *  1.通道（Channel）：负责连接
 *
 *     java.nio.channels.Channel接口
 *           |--SelectableChannel
 *              |--SocketChannel
 *              |--ServerSocketChannel
 *              |--DatagramChannel
 *
 *              |--Pipe.SinkChannel
 *              |--Pipe.SourceChannel
 *  2.缓冲区（Buffer）：负责数据的存取
 *  3.选择器（Selector）：是SelectableChannel的多路复用器，用于监控SelectableChannel的IO状态
 */
public class BlockingNIO2Test2 {

    //客户端
    @Test
    public void client() throws IOException{
        //1.获取通道
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1",9898));

        FileChannel inChannel = FileChannel.open(Paths.get("Java NIO.pdf"), StandardOpenOption.READ);
        //2.分配指定大小的缓冲区
        ByteBuffer buf = ByteBuffer.allocate(1024);

        //3.读取本地文件，并发送到服务端
        while(inChannel.read(buf) != -1) {
            buf.flip();
            socketChannel.write(buf);
            buf.clear();
        }

        socketChannel.shutdownOutput();

        //4.接收服务器端的反馈
        int len = 0;
        while ( (len = socketChannel.read(buf)) != -1) {
            buf.flip();
            System.out.println(new String(buf.array(),0,len));
            buf.clear();
        }

        //5.关闭通道
        inChannel.close();
        socketChannel.close();

    }

    //服务端
    @Test
    public void server() throws IOException{
        //1.获取通道
        ServerSocketChannel ssChannel = ServerSocketChannel.open();

        FileChannel outChannel = FileChannel.open(Paths.get("Java NIO2.pdf"),StandardOpenOption.WRITE,StandardOpenOption.READ,StandardOpenOption.CREATE);

        //2.绑定连接端口号
        ssChannel.bind(new InetSocketAddress(9898));

        //3.获取客户端连接的通信
        SocketChannel socketChannel = ssChannel.accept();

        //4.分配指定大小的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        //5.接收客户端的数据，并保存到本地
        while(socketChannel.read(buffer) != -1) {
            buffer.flip();
            outChannel.write(buffer);
            buffer.clear();
        }
        //6.发送反馈给客户端
        buffer.put("服务端接收数据成功".getBytes());
        buffer.flip();
        socketChannel.write(buffer);

        //7.关闭通道
        socketChannel.close();
        outChannel.close();
        ssChannel.close();
    }
}
