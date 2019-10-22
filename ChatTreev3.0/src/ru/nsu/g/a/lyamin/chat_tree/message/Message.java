package ru.nsu.g.a.lyamin.chat_tree.message;

import ru.nsu.g.a.lyamin.chat_tree.client_info.ClientINFO;
import ru.nsu.g.a.lyamin.chat_tree.int_decoder.IntDecoder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

public class Message implements Comparable
{
    private int MESS_TYPE;
    private UUID messID;
    private int messLength;
    private byte[] mess;
    private ClientINFO clientINFO;
    private int retries = 0;

    public Message(int _MESS_TYPE, UUID _mess_id, byte[] _mess, InetAddress ip, int port)
    {
        MESS_TYPE = _MESS_TYPE;
        messID = _mess_id;
        mess = _mess;
        messLength = (_mess == null) ? 0 : _mess.length;
        clientINFO = new ClientINFO(ip, port);
    }

    public static byte[] encodeIp(InetAddress ip, int port)
    {

        byte[] ipByte = ip.getAddress();
        byte[] portByte = IntDecoder.getByte(port);

        byte[] result = new byte[portByte.length + ipByte.length];

        System.arraycopy(portByte, 0, result, 0, portByte.length);
        System.arraycopy(ipByte, 0, result, portByte.length, ipByte.length);
        return result;
    }

    public static ClientINFO decodeIp(byte[] data) throws UnknownHostException
    {

        byte[] portByte = new byte[Integer.BYTES];
        byte[] ipByte = new byte[data.length - portByte.length];

        System.arraycopy(data, 0, portByte, 0, portByte.length);
        System.arraycopy(data, portByte.length, ipByte, 0, ipByte.length);

        int port = IntDecoder.getInt(portByte);
        InetAddress ip = InetAddress.getByAddress(ipByte);

        return new ClientINFO(ip, port);
    }

    public static String decodeString(byte[] data)
    {
        return new String(data, StandardCharsets.UTF_8);
    }

    public static byte[] encodeString(String s)
    {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    public int getMESS_TYPE()
    {
        return MESS_TYPE;
    }

    public byte[] getMess()
    {
        return mess;
    }

    public ClientINFO getClientINFO()
    {
        return clientINFO;
    }

    public void incrementRetries()
    {
        ++retries;
    }

    public UUID getMessID()
    {
        return messID;
    }

    public int getRetries()
    {
        return retries;
    }

    public int getMessLength()
    {
        return messLength;
    }

    @Override
    public int compareTo(Object o)
    {
        if(o instanceof Message)
        {
            Message m = (Message)o;
            return
                (m.messID == this.messID && m.MESS_TYPE == this.MESS_TYPE && Arrays.equals(m.mess, this.mess) &&
                 m.retries == this.retries && m.clientINFO.getIp() == this.clientINFO.getIp() &&
                        m.clientINFO.getPort() == this.clientINFO.getPort()) ? 0 : -1;
        }
        else return -1;
    }


}
