package ru.nsu.g.a.lyamin.tcpfilereciver.server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

class FileWriterFromStream
{
    private Socket socket;
    private FileOutputStream out;
    private long timeout = 3000;

    private int fileBytesNum = 0;
    private int prevFileBytesNum = 0;
    private byte[] rest;
    private int restLen;

    private String filename;
    private long startTime;


    FileWriterFromStream(Socket _socket, byte[] _rest, int _restLen, String _filename)
    {
        socket = _socket;
        rest = _rest;
        restLen = _restLen;
        filename = _filename;
    }

    private void proccedSpeed(int N)
    {
        double speed = (double)fileBytesNum/(1000*(System.currentTimeMillis() - startTime))/(1024 * 1024);
        double instantSpeed = (double)(fileBytesNum - prevFileBytesNum)/(1000 * timeout)/(1024 * 1024);
        System.out.printf("%20s %3.5f Mb/s %3.5f Mb/s %2.2f\n", filename, instantSpeed, speed, (double)fileBytesNum/N);
    }

    void writeNBytesFromInputToFile(InputStream input, FileOutputStream out, int count) throws IOException
    {
        fileBytesNum = 0;
        if(restLen != 0){
            if(count <= restLen){
                out.write(rest, 0, count);
                return;
            }
            out.write(rest, 0, restLen);
            fileBytesNum += restLen;
        }

        byte[] buffer = new byte[1024];

        startTime = System.currentTimeMillis();

        long timeStart = System.currentTimeMillis();
        long curTimeout = timeout;
        while(true)
        {

            curTimeout -= (System.currentTimeMillis() - timeStart);
            socket.setSoTimeout((int)curTimeout);
            if(System.currentTimeMillis() - timeStart >= curTimeout)
            {
                proccedSpeed(count);
                curTimeout = timeout;
                timeStart = System.currentTimeMillis();
                prevFileBytesNum = fileBytesNum;
            }


            int num;

            try                             {   num = input.read(buffer); }
            catch(SocketTimeoutException e) {   continue;                 }

            if(fileBytesNum + num >= count)
            {
                fileBytesNum += count - num;
                out.write(buffer, 0, count - num);
                proccedSpeed(count);
                return;
            }
            fileBytesNum += num;

            out.write(buffer, 0, num);
        }
    }
}
