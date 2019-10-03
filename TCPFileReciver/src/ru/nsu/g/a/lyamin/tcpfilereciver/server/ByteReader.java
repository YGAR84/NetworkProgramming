package ru.nsu.g.a.lyamin.tcpfilereciver.server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;

public class ByteReader
{
    private byte[] buffer = new byte[1024];
    private int rest = 0;
    private int pos = 0;
    private InputStream input;

    public ByteReader(InputStream _input)
    {
        input = _input;
    }

    public byte[] readNBytes(int N) throws IOException
    {
        byte[] result = new byte[N];
        int n = 0;
        if (rest != 0)
        {
            if(N <= rest)
            {
                System.arraycopy(buffer, pos, result, 0, N);
                rest -= N;
                pos += N;
                return result;
            }

            n += rest;
            System.arraycopy(buffer, pos, result,  0, rest);
            pos = 0;
        }

        while(true)
        {
            int num;
            try
            {
                 num = input.read(buffer);
            }
            catch(SocketTimeoutException e)
            {
                continue;
            }
            if(n + num >= N)
            {
                System.arraycopy(buffer, 0, result, n, N - n);
                rest = num - N + n;
                pos = N - n;
                return result;
            }

            System.arraycopy(buffer, 0, result, n, num);
            n += num;
        }
    }


    public byte[] getRest()
    {
        byte[] result = new byte[rest];
        System.arraycopy(buffer, pos, result, 0, rest);
        return result;
    }

    public int getRestLen()
    {
        return rest;
    }
}
