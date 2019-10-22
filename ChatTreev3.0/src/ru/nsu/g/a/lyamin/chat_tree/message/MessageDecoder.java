package ru.nsu.g.a.lyamin.chat_tree.message;

import ru.nsu.g.a.lyamin.chat_tree.client_info.ClientINFO;
import ru.nsu.g.a.lyamin.chat_tree.int_decoder.IntDecoder;
import ru.nsu.g.a.lyamin.chat_tree.uuid_decoder.UUIDDecoder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.UUID;

public class MessageDecoder
{
    public static final int MESS_HEALTH = 0;
    public static final int MESS_CONNECT = 1;
    public static final int MESS_MESS = 2;
    public static final int MESS_ACCEPT = 3;
    public static final int MESS_ALTERNATIVE = 4;
    public static final int MESS_DISCONNECT = 5;


    public static Message decodeMess(DatagramPacket dp) throws IOException, ClassNotFoundException
    {
        byte[] data = dp.getData();
        byte[] type = new byte[Integer.BYTES];
        byte[] messID = UUIDDecoder.getByte(UUID.randomUUID());
        byte[] messLen = new byte[Integer.BYTES];


        System.arraycopy(data, 0, type, 0, type.length);
        System.arraycopy(data, type.length, messID, 0, messID.length);
        System.arraycopy(data, type.length + messID.length, messLen, 0, messLen.length);

        int messLength = IntDecoder.getInt(messLen);

        byte[] mess = (messLength > 0) ? new byte[messLength] : null;

        if(mess != null)
        {
            System.arraycopy(data, type.length + messID.length + messLen.length, mess, 0, mess.length);
        }

        return new Message(IntDecoder.getInt(type), UUIDDecoder.getUUID(messID), mess, dp.getAddress(), dp.getPort());
    }

    public static DatagramPacket encodeMessage(Message mess) throws IOException
    {
        ClientINFO ci = mess.getClientINFO();

        byte[] type = IntDecoder.getByte(mess.getMESS_TYPE());
        byte[] messID = UUIDDecoder.getByte(mess.getMessID());
        int mesLen = mess.getMessLength();
        byte[] mesLenByte = IntDecoder.getByte(mesLen);

        int resultLength = type.length + messID.length + mesLen + mesLenByte.length;


        byte[] packetData = new byte[resultLength];

        System.arraycopy(type,  0, packetData, 0, type.length);
        System.arraycopy(messID,0, packetData, type.length, messID.length);
        System.arraycopy(mesLenByte, 0, packetData, type.length + messID.length, mesLenByte.length);
        if(mesLen != 0)
        {
            System.arraycopy(mess.getMess(),   0, packetData, type.length + messID.length + mesLenByte.length, mesLen);
        }

        return new DatagramPacket(packetData, 0, packetData.length,
                                                        mess.getClientINFO().getIp(), mess.getClientINFO().getPort());
    }


}
