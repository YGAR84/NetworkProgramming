package ru.nsu.g.a.lyamin.tcpfilereciver;

import java.nio.ByteBuffer;

public class LengthDecoder {
    private static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);

    public static byte[] intToBytes(int x) {
        buffer.putInt(0, x);
        return buffer.array();
    }

    public static int bytesToInt(byte[] bytes) {
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip
        return buffer.getInt();
    }
}