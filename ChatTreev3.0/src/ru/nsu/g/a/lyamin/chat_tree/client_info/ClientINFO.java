package ru.nsu.g.a.lyamin.chat_tree.client_info;

import java.net.InetAddress;

public class ClientINFO
{
    private InetAddress ip;
    private int port;

    public ClientINFO(InetAddress _ip, int _port)
    {
        ip = _ip;
        port = _port;
    }


    public InetAddress getIp()
    {
        return ip;
    }

    public int getPort()
    {
        return port;
    }

    @Override
    public boolean equals(Object obj)
    {

        if(obj == this)
        {
            return true;
        }
        if(obj == null || obj.getClass() != this.getClass())
        {
            return false;
        }
        ClientINFO ci = (ClientINFO) obj;
        return ci.ip.equals(this.ip) && ci.port == this.port;
    }

    @Override
    public int hashCode()
    {
        String hash = this.ip.toString() + this.port;
        return hash.hashCode();
    }

}
