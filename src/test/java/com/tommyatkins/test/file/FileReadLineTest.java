package com.tommyatkins.test.file;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileReadLineTest {

    public static void main(String[] args) throws IOException {
        FileInputStream fis = new FileInputStream("src/test/java/com/tommyatkins/test/file/demo.txt");
        FileChannel channel = fis.getChannel();
        ByteBuffer bf = ByteBuffer.allocate(3);
        bf.limit(1);
        bf.compact();
        int length = -1;
        StringBuilder sb = new StringBuilder();
        while ((length = channel.read(bf)) != -1) {
            bf.flip();
            sb.append(new String(bf.array()));
            System.out.println(length);
        }

        System.out.println(sb);
    }


}
