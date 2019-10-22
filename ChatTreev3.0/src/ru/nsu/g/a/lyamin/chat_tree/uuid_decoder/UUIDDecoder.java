package ru.nsu.g.a.lyamin.chat_tree.uuid_decoder;

import java.io.*;
import java.util.UUID;

public class UUIDDecoder
{
    public static byte[] getByte(UUID obj) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public static UUID getUUID(byte[] data) throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return (UUID) is.readObject();
    }
}
