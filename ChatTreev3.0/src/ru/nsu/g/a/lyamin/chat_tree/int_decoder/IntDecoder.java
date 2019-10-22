package ru.nsu.g.a.lyamin.chat_tree.int_decoder;

import java.nio.ByteBuffer;

public class IntDecoder
{
    private static final ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);

    public static byte[] getByte(int x)
    {
        byte[] result = new byte[Integer.BYTES];
        buffer.putInt(0, x);
        System.arraycopy(buffer.array(), 0, result, 0, result.length);
        return result;
    }

    public static int getInt(byte[] b)
    {
        return ByteBuffer.wrap(b).getInt();
    }
}
