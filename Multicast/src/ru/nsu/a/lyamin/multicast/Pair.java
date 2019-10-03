package ru.nsu.a.lyamin.multicast;

import java.net.InetAddress;

public class Pair
{
    private InetAddress ip;
    private long      time;

    Pair(InetAddress _ip, long _time)
    {
        ip = _ip;
        time = _time;
    }

    public long getTime()
    {
        return this.time;
    }

    public InetAddress getIp()
    {
        return this.ip;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == this){
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass())
        {
            return false;
        }

        Pair p = (Pair)obj;

        return p.ip == this.ip && p.time == this.time ;
    }
}
