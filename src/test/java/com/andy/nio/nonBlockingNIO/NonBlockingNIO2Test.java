package com.andy.nio.nonBlockingNIO;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Scanner;

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
public class NonBlockingNIO2Test {

    //客户端
    @Test
    public void send() throws IOException{
        //1.获取通道
        DatagramChannel  dc = DatagramChannel.open();

        //2.切换成非阻塞模式
        dc.configureBlocking(false);

        //3.分配指定大小的缓冲区
        ByteBuffer buf  = ByteBuffer.allocate(1024);

        //4.发送数据给服务端
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNext()) {
            String str = scanner.next();
            buf.put((LocalDateTime.now().toString()+"\n"+str).getBytes());
            buf.flip();
            dc.send(buf,new InetSocketAddress("127.0.0.1",9898));
            buf.clear();
        }

        //5.关闭通道
        dc.close();
    }

    //服务端
    @Test
    public void receive() throws  IOException {
        //1.获取通道
        DatagramChannel dc = DatagramChannel.open();

        //2.切换成非阻塞模式
        dc.configureBlocking(false);

        //3.绑定连接端口号
        dc.bind(new InetSocketAddress(9898));

        //4.获取选择器
        Selector selector = Selector.open();

        //5.将通道注册到选择器上,并且指定“监听接收事件”
        dc.register(selector, SelectionKey.OP_READ);

        //6.轮询式的获取选择器上已经“准备就绪”的事件
        while(selector.select()>0) {
            //7.获取当前选择器中所有的注册的“选择键（已经就绪的监听事件）”
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                //8.获取准备“就绪”的事件
                SelectionKey sk = it.next();
                //9.判断具体是什么事件准备就绪
               if(sk.isReadable()) {
                    //14.读取数据
                    ByteBuffer buf = ByteBuffer.allocate(1024);
                    dc.receive(buf);
                    buf.flip();
                    System.out.println(new String(buf.array(),0,buf.limit()));
                    buf.clear();
               }
            }
            //15.取消选择器SelectionKey
            it.remove();
        }
    }

}
