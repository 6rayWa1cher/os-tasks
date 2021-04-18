package com.a6raywa1cher.ostasks.tsk3.stringtunnel;

import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

public class MemoryMappedStringTunnel implements StringTunnel {
    private final CharBuffer charBuffer;

    public MemoryMappedStringTunnel(String bufferLocation, int bufferSize) throws IOException {
        File f = new File( bufferLocation );

        FileChannel channel = FileChannel.open( f.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE );

        MappedByteBuffer b = channel.map( FileChannel.MapMode.READ_WRITE, 0, bufferSize );
        this.charBuffer = b.asCharBuffer();
    }


    @Override
    public void send(String message) {
        charBuffer.put(message + '\0');
    }

    @Override
    public String read() {
        StringBuilder stringBuilder = new StringBuilder();
        char c;
        while ( (c = charBuffer.get()) != 0 ) {
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    @Override
    public void close() throws Exception {

    }
}
