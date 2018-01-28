package com.andy.nio.pipe;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

public class PipeTest {

    @Test
    public void testPipe() throws IOException{
        //1.获取通道
        Pipe pipe = Pipe.open();

        //2.获取sinkChannel
        Pipe.SinkChannel sinkChannel =  pipe.sink();

        //3.
        ByteBuffer buf = ByteBuffer.allocate(1024);
        buf.put("通过单向通道发送数据".getBytes());
        buf.flip();
        sinkChannel.write(buf);

        //4.
        Pipe.SourceChannel sourceChannel = pipe.source();
        buf.flip();
        int len = sourceChannel.read(buf);
        System.out.println(new String(buf.array(),0,len));

        sinkChannel.close();
        sourceChannel.close();
    }

}
