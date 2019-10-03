package ru.nsu.a.lyamin.multicast;

import java.io.IOException;

public class Main
{
    public static void main(String[] args)
    {
        try
        {
            UDPClient updClient = new UDPClient("224.0.0.11", 8080);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
